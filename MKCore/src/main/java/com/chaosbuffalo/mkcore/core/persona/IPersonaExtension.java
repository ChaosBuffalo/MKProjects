package com.chaosbuffalo.mkcore.core.persona;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public interface IPersonaExtension {
    ResourceLocation getName();

    @Nullable
    default CompoundTag serialize(HolderLookup.Provider provider) {
        return null;
    }

    default void deserialize(HolderLookup.Provider provider, CompoundTag tag) {

    }
}
