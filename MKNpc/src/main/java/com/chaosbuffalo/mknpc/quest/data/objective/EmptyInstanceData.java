package com.chaosbuffalo.mknpc.quest.data.objective;

import net.minecraft.nbt.CompoundTag;

public class EmptyInstanceData extends ObjectiveInstanceData{

    @Override
    public CompoundTag serializeNBT() {
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {

    }
}
