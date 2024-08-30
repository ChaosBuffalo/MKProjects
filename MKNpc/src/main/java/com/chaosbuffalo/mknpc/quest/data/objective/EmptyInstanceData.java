package com.chaosbuffalo.mknpc.quest.data.objective;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public class EmptyInstanceData extends ObjectiveInstanceData {

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {

    }
}
