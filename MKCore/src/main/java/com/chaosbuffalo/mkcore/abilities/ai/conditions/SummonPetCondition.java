package com.chaosbuffalo.mkcore.abilities.ai.conditions;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityDecisionContext;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityTargetingDecision;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;

public class SummonPetCondition extends AbilityUseCondition {

    public SummonPetCondition(MKAbility ability) {
        super(ability);
    }

    private boolean hasPet(LivingEntity entity) {
        return MKCore.getEntityData(entity).map(x -> x.getPets().isPetActive(getAbility().getAbilityId())).orElse(false);
    }

    @Nonnull
    @Override
    public AbilityTargetingDecision getDecision(AbilityDecisionContext context) {
        if (!hasPet(context.getCaster())) {
            return new AbilityTargetingDecision(context.getCaster(), getAbility());
        }
        return AbilityTargetingDecision.UNDECIDED;
    }

}