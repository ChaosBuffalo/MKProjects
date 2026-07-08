package com.chaosbuffalo.mknpc.network.packets;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class OpenWorkspaceScreenPacket implements CustomPacketPayload {
    public static final Type<OpenWorkspaceScreenPacket> TYPE = new Type<>(MKNpc.id("open_workspace_screen"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenWorkspaceScreenPacket> STREAM_CODEC = StreamCodec.ofMember(
            OpenWorkspaceScreenPacket::toBytes, OpenWorkspaceScreenPacket::new
    );

    private final BlockPos anchor;
    private final CompoundTag workspaceTag;
    private final java.util.List<String> importManifestIds;
    private final java.util.List<String> backupManifestFiles;
    private final int totalPieces;
    private final int nextPieceOffset;
    private final long pieceRevision;

    public OpenWorkspaceScreenPacket(BlockPos anchor, MKStructureWorkspace workspace) {
        this(anchor, workspace, java.util.List.of(), java.util.List.of());
    }

    public OpenWorkspaceScreenPacket(BlockPos anchor, MKStructureWorkspace workspace, java.util.List<String> importManifestIds) {
        this(anchor, workspace, importManifestIds, java.util.List.of());
    }

    public OpenWorkspaceScreenPacket(BlockPos anchor, MKStructureWorkspace workspace, java.util.List<String> importManifestIds,
                                     java.util.List<String> backupManifestFiles) {
        this.anchor = anchor;
        MKWorkspacePacketPayloads.PieceChunk firstChunk = workspace != null ?
                MKWorkspacePacketPayloads.firstPieceChunk(workspace) :
                new MKWorkspacePacketPayloads.PieceChunk(java.util.List.of(), 0, 0, 0L);
        this.workspaceTag = workspace != null ? MKWorkspacePacketPayloads.screenWorkspaceChunkTag(workspace, firstChunk) : null;
        this.importManifestIds = java.util.List.copyOf(importManifestIds);
        this.backupManifestFiles = java.util.List.copyOf(backupManifestFiles);
        this.totalPieces = firstChunk.totalPieces();
        this.nextPieceOffset = firstChunk.nextOffset();
        this.pieceRevision = firstChunk.revision();
    }

    public OpenWorkspaceScreenPacket(FriendlyByteBuf buffer) {
        BlockPos decodedAnchor = buffer.readBlockPos();
        CompoundTag decodedWorkspaceTag = null;
        java.util.List<String> decodedImportManifestIds = java.util.List.of();
        java.util.List<String> decodedBackupManifestFiles = java.util.List.of();
        int decodedTotalPieces = 0;
        int decodedNextPieceOffset = 0;
        long decodedPieceRevision = 0L;
        try {
            decodedWorkspaceTag = buffer.readBoolean() ? buffer.readNbt() : null;
            decodedImportManifestIds = buffer.readList(FriendlyByteBuf::readUtf);
            decodedBackupManifestFiles = buffer.readList(FriendlyByteBuf::readUtf);
            decodedTotalPieces = buffer.readVarInt();
            decodedNextPieceOffset = buffer.readVarInt();
            decodedPieceRevision = buffer.readLong();
        } catch (RuntimeException ex) {
            MKNpc.LOGGER.error("Failed to decode workspace screen payload at {}; opening without workspace data.",
                    decodedAnchor, ex);
        }
        this.anchor = decodedAnchor;
        this.workspaceTag = decodedWorkspaceTag;
        this.importManifestIds = decodedImportManifestIds;
        this.backupManifestFiles = decodedBackupManifestFiles;
        this.totalPieces = decodedTotalPieces;
        this.nextPieceOffset = decodedNextPieceOffset;
        this.pieceRevision = decodedPieceRevision;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        int startIndex = buffer.writerIndex();
        buffer.writeBlockPos(anchor);
        buffer.writeBoolean(workspaceTag != null);
        if (workspaceTag != null) {
            buffer.writeNbt(workspaceTag);
        }
        buffer.writeCollection(importManifestIds, FriendlyByteBuf::writeUtf);
        buffer.writeCollection(backupManifestFiles, FriendlyByteBuf::writeUtf);
        buffer.writeVarInt(totalPieces);
        buffer.writeVarInt(nextPieceOffset);
        buffer.writeLong(pieceRevision);
        MKWorkspacePacketPayloads.warnIfLarge("open_workspace_screen", buffer.writerIndex() - startIndex);
    }

    public static void handle(OpenWorkspaceScreenPacket packet, IPayloadContext context) {
        MKStructureWorkspace workspace = packet.workspaceTag != null ? MKStructureWorkspace.fromTag(packet.workspaceTag) : null;
        if (Minecraft.getInstance().screen instanceof MKWorkspaceScreen current) {
            Minecraft.getInstance().setScreen(current.copyWithWorkspace(workspace, packet.importManifestIds,
                    packet.backupManifestFiles, packet.totalPieces, packet.nextPieceOffset, packet.pieceRevision));
        } else {
            Minecraft.getInstance().setScreen(new MKWorkspaceScreen(packet.anchor, workspace, packet.importManifestIds,
                    packet.backupManifestFiles, packet.totalPieces, packet.nextPieceOffset, packet.pieceRevision));
        }
        if (workspace != null && packet.nextPieceOffset < packet.totalPieces) {
            PacketDistributor.sendToServer(new RequestWorkspacePieceChunkPacket(packet.anchor, packet.pieceRevision,
                    packet.nextPieceOffset));
        }
    }
}
