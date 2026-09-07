package com.chaosbuffalo.mkworkspace.network.packets;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeSummary;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.time.Instant;
import java.util.UUID;

public record WorkspaceChangePreflightPacket(UUID requestId, UUID planId, BlockPos anchor, long expiresAtEpochMilli,
                                             MKWorkspaceChangeSummary summary, int totalEffects)
        implements CustomPacketPayload {
    public static final Type<WorkspaceChangePreflightPacket> TYPE =
            new Type<>(MKWorkspace.id("workspace_change_preflight"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WorkspaceChangePreflightPacket> STREAM_CODEC =
            StreamCodec.ofMember(WorkspaceChangePreflightPacket::write, WorkspaceChangePreflightPacket::new);

    private WorkspaceChangePreflightPacket(FriendlyByteBuf buffer) {
        this(buffer.readUUID(), buffer.readUUID(), buffer.readBlockPos(), buffer.readLong(),
                MKWorkspaceChangeNetworkCodecs.readSummaryMetadata(buffer), buffer.readVarInt());
    }

    private void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(requestId);
        buffer.writeUUID(planId);
        buffer.writeBlockPos(anchor);
        buffer.writeLong(expiresAtEpochMilli);
        MKWorkspaceChangeNetworkCodecs.writeSummaryMetadata(buffer, summary);
        buffer.writeVarInt(totalEffects);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(WorkspaceChangePreflightPacket packet, IPayloadContext context) {
        MKWorkspaceClientPackets.beginChangePreflight(packet.requestId, packet.planId, packet.anchor,
                Instant.ofEpochMilli(packet.expiresAtEpochMilli), packet.summary, packet.totalEffects);
    }
}
