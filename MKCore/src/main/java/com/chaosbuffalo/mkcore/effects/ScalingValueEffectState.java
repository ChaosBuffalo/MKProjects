package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.FormulaContext;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

public abstract class ScalingValueEffectState extends ParticleEffectState {
    protected AbilityFormula scalingFormula = AbilityFormula.constant(0.0f);
    protected float modScale = 1.0f;

    public void setScalingParameters(float base, float scale) {
        setScalingParameters(base, scale, 1.0f);
    }

    public void setScalingParameters(float base, float scale, float modScale) {
        setScalingFormula(createLegacyScalingFormula(base, scale), modScale);
    }

    public void setScalingFormula(AbilityFormula scalingFormula) {
        setScalingFormula(scalingFormula, 1.0f);
    }

    public void setScalingFormula(AbilityFormula scalingFormula, float modScale) {
        this.scalingFormula = scalingFormula;
        this.modScale = modScale;
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

    public float getModifierScale() {
        return modScale;
    }

    @Override
    public void serializeStorage(CompoundTag stateTag) {
        super.serializeStorage(stateTag);
        Tag formulaTag = AbilityFormula.CODEC.encodeStart(NbtOps.INSTANCE, scalingFormula).getOrThrow();
        stateTag.put("scalingFormula", formulaTag);
        stateTag.putFloat("modScale", modScale);
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
        modScale = stateTag.contains("modScale") ? stateTag.getFloat("modScale") : 1.0f;
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
