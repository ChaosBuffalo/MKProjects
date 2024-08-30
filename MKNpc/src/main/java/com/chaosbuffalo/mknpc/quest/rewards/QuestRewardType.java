package com.chaosbuffalo.mknpc.quest.rewards;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public interface QuestRewardType<T extends QuestReward> {

    MapCodec<T> codec();
}
