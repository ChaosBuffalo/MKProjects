package com.chaosbuffalo.mknpc.quest.data.objective;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public abstract class ObjectiveInstanceData implements INBTSerializable<CompoundTag> {

    public ObjectiveInstanceData(){

    }

    public ObjectiveInstanceData(CompoundTag nbt){
        deserializeNBT(nbt);
    }
}
