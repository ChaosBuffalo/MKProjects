package com.chaosbuffalo.mkcore.abilities.ai.conditions;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityDecisionContext;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityTargetingDecision;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class NeedsBuffCondition extends AbilityUseCondition {

    private final MobEffect buffEffect;
    private final Supplier<? extends MKEffect> buffMKEffect;
    private final AbilityTargetingDecision.MovementSuggestion movementSuggestion;
    private boolean selfOnly;


    public NeedsBuffCondition(MobEffect buffEffect) {
        this.buffEffect = buffEffect;
        buffMKEffect = null;
        this.movementSuggestion = AbilityTargetingDecision.MovementSuggestion.FOLLOW;
        selfOnly = false;
    }

    public NeedsBuffCondition(Supplier<? extends MKEffect> buffEffect) {
        this.buffEffect = null;
        buffMKEffect = buffEffect;
        this.movementSuggestion = AbilityTargetingDecision.MovementSuggestion.FOLLOW;
        selfOnly = false;
    }

    public NeedsBuffCondition setSelfOnly(boolean selfOnly) {
        this.selfOnly = selfOnly;
        return this;
    }

    private boolean needsBuff(LivingEntity entity) {
        if (buffEffect != null) {
            return entity.getEffect(buffEffect) == null;
        } else if (buffMKEffect != null) {
            return MKCore.getEntityData(entity)
                    .map(entityData -> !entityData.getEffects().isEffectActive(buffMKEffect.get()))
                    .orElse(false);
        }
        return false;
    }

    @Nonnull
    @Override
    public AbilityTargetingDecision getDecision(MKAbilityInfo abilityInfo, AbilityDecisionContext context) {
        if (abilityInfo.getAbility().getTargetContext().canTargetCaster() && needsBuff(context.getCaster())) {
            return new AbilityTargetingDecision(context.getCaster(), abilityInfo);
        }
        if (!selfOnly) {
            for (LivingEntity friendly : context.getFriendlies()) {
                if (needsBuff(friendly)) {
                    return new AbilityTargetingDecision(friendly, abilityInfo, movementSuggestion);
                }
            }
        }
        return AbilityTargetingDecision.UNDECIDED;
    }

}
