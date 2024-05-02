package com.chaosbuffalo.mkultra.effects;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.ScalingDamageEffectState;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkultra.abilities.MKUAbilityUtils;
import com.chaosbuffalo.mkultra.init.MKUEffects;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

public class FlameWaveEffect extends MKEffect {

    public static MKEffectBuilder<?> from(LivingEntity source, float baseDamage, float scaling, float modifierScaling,
                                          int witherBase, int witherScale, float damageMultiplier) {
        return MKUEffects.FLAME_WAVE.get().builder(source).state(s -> {
            s.witherDurationBase = witherBase;
            s.witherDurationScale = witherScale;
            s.damageBoost = damageMultiplier;
            s.setScalingParameters(baseDamage, scaling, modifierScaling);
        });
    }

    public FlameWaveEffect() {
        super(MobEffectCategory.HARMFUL);
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
        public int witherDurationBase;
        public int witherDurationScale;
        public float damageBoost;

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect activeEffect) {

            float damage = getScaledValue(activeEffect.getStackCount(), activeEffect.getSkillLevel());
            if (MKUAbilityUtils.isBurning(targetData)) {
                int dur = witherDurationBase + activeEffect.getStackCount() * witherDurationScale;
                MobEffectInstance witherEffect = new MobEffectInstance(MobEffects.WITHER, dur * GameConstants.TICKS_PER_SECOND, 0);
                damage *= damageBoost;
                targetData.getEntity().addEffect(witherEffect);
            }

            targetData.getEntity().hurt(MKDamageSource.causeAbilityDamage(targetData.getEntity().getLevel(),
                    CoreDamageTypes.FireDamage.get(),
                    activeEffect.getAbilityId(), activeEffect.getDirectEntity(), activeEffect.getSourceEntity(), getModifierScale()), damage);
            return true;
        }
    }
}
