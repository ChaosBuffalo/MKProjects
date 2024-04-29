package com.chaosbuffalo.mkultra.effects;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.healing.MKHealSource;
import com.chaosbuffalo.mkcore.core.healing.MKHealing;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.ScalingDamageEffectState;
import com.chaosbuffalo.mkultra.init.MKUEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

public class NaturesRemedyEffect extends MKEffect {

    public static final int DEFAULT_PERIOD = 20;

    public NaturesRemedyEffect() {
        super(MobEffectCategory.BENEFICIAL);
    }

    public static MKEffectBuilder<?> from(LivingEntity source, float base, float scale, float modScale,
                                          ResourceLocation castParticles) {
        return MKUEffects.NATURES_REMEDY.get().builder(source)
                .state(s -> {
                    s.setEffectParticles(castParticles);
                    s.setScalingParameters(base, scale, modScale);
                })
                .periodic(DEFAULT_PERIOD);
    }

    @Override
    public State makeState() {
        return new State();
    }

    @Override
    public MKEffectBuilder<State> builder(UUID sourceId) {
        return new MKEffectBuilder<>(this, sourceId, this::makeState);
    }

    @Override
    public MKEffectBuilder<State> builder(LivingEntity sourceEntity) {
        return new MKEffectBuilder<>(this, sourceEntity, this::makeState);
    }

    public static class State extends ScalingDamageEffectState {

        @Override
        public boolean validateOnApply(IMKEntityData targetData, MKActiveEffect activeEffect) {
            return particles != null;
        }

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect activeEffect) {
            LivingEntity target = targetData.getEntity();
            float value = getScaledValue(activeEffect.getStackCount(), activeEffect.getSkillLevel());
            MKHealSource heal = MKHealSource.getNatureHeal(activeEffect.getAbilityId(),
                    activeEffect.getDirectEntity(),
                    activeEffect.getSourceEntity(),
                    getModifierScale());
            heal.setDamageUndead(activeEffect.hasSourceEntity() && !activeEffect.getSourceEntity().isInvertedHealAndHarm());
            MKHealing.healEntityFrom(target, value, heal);
            sendEffectParticles(targetData.getEntity());
            return true;
        }
    }
}
