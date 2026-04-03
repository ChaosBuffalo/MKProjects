package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ProjectileCritMessagePacket(int targetId, int sourceId, float critDamage, int projectileId)
        implements CustomPacketPayload {
    public static final Type<ProjectileCritMessagePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "projectile_crit_message"));
    public static final StreamCodec<FriendlyByteBuf, ProjectileCritMessagePacket> STREAM_CODEC = StreamCodec.ofMember(
            ProjectileCritMessagePacket::toBytes, ProjectileCritMessagePacket::new
    );

    public ProjectileCritMessagePacket(FriendlyByteBuf buffer) {
        this(buffer.readInt(), buffer.readInt(), buffer.readFloat(), buffer.readInt());
    }

    private void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(targetId);
        buffer.writeInt(sourceId);
        buffer.writeFloat(critDamage);
        buffer.writeInt(projectileId);
    }

    public static void handle(final ProjectileCritMessagePacket packet, IPayloadContext context) {
        CritMessageClientHandler.handleProjectile(packet);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
