package com.chaosbuffalo.mkcore.client.rendering.animations.spell;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

public interface SpellAnimationProfileResolver {
    @Nullable
    ResourceLocation resolve(LivingEntity entity);
}
