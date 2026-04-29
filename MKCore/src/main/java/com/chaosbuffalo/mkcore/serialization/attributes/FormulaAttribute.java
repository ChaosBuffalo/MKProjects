package com.chaosbuffalo.mkcore.serialization.attributes;

import com.chaosbuffalo.mkcore.formulas.AbilityFormula;

public class FormulaAttribute extends CodecAttribute<AbilityFormula> {
    public FormulaAttribute(String name, AbilityFormula defaultValue) {
        super(name, defaultValue, AbilityFormula.CODEC);
    }

    public AbilityFormula value() {
        return getValue();
    }
}
