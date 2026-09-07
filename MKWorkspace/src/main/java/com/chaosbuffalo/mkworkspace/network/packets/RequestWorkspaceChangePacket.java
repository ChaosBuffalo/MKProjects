package com.chaosbuffalo.mkworkspace.network.packets;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeCoordinator;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeRequest;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestWorkspaceChangePacket(MKWorkspaceChangeRequest request) implements CustomPacketPayload {
    public static final Type<RequestWorkspaceChangePacket> TYPE =
            new Type<>(MKWorkspace.id("request_workspace_change"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestWorkspaceChangePacket> STREAM_CODEC =
            StreamCodec.ofMember(RequestWorkspaceChangePacket::write, RequestWorkspaceChangePacket::new);

    private RequestWorkspaceChangePacket(FriendlyByteBuf buffer) {
        this(new MKWorkspaceChangeRequest(buffer.readUUID(), buffer.readResourceLocation(), buffer.readBlockPos(),
                requirePayload(buffer.readNbt())));
    }

    private void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(request.requestId());
        buffer.writeResourceLocation(request.operationId());
        buffer.writeBlockPos(request.anchor());
        buffer.writeNbt(request.payload());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestWorkspaceChangePacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || !player.isCreative()) {
            return;
        }
        try {
            MKWorkspaceChangePackets.sendPreparedPlan(player,
                    MKWorkspaceChangeCoordinator.shared().prepare(player, packet.request));
        } catch (Exception exception) {
            MKWorkspace.LOGGER.error("Failed to prepare workspace change {}", packet.request.operationId(), exception);
            MKWorkspaceChangePackets.sendFailure(player, packet.request.requestId(), packet.request.anchor(),
                    "Workspace preflight failed: " + safeMessage(exception));
        }
    }

    private static CompoundTag requirePayload(CompoundTag tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Workspace change request is missing its payload");
        }
        return tag;
    }

    private static String safeMessage(Exception exception) {
        return exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
    }
}
