package com.chaosbuffalo.mknpc.quest.data;

import com.chaosbuffalo.mknpc.quest.QuestDefinition;
import net.minecraft.nbt.CompoundTag;

public interface IQuestInstanceData {

    CompoundTag serializeNBT();
    void deserializeNBT(CompoundTag nbt, QuestDefinition definition);
}
