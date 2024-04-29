package com.chaosbuffalo.mkultra.effects;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKCombatFormulas;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.ScalingValueEffectState;
import com.chaosbuffalo.mkcore.effects.status.OnStackEffect;
import com.chaosbuffalo.mkcore.effects.status.StunEffect;
import com.chaosbuffalo.mkultra.init.MKUEffects;
import com.google.common.reflect.TypeToken;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

public class HolyWordEffect extends OnStackEffect {
    public HolyWordEffect() {
        super(MobEffectCategory.HARMFUL);
    }

    @Override
    public State makeState() {
        return new State();
    }

    public static MKEffectBuilder<State> from(LivingEntity source, float baseStunSeconds, float scalingSeconds, float modifierScaling) {
        return MKUEffects.HOLY_WORD_EFFECT.get().builder(source)
                .state(s -> s.setScalingParameters(baseStunSeconds, scalingSeconds, modifierScaling));
    }

    @Override
    public MKEffectBuilder<State> builder(UUID sourceId) {
        return new MKEffectBuilder<>(this, sourceId, this::makeState);
    }

    @Override
    public MKEffectBuilder<State> builder(LivingEntity sourceEntity) {
        return new MKEffectBuilder<>(this, sourceEntity, this::makeState);
    }


    @Override
    protected void Detonate(IMKEntityData targetData, MKActiveEffect instance) {
        State state = instance.getState(TypeToken.of(State.class));
        float duration = state.getScaledValue(1, instance.getSkillLevel());
        int stunDur = MKCombatFormulas.secondsToTicks(duration);

        MKEffectBuilder<?> stun = StunEffect.from(instance.getSourceEntity())
                .ability(instance.getAbilityId())
                .skillLevel(instance.getSkillLevel()).timed(
                        stunDur);
        targetData.getEffects().addEffect(stun);
    }

    public static class State extends ScalingValueEffectState {
        @Override
        public boolean isReady(IMKEntityData targetData, MKActiveEffect instance) {
            return false;
        }

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect instance) {
            return true;
        }
    }
}
