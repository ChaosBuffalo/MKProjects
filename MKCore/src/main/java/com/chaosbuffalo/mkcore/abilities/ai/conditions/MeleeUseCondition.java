package com.chaosbuffalo.mkcore.abilities.ai.conditions;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityDecisionContext;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityTargetingDecision;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;

public class MeleeUseCondition extends AbilityUseCondition {
    private final AbilityTargetingDecision.MovementSuggestion movementSuggestion;

    public MeleeUseCondition(MKAbility ability) {
        super(ability);
        movementSuggestion = AbilityTargetingDecision.MovementSuggestion.MELEE;
    }

    @Nonnull
    @Override
    public AbilityTargetingDecision getDecision(AbilityDecisionContext context) {
        LivingEntity threatTarget = context.getThreatTarget();
        if (threatTarget != null) {
            return new AbilityTargetingDecision(threatTarget, movementSuggestion, getAbility());
        }
        return AbilityTargetingDecision.UNDECIDED;
    }
}
