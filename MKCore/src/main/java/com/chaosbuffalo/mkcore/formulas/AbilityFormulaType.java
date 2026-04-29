package com.chaosbuffalo.mkcore.formulas;

import com.mojang.serialization.MapCodec;

public interface AbilityFormulaType<T extends AbilityFormula> {
    MapCodec<T> codec();
}
