package com.chaosbuffalo.mknpc.quest.requirements;

import com.mojang.serialization.MapCodec;

public interface QuestRequirementType<T extends QuestRequirement> {
    MapCodec<T> codec();
}
