package com.chaosbuffalo.mkworkspace.network.packets;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.MKStructureWorkspaceService;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeCoordinator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record ConfirmWorkspaceChangePacket(UUID planId) implements CustomPacketPayload {
    public static final Type<ConfirmWorkspaceChangePacket> TYPE =
            new Type<>(MKWorkspace.id("confirm_workspace_change"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfirmWorkspaceChangePacket> STREAM_CODEC =
            StreamCodec.ofMember(ConfirmWorkspaceChangePacket::write, ConfirmWorkspaceChangePacket::new);

    private ConfirmWorkspaceChangePacket(FriendlyByteBuf buffer) {
        this(buffer.readUUID());
    }

    private void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(planId);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ConfirmWorkspaceChangePacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || !player.isCreative()) {
            return;
        }
        MKWorkspaceChangeCoordinator.Confirmation confirmation =
                MKWorkspaceChangeCoordinator.shared().confirm(player, packet.planId);
        if (confirmation.status() == MKWorkspaceChangeCoordinator.ConfirmationStatus.STALE_REPREPARED &&
                confirmation.replacement() != null) {
            MKWorkspaceChangePackets.sendPreparedPlan(player, confirmation.replacement());
            MKWorkspaceChangePackets.sendResult(player, confirmation.replacement().requestId(),
                    confirmation.replacement().change().anchor(), false, true, confirmation.message());
            return;
        }
        MKWorkspaceChangePackets.sendResult(player, confirmation.requestId(), confirmation.anchor(),
                confirmation.status() == MKWorkspaceChangeCoordinator.ConfirmationStatus.APPLIED,
                false, confirmation.message());
        if (confirmation.status() == MKWorkspaceChangeCoordinator.ConfirmationStatus.APPLIED &&
                confirmation.result() != null && confirmation.result().refreshWorkspaceScreen()) {
            new MKStructureWorkspaceService().openWorkspaceScreen(player, confirmation.result().anchor());
        }
    }
}
