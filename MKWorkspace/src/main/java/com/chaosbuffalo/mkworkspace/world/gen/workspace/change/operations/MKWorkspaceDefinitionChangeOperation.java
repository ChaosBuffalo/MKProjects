package com.chaosbuffalo.mkworkspace.world.gen.workspace.change.operations;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.MKStructureWorkspaceService;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.capability.IMKStructureWorkspaceData;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeApplyResult;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeCoordinator;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeOperation;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeRequest;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeSummary;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceDefinitionSummaryBuilder;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspacePreparedChange;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspacePlannerChangePlan;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceMutationPreflight;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKWorkspacePlannerRegistry;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceGeneratedLayer;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MKWorkspaceDefinitionChangeOperation
        implements MKWorkspaceChangeOperation<MKWorkspaceDefinitionChangePayload> {
    public static final ResourceLocation ID = MKWorkspace.id("update_definition");

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public Codec<MKWorkspaceDefinitionChangePayload> codec() {
        return MKWorkspaceDefinitionChangePayload.CODEC;
    }

    @Override
    public MKWorkspacePreparedChange prepare(ServerPlayer player, BlockPos anchor,
                                             MKWorkspaceChangeRequest request,
                                             MKWorkspaceDefinitionChangePayload payload) {
        MKStructureWorkspace requested = payload.workspace();
        if (!anchor.equals(requested.anchor())) {
            throw new IllegalArgumentException("Workspace payload anchor does not match the requested anchor");
        }
        MKStructureWorkspaceService service = new MKStructureWorkspaceService();
        List<String> validationErrors = service.validateWorkspace(requested);
        Optional<MKStructureWorkspace> existingOpt = IMKStructureWorkspaceData.get(player.serverLevel())
                .getWorkspaceByAnchor(anchor);
        if (existingOpt.isEmpty()) {
            ArrayList<String> blockers = new ArrayList<>(validationErrors);
            MKWorkspaceChangeSummary base = MKWorkspaceDefinitionSummaryBuilder.create(requested);
            MKWorkspaceChangeSummary summary = new MKWorkspaceChangeSummary(base.operationId(), base.title(),
                    base.summary(), base.safety(), false, base.invalidatedLayers(), base.warnings(), blockers,
                    base.fieldChanges(), base.effects());
            return new MKWorkspacePreparedChange(request, anchor, null, 0L, "empty-anchor", summary, applyPlayer -> {
                Optional<MKStructureWorkspace> created = service.applyPreparedUpdate(applyPlayer.serverLevel(),
                        requested, payload.acceptedRemaps(), payload.addedVariants(),
                        payload.deletedVariantPieceIds(), payload.workspaceSettingsDirty(),
                        emptyPreflight(requested), MKStructureWorkspaceService.PreparedUpdateStrategy.CREATE);
                if (created.isEmpty()) {
                    return MKWorkspaceChangeApplyResult.failure(anchor, "Workspace creation failed.");
                }
                if (payload.generateAfterApply() && service.generateWorkspace(applyPlayer.serverLevel(), anchor).isEmpty()) {
                    return MKWorkspaceChangeApplyResult.failure(anchor, "Workspace creation succeeded, but generation failed.");
                }
                return MKWorkspaceChangeApplyResult.success(anchor, "Workspace created.");
            });
        }

        MKStructureWorkspace existing = existingOpt.get();
        MKWorkspaceMutationPreflight preflight = service.preflightWorkspaceUpdate(existing, requested,
                System.currentTimeMillis(), payload.acceptedRemaps(), payload.addedVariants(),
                payload.deletedVariantPieceIds(), payload.workspaceSettingsDirty());
        Optional<MKWorkspacePlannerChangePlan> plannerPlan = payload.forceFullRegenerate() ||
                !payload.addedVariants().isEmpty() || !payload.deletedVariantPieceIds().isEmpty() ? Optional.empty() :
                MKWorkspacePlannerRegistry.shared().plannerFor(existing)
                        .prepareDefinitionChange(player, existing, requested, preflight);
        if (plannerPlan.isPresent()) {
            return preparePlannerChange(request, anchor, existing, requested, payload, preflight,
                    validationErrors, plannerPlan.get());
        }
        MKStructureWorkspaceService.PreparedUpdateStrategy strategy = payload.forceFullRegenerate() ?
                MKStructureWorkspaceService.PreparedUpdateStrategy.FULL_REGENERATE :
                service.classifyPreparedUpdate(existing, requested, payload.acceptedRemaps(),
                        payload.addedVariants(), payload.deletedVariantPieceIds(), preflight);
        ArrayList<String> blockers = new ArrayList<>(validationErrors);
        List<MKWorkspaceGeneratedLayer> invalidated =
                MKWorkspaceDefinitionSummaryBuilder.invalidatedLayers(preflight, strategy);
        List<MKWorkspaceGeneratedLayer> locked = existing.layerStates().stream()
                .filter(state -> state.locked() && invalidated.contains(state.layer()))
                .map(state -> state.layer()).toList();
        if (!locked.isEmpty()) {
            blockers.add("Unlock invalidated layer(s): " + locked.stream()
                    .map(MKWorkspaceGeneratedLayer::getSerializedName)
                    .reduce((left, right) -> left + ", " + right).orElse(""));
        }
        MKWorkspaceChangeSummary summary = MKWorkspaceDefinitionSummaryBuilder.build(existing, requested, preflight,
                strategy, payload.addedVariants(), payload.deletedVariantPieceIds(), blockers);
        return new MKWorkspacePreparedChange(request, anchor, existing.id(),
                MKWorkspaceChangeCoordinator.fingerprint(existing), strategy.name(), summary, applyPlayer -> {
            Optional<MKStructureWorkspace> updated = service.applyPreparedUpdate(applyPlayer.serverLevel(), requested,
                    payload.acceptedRemaps(), payload.addedVariants(), payload.deletedVariantPieceIds(),
                    payload.workspaceSettingsDirty(), preflight, strategy);
            if (updated.isEmpty()) {
                return MKWorkspaceChangeApplyResult.failure(anchor, "Workspace update failed.");
            }
            if (payload.generateAfterApply() && service.generateWorkspace(applyPlayer.serverLevel(), anchor).isEmpty()) {
                return MKWorkspaceChangeApplyResult.failure(anchor, "Workspace update succeeded, but generation failed.");
            }
            return MKWorkspaceChangeApplyResult.success(anchor, "Workspace change applied.");
        });
    }

    private MKWorkspacePreparedChange preparePlannerChange(MKWorkspaceChangeRequest request, BlockPos anchor,
                                                            MKStructureWorkspace existing,
                                                            MKStructureWorkspace requested,
                                                            MKWorkspaceDefinitionChangePayload payload,
                                                            MKWorkspaceMutationPreflight preflight,
                                                            List<String> validationErrors,
                                                            MKWorkspacePlannerChangePlan plannerPlan) {
        ArrayList<String> blockers = new ArrayList<>(validationErrors);
        List<MKWorkspaceGeneratedLayer> locked = existing.layerStates().stream()
                .filter(state -> state.locked() && plannerPlan.invalidatedLayers().contains(state.layer()))
                .map(state -> state.layer()).toList();
        if (!locked.isEmpty()) {
            blockers.add("Unlock invalidated layer(s): " + locked.stream()
                    .map(MKWorkspaceGeneratedLayer::getSerializedName)
                    .reduce((left, right) -> left + ", " + right).orElse(""));
        }
        MKWorkspaceChangeSummary common = MKWorkspaceDefinitionSummaryBuilder.build(existing, requested, preflight,
                MKStructureWorkspaceService.PreparedUpdateStrategy.METADATA_UPDATE,
                payload.addedVariants(), payload.deletedVariantPieceIds(), blockers);
        ArrayList<String> warnings = new ArrayList<>(common.warnings());
        warnings.addAll(plannerPlan.warnings());
        ArrayList<com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeEffect> effects =
                new ArrayList<>(common.effects());
        effects.addAll(plannerPlan.effects());
        MKWorkspaceChangeSummary summary = new MKWorkspaceChangeSummary(ID,
                "Confirm Planner Workspace Change", plannerPlan.summary(), plannerPlan.safety(), true,
                plannerPlan.invalidatedLayers(), warnings, blockers, common.fieldChanges(), effects);
        return new MKWorkspacePreparedChange(request, anchor, existing.id(),
                MKWorkspaceChangeCoordinator.fingerprint(existing),
                plannerPlan.changeId() + ":" + plannerPlan.stateGuard(), summary, plannerPlan.mutation());
    }

    private MKWorkspaceMutationPreflight emptyPreflight(MKStructureWorkspace workspace) {
        return new MKWorkspaceMutationPreflight(
                com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceInvalidationReport.noChanges(
                        "Create workspace"), workspace);
    }
}
