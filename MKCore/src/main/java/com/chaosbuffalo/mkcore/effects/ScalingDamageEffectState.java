package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.core.healing.MKHealSource;
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.BonusFormulaSpec;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.formulas.StackingBonusFormulaSpec;
import com.chaosbuffalo.mkcore.utils.MKNBTUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;

public abstract class ScalingDamageEffectState extends ScalingValueEffectState {
    @Nullable
    protected MKDamageType damageType = null;
    protected AbilityFormula damageBonusFormula = AbilityFormula.constant(0.0f);
    protected AbilityFormula healBonusFormula = AbilityFormula.constant(0.0f);

    public void setDamageType(@Nullable MKDamageType damageType) {
        this.damageType = damageType;
    }

    @Nullable
    public MKDamageType getDamageType() {
        return damageType;
    }

    public void setDamageParameters(float base, float scale) {
        setDamageParameters(base, scale, 1.0f);
    }

    public void setDamageParameters(float base, float scale, float modifierScaling) {
        super.setScalingParameters(base, scale);
        damageBonusFormula = MKDamageSource.createLegacyDamageBonusFormula(modifierScaling);
        healBonusFormula = AbilityFormula.constant(0.0f);
    }

    public void setDamageFormula(AbilityFormula scalingFormula) {
        setDamageFormula(scalingFormula, 1.0f);
    }

    public void setDamageFormula(AbilityFormula scalingFormula, float modifierScaling) {
        super.setScalingFormula(scalingFormula);
        damageBonusFormula = MKDamageSource.createLegacyDamageBonusFormula(modifierScaling);
        healBonusFormula = AbilityFormula.constant(0.0f);
    }

    public void setDamageFormula(AbilityFormula scalingFormula, FormulaParameters parameters, float modifierScaling) {
        super.setScalingFormula(scalingFormula, parameters);
        damageBonusFormula = MKDamageSource.createLegacyDamageBonusFormula(modifierScaling);
        healBonusFormula = AbilityFormula.constant(0.0f);
    }

    public void setHealingParameters(float base, float scale) {
        setHealingParameters(base, scale, 1.0f);
    }

    public void setHealingParameters(float base, float scale, float modifierScaling) {
        super.setScalingParameters(base, scale);
        damageBonusFormula = AbilityFormula.constant(0.0f);
        healBonusFormula = MKHealSource.createLegacyHealBonusFormula(modifierScaling);
    }

    public void setHealingFormula(AbilityFormula scalingFormula) {
        setHealingFormula(scalingFormula, 1.0f);
    }

    public void setHealingFormula(AbilityFormula scalingFormula, float modifierScaling) {
        super.setScalingFormula(scalingFormula);
        damageBonusFormula = AbilityFormula.constant(0.0f);
        healBonusFormula = MKHealSource.createLegacyHealBonusFormula(modifierScaling);
    }

    public void setHealingFormula(AbilityFormula scalingFormula, FormulaParameters parameters, float modifierScaling) {
        super.setScalingFormula(scalingFormula, parameters);
        damageBonusFormula = AbilityFormula.constant(0.0f);
        healBonusFormula = MKHealSource.createLegacyHealBonusFormula(modifierScaling);
    }

    public void setParameterizedDamageFormula(BonusFormulaSpec scalingFormula, FormulaParameters parameters) {
        BonusFormulaSpec bound = scalingFormula.bindStrict(parameters);
        applyScalingSpec(bound);
        applyDamageBonusFormula(bound.bonusFormula());
    }

    public void setParameterizedDamageFormula(StackingBonusFormulaSpec scalingFormula, FormulaParameters parameters) {
        StackingBonusFormulaSpec bound = scalingFormula.bindStrict(parameters);
        applyScalingSpec(bound);
        applyDamageBonusFormula(bound.bonusFormula());
    }

    public void setParameterizedHealingFormula(BonusFormulaSpec scalingFormula, FormulaParameters parameters) {
        BonusFormulaSpec bound = scalingFormula.bindStrict(parameters);
        applyScalingSpec(bound);
        applyHealBonusFormula(bound.bonusFormula());
    }

    public void setParameterizedHealingFormula(StackingBonusFormulaSpec scalingFormula, FormulaParameters parameters) {
        StackingBonusFormulaSpec bound = scalingFormula.bindStrict(parameters);
        applyScalingSpec(bound);
        applyHealBonusFormula(bound.bonusFormula());
    }

    public AbilityFormula getDamageBonusFormula() {
        return damageBonusFormula;
    }

    public AbilityFormula getHealBonusFormula() {
        return healBonusFormula;
    }

    private void applyScalingSpec(BonusFormulaSpec bound) {
        super.setScalingFormula(bound.baseFormula(), ValueStackPolicy.IGNORE_STACKS);
    }

    private void applyScalingSpec(StackingBonusFormulaSpec bound) {
        super.setBaseAndPerStackScalingFormulas(bound.baseFormula(), bound.perStackFormula());
    }

    private void applyDamageBonusFormula(AbilityFormula bonusFormula) {
        damageBonusFormula = bonusFormula;
        healBonusFormula = AbilityFormula.constant(0.0f);
    }

    private void applyHealBonusFormula(AbilityFormula bonusFormula) {
        damageBonusFormula = AbilityFormula.constant(0.0f);
        healBonusFormula = bonusFormula;
    }

    @Override
    public void serializeStorage(CompoundTag stateTag) {
        super.serializeStorage(stateTag);
        if (damageType != null) {
            MKNBTUtil.writeResourceLocation(stateTag, "damageType", damageType.getId());
        }
        Tag damageFormulaTag = AbilityFormula.CODEC.encodeStart(NbtOps.INSTANCE, damageBonusFormula).getOrThrow();
        stateTag.put("damageBonusFormula", damageFormulaTag);
        Tag healFormulaTag = AbilityFormula.CODEC.encodeStart(NbtOps.INSTANCE, healBonusFormula).getOrThrow();
        stateTag.put("healBonusFormula", healFormulaTag);
    }

    @Override
    public void deserializeStorage(CompoundTag stateTag) {
        super.deserializeStorage(stateTag);
        float legacyModifierScaling = stateTag.contains("modScale") ? stateTag.getFloat("modScale") : 1.0f;
        if (stateTag.contains("damageType")) {
            damageType = MKCoreRegistry.getDamageType(MKNBTUtil.readResourceLocation(stateTag, "damageType"));
        }
        if (stateTag.contains("damageBonusFormula")) {
            damageBonusFormula = AbilityFormula.CODEC.parse(NbtOps.INSTANCE, stateTag.get("damageBonusFormula")).getOrThrow();
        } else {
            damageBonusFormula = MKDamageSource.createLegacyDamageBonusFormula(legacyModifierScaling);
        }
        if (stateTag.contains("healBonusFormula")) {
            healBonusFormula = AbilityFormula.CODEC.parse(NbtOps.INSTANCE, stateTag.get("healBonusFormula")).getOrThrow();
        } else {
            healBonusFormula = MKHealSource.createLegacyHealBonusFormula(legacyModifierScaling);
        }
    }

}
