package com.chaosbuffalo.mkcore.abilities.ai.conditions;

import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityDecisionContext;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityTargetingDecision;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;

public class StandardUseCondition extends AbilityUseCondition {
    private final AbilityTargetingDecision.MovementSuggestion movementSuggestion;

    public StandardUseCondition() {
        super();
        movementSuggestion = AbilityTargetingDecision.MovementSuggestion.KITE;
    }

    @Nonnull
    @Override
    public AbilityTargetingDecision getDecision(MKAbilityInfo abilityInfo, AbilityDecisionContext context) {
        LivingEntity threatTarget = context.getThreatTarget();
        if (threatTarget != null) {
            return new AbilityTargetingDecision(threatTarget, abilityInfo, movementSuggestion);
        }
        return AbilityTargetingDecision.UNDECIDED;
    }
}
