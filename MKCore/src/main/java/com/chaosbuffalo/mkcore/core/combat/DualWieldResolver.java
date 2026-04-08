package com.chaosbuffalo.mkcore.core.combat;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

public interface DualWieldResolver {
    default boolean canUseCustomMelee(LivingEntity entity, InteractionHand hand) {
        return canUseForAttack(entity, hand);
    }

    boolean canUseForAttack(LivingEntity entity, InteractionHand hand);
}
