package com.chaosbuffalo.mkcore.core.persona;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public interface IPersonaExtension {
    ResourceLocation getName();

    CompoundTag serialize(HolderLookup.Provider provider);

    void deserialize(HolderLookup.Provider provider, CompoundTag tag);
}
