package com.chaosbuffalo.mkworkspace.world.gen.workspace.change;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.capability.IMKStructureWorkspaceData;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.export.MKWorkspaceBackupManifestWriter;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Server-owned prepare/confirm boundary for every persistent authored workspace mutation.
 * Apply packets carry only a plan id, so the client cannot alter a confirmed payload.
 */
public final class MKWorkspaceChangeCoordinator {
    public enum ConfirmationStatus {
        APPLIED, STALE_REPREPARED, REJECTED, FAILED
    }

    public record PreparedPlan(UUID planId, UUID requestId, MKWorkspacePreparedChange change, Instant expiresAt) {
    }

    public record Confirmation(ConfirmationStatus status, UUID requestId, BlockPos anchor,
                               @Nullable PreparedPlan replacement,
                               @Nullable MKWorkspaceChangeApplyResult result, String message) {
    }

    private record PendingPlan(UUID playerId, PreparedPlan plan) {
    }

    private static final MKWorkspaceChangeCoordinator SHARED = new MKWorkspaceChangeCoordinator(
            MKWorkspaceChangeRegistry.shared(), Duration.ofMinutes(5));

    private final MKWorkspaceChangeRegistry registry;
    private final Duration planLifetime;
    private final MKWorkspaceBackupManifestWriter backupWriter = new MKWorkspaceBackupManifestWriter();
    private final Map<UUID, PendingPlan> pendingPlans = new LinkedHashMap<>();

