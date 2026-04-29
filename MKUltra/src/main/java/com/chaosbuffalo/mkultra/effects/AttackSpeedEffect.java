package com.chaosbuffalo.mkultra.effects;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.ScalingValueEffectState;
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class AttackSpeedEffect extends MKEffect {

    public AttackSpeedEffect(UUID attributeModifierId, double base, double scaling, Holder<Attribute> skill) {
        super(base >= 0.0 ? MobEffectCategory.BENEFICIAL : MobEffectCategory.HARMFUL);
        addAttribute(Attributes.ATTACK_SPEED, attributeModifierId, base, scaling,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, skill);
    }

    @Override
    public State makeState() {
        return new State();
    }

    public MKEffectBuilder<State> from(LivingEntity source, AbilityFormula valueFormula,
                                       FormulaParameters parameters,
                                       ScalingValueEffectState.ValueStackPolicy stackPolicy) {
        return new MKEffectBuilder<>(this, source, State::new)
                .state(s -> s.setScalingFormula(valueFormula, parameters, stackPolicy));
    }

    public MKEffectBuilder<State> from(LivingEntity source,
                                       AbilityFormula baseFormula,
                                       AbilityFormula perStackFormula,
                                       FormulaParameters parameters) {
        return new MKEffectBuilder<>(this, source, State::new)
                .state(s -> s.setBaseAndPerStackScalingFormulas(baseFormula, perStackFormula, parameters));
    }

    @Override
    protected double calculateInstanceModifierValue(Modifier modifier, MKActiveEffect activeEffect) {
        if (activeEffect.getState() instanceof State state && state.hasScalingFormulaOverride()) {
            return state.getScaledValue(activeEffect.getStackCount(), activeEffect.getSkillLevel());
        }
        return super.calculateInstanceModifierValue(modifier, activeEffect);
    }

    public static class State extends ScalingValueEffectState {
        private boolean scalingFormulaOverride = false;

        @Override
        public void setScalingParameters(float base, float scale) {
            super.setScalingParameters(base, scale);
            scalingFormulaOverride = true;
        }

        @Override
        public void setScalingFormula(AbilityFormula scalingFormula, FormulaParameters parameters) {
            super.setScalingFormula(scalingFormula, parameters);
            scalingFormulaOverride = true;
        }

        @Override
        public void setScalingFormula(AbilityFormula scalingFormula,
                                      FormulaParameters parameters,
                                      ValueStackPolicy stackPolicy) {
            super.setScalingFormula(scalingFormula, parameters, stackPolicy);
            scalingFormulaOverride = true;
        }

        @Override
        public void setScalingFormula(AbilityFormula scalingFormula) {
            super.setScalingFormula(scalingFormula);
            scalingFormulaOverride = true;
        }

        @Override
        public void setScalingFormula(AbilityFormula scalingFormula, ValueStackPolicy stackPolicy) {
            super.setScalingFormula(scalingFormula, stackPolicy);
            scalingFormulaOverride = true;
        }

        @Override
        public void setBaseAndPerStackScalingFormulas(AbilityFormula baseFormula,
                                                      AbilityFormula perStackFormula,
                                                      FormulaParameters parameters) {
            super.setBaseAndPerStackScalingFormulas(baseFormula, perStackFormula, parameters);
            scalingFormulaOverride = true;
        }

        @Override
        public void setBaseAndPerStackScalingFormulas(AbilityFormula baseFormula, AbilityFormula perStackFormula) {
            super.setBaseAndPerStackScalingFormulas(baseFormula, perStackFormula);
            scalingFormulaOverride = true;
        }

        public boolean hasScalingFormulaOverride() {
            return scalingFormulaOverride;
        }

        @Override
        public boolean isReady(IMKEntityData targetData, MKActiveEffect instance) {
            return false;
        }

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect instance) {
            return true;
        }

        @Override
        public void deserializeStorage(CompoundTag stateTag) {
            super.deserializeStorage(stateTag);
            scalingFormulaOverride = stateTag.contains("valueStackPolicy")
                    || stateTag.contains("scalingFormula")
                    || stateTag.contains("scalingBaseFormula")
                    || stateTag.contains("scalingPerStackFormula");
        }
    }
}
