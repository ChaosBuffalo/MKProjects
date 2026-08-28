package com.chaosbuffalo.mkworkspace.network.packets;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.insert.MKWorkspaceInsertOverlayService;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.insert.MKWorkspaceInsertOverlaySnapshot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class RequestWorkspaceInsertOverlayPacket implements CustomPacketPayload {
    public static final Type<RequestWorkspaceInsertOverlayPacket> TYPE =
            new Type<>(MKWorkspace.id("request_workspace_insert_overlay"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestWorkspaceInsertOverlayPacket> STREAM_CODEC =
            StreamCodec.ofMember(RequestWorkspaceInsertOverlayPacket::toBytes,
                    RequestWorkspaceInsertOverlayPacket::new);

    public RequestWorkspaceInsertOverlayPacket() {
    }

    public RequestWorkspaceInsertOverlayPacket(FriendlyByteBuf buffer) {
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buffer) {
    }

    public static void handle(RequestWorkspaceInsertOverlayPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || !player.isCreative()) {
            return;
        }
        MKWorkspaceInsertOverlaySnapshot snapshot = MKWorkspaceInsertOverlayService.capture(player.serverLevel(),
                player.blockPosition());
        PacketDistributor.sendToPlayer(player, new WorkspaceInsertOverlayPacket(snapshot));
    }
}
