package com.chaosbuffalo.mknpc.quest.requirements;

import com.mojang.serialization.Codec;

public interface QuestRequirementType<T extends QuestRequirement> {
    Codec<T> codec();
}
