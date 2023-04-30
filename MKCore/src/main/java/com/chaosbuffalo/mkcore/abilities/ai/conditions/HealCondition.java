package com.chaosbuffalo.mkcore.abilities.ai.conditions;

import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityDecisionContext;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityTargetingDecision;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;
import java.util.List;

public class HealCondition extends AbilityUseCondition {

    private final float healThreshold;
    private final AbilityTargetingDecision.MovementSuggestion movementSuggestion;
    private boolean selfOnly;

    public HealCondition(float healThreshold) {
        this.healThreshold = healThreshold;
        this.movementSuggestion = AbilityTargetingDecision.MovementSuggestion.FOLLOW;
        selfOnly = false;
    }

    public HealCondition setSelfOnly(boolean selfOnly) {
        this.selfOnly = selfOnly;
        return this;
    }

    public HealCondition() {
        this(.75f);
    }

    private boolean needsHealing(LivingEntity entity) {
        return entity.getHealth() <= entity.getMaxHealth() * healThreshold;
    }

    @Nonnull
    @Override
    public AbilityTargetingDecision getDecision(MKAbilityInfo abilityInfo, AbilityDecisionContext context) {
        if (abilityInfo.getAbility().getTargetContext().canTargetCaster() && needsHealing(context.getCaster())) {
            return new AbilityTargetingDecision(context.getCaster(), abilityInfo);
        } else if (!selfOnly) {
            List<LivingEntity> friends = context.getFriendlies();
            for (LivingEntity target : friends) {
                if (needsHealing(target)) {
                    return new AbilityTargetingDecision(target, abilityInfo, movementSuggestion);
                }
            }
        }
        return AbilityTargetingDecision.UNDECIDED;
    }
}
