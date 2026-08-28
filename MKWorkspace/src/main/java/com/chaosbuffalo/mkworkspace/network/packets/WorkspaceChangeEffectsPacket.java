package com.chaosbuffalo.mkworkspace.network.packets;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeEffect;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.UUID;

public record WorkspaceChangeEffectsPacket(UUID requestId, UUID planId, int offset, int totalEffects,
                                           List<MKWorkspaceChangeEffect> effects) implements CustomPacketPayload {
    public static final Type<WorkspaceChangeEffectsPacket> TYPE =
            new Type<>(MKWorkspace.id("workspace_change_effects"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WorkspaceChangeEffectsPacket> STREAM_CODEC =
            StreamCodec.ofMember(WorkspaceChangeEffectsPacket::write, WorkspaceChangeEffectsPacket::new);

    public WorkspaceChangeEffectsPacket {
        effects = List.copyOf(effects);
    }

    private WorkspaceChangeEffectsPacket(FriendlyByteBuf buffer) {
        this(buffer.readUUID(), buffer.readUUID(), buffer.readVarInt(), buffer.readVarInt(),
                MKWorkspaceChangeNetworkCodecs.readEffects(buffer));
    }

    private void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(requestId);
        buffer.writeUUID(planId);
        buffer.writeVarInt(offset);
        buffer.writeVarInt(totalEffects);
        MKWorkspaceChangeNetworkCodecs.writeEffects(buffer, effects);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(WorkspaceChangeEffectsPacket packet, IPayloadContext context) {
        MKWorkspaceClientPackets.appendChangeEffects(packet.requestId, packet.planId, packet.offset,
                packet.totalEffects, packet.effects);
    }
}
