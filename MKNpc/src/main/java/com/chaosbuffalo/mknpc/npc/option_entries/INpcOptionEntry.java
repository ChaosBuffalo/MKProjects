package com.chaosbuffalo.mknpc.npc.option_entries;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.INBTSerializable;

public interface INpcOptionEntry extends INBTSerializable<CompoundTag> {

    void applyToEntity(Entity entity);

    default boolean isValid() {
        return true;
    }
}
