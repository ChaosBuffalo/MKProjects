package com.chaosbuffalo.mknpc.quest.data.objective;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public abstract class ObjectiveInstanceData implements INBTSerializable<CompoundTag> {

    public ObjectiveInstanceData() {

    }

    public ObjectiveInstanceData(HolderLookup.Provider provider, CompoundTag nbt) {
        deserializeNBT(provider, nbt);
    }
}
