package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public class ForgetAbilitiesRequestPacket implements CustomPacketPayload {
    private final List<ResourceLocation> forgetting;

    public static final CustomPacketPayload.Type<ForgetAbilitiesRequestPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "forget_abilities_request"));

    public static final StreamCodec<FriendlyByteBuf, ForgetAbilitiesRequestPacket> STREAM_CODEC = StreamCodec.ofMember(
            ForgetAbilitiesRequestPacket::toBytes, ForgetAbilitiesRequestPacket::new
    );

    public ForgetAbilitiesRequestPacket(List<ResourceLocation> forgetting) {
        this.forgetting = forgetting;
    }

    public ForgetAbilitiesRequestPacket(FriendlyByteBuf buffer) {
        int count = buffer.readInt();
        forgetting = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            forgetting.add(buffer.readResourceLocation());
        }
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(forgetting.size());
        for (ResourceLocation loc : forgetting) {
            buffer.writeResourceLocation(loc);
        }
    }

    public static void handle(final ForgetAbilitiesRequestPacket packet, IPayloadContext context) {
        MKCore.getPlayer(context.player()).ifPresent(playerData -> {
            for (ResourceLocation toForget : packet.forgetting) {
                playerData.getAbilities().unlearnAbility(toForget, AbilitySource.TRAINED);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
