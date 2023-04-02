package com.chaosbuffalo.mkcore.abilities.training;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.network.OpenLearnAbilitiesGuiPacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import net.minecraft.server.level.ServerPlayer;

public interface IAbilityTrainingEntity {
    IAbilityTrainer getAbilityTrainer();

    default void openTrainingGui(ServerPlayer playerEntity) {
        MKCore.getPlayer(playerEntity).ifPresent(playerData -> {
            PacketHandler.sendMessage(new OpenLearnAbilitiesGuiPacket(playerData, getAbilityTrainer()), playerEntity);
        });
    }
}
