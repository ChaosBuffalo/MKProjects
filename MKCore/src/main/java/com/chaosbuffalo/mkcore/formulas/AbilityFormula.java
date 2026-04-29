package com.chaosbuffalo.mkcore.formulas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A composable numeric expression tree used for data-driven ability scaling.
 * <p>
 * Formulas combine runtime values from a {@link FormulaContext} with authoring-time
 * parameter values from {@link FormulaParameters}. The static factory methods on this
 * interface create the concrete formula node types that are serialized through
 * {@link #CODEC}.
 */
public interface AbilityFormula {
    Codec<AbilityFormula> CODEC = AbilityFormulaCodecs.codec();

    /**
     * Creates a formula that always evaluates to the supplied literal value.
     */
    static Constant constant(float value) {
        return new Constant(value);
    }

    /**
     * Creates a formula that reads a runtime value from the evaluation context.
     */
    static ContextValue context(FormulaContextKey key) {
        return new ContextValue(key);
    }

    /**
     * Creates a formula that reads a design-time parameter value.
     * <p>
     * The returned node remains unresolved until parameters are supplied through
     * {@link #bindParameters(FormulaParameters)} or evaluation is performed with a
     * {@link FormulaEvaluationContext} that contains the parameter.
     */
    static ParameterValue param(FormulaParameterKey key) {
        return new ParameterValue(key);
    }

    /**
     * Creates a legacy-style linear formula:
     * <pre>{@code
     * base + scale * skill_level
     * }</pre>
     */
    static Linear linear(float base, float scale) {
        return new Linear(base, scale);
    }

    /**
     * Creates a semantic damage/healing style formula:
     * <pre>{@code
     * baseParameter
     *   + perLevelParameter * skill_level
     *   + bonusScaleParameter * bonusKey
     * }</pre>
     * <p>
     * {@code baseParameter}, {@code perLevelParameter}, and
     * {@code bonusScaleParameter} come from {@link FormulaParameters}.
     * {@code skill_level} and {@code bonusKey} come from the runtime
     * {@link FormulaContext}.
     * <p>
     * The first two terms form the "base" contribution. The final term is a
     * separate runtime bonus contribution. This split is used by callers such as
     * tooltip rendering and runtime damage/heal plumbing that need to carry the
     * bonus portion separately from the immediately evaluated base portion.
     */
    static BonusScaledLinear bonusScaledLinear(FormulaParameterKey baseParameter,
                                               FormulaParameterKey perLevelParameter,
                                               FormulaContextKey bonusKey,
                                               FormulaParameterKey bonusScaleParameter) {
        return new BonusScaledLinear(baseParameter, perLevelParameter, bonusKey, bonusScaleParameter);
    }

    /**
     * Creates a formula that evaluates each child and returns their sum.
     */
    static Add add(AbilityFormula... terms) {
        return new Add(List.of(terms));
    }

    /**
     * Creates a formula that evaluates each child and returns their product.
     */
    static Multiply multiply(AbilityFormula... factors) {
        return new Multiply(List.of(factors));
    }

    /**
     * Creates a formula that evaluates {@code numerator / denominator}.
     * <p>
     * Division by zero returns {@code 0.0f}.
     */
    static Fraction fraction(AbilityFormula numerator, AbilityFormula denominator) {
        return new Fraction(numerator, denominator);
    }

    /**
     * Creates a formula that clamps the child formula into the inclusive range
     * {@code [min, max]}.
     */
    static Clamped clamped(AbilityFormula value, float min, float max) {
        return new Clamped(value, min, max);
    }

    /**
     * Evaluates this formula using both runtime context values and parameter values.
     */
    float evaluate(FormulaEvaluationContext context);

    /**
     * Convenience overload for formulas that only depend on runtime context values.
     * Parameter lookup nodes will still fail at evaluation time unless they were
     * already bound beforehand.
     */
    default float evaluate(FormulaContext context) {
        return evaluate(FormulaEvaluationContext.of(context));
    }

    /**
     * Returns a version of this formula with any resolvable parameter lookups replaced
     * by constants from {@code parameters}.
     * <p>
     * Missing parameters are left in place so callers can partially apply a formula.
     */
    default AbilityFormula bindParameters(FormulaParameters parameters) {
        return this;
    }

    /**
     * Binds parameters like {@link #bindParameters(FormulaParameters)} and then fails
     * if any parameter lookup nodes remain unresolved.
     */
    default AbilityFormula bindParametersStrict(FormulaParameters parameters) {
        AbilityFormula bound = bindParameters(parameters);
        List<FormulaParameterKey> unboundParameters = bound.getUnboundParameters();
        if (!unboundParameters.isEmpty()) {
            throw new IllegalStateException("Formula still has unbound parameters: " + unboundParameters);
        }
        return bound;
    }

    /**
     * Returns an optional semantic split of this formula into
     * {@code total = baseFormula + bonusFormula}.
     * <p>
     * This is primarily used by renderers and runtime plumbing that need to display or
     * transport the base and bonus contributions separately.
     */
    default @Nullable Breakdown breakdown(FormulaParameters parameters) {
        return null;
    }

    /**
     * Returns every parameter that is still unresolved in this formula.
     */
    default List<FormulaParameterKey> getUnboundParameters() {
        Set<FormulaParameterKey> output = new LinkedHashSet<>();
        collectUnboundParameters(output);
        return List.copyOf(output);
    }

    default void collectUnboundParameters(Set<FormulaParameterKey> output) {
    }

    AbilityFormulaType<? extends AbilityFormula> getType();

    /**
     * Semantic decomposition of a formula into base and bonus contributions.
     */
    record Breakdown(AbilityFormula baseFormula, AbilityFormula bonusFormula) {
    }

    private static <T extends List<AbilityFormula>> DataResult<T> requireNonEmpty(T values, String fieldName) {
        if (values.isEmpty()) {
            return DataResult.error(() -> fieldName + " must not be empty");
        }
        return DataResult.success(values);
    }

    record Constant(float value) implements AbilityFormula {
        public static final Codec<Constant> INLINE_CODEC = Codec.FLOAT.xmap(Constant::new, Constant::value);
        public static final MapCodec<Constant> MAP_CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(Codec.FLOAT.fieldOf("value").forGetter(Constant::value))
                        .apply(instance, Constant::new)
        );

        @Override
        public float evaluate(FormulaEvaluationContext context) {
            return value;
        }

        @Override
        public AbilityFormulaType<Constant> getType() {
            return AbilityFormulaTypes.CONSTANT.get();
        }
    }

    record ContextValue(FormulaContextKey key) implements AbilityFormula {
        public static final MapCodec<ContextValue> MAP_CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(FormulaContextKey.CODEC.fieldOf("key").forGetter(ContextValue::key))
                        .apply(instance, ContextValue::new)
        );

        @Override
        public float evaluate(FormulaEvaluationContext context) {
            return context.getContext(key);
        }

        @Override
        public AbilityFormulaType<ContextValue> getType() {
            return AbilityFormulaTypes.CONTEXT_VALUE.get();
        }
    }

    record ParameterValue(FormulaParameterKey key) implements AbilityFormula {
        public static final MapCodec<ParameterValue> MAP_CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(FormulaParameterKey.CODEC.fieldOf("key").forGetter(ParameterValue::key))
                        .apply(instance, ParameterValue::new)
        );

        @Override
        public float evaluate(FormulaEvaluationContext context) {
            return context.getParameter(key);
        }

        @Override
        public AbilityFormula bindParameters(FormulaParameters parameters) {
            return parameters.contains(key) ? new Constant(parameters.get(key)) : this;
        }

        @Override
        public void collectUnboundParameters(Set<FormulaParameterKey> output) {
            output.add(key);
        }

        @Override
        public AbilityFormulaType<ParameterValue> getType() {
            return AbilityFormulaTypes.PARAMETER_VALUE.get();
        }
    }

    record Linear(float base, float scale) implements AbilityFormula {
        public static final MapCodec<Linear> MAP_CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                                Codec.FLOAT.fieldOf("base").forGetter(Linear::base),
                                Codec.FLOAT.fieldOf("scale").forGetter(Linear::scale)
                        )
                        .apply(instance, Linear::new)
        );

        @Override
        public float evaluate(FormulaEvaluationContext context) {
            return base + scale * context.getContext(FormulaContextKey.SKILL_LEVEL);
        }

        @Override
        public AbilityFormulaType<Linear> getType() {
            return AbilityFormulaTypes.LINEAR.get();
        }
    }

    /**
     * Semantic helper for formulas that behave like
     * {@code base + perLevel * skill_level + bonusScale * runtimeBonus}.
     */
    record BonusScaledLinear(FormulaParameterKey baseParameter,
                             FormulaParameterKey perLevelParameter,
                             FormulaContextKey bonusKey,
                             FormulaParameterKey bonusScaleParameter) implements AbilityFormula {
        public static final MapCodec<BonusScaledLinear> MAP_CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                                FormulaParameterKey.CODEC.fieldOf("base_param").forGetter(BonusScaledLinear::baseParameter),
                                FormulaParameterKey.CODEC.fieldOf("per_level_param").forGetter(BonusScaledLinear::perLevelParameter),
                                FormulaContextKey.CODEC.fieldOf("bonus_key").forGetter(BonusScaledLinear::bonusKey),
                                FormulaParameterKey.CODEC.fieldOf("bonus_scale_param").forGetter(BonusScaledLinear::bonusScaleParameter)
                        )
                        .apply(instance, BonusScaledLinear::new)
        );

        @Override
        public float evaluate(FormulaEvaluationContext context) {
            return context.getParameter(baseParameter)
                    + context.getParameter(perLevelParameter) * context.getContext(FormulaContextKey.SKILL_LEVEL)
                    + context.getParameter(bonusScaleParameter) * context.getContext(bonusKey);
        }

        @Override
        public AbilityFormula bindParameters(FormulaParameters parameters) {
            return compose().bindParameters(parameters);
        }

        @Override
        public Breakdown breakdown(FormulaParameters parameters) {
            return new Breakdown(baseContribution().bindParametersStrict(parameters),
                    bonusContribution().bindParametersStrict(parameters));
        }

        @Override
        public void collectUnboundParameters(Set<FormulaParameterKey> output) {
            output.add(baseParameter);
            output.add(perLevelParameter);
            output.add(bonusScaleParameter);
        }

        @Override
        public AbilityFormulaType<BonusScaledLinear> getType() {
            return AbilityFormulaTypes.BONUS_SCALED_LINEAR.get();
        }

        private AbilityFormula compose() {
            return AbilityFormula.add(baseContribution(), bonusContribution());
        }

        private AbilityFormula baseContribution() {
            return AbilityFormula.add(
                    AbilityFormula.param(baseParameter),
                    AbilityFormula.multiply(
                            AbilityFormula.param(perLevelParameter),
                            AbilityFormula.context(FormulaContextKey.SKILL_LEVEL)
                    )
            );
        }

        private AbilityFormula bonusContribution() {
            return AbilityFormula.multiply(
                    AbilityFormula.param(bonusScaleParameter),
                    AbilityFormula.context(bonusKey)
            );
        }
    }

    record Add(List<AbilityFormula> terms) implements AbilityFormula {
        public static final MapCodec<Add> MAP_CODEC = RecordCodecBuilder.<Add>mapCodec(
                instance -> instance.group(AbilityFormulaCodecs.codec().listOf().fieldOf("terms").forGetter(Add::terms))
                        .apply(instance, Add::new)
        ).validate(add -> requireNonEmpty(add.terms(), "terms").map(ignored -> add));

        @Override
        public float evaluate(FormulaEvaluationContext context) {
            return (float) terms.stream()
                    .mapToDouble(term -> term.evaluate(context))
                    .sum();
        }

        @Override
        public AbilityFormula bindParameters(FormulaParameters parameters) {
            return new Add(terms.stream().map(term -> term.bindParameters(parameters)).toList());
        }

        @Override
        public void collectUnboundParameters(Set<FormulaParameterKey> output) {
            terms.forEach(term -> term.collectUnboundParameters(output));
        }

        @Override
        public AbilityFormulaType<Add> getType() {
            return AbilityFormulaTypes.ADD.get();
        }
    }

    record Multiply(List<AbilityFormula> factors) implements AbilityFormula {
        public static final MapCodec<Multiply> MAP_CODEC = RecordCodecBuilder.<Multiply>mapCodec(
                instance -> instance.group(AbilityFormulaCodecs.codec().listOf().fieldOf("factors").forGetter(Multiply::factors))
                        .apply(instance, Multiply::new)
        ).validate(multiply -> requireNonEmpty(multiply.factors(), "factors").map(ignored -> multiply));

        @Override
        public float evaluate(FormulaEvaluationContext context) {
            float result = 1.0f;
            for (AbilityFormula factor : factors) {
                result *= factor.evaluate(context);
            }
            return result;
        }

        @Override
        public AbilityFormula bindParameters(FormulaParameters parameters) {
            return new Multiply(factors.stream().map(factor -> factor.bindParameters(parameters)).toList());
        }

        @Override
        public void collectUnboundParameters(Set<FormulaParameterKey> output) {
            factors.forEach(factor -> factor.collectUnboundParameters(output));
        }

        @Override
        public AbilityFormulaType<Multiply> getType() {
            return AbilityFormulaTypes.MULTIPLY.get();
        }
    }

    record Fraction(AbilityFormula numerator, AbilityFormula denominator) implements AbilityFormula {
        public static final MapCodec<Fraction> MAP_CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                                AbilityFormulaCodecs.codec().fieldOf("numerator").forGetter(Fraction::numerator),
                                AbilityFormulaCodecs.codec().fieldOf("denominator").forGetter(Fraction::denominator)
                        )
                        .apply(instance, Fraction::new)
        );

        @Override
        public float evaluate(FormulaEvaluationContext context) {
            float denominatorValue = denominator.evaluate(context);
            return denominatorValue == 0.0f ? 0.0f : numerator.evaluate(context) / denominatorValue;
        }

        @Override
        public AbilityFormula bindParameters(FormulaParameters parameters) {
            return new Fraction(numerator.bindParameters(parameters), denominator.bindParameters(parameters));
        }

        @Override
        public void collectUnboundParameters(Set<FormulaParameterKey> output) {
            numerator.collectUnboundParameters(output);
            denominator.collectUnboundParameters(output);
        }

        @Override
        public AbilityFormulaType<Fraction> getType() {
            return AbilityFormulaTypes.FRACTION.get();
        }
    }

    record Clamped(AbilityFormula value, float min, float max) implements AbilityFormula {
        public static final MapCodec<Clamped> MAP_CODEC = RecordCodecBuilder.<Clamped>mapCodec(
                instance -> instance.group(
                                AbilityFormulaCodecs.codec().fieldOf("value").forGetter(Clamped::value),
                                Codec.FLOAT.fieldOf("min").forGetter(Clamped::min),
                                Codec.FLOAT.fieldOf("max").forGetter(Clamped::max)
                        )
                        .apply(instance, Clamped::new)
        ).validate(clamped ->
                clamped.max() <= clamped.min()
                        ? DataResult.error(() -> "Max must be larger than min, min: %s, max: %s".formatted(clamped.min(), clamped.max()))
                        : DataResult.success(clamped));

        @Override
        public float evaluate(FormulaEvaluationContext context) {
            return Mth.clamp(value.evaluate(context), min, max);
        }

        @Override
        public AbilityFormula bindParameters(FormulaParameters parameters) {
            return new Clamped(value.bindParameters(parameters), min, max);
        }

        @Override
        public void collectUnboundParameters(Set<FormulaParameterKey> output) {
            value.collectUnboundParameters(output);
        }

        @Override
        public AbilityFormulaType<Clamped> getType() {
            return AbilityFormulaTypes.CLAMPED.get();
        }
    }
}
