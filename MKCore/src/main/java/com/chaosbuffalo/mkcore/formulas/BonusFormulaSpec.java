package com.chaosbuffalo.mkcore.formulas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * An explicit two-channel authored value made up of:
 * {@code total = baseFormula + bonusFormula}.
 * <p>
 * This is used for ability values such as direct spell damage or direct heals
 * where the runtime and tooltip systems need to preserve the authored base
 * contribution separately from the stat-derived bonus contribution.
 */
public record BonusFormulaSpec(AbilityFormula baseFormula, AbilityFormula bonusFormula) {
    public static final Codec<BonusFormulaSpec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            AbilityFormula.CODEC.fieldOf("baseFormula").forGetter(BonusFormulaSpec::baseFormula),
            AbilityFormula.CODEC.fieldOf("bonusFormula").forGetter(BonusFormulaSpec::bonusFormula)
    ).apply(instance, BonusFormulaSpec::new));

    /**
     * Creates the common authored shape:
     * {@code baseParameter + perLevelParameter * skill_level + bonusScaleParameter * bonusKey}.
     * <p>
     * The first two terms are stored in the spec's {@code baseFormula} channel,
     * while the final term is stored in the {@code bonusFormula} channel.
     * This keeps the authored spell magnitude separate from the runtime stat
     * bonus for tooltips and runtime effect plumbing.
     */
    public static BonusFormulaSpec skilledBonusScaled(FormulaParameterKey baseParameter,
                                                      FormulaParameterKey perLevelParameter,
                                                      FormulaContextKey bonusKey,
                                                      FormulaParameterKey bonusScaleParameter) {
        return new BonusFormulaSpec(
                AbilityFormula.skilledLinear(baseParameter, perLevelParameter),
                AbilityFormula.multiply(
                        AbilityFormula.param(bonusScaleParameter),
                        AbilityFormula.context(bonusKey)
                )
        );
    }

    public BonusFormulaSpec bindStrict(FormulaParameters parameters) {
        return new BonusFormulaSpec(
                baseFormula.bindParametersStrict(parameters),
                bonusFormula.bindParametersStrict(parameters)
        );
    }

    public float evaluate(FormulaContext context) {
        return baseFormula.evaluate(context) + bonusFormula.evaluate(context);
    }

    public AbilityFormula totalFormula() {
        return AbilityFormula.add(baseFormula, bonusFormula);
    }
}
