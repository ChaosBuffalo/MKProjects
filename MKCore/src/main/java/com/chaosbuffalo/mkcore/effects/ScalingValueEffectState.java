package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.FormulaContext;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

public abstract class ScalingValueEffectState extends ParticleEffectState {
    protected AbilityFormula scalingFormula = AbilityFormula.constant(0.0f);

    public void setScalingParameters(float base, float scale) {
        setScalingFormula(createLegacyScalingFormula(base, scale));
    }

    public void setScalingFormula(AbilityFormula scalingFormula, FormulaParameters parameters) {
        setScalingFormula(scalingFormula.bindParametersStrict(parameters));
    }

    public void setScalingFormula(AbilityFormula scalingFormula) {
        this.scalingFormula = scalingFormula;
    }

    public float getScaledValue(int stacks, float skillLevel) {
        FormulaContext context = FormulaContext.builder()
                .withStackCount(stacks)
                .withSkillLevel(skillLevel)
                .build();
        return scalingFormula.evaluate(context);
    }

    public AbilityFormula getScalingFormula() {
        return scalingFormula;
    }

    @Override
    public void serializeStorage(CompoundTag stateTag) {
        super.serializeStorage(stateTag);
        Tag formulaTag = AbilityFormula.CODEC.encodeStart(NbtOps.INSTANCE, scalingFormula).getOrThrow();
        stateTag.put("scalingFormula", formulaTag);
    }

    @Override
    public void deserializeStorage(CompoundTag stateTag) {
        super.deserializeStorage(stateTag);
        if (stateTag.contains("scalingFormula")) {
            Tag formulaTag = stateTag.get("scalingFormula");
            if (formulaTag != null) {
                scalingFormula = AbilityFormula.CODEC.parse(NbtOps.INSTANCE, formulaTag).getOrThrow();
            }
        } else {
            scalingFormula = createLegacyScalingFormula(stateTag.getFloat("base"), stateTag.getFloat("scale"));
        }
    }

    private static AbilityFormula createLegacyScalingFormula(float base, float scale) {
        return AbilityFormula.add(
                AbilityFormula.constant(base),
                AbilityFormula.multiply(
                        AbilityFormula.context(FormulaContextKey.STACK_COUNT),
                        AbilityFormula.constant(scale),
                        AbilityFormula.context(FormulaContextKey.SKILL_LEVEL)
                )
        );
    }
}
