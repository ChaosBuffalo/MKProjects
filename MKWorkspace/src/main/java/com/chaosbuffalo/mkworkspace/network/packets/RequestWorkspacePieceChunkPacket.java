package com.chaosbuffalo.mkworkspace.network.packets;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.capability.IMKStructureWorkspaceData;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class RequestWorkspacePieceChunkPacket implements CustomPacketPayload {
    public static final Type<RequestWorkspacePieceChunkPacket> TYPE =
            new Type<>(MKWorkspace.id("request_workspace_piece_chunk"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestWorkspacePieceChunkPacket> STREAM_CODEC =
            StreamCodec.ofMember(RequestWorkspacePieceChunkPacket::toBytes, RequestWorkspacePieceChunkPacket::new);

    private final BlockPos anchor;
    private final long pieceRevision;
    private final int offset;

    public RequestWorkspacePieceChunkPacket(BlockPos anchor, long pieceRevision, int offset) {
        this.anchor = anchor;
        this.pieceRevision = pieceRevision;
        this.offset = offset;
    }

    public RequestWorkspacePieceChunkPacket(FriendlyByteBuf buffer) {
        this.anchor = buffer.readBlockPos();
        this.pieceRevision = buffer.readLong();
        this.offset = buffer.readVarInt();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(anchor);
        buffer.writeLong(pieceRevision);
        buffer.writeVarInt(offset);
    }

    public static void handle(RequestWorkspacePieceChunkPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || !player.isCreative()) {
            return;
        }
        IMKStructureWorkspaceData.get(player.serverLevel())
                .getWorkspaceByAnchor(packet.anchor)
                .ifPresent(workspace -> sendChunk(player, workspace, packet));
    }

    private static void sendChunk(ServerPlayer player, MKStructureWorkspace workspace,
                                  RequestWorkspacePieceChunkPacket packet) {
        MKWorkspacePacketPayloads.PieceChunk chunk = MKWorkspacePacketPayloads.pieceChunk(workspace, packet.offset);
        if (chunk.revision() != packet.pieceRevision) {
            MKWorkspace.LOGGER.warn("Workspace piece chunk request at {} used stale revision {}; current revision is {}.",
                    packet.anchor, packet.pieceRevision, chunk.revision());
            chunk = MKWorkspacePacketPayloads.pieceChunk(workspace, 0);
        }
        PacketDistributor.sendToPlayer(player, new WorkspacePieceChunkPacket(packet.anchor, chunk));
    }
}
