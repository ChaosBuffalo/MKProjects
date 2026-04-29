package com.chaosbuffalo.mkcore.formulas;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record FormulaParameterKey(ResourceLocation id) {
    public static final Codec<FormulaParameterKey> CODEC =
            ResourceLocation.CODEC.xmap(FormulaParameterKey::new, FormulaParameterKey::id);

    public FormulaParameterKey {
        Objects.requireNonNull(id, "id");
    }

    public static FormulaParameterKey of(ResourceLocation id) {
        return new FormulaParameterKey(id);
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
