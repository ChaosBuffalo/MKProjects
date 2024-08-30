package com.chaosbuffalo.mknpc.quest.objectives;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public interface QuestObjectiveType<T extends QuestObjective<?>> {
    MapCodec<T> codec();
}
