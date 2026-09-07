package com.chaosbuffalo.mkworkspace.network.packets;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record WorkspaceChangeResultPacket(UUID requestId, BlockPos anchor, boolean success,
                                          boolean stale, String message) implements CustomPacketPayload {
    public static final Type<WorkspaceChangeResultPacket> TYPE =
            new Type<>(MKWorkspace.id("workspace_change_result"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WorkspaceChangeResultPacket> STREAM_CODEC =
            StreamCodec.ofMember(WorkspaceChangeResultPacket::write, WorkspaceChangeResultPacket::new);

    private WorkspaceChangeResultPacket(FriendlyByteBuf buffer) {
        this(buffer.readUUID(), buffer.readBlockPos(), buffer.readBoolean(), buffer.readBoolean(), buffer.readUtf());
    }

    private void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(requestId);
        buffer.writeBlockPos(anchor);
        buffer.writeBoolean(success);
        buffer.writeBoolean(stale);
        buffer.writeUtf(message);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(WorkspaceChangeResultPacket packet, IPayloadContext context) {
        MKWorkspaceClientPackets.applyChangeResult(packet.requestId, packet.anchor, packet.success,
                packet.stale, packet.message);
    }
}
