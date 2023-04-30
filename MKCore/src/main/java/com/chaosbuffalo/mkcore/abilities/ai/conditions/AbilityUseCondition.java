package com.chaosbuffalo.mkcore.abilities.ai.conditions;

import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityDecisionContext;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityTargetingDecision;

import javax.annotation.Nonnull;

public abstract class AbilityUseCondition {

    @Nonnull
    public abstract AbilityTargetingDecision getDecision(MKAbilityInfo abilityInfo, AbilityDecisionContext context);

}
