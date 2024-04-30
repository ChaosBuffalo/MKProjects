package com.chaosbuffalo.mknpc.quest.rewards;

import com.mojang.serialization.Codec;

public interface QuestRewardType<T extends QuestReward> {

    Codec<T> codec();
}
