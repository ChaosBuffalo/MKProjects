package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.training.AbilityTrainingEvaluation;
import com.chaosbuffalo.mkcore.abilities.training.IAbilityTrainer;
import com.chaosbuffalo.mkcore.client.gui.LearnAbilityPage;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public class OpenLearnAbilitiesGuiPacket implements CustomPacketPayload {
    private final int entityId;
    private final List<AbilityTrainingEvaluation> abilities;

    public static final CustomPacketPayload.Type<OpenLearnAbilitiesGuiPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "open_learn_abilities_gui"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenLearnAbilitiesGuiPacket> STREAM_CODEC = StreamCodec.ofMember(
            OpenLearnAbilitiesGuiPacket::toBytes, OpenLearnAbilitiesGuiPacket::new
    );


    public OpenLearnAbilitiesGuiPacket(MKPlayerData playerData, IAbilityTrainer trainingEntity) {
        abilities = new ArrayList<>(5);
        entityId = trainingEntity.getEntityId();
        trainingEntity.getTrainableAbilities(playerData).forEach(entry -> {
            AbilityTrainingEvaluation evaluation = entry.evaluate(playerData);
            abilities.add(evaluation);
        });
    }

    public OpenLearnAbilitiesGuiPacket(RegistryFriendlyByteBuf buffer) {
        entityId = buffer.readInt();
        int count = buffer.readVarInt();
        abilities = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            AbilityTrainingEvaluation evaluation = AbilityTrainingEvaluation.read(buffer);
            if (evaluation != null) {
                abilities.add(evaluation);
            }
        }
    }

    public void toBytes(RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(entityId);
        buffer.writeVarInt(abilities.size());
        abilities.forEach(offer -> offer.write(buffer));
    }

    public static void handle(final OpenLearnAbilitiesGuiPacket packet, IPayloadContext context) {
        ClientHandler.handleClient(packet);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    static class ClientHandler {
        public static void handleClient(OpenLearnAbilitiesGuiPacket packet) {
            Player player = Minecraft.getInstance().player;
            if (player == null)
                return;
            MKCore.getPlayer(player).ifPresent(playerData ->
                    Minecraft.getInstance().setScreen(new LearnAbilityPage(playerData, packet.abilities, packet.entityId)));

        }
    }
}
