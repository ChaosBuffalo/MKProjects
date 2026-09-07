package com.chaosbuffalo.mkworkspace.network.packets;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeCoordinator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record CancelWorkspaceChangePacket(UUID planId) implements CustomPacketPayload {
    public static final Type<CancelWorkspaceChangePacket> TYPE =
            new Type<>(MKWorkspace.id("cancel_workspace_change"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CancelWorkspaceChangePacket> STREAM_CODEC =
            StreamCodec.ofMember(CancelWorkspaceChangePacket::write, CancelWorkspaceChangePacket::new);

    private CancelWorkspaceChangePacket(FriendlyByteBuf buffer) {
        this(buffer.readUUID());
    }

    private void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(planId);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CancelWorkspaceChangePacket packet, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            MKWorkspaceChangeCoordinator.shared().cancel(player, packet.planId);
        }
    }
}
