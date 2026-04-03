package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record EffectCritMessagePacket(int targetId, int sourceId, float critDamage,
                                      ResourceLocation damageTypeId, String effectTypeName)
        implements CustomPacketPayload {
    public static final Type<EffectCritMessagePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "effect_crit_message"));
    public static final StreamCodec<FriendlyByteBuf, EffectCritMessagePacket> STREAM_CODEC = StreamCodec.ofMember(
            EffectCritMessagePacket::toBytes, EffectCritMessagePacket::new
    );

    public EffectCritMessagePacket(FriendlyByteBuf buffer) {
        this(buffer.readInt(), buffer.readInt(), buffer.readFloat(),
                buffer.readResourceLocation(), buffer.readUtf());
    }

    private void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(targetId);
        buffer.writeInt(sourceId);
        buffer.writeFloat(critDamage);
        buffer.writeResourceLocation(damageTypeId);
        buffer.writeUtf(effectTypeName);
    }

    public static void handle(final EffectCritMessagePacket packet, IPayloadContext context) {
        CritMessageClientHandler.handleEffect(packet);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
