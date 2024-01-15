package com.chaosbuffalo.mkcore.core.persona;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public interface IPersonaExtension {
    ResourceLocation getName();

    CompoundTag serialize();

    void deserialize(CompoundTag tag);
}
