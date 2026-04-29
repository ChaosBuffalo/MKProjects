package com.chaosbuffalo.mkcore.serialization.attributes;

import com.chaosbuffalo.mkcore.formulas.FormulaParameters;

public class FormulaParameterMapAttribute extends CodecAttribute<FormulaParameters> {
    public FormulaParameterMapAttribute(String name, FormulaParameters defaultValue) {
        super(name, defaultValue, FormulaParameters.CODEC);
    }

    public FormulaParameters value() {
        return getValue();
    }
}
