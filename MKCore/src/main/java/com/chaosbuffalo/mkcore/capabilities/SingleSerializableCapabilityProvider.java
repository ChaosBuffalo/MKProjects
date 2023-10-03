package com.chaosbuffalo.mkcore.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public abstract class SingleSerializableCapabilityProvider<CapTarget, CapType extends INBTSerializable<CompoundTag>>
        extends SingleCapabilityProvider<CapTarget, CapType> {

    public SingleSerializableCapabilityProvider(CapTarget attached) {
        super(attached);
    }

    @Override
    public CompoundTag serializeNBT() {
        return data.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        data.deserializeNBT(nbt);
    }
}
