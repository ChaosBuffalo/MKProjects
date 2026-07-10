package com.chaosbuffalo.mkworkspace.network.packets;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMutationPreflight;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
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
                                           int totalPieces, int nextPieceOffset, long pieceRevision) {
        if (handler == null) {
            MKWorkspace.LOGGER.warn("Received workspace screen payload before a client handler was registered.");
            return;
        }
        handler.openWorkspaceScreen(anchor, workspace, importManifestIds, backupManifestFiles,
                totalPieces, nextPieceOffset, pieceRevision);
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

    public interface Handler {
        void openWorkspaceScreen(BlockPos anchor, @Nullable MKStructureWorkspace workspace,
                                 List<String> importManifestIds, List<String> backupManifestFiles,
                                 int totalPieces, int nextPieceOffset, long pieceRevision);

        void applyPieceChunk(BlockPos anchor, List<MKWorkspacePieceDefinition> pieces,
                             int totalPieces, int nextPieceOffset, long pieceRevision);

        void applyPreflight(BlockPos anchor, MKWorkspaceMutationPreflight preflight);
    }
}
