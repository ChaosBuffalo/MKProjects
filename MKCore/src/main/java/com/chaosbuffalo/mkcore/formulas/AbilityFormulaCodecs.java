package com.chaosbuffalo.mkcore.formulas;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

final class AbilityFormulaCodecs {
    private static final Codec<AbilityFormula> CODEC = Codec.lazyInitialized(AbilityFormulaCodecs::createCodec);

    private AbilityFormulaCodecs() {
    }

    public static Codec<AbilityFormula> codec() {
        return CODEC;
    }

    private static Codec<AbilityFormula> createCodec() {
        Codec<AbilityFormula> typedCodec = Codec.lazyInitialized(MKCoreRegistry.ABILITY_FORMULA_TYPES::byNameCodec)
                .dispatch(AbilityFormula::getType, AbilityFormulaType::codec);
        return Codec.either(AbilityFormula.Constant.INLINE_CODEC, typedCodec)
                .xmap(
                        either -> either.map(constant -> (AbilityFormula) constant, formula -> formula),
                        formula -> formula instanceof AbilityFormula.Constant constant ? Either.left(constant) : Either.right(formula)
                );
    }
}
