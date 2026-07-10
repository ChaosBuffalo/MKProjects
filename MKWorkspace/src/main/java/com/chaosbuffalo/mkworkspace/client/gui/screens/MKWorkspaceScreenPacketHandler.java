package com.chaosbuffalo.mkworkspace.client.gui.screens;

import com.chaosbuffalo.mkworkspace.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mkworkspace.network.packets.MKWorkspaceClientPackets;
import com.chaosbuffalo.mkworkspace.network.packets.RequestWorkspacePieceChunkPacket;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceMutationPreflight;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.List;

public final class MKWorkspaceScreenPacketHandler implements MKWorkspaceClientPackets.Handler {
    private static final MKWorkspaceScreenPacketHandler INSTANCE = new MKWorkspaceScreenPacketHandler();

    private MKWorkspaceScreenPacketHandler() {
    }

    public static void register() {
        MKWorkspaceClientPackets.setHandler(INSTANCE);
    }

    @Override
    public void openWorkspaceScreen(BlockPos anchor, @Nullable MKStructureWorkspace workspace,
                                    List<String> importManifestIds, List<String> backupManifestFiles,
                                    int totalPieces, int nextPieceOffset, long pieceRevision) {
        if (Minecraft.getInstance().screen instanceof MKWorkspaceScreen current) {
            Minecraft.getInstance().setScreen(current.copyWithWorkspace(workspace, importManifestIds,
                    backupManifestFiles, totalPieces, nextPieceOffset, pieceRevision));
        } else {
            Minecraft.getInstance().setScreen(new MKWorkspaceScreen(anchor, workspace, importManifestIds,
                    backupManifestFiles, totalPieces, nextPieceOffset, pieceRevision));
        }
        requestNextPieceChunk(anchor, workspace, totalPieces, nextPieceOffset, pieceRevision);
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
    public void applyPreflight(BlockPos anchor, MKWorkspaceMutationPreflight preflight) {
        if (Minecraft.getInstance().screen instanceof MKWorkspaceScreen current &&
                current.anchor().equals(anchor)) {
            Minecraft.getInstance().setScreen(current.copyWithPreflight(preflight));
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
