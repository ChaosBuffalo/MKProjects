package com.chaosbuffalo.mkworkspace.client.gui.screens;

import com.chaosbuffalo.mkworkspace.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mkworkspace.client.render.MKWorkspaceInsertOverlayRenderer;
import com.chaosbuffalo.mkworkspace.network.packets.MKWorkspaceClientPackets;
import com.chaosbuffalo.mkworkspace.network.packets.OpenWorkspaceInsertSocketScreenPacket;
import com.chaosbuffalo.mkworkspace.network.packets.RequestWorkspacePieceChunkPacket;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.insert.MKWorkspaceInsertOverlaySnapshot;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceSamplePreviewState;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.network.PacketDistributor;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.MKWorkspaceClientChangePlan;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeEffect;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeSummary;
import net.minecraft.network.chat.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.annotation.Nullable;
import java.util.List;

public final class MKWorkspaceScreenPacketHandler implements MKWorkspaceClientPackets.Handler {
    private static final MKWorkspaceScreenPacketHandler INSTANCE = new MKWorkspaceScreenPacketHandler();
    private final Map<UUID, EffectAccumulator> changeEffects = new HashMap<>();

    private static final class EffectAccumulator {
        private final MKWorkspaceClientChangePlan plan;
        private final TreeMap<Integer, List<MKWorkspaceChangeEffect>> chunks = new TreeMap<>();

        private EffectAccumulator(MKWorkspaceClientChangePlan plan) {
            this.plan = plan;
        }
    }

    private MKWorkspaceScreenPacketHandler() {
    }

    public static void register() {
        MKWorkspaceClientPackets.setHandler(INSTANCE);
    }

    @Override
    public void openWorkspaceScreen(BlockPos anchor, @Nullable MKStructureWorkspace workspace,
                                    List<String> importManifestIds, List<String> backupManifestFiles,
                                    @Nullable MKWorkspaceSamplePreviewState samplePreviewState,
                                    int totalPieces, int nextPieceOffset, long pieceRevision) {
        if (Minecraft.getInstance().screen instanceof MKWorkspaceScreen current) {
            Minecraft.getInstance().setScreen(current.copyWithWorkspace(workspace, importManifestIds,
                    backupManifestFiles, samplePreviewState, totalPieces, nextPieceOffset, pieceRevision));
        } else {
            Minecraft.getInstance().setScreen(new MKWorkspaceScreen(anchor, workspace, importManifestIds,
                    backupManifestFiles, samplePreviewState, totalPieces, nextPieceOffset, pieceRevision));
        }
        requestNextPieceChunk(anchor, workspace, totalPieces, nextPieceOffset, pieceRevision);
    }

    @Override
    public void openWorkspaceInsertSocketScreen(
            OpenWorkspaceInsertSocketScreenPacket.InsertSocketContext socketContext) {
        Minecraft.getInstance().setScreen(new MKWorkspaceInsertSocketScreen(socketContext));
    }

    @Override
    public void applyPieceChunk(BlockPos anchor, List<MKWorkspacePieceDefinition> pieces,
                                int totalPieces, int nextPieceOffset, long pieceRevision) {
        if (Minecraft.getInstance().screen instanceof MKWorkspaceScreen current &&
                current.anchor().equals(anchor)) {
            MKWorkspaceScreen updated = current.copyWithWorkspacePieceChunk(pieces, totalPieces,
                    nextPieceOffset, pieceRevision);
            Minecraft.getInstance().setScreen(updated);
            requestNextPieceChunk(anchor, updated.workspace(), totalPieces, nextPieceOffset, pieceRevision);
        }
    }

    @Override
    public void applyInsertOverlay(List<MKWorkspaceInsertOverlaySnapshot.Entry> entries) {
        MKWorkspaceInsertOverlayRenderer.update(entries);
    }

    @Override
    public void beginChangePreflight(UUID requestId, UUID planId, BlockPos anchor, Instant expiresAt,
                                     MKWorkspaceChangeSummary summary, int totalEffects) {
        if (!(Minecraft.getInstance().screen instanceof MKWorkspaceScreen current) ||
                !current.acceptsWorkspaceChange(requestId, anchor)) {
            return;
        }
        MKWorkspaceClientChangePlan plan = new MKWorkspaceClientChangePlan(requestId, planId, anchor, expiresAt,
                summary, totalEffects, 0, totalEffects == 0);
        changeEffects.put(requestId, new EffectAccumulator(plan));
        Minecraft.getInstance().setScreen(current.copyWithChangePlan(plan));
    }

    @Override
    public void appendChangeEffects(UUID requestId, UUID planId, int offset, int totalEffects,
                                    List<MKWorkspaceChangeEffect> effects) {
        EffectAccumulator accumulator = changeEffects.get(requestId);
        if (accumulator == null || !accumulator.plan.planId().equals(planId) ||
                accumulator.plan.totalEffects() != totalEffects) {
            return;
        }
        accumulator.chunks.putIfAbsent(offset, List.copyOf(effects));
        ArrayList<MKWorkspaceChangeEffect> assembled = new ArrayList<>(totalEffects);
        int expectedOffset = 0;
        for (Map.Entry<Integer, List<MKWorkspaceChangeEffect>> entry : accumulator.chunks.entrySet()) {
            if (entry.getKey() != expectedOffset) {
                break;
            }
            assembled.addAll(entry.getValue());
            expectedOffset += entry.getValue().size();
        }
        boolean complete = expectedOffset == totalEffects;
        if (Minecraft.getInstance().screen instanceof MKWorkspaceScreen current &&
                current.acceptsWorkspaceChange(requestId, accumulator.plan.anchor())) {
            Minecraft.getInstance().setScreen(current.copyWithChangePlan(
                    accumulator.plan.withEffects(assembled, complete)));
        }
        if (complete) {
            changeEffects.remove(requestId);
        }
    }

    @Override
    public void applyChangeResult(UUID requestId, BlockPos anchor, boolean success, boolean stale, String message) {
        if (!(Minecraft.getInstance().screen instanceof MKWorkspaceScreen current) ||
                (!requestId.equals(new UUID(0L, 0L)) && !current.acceptsWorkspaceChange(requestId, anchor))) {
            return;
        }
        if (success) {
            current.draftSession().markServerApplied();
        }
        current.workspaceChangeMessage(message, !success && !stale);
        if (!message.isBlank() && Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.displayClientMessage(Component.literal(message), false);
        }
    }

    private void requestNextPieceChunk(BlockPos anchor, @Nullable MKStructureWorkspace workspace,
                                       int totalPieces, int nextPieceOffset, long pieceRevision) {
        if (workspace != null && nextPieceOffset < totalPieces) {
            PacketDistributor.sendToServer(new RequestWorkspacePieceChunkPacket(anchor, pieceRevision,
                    nextPieceOffset));
        }
    }
}
