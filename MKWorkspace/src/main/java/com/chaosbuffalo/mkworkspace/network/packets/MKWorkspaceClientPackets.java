package com.chaosbuffalo.mkworkspace.network.packets;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.insert.MKWorkspaceInsertOverlaySnapshot;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceSamplePreviewState;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import net.minecraft.core.BlockPos;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeEffect;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeSummary;

import java.time.Instant;
import java.util.UUID;

import javax.annotation.Nullable;
import java.util.List;

public final class MKWorkspaceClientPackets {
    private static Handler handler;

    private MKWorkspaceClientPackets() {
    }

    public static void setHandler(Handler handler) {
        MKWorkspaceClientPackets.handler = handler;
    }

    public static void openWorkspaceScreen(BlockPos anchor, @Nullable MKStructureWorkspace workspace,
                                           List<String> importManifestIds, List<String> backupManifestFiles,
                                           @Nullable MKWorkspaceSamplePreviewState samplePreviewState,
                                           int totalPieces, int nextPieceOffset, long pieceRevision) {
        if (handler == null) {
            MKWorkspace.LOGGER.warn("Received workspace screen payload before a client handler was registered.");
            return;
        }
        handler.openWorkspaceScreen(anchor, workspace, importManifestIds, backupManifestFiles, samplePreviewState,
                totalPieces, nextPieceOffset, pieceRevision);
    }

    public static void openWorkspaceInsertSocketScreen(
            OpenWorkspaceInsertSocketScreenPacket.InsertSocketContext socketContext) {
        if (handler == null) {
            MKWorkspace.LOGGER.warn("Received workspace insert socket screen payload before a client handler was registered.");
            return;
        }
        handler.openWorkspaceInsertSocketScreen(socketContext);
    }

    public static void applyPieceChunk(BlockPos anchor, List<MKWorkspacePieceDefinition> pieces,
                                       int totalPieces, int nextPieceOffset, long pieceRevision) {
        if (handler == null) {
            MKWorkspace.LOGGER.warn("Received workspace piece chunk before a client handler was registered.");
            return;
        }
        handler.applyPieceChunk(anchor, pieces, totalPieces, nextPieceOffset, pieceRevision);
    }

    public static void applyInsertOverlay(List<MKWorkspaceInsertOverlaySnapshot.Entry> entries) {
        if (handler == null) {
            MKWorkspace.LOGGER.warn("Received workspace insert overlay before a client handler was registered.");
            return;
        }
        handler.applyInsertOverlay(entries);
    }

    public static void beginChangePreflight(UUID requestId, UUID planId, BlockPos anchor, Instant expiresAt,
                                            MKWorkspaceChangeSummary summary, int totalEffects) {
        if (handler != null) {
            handler.beginChangePreflight(requestId, planId, anchor, expiresAt, summary, totalEffects);
        }
    }

    public static void appendChangeEffects(UUID requestId, UUID planId, int offset, int totalEffects,
                                           List<MKWorkspaceChangeEffect> effects) {
        if (handler != null) {
            handler.appendChangeEffects(requestId, planId, offset, totalEffects, effects);
        }
    }

    public static void applyChangeResult(UUID requestId, BlockPos anchor, boolean success,
                                         boolean stale, String message) {
        if (handler != null) {
            handler.applyChangeResult(requestId, anchor, success, stale, message);
        }
    }

    public interface Handler {
        void openWorkspaceScreen(BlockPos anchor, @Nullable MKStructureWorkspace workspace,
                                 List<String> importManifestIds, List<String> backupManifestFiles,
                                 @Nullable MKWorkspaceSamplePreviewState samplePreviewState,
                                 int totalPieces, int nextPieceOffset, long pieceRevision);

        void openWorkspaceInsertSocketScreen(OpenWorkspaceInsertSocketScreenPacket.InsertSocketContext socketContext);

        void applyPieceChunk(BlockPos anchor, List<MKWorkspacePieceDefinition> pieces,
                             int totalPieces, int nextPieceOffset, long pieceRevision);

        void applyInsertOverlay(List<MKWorkspaceInsertOverlaySnapshot.Entry> entries);

        void beginChangePreflight(UUID requestId, UUID planId, BlockPos anchor, Instant expiresAt,
                                  MKWorkspaceChangeSummary summary, int totalEffects);

        void appendChangeEffects(UUID requestId, UUID planId, int offset, int totalEffects,
                                 List<MKWorkspaceChangeEffect> effects);

        void applyChangeResult(UUID requestId, BlockPos anchor, boolean success, boolean stale, String message);
    }
}
