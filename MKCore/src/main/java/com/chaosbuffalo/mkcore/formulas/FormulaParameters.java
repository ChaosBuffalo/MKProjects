package com.chaosbuffalo.mkcore.formulas;

import com.mojang.serialization.Codec;

import java.util.HashMap;
import java.util.Map;

public class FormulaParameters {
    public static final FormulaParameters EMPTY = new FormulaParameters(Map.of());
    public static final Codec<FormulaParameters> CODEC = Codec.unboundedMap(FormulaParameterKey.CODEC, Codec.FLOAT)
            .xmap(FormulaParameters::fromMap, FormulaParameters::asMap);

    private final Map<FormulaParameterKey, Float> values;

    private FormulaParameters(Map<FormulaParameterKey, Float> values) {
        this.values = values;
    }

    public static Builder builder() {
        return new Builder();
    }

    private static FormulaParameters fromMap(Map<FormulaParameterKey, Float> values) {
        return values.isEmpty() ? EMPTY : new FormulaParameters(Map.copyOf(values));
    }

    public Map<FormulaParameterKey, Float> asMap() {
        return values;
    }

    public boolean contains(FormulaParameterKey key) {
        return values.containsKey(key);
    }

    public float get(FormulaParameterKey key) {
        return values.getOrDefault(key, 0.0f);
    }

    public static class Builder {
        private final Map<FormulaParameterKey, Float> values = new HashMap<>();

        public Builder with(FormulaParameterKey key, float value) {
            values.put(key, value);
            return this;
        }

        public FormulaParameters build() {
            return fromMap(values);
        }
    }
}
