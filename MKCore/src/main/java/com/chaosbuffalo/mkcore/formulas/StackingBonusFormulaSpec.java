package com.chaosbuffalo.mkcore.formulas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * An explicit three-channel authored value made up of:
 * {@code total = baseFormula + stackCount * perStackFormula + bonusFormula}.
 * <p>
 * This is used for ability values such as periodic damage or healing where the
 * runtime needs to preserve a flat base contribution, a per-stack contribution,
 * and a stat-derived bonus contribution as separate authored channels.
 */
public record StackingBonusFormulaSpec(AbilityFormula baseFormula,
                                       AbilityFormula perStackFormula,
                                       AbilityFormula bonusFormula) {
    public static final Codec<StackingBonusFormulaSpec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            AbilityFormula.CODEC.fieldOf("baseFormula").forGetter(StackingBonusFormulaSpec::baseFormula),
            AbilityFormula.CODEC.fieldOf("perStackFormula").forGetter(StackingBonusFormulaSpec::perStackFormula),
            AbilityFormula.CODEC.fieldOf("bonusFormula").forGetter(StackingBonusFormulaSpec::bonusFormula)
    ).apply(instance, StackingBonusFormulaSpec::new));

    /**
     * Creates the common authored shape:
     * {@code baseParameter + stackCount * perLevelParameter * skill_level + bonusScaleParameter * bonusKey}.
     * <p>
     * The flat base term is stored in {@code baseFormula}, the
     * {@code perLevelParameter * skill_level} term is stored in
     * {@code perStackFormula}, and the stat-derived bonus term is stored in
     * {@code bonusFormula}. This keeps stack-driven magnitude and runtime stat
     * scaling explicit for tooltips and runtime effect plumbing.
     */
    public static StackingBonusFormulaSpec skilledBonusScaled(FormulaParameterKey baseParameter,
                                                              FormulaParameterKey perLevelParameter,
                                                              FormulaContextKey bonusKey,
                                                              FormulaParameterKey bonusScaleParameter) {
        return new StackingBonusFormulaSpec(
                AbilityFormula.param(baseParameter),
                AbilityFormula.multiply(
                        AbilityFormula.param(perLevelParameter),
                        AbilityFormula.context(FormulaContextKey.SKILL_LEVEL)
                ),
                AbilityFormula.multiply(
                        AbilityFormula.param(bonusScaleParameter),
                        AbilityFormula.context(bonusKey)
                )
        );
    }

    public StackingBonusFormulaSpec bindStrict(FormulaParameters parameters) {
        return new StackingBonusFormulaSpec(
                baseFormula.bindParametersStrict(parameters),
                perStackFormula.bindParametersStrict(parameters),
                bonusFormula.bindParametersStrict(parameters)
        );
    }

    public BonusFormulaSpec singleStackSpec() {
        return new BonusFormulaSpec(AbilityFormula.add(baseFormula, perStackFormula), bonusFormula);
    }

    public AbilityFormula totalFormula() {
        return AbilityFormula.add(
                baseFormula,
                AbilityFormula.multiply(
                        AbilityFormula.context(FormulaContextKey.STACK_COUNT),
                        perStackFormula
                ),
                bonusFormula
        );
    }
}
