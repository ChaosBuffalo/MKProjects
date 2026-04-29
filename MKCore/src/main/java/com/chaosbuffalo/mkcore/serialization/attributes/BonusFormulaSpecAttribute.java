package com.chaosbuffalo.mkcore.serialization.attributes;

import com.chaosbuffalo.mkcore.formulas.BonusFormulaSpec;

public class BonusFormulaSpecAttribute extends CodecAttribute<BonusFormulaSpec> {
    public BonusFormulaSpecAttribute(String name, BonusFormulaSpec defaultValue) {
        super(name, defaultValue, BonusFormulaSpec.CODEC);
    }

    public BonusFormulaSpec value() {
        return getValue();
    }
}
