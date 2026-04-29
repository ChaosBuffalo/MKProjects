package com.chaosbuffalo.mkcore.formulas;

import java.util.HashMap;
import java.util.Map;

public class FormulaContext {
    private final Map<FormulaContextKey, Float> values;

    private FormulaContext(Map<FormulaContextKey, Float> values) {
        this.values = values;
    }

    public static Builder builder() {
        return new Builder();
    }

    public float get(FormulaContextKey key) {
        return values.getOrDefault(key, 0.0f);
    }

    public static class Builder {
        private final Map<FormulaContextKey, Float> values = new HashMap<>();

        public Builder with(FormulaContextKey key, float value) {
            values.put(key, value);
            return this;
        }

        public Builder withSkillLevel(float value) {
            return with(FormulaContextKey.SKILL_LEVEL, value);
        }

        public Builder withStackCount(float value) {
            return with(FormulaContextKey.STACK_COUNT, value);
        }

        public Builder withModifierScaling(float value) {
            return with(FormulaContextKey.MODIFIER_SCALING, value);
        }

        public Builder withDamageBonus(float value) {
            return with(FormulaContextKey.DAMAGE_BONUS, value);
        }

        public Builder withHealBonus(float value) {
            return with(FormulaContextKey.HEAL_BONUS, value);
        }

        public Builder withBuffDurationMultiplier(float value) {
            return with(FormulaContextKey.BUFF_DURATION_MULTIPLIER, value);
        }

        public FormulaContext build() {
            return new FormulaContext(Map.copyOf(values));
        }
    }
}
