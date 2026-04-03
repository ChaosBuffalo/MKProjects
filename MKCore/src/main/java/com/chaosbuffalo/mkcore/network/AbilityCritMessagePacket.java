package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AbilityCritMessagePacket(int targetId, int sourceId, float critDamage,
                                       ResourceLocation abilityId, ResourceLocation damageTypeId)
        implements CustomPacketPayload {
    public static final Type<AbilityCritMessagePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "ability_crit_message"));
    public static final StreamCodec<FriendlyByteBuf, AbilityCritMessagePacket> STREAM_CODEC = StreamCodec.ofMember(
            AbilityCritMessagePacket::toBytes, AbilityCritMessagePacket::new
    );

    public AbilityCritMessagePacket(FriendlyByteBuf buffer) {
        this(buffer.readInt(), buffer.readInt(), buffer.readFloat(),
                buffer.readResourceLocation(), buffer.readResourceLocation());
    }

    private void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(targetId);
        buffer.writeInt(sourceId);
        buffer.writeFloat(critDamage);
        buffer.writeResourceLocation(abilityId);
        buffer.writeResourceLocation(damageTypeId);
    }

    public static void handle(final AbilityCritMessagePacket packet, IPayloadContext context) {
        CritMessageClientHandler.handleAbility(packet);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
