package com.chaosbuffalo.mkcore.effects.instant;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.ScalingDamageEffectState;
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.BonusFormulaSpec;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.formulas.StackingBonusFormulaSpec;
import com.chaosbuffalo.mkcore.init.CoreEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

public class MKAbilityDamageEffect extends MKEffect {

    public MKAbilityDamageEffect() {
        super(MobEffectCategory.HARMFUL);
    }

    public static MKEffectBuilder<State> from(LivingEntity source, MKDamageType damageType, float baseDamage,
                                              float scaling, float modifierScaling) {
        return from(source, damageType, AbilityFormula.linear(baseDamage, scaling), modifierScaling);
    }

    public static MKEffectBuilder<State> from(LivingEntity source, MKDamageType damageType, AbilityFormula damageFormula,
                                              float modifierScaling) {
        return CoreEffects.ABILITY_DAMAGE.get().builder(source).state(s -> {
            s.setDamageType(damageType);
            s.setDamageFormula(damageFormula, modifierScaling);
        });
    }

    public static MKEffectBuilder<State> from(LivingEntity source, MKDamageType damageType, BonusFormulaSpec damageFormula,
                                              FormulaParameters parameters) {
        return CoreEffects.ABILITY_DAMAGE.get().builder(source).state(s -> {
            s.setDamageType(damageType);
            s.setParameterizedDamageFormula(damageFormula, parameters);
        });
    }

    public static MKEffectBuilder<State> from(LivingEntity source, MKDamageType damageType,
                                              StackingBonusFormulaSpec damageFormula, FormulaParameters parameters) {
        return CoreEffects.ABILITY_DAMAGE.get().builder(source).state(s -> {
            s.setDamageType(damageType);
            s.setParameterizedDamageFormula(damageFormula, parameters);
        });
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
        public boolean validateOnLoad(MKActiveEffect activeEffect) {
            return damageType != null;
        }

        @Override
        public boolean validateOnApply(IMKEntityData targetData, MKActiveEffect activeEffect) {
            return damageType != null;
        }

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect activeEffect) {
            DamageSource damage = MKDamageSource.causeAbilityDamage(targetData.getEntity().level(),
                    damageType, activeEffect.getAbilityId(),
                    activeEffect.getDirectEntity(), activeEffect.getSourceEntity())
                    .setDamageBonusFormula(getDamageBonusFormula());

            float value = getScaledValue(activeEffect.getStackCount(), activeEffect.getSkillLevel());
            targetData.getEntity().hurt(damage, value);
            return true;
        }
    }
}
