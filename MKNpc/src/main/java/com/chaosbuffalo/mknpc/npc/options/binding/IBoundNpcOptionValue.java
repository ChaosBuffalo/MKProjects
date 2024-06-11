package com.chaosbuffalo.mknpc.npc.options.binding;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public interface IBoundNpcOptionValue {

    ResourceLocation getOptionId();

    void applyToEntity(Entity entity);
}
