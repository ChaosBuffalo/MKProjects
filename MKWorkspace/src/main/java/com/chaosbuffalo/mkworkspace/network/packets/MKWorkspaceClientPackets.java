package com.chaosbuffalo.mkworkspace.network.packets;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.insert.MKWorkspaceInsertOverlaySnapshot;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceMutationPreflight;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceSamplePreviewState;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import net.minecraft.core.BlockPos;

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

    public static void applyPreflight(BlockPos anchor, MKWorkspaceMutationPreflight preflight) {
        if (handler == null) {
            MKWorkspace.LOGGER.warn("Received workspace preflight report before a client handler was registered.");
            return;
        }
        handler.applyPreflight(anchor, preflight);
    }

    public static void applyInsertOverlay(List<MKWorkspaceInsertOverlaySnapshot.Entry> entries) {
        if (handler == null) {
            MKWorkspace.LOGGER.warn("Received workspace insert overlay before a client handler was registered.");
            return;
        }
        handler.applyInsertOverlay(entries);
    }

    public interface Handler {
        void openWorkspaceScreen(BlockPos anchor, @Nullable MKStructureWorkspace workspace,
                                 List<String> importManifestIds, List<String> backupManifestFiles,
                                 @Nullable MKWorkspaceSamplePreviewState samplePreviewState,
                                 int totalPieces, int nextPieceOffset, long pieceRevision);

        void openWorkspaceInsertSocketScreen(OpenWorkspaceInsertSocketScreenPacket.InsertSocketContext socketContext);

        void applyPieceChunk(BlockPos anchor, List<MKWorkspacePieceDefinition> pieces,
                             int totalPieces, int nextPieceOffset, long pieceRevision);

        void applyPreflight(BlockPos anchor, MKWorkspaceMutationPreflight preflight);

        void applyInsertOverlay(List<MKWorkspaceInsertOverlaySnapshot.Entry> entries);
    }
}
