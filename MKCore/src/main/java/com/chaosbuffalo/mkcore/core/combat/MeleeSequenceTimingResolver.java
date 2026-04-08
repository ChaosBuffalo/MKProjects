package com.chaosbuffalo.mkcore.core.combat;

import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

@FunctionalInterface
public interface MeleeSequenceTimingResolver {
    @Nullable
    MeleeSequenceTimings resolve(LivingEntity attacker, int attackCount, int firstAttackIndex,
                                 int baseCooldownTicks, int baseSwingDurationTicks, int currentSwingCount);
}
