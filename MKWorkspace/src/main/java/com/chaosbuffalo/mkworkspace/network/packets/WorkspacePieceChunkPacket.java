package com.chaosbuffalo.mkworkspace.network.packets;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public class WorkspacePieceChunkPacket implements CustomPacketPayload {
    public static final Type<WorkspacePieceChunkPacket> TYPE = new Type<>(MKWorkspace.id("workspace_piece_chunk"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WorkspacePieceChunkPacket> STREAM_CODEC =
            StreamCodec.ofMember(WorkspacePieceChunkPacket::toBytes, WorkspacePieceChunkPacket::new);

    private final BlockPos anchor;
    private final CompoundTag piecesTag;
    private final int totalPieces;
    private final int nextPieceOffset;
    private final long pieceRevision;

    public WorkspacePieceChunkPacket(BlockPos anchor, MKWorkspacePacketPayloads.PieceChunk chunk) {
        this.anchor = anchor;
        this.piecesTag = MKWorkspacePacketPayloads.pieceListTag(chunk.pieces());
        this.totalPieces = chunk.totalPieces();
        this.nextPieceOffset = chunk.nextOffset();
        this.pieceRevision = chunk.revision();
    }

    public WorkspacePieceChunkPacket(FriendlyByteBuf buffer) {
        this.anchor = buffer.readBlockPos();
        CompoundTag tag = buffer.readNbt();
        if (tag == null) {
            throw new IllegalStateException("workspace piece chunk packet was missing payload");
        }
        this.piecesTag = tag;
        this.totalPieces = buffer.readVarInt();
        this.nextPieceOffset = buffer.readVarInt();
        this.pieceRevision = buffer.readLong();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        int startIndex = buffer.writerIndex();
        buffer.writeBlockPos(anchor);
        buffer.writeNbt(piecesTag);
        buffer.writeVarInt(totalPieces);
        buffer.writeVarInt(nextPieceOffset);
        buffer.writeLong(pieceRevision);
        MKWorkspacePacketPayloads.warnIfLarge("workspace_piece_chunk", buffer.writerIndex() - startIndex);
    }

    public static void handle(WorkspacePieceChunkPacket packet, IPayloadContext context) {
        List<MKWorkspacePieceDefinition> pieces = MKWorkspacePacketPayloads.parsePieceListTag(packet.piecesTag);
        MKWorkspaceClientPackets.applyPieceChunk(packet.anchor, pieces, packet.totalPieces, packet.nextPieceOffset,
                packet.pieceRevision);
    }
}
