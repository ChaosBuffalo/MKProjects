package com.chaosbuffalo.mknpc.quest.objectives;

import com.mojang.serialization.Codec;

public interface QuestObjectiveType<T extends QuestObjective<?>> {
    Codec<T> codec();
}
