package com.chaosbuffalo.mkcore.abilities.ai.conditions;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityDecisionContext;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityTargetingDecision;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;

public abstract class AbilityUseCondition {
    private final MKAbility ability;

    public AbilityUseCondition(MKAbility ability) {
        this.ability = ability;
    }

    public MKAbility getAbility() {
        return ability;
    }

    @Nonnull
    public abstract AbilityTargetingDecision getDecision(AbilityDecisionContext context);

    protected boolean isInRange(AbilityDecisionContext context, LivingEntity target) {
        float range = getAbility().getDistance(context.getCaster());
        return target.distanceToSqr(context.getCaster()) <= range * range;
    }
}
