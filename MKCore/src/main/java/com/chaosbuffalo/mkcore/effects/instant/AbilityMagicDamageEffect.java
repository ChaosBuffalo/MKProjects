package com.chaosbuffalo.mkcore.effects.instant;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.ScalingValueEffectState;
import com.chaosbuffalo.mkcore.init.CoreEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

public class AbilityMagicDamageEffect extends MKEffect {

    public AbilityMagicDamageEffect() {
        super(MobEffectCategory.HARMFUL);
    }

    public static MKEffectBuilder<State> from(LivingEntity source, float baseDamage, float scaling, float modifierScaling) {
        return CoreEffects.ABILITY_MAGIC_DAMAGE.get().builder(source)
                .state(s -> s.setScalingParameters(baseDamage, scaling, modifierScaling));
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

    public static class State extends ScalingValueEffectState {

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect activeEffect) {

            DamageSource damage;
            if (activeEffect.getDirectEntity() != null) {
                damage = DamageSource.indirectMagic(activeEffect.getDirectEntity(), activeEffect.getSourceEntity());
            } else {
                damage = DamageSource.MAGIC;
            }

            float value = getScaledValue(activeEffect.getStackCount(), activeEffect.getSkillLevel());
            targetData.getEntity().hurt(damage, value);
            return true;
        }
    }
}
