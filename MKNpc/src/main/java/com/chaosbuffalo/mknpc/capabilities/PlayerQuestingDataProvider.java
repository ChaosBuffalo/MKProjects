package com.chaosbuffalo.mknpc.capabilities;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;

public class PlayerQuestingDataProvider extends NpcCapabilities.Provider<Player, IPlayerQuestingData> {

    public PlayerQuestingDataProvider(Player attached) {
        super(attached);
    }

    @Override
    IPlayerQuestingData makeData(Player attached) {
        return new PlayerQuestingDataHandler(attached);
    }

    @Override
    Capability<IPlayerQuestingData> getCapability() {
        return NpcCapabilities.PLAYER_QUEST_DATA_CAPABILITY;
    }
}