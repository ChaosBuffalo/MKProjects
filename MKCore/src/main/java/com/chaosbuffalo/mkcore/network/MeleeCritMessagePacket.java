package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MeleeCritMessagePacket(int targetId, int sourceId, float critDamage) implements CustomPacketPayload {
    public static final Type<MeleeCritMessagePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "melee_crit_message"));
    public static final StreamCodec<FriendlyByteBuf, MeleeCritMessagePacket> STREAM_CODEC = StreamCodec.ofMember(
            MeleeCritMessagePacket::toBytes, MeleeCritMessagePacket::new
    );

    public MeleeCritMessagePacket(FriendlyByteBuf buffer) {
        this(buffer.readInt(), buffer.readInt(), buffer.readFloat());
    }

    private void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(targetId);
        buffer.writeInt(sourceId);
        buffer.writeFloat(critDamage);
    }

    public static void handle(final MeleeCritMessagePacket packet, IPayloadContext context) {
        CritMessageClientHandler.handleMelee(packet);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
