package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.training.AbilityTrainingEvaluation;
import com.chaosbuffalo.mkcore.client.gui.LearnAbilityPage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record OpenLearnAbilitiesGuiPacket(int entityId,
                                          List<AbilityTrainingEvaluation> abilities) implements CustomPacketPayload {
    public static final Type<OpenLearnAbilitiesGuiPacket> TYPE = new Type<>(MKCore.id("open_learn_abilities_gui"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenLearnAbilitiesGuiPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, OpenLearnAbilitiesGuiPacket::entityId,
            AbilityTrainingEvaluation.STREAM_CODEC.apply(ByteBufCodecs.list()), OpenLearnAbilitiesGuiPacket::abilities,
            OpenLearnAbilitiesGuiPacket::new
    );


    public static void handle(final OpenLearnAbilitiesGuiPacket packet, IPayloadContext context) {
        ClientHandler.handleClient(packet, context);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    static class ClientHandler {
        public static void handleClient(OpenLearnAbilitiesGuiPacket packet, IPayloadContext context) {
            var playerData = MKCore.getPlayerOrThrow(context.player());

            Minecraft.getInstance().setScreen(new LearnAbilityPage(playerData, packet.abilities, packet.entityId));
        }
    }
}