    public MKWorkspaceChangeCoordinator(MKWorkspaceChangeRegistry registry, Duration planLifetime) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.planLifetime = Objects.requireNonNull(planLifetime, "planLifetime");
    }

    public static MKWorkspaceChangeCoordinator shared() {
        return SHARED;
    }

    public synchronized PreparedPlan prepare(ServerPlayer player, MKWorkspaceChangeRequest request) throws Exception {
        evictExpired();
        pendingPlans.values().removeIf(pending -> pending.playerId().equals(player.getUUID()) &&
                pending.plan().requestId().equals(request.requestId()));
        MKWorkspacePreparedChange change = registry.prepare(player, request);
        MKWorkspace.LOGGER.info("Prepared workspace change request={} player={} anchor={}\n{}",
                request.requestId(), player.getGameProfile().getName(), change.anchor(),
                String.join("\n", MKWorkspaceChangeSummaryFormatter.lines(change.summary())));
        PreparedPlan plan = new PreparedPlan(UUID.randomUUID(), request.requestId(), change,
                Instant.now().plus(planLifetime));
        pendingPlans.put(plan.planId(), new PendingPlan(player.getUUID(), plan));
        return plan;
    }

    public synchronized void cancel(ServerPlayer player, UUID planId) {
        PendingPlan pending = pendingPlans.get(planId);
        if (pending != null && pending.playerId().equals(player.getUUID())) {
            pendingPlans.remove(planId);
        }
    }

    public synchronized Confirmation confirm(ServerPlayer player, UUID planId) {
        evictExpired();
        PendingPlan pending = pendingPlans.get(planId);
        if (pending == null) {
            return new Confirmation(ConfirmationStatus.REJECTED, new UUID(0L, 0L), player.blockPosition(), null, null,
                    "That workspace change plan has expired or no longer exists.");
        }
        if (!pending.playerId().equals(player.getUUID())) {
            return new Confirmation(ConfirmationStatus.REJECTED, pending.plan().requestId(),
                    pending.plan().change().anchor(), null, null,
                    "That workspace change plan belongs to another player.");
        }
        PreparedPlan oldPlan = pending.plan();
        if (!oldPlan.change().summary().canConfirm()) {
            pendingPlans.remove(planId);
            return new Confirmation(ConfirmationStatus.REJECTED, oldPlan.requestId(), oldPlan.change().anchor(),
                    null, null,
                    "This workspace change is blocked and cannot be applied.");
        }

        final MKWorkspacePreparedChange revalidated;
        try {
            revalidated = registry.prepare(player, oldPlan.change().request());
        } catch (Exception exception) {
            pendingPlans.remove(planId);
            MKWorkspace.LOGGER.error("Failed to revalidate workspace change {}", planId, exception);
            return new Confirmation(ConfirmationStatus.FAILED, oldPlan.requestId(), oldPlan.change().anchor(),
                    null, null,
                    "Workspace change revalidation failed: " + safeMessage(exception));
        }

        if (!samePreparedState(oldPlan.change(), revalidated)) {
            pendingPlans.remove(planId);
            PreparedPlan replacement = new PreparedPlan(UUID.randomUUID(), oldPlan.requestId(), revalidated,
                    Instant.now().plus(planLifetime));
            pendingPlans.put(replacement.planId(), new PendingPlan(player.getUUID(), replacement));
            return new Confirmation(ConfirmationStatus.STALE_REPREPARED, oldPlan.requestId(),
                    revalidated.anchor(), replacement, null,
                    "The workspace changed after preflight. Review the refreshed effects before confirming again.");
        }

        pendingPlans.remove(planId);
        try {
            Optional<MKStructureWorkspace> existing = IMKStructureWorkspaceData.get(player.serverLevel())
                    .getWorkspaceByAnchor(revalidated.anchor());
            MKWorkspaceBackupManifestWriter.WrittenBackup backup = null;
            if (revalidated.summary().backupRequired()) {
                MKStructureWorkspace backupTarget = existing.orElseThrow(() -> new IllegalStateException(
                        "The workspace disappeared before its required backup could be written."));
                if (revalidated.workspaceId() != null && !revalidated.workspaceId().equals(backupTarget.id())) {
                    throw new IllegalStateException("The workspace identity changed before backup.");
                }
                backup = backupWriter.writeBeforeMutation(player.serverLevel(), backupTarget,
                        revalidated.summary().operationId().getPath());
            }
            MKWorkspaceChangeApplyResult result;
            try (MKWorkspaceBackupManifestWriter.TransactionScope ignored =
                         MKWorkspaceBackupManifestWriter.enterTransaction(backup)) {
                result = revalidated.mutation().apply(player);
            }
            MKWorkspace.LOGGER.info("Applied workspace change plan={} request={} success={} message={}",
                    planId, oldPlan.requestId(), result.success(), result.message());
            return new Confirmation(result.success() ? ConfirmationStatus.APPLIED : ConfirmationStatus.FAILED,
                    oldPlan.requestId(), revalidated.anchor(), null, result, result.message());
        } catch (IOException exception) {
            MKWorkspace.LOGGER.error("Workspace change {} was blocked because its backup failed", planId, exception);
            return new Confirmation(ConfirmationStatus.FAILED, oldPlan.requestId(), revalidated.anchor(),
                    null, null,
                    "Workspace backup failed; no changes were applied: " + safeMessage(exception));
        } catch (Exception exception) {
            MKWorkspace.LOGGER.error("Workspace change {} failed", planId, exception);
            return new Confirmation(ConfirmationStatus.FAILED, oldPlan.requestId(), revalidated.anchor(),
                    null, null,
                    "Workspace change failed: " + safeMessage(exception));
        }
    }

    public synchronized int pendingPlanCount() {
        evictExpired();
        return pendingPlans.size();
    }

    public static long fingerprint(@Nullable MKStructureWorkspace workspace) {
        if (workspace == null) {
            return 0L;
        }
        long fingerprint = workspace.id().hashCode();
        fingerprint = 31L * fingerprint + workspace.updatedAt();
        fingerprint = 31L * fingerprint + workspace.toTag().hashCode();
        return fingerprint;
    }

    private boolean samePreparedState(MKWorkspacePreparedChange previous, MKWorkspacePreparedChange current) {
        return previous.anchor().equals(current.anchor()) &&
                Objects.equals(previous.workspaceId(), current.workspaceId()) &&
                previous.workspaceFingerprint() == current.workspaceFingerprint() &&
                previous.operationStateGuard().equals(current.operationStateGuard()) &&
                previous.summary().equals(current.summary());
    }

    private void evictExpired() {
        Instant now = Instant.now();
        Iterator<Map.Entry<UUID, PendingPlan>> iterator = pendingPlans.entrySet().iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().getValue().plan().expiresAt().isAfter(now)) {
                iterator.remove();
            }
        }
    }

    private static String safeMessage(Exception exception) {
        return exception.getMessage() == null || exception.getMessage().isBlank() ?
                exception.getClass().getSimpleName() : exception.getMessage();
    }
}
