package com.chaosbuffalo.mkcore.client.rendering.animations.melee;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

public interface MeleeAnimationProfileResolver {
    @Nullable
    default ResourceLocation resolve(LivingEntity entity) {
        return resolve(entity, InteractionHand.MAIN_HAND);
    }

    @Nullable
    ResourceLocation resolve(LivingEntity entity, InteractionHand hand);
}
