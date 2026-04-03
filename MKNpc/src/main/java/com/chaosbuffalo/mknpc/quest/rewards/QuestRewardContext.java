package com.chaosbuffalo.mknpc.quest.rewards;

import com.chaosbuffalo.mknpc.capabilities.IPlayerQuestingData;
import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.quest.Quest;
import com.chaosbuffalo.mknpc.quest.QuestChainInstance;
import net.minecraft.world.entity.player.Player;

public record QuestRewardContext(
        Player player,
        IPlayerQuestingData playerQuestingData,
        IWorldNpcData worldNpcData,
        QuestChainInstance questChain,
        Quest quest
) {
}
