package com.chaosbuffalo.mknpc.npc.option_entries;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public interface INpcOptionEntry {

    ResourceLocation getOptionId();

    void applyToEntity(Entity entity);

    default boolean isValid() {
        return true;
    }
}
