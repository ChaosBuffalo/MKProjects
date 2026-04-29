package com.chaosbuffalo.mkcore.serialization.attributes;

import com.chaosbuffalo.mkcore.formulas.StackingBonusFormulaSpec;

public class StackingBonusFormulaSpecAttribute extends CodecAttribute<StackingBonusFormulaSpec> {
    public StackingBonusFormulaSpecAttribute(String name, StackingBonusFormulaSpec defaultValue) {
        super(name, defaultValue, StackingBonusFormulaSpec.CODEC);
    }

    public StackingBonusFormulaSpec value() {
        return getValue();
    }
}
