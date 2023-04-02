package com.chaosbuffalo.mkcore.abilities.ai.conditions;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityDecisionContext;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityTargetingDecision;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;

public class MeleeSpellInterruptUseCondition extends AbilityUseCondition {
    private final AbilityTargetingDecision.MovementSuggestion movementSuggestion;

    public MeleeSpellInterruptUseCondition(MKAbility ability) {
        super(ability);
        movementSuggestion = AbilityTargetingDecision.MovementSuggestion.MELEE;
    }

    @Nonnull
    @Override
    public AbilityTargetingDecision getDecision(AbilityDecisionContext context) {
        LivingEntity threatTarget = context.getThreatTarget();
        if (threatTarget != null) {
            if (MKCore.getEntityData(threatTarget).map(x -> x.getAbilityExecutor().isCasting()).orElse(false)) {
                return new AbilityTargetingDecision(threatTarget, movementSuggestion, getAbility());
            }
        }
        return AbilityTargetingDecision.UNDECIDED;
    }
}