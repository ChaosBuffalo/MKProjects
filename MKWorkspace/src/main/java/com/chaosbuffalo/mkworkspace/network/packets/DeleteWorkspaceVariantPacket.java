package com.chaosbuffalo.mkworkspace.network.packets;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.MKStructureWorkspaceService;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.insert.MKWorkspaceInsertOverlayService;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.insert.MKWorkspaceInsertOverlaySnapshot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class DeleteWorkspaceVariantPacket implements CustomPacketPayload {
    public static final Type<DeleteWorkspaceVariantPacket> TYPE =
            new Type<>(MKWorkspace.id("delete_workspace_variant"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DeleteWorkspaceVariantPacket> STREAM_CODEC =
            StreamCodec.ofMember(DeleteWorkspaceVariantPacket::toBytes, DeleteWorkspaceVariantPacket::new);

    private final BlockPos anchor;
    private final UUID pieceId;

    public DeleteWorkspaceVariantPacket(BlockPos anchor, UUID pieceId) {
        this.anchor = anchor;
        this.pieceId = pieceId;
    }

    public DeleteWorkspaceVariantPacket(FriendlyByteBuf buffer) {
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

    public static void handle(DeleteWorkspaceVariantPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || !player.isCreative()) {
            return;
        }
        new MKStructureWorkspaceService()
                .deleteWorkspaceVariant(player.serverLevel(), packet.anchor, packet.pieceId)
                .ifPresentOrElse(
                        updated -> {
                            player.connection.send(new OpenWorkspaceScreenPacket(packet.anchor, updated));
                            MKWorkspaceInsertOverlaySnapshot snapshot = MKWorkspaceInsertOverlayService.capture(
                                    player.serverLevel(), player.blockPosition());
                            PacketDistributor.sendToPlayer(player, new WorkspaceInsertOverlayPacket(snapshot));
                        },
                        () -> MKWorkspaceValidationMessages.displayFailure(player,
                                "Workspace variant delete failed: no matching authored variant was found.")
                );
    }
}
