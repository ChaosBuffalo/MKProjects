package com.chaosbuffalo.mkcore.core.combat;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public record AbilityMeleeAttackContext(IMKEntityData attackerData,
                                        LivingEntity attacker,
                                        LivingEntity target,
                                        InteractionHand hand,
                                        ResourceLocation abilityId,
                                        float swingDamageScale,
                                        float bonusDamage,
                                        Entity directEntity,
                                        int[] swingStartTicks,
                                        int[] swingDurationTicks,
                                        boolean triggerVisualSequence) {
}
