package com.chaosbuffalo.mkcore.capabilities;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public abstract class SingleSerializableCapabilityProvider<CapTarget, CapType extends INBTSerializable<CompoundTag>>
        extends SingleCapabilityProvider<CapTarget, CapType> {

    public SingleSerializableCapabilityProvider(CapTarget attached) {
        super(attached);
    }



    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return data.serializeNBT(provider);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        data.deserializeNBT(provider, nbt);
    }
}
