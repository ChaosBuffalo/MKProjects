package com.chaosbuffalo.mknpc.network.packets;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.world.gen.workspace.MKStructureWorkspaceService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class TeleportToWorkspacePiecePacket implements CustomPacketPayload {
    public static final Type<TeleportToWorkspacePiecePacket> TYPE =
            new Type<>(MKNpc.id("teleport_to_workspace_piece"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TeleportToWorkspacePiecePacket> STREAM_CODEC =
            StreamCodec.ofMember(TeleportToWorkspacePiecePacket::toBytes, TeleportToWorkspacePiecePacket::new);

    private final BlockPos anchor;
    private final UUID pieceId;

    public TeleportToWorkspacePiecePacket(BlockPos anchor, UUID pieceId) {
        this.anchor = anchor;
        this.pieceId = pieceId;
    }

    public TeleportToWorkspacePiecePacket(FriendlyByteBuf buffer) {
        this.anchor = buffer.readBlockPos();
        this.pieceId = buffer.readUUID();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(anchor);
        buffer.writeUUID(pieceId);
    }

    public static void handle(TeleportToWorkspacePiecePacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || !player.isCreative()) {
            return;
        }
        boolean teleported = new MKStructureWorkspaceService()
                .teleportToWorkspacePiece(player, packet.anchor, packet.pieceId);
        if (!teleported) {
            MKWorkspaceValidationMessages.displayFailure(player,
                    "Workspace teleport failed: no matching template row was found.");
        }
    }
}
