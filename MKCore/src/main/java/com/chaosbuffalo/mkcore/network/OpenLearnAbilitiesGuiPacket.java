package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.training.AbilityTrainingEvaluation;
import com.chaosbuffalo.mkcore.abilities.training.IAbilityTrainer;
import com.chaosbuffalo.mkcore.client.gui.LearnAbilityPage;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class OpenLearnAbilitiesGuiPacket {
    private final int entityId;
    private final List<AbilityTrainingEvaluation> abilities;

    public OpenLearnAbilitiesGuiPacket(MKPlayerData playerData, IAbilityTrainer trainingEntity) {
        abilities = new ArrayList<>(5);
        entityId = trainingEntity.getEntityId();
        trainingEntity.getTrainableAbilities(playerData).forEach(entry -> {
            AbilityTrainingEvaluation evaluation = entry.evaluate(playerData);
            abilities.add(evaluation);
        });
    }

    public OpenLearnAbilitiesGuiPacket(FriendlyByteBuf buffer) {
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

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(entityId);
        buffer.writeVarInt(abilities.size());
        abilities.forEach(offer -> offer.write(buffer));
    }

    public static void handle(OpenLearnAbilitiesGuiPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> ClientHandler.handleClient(packet));
        ctx.setPacketHandled(true);
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
