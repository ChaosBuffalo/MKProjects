package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.FormulaContext;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

public abstract class ScalingValueEffectState extends ParticleEffectState {
    public enum ValueStackPolicy {
        FORMULA_DRIVEN,
        IGNORE_STACKS,
        BASE_PLUS_STACKS
    }

    protected AbilityFormula scalingFormula = AbilityFormula.constant(0.0f);
    protected AbilityFormula scalingBaseFormula = AbilityFormula.constant(0.0f);
    protected AbilityFormula scalingPerStackFormula = AbilityFormula.constant(0.0f);
    protected ValueStackPolicy valueStackPolicy = ValueStackPolicy.FORMULA_DRIVEN;

    public void setScalingParameters(float base, float scale) {
        setBaseAndPerStackScalingFormulas(
                AbilityFormula.constant(base),
                AbilityFormula.multiply(
                        AbilityFormula.constant(scale),
                        AbilityFormula.context(FormulaContextKey.SKILL_LEVEL)
                )
        );
    }

    public void setScalingFormula(AbilityFormula scalingFormula, FormulaParameters parameters) {
        setScalingFormula(scalingFormula.bindParametersStrict(parameters));
    }

    public void setScalingFormula(AbilityFormula scalingFormula, FormulaParameters parameters, ValueStackPolicy stackPolicy) {
        setScalingFormula(scalingFormula.bindParametersStrict(parameters), stackPolicy);
    }

    public void setScalingFormula(AbilityFormula scalingFormula) {
        setScalingFormula(scalingFormula, ValueStackPolicy.FORMULA_DRIVEN);
    }

    public void setScalingFormula(AbilityFormula scalingFormula, ValueStackPolicy stackPolicy) {
        if (stackPolicy == ValueStackPolicy.BASE_PLUS_STACKS) {
            throw new IllegalArgumentException("Use setBaseAndPerStackScalingFormulas for BASE_PLUS_STACKS semantics");
        }
        this.scalingFormula = scalingFormula;
        this.scalingBaseFormula = AbilityFormula.constant(0.0f);
        this.scalingPerStackFormula = AbilityFormula.constant(0.0f);
        this.valueStackPolicy = stackPolicy;
    }

    public void setBaseAndPerStackScalingFormulas(AbilityFormula baseFormula,
                                                  AbilityFormula perStackFormula,
                                                  FormulaParameters parameters) {
        setBaseAndPerStackScalingFormulas(baseFormula.bindParametersStrict(parameters),
                perStackFormula.bindParametersStrict(parameters));
    }

    public void setBaseAndPerStackScalingFormulas(AbilityFormula baseFormula, AbilityFormula perStackFormula) {
        this.scalingBaseFormula = baseFormula;
        this.scalingPerStackFormula = perStackFormula;
        this.scalingFormula = composeBaseAndPerStackFormula();
        this.valueStackPolicy = ValueStackPolicy.BASE_PLUS_STACKS;
    }

    public float getScaledValue(int stacks, float skillLevel) {
        FormulaContext stackedContext = createScalingContext(stacks, skillLevel);
        FormulaContext singleStackContext = createScalingContext(1.0f, skillLevel);
        return switch (valueStackPolicy) {
            case FORMULA_DRIVEN -> scalingFormula.evaluate(stackedContext);
            case IGNORE_STACKS -> scalingFormula.evaluate(singleStackContext);
            case BASE_PLUS_STACKS -> scalingBaseFormula.evaluate(singleStackContext)
                    + stacks * scalingPerStackFormula.evaluate(singleStackContext);
        };
    }

    public AbilityFormula getScalingFormula() {
        return valueStackPolicy == ValueStackPolicy.BASE_PLUS_STACKS
                ? composeBaseAndPerStackFormula()
                : scalingFormula;
    }

    public ValueStackPolicy getValueStackPolicy() {
        return valueStackPolicy;
    }

    @Override
    public void serializeStorage(CompoundTag stateTag) {
        super.serializeStorage(stateTag);
        stateTag.putString("valueStackPolicy", valueStackPolicy.name());
        if (valueStackPolicy == ValueStackPolicy.BASE_PLUS_STACKS) {
            Tag baseFormulaTag = AbilityFormula.CODEC.encodeStart(NbtOps.INSTANCE, scalingBaseFormula).getOrThrow();
            stateTag.put("scalingBaseFormula", baseFormulaTag);
            Tag perStackFormulaTag = AbilityFormula.CODEC.encodeStart(NbtOps.INSTANCE, scalingPerStackFormula).getOrThrow();
            stateTag.put("scalingPerStackFormula", perStackFormulaTag);
        } else {
            Tag formulaTag = AbilityFormula.CODEC.encodeStart(NbtOps.INSTANCE, scalingFormula).getOrThrow();
            stateTag.put("scalingFormula", formulaTag);
        }
    }

    @Override
    public void deserializeStorage(CompoundTag stateTag) {
        super.deserializeStorage(stateTag);
        valueStackPolicy = ValueStackPolicy.valueOf(stateTag.getString("valueStackPolicy"));
        if (valueStackPolicy == ValueStackPolicy.BASE_PLUS_STACKS) {
            scalingBaseFormula = AbilityFormula.CODEC.parse(NbtOps.INSTANCE, stateTag.get("scalingBaseFormula")).getOrThrow();
            scalingPerStackFormula = AbilityFormula.CODEC.parse(NbtOps.INSTANCE, stateTag.get("scalingPerStackFormula")).getOrThrow();
            scalingFormula = composeBaseAndPerStackFormula();
        } else {
            scalingFormula = AbilityFormula.CODEC.parse(NbtOps.INSTANCE, stateTag.get("scalingFormula")).getOrThrow();
            scalingBaseFormula = AbilityFormula.constant(0.0f);
            scalingPerStackFormula = AbilityFormula.constant(0.0f);
        }
    }

    private AbilityFormula composeBaseAndPerStackFormula() {
        return AbilityFormula.add(
                scalingBaseFormula,
                AbilityFormula.multiply(
                        AbilityFormula.context(FormulaContextKey.STACK_COUNT),
                        scalingPerStackFormula
                )
        );
    }

    private static FormulaContext createScalingContext(float stacks, float skillLevel) {
        return FormulaContext.builder()
                .withStackCount(stacks)
                .withSkillLevel(skillLevel)
                .build();
    }
}
