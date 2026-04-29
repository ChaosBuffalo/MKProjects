package com.chaosbuffalo.mkcore.formulas;

import com.chaosbuffalo.mkcore.MKCore;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record FormulaContextKey(ResourceLocation id) {
    public static final FormulaContextKey SKILL_LEVEL = core("skill_level");
    public static final FormulaContextKey STACK_COUNT = core("stack_count");
    public static final FormulaContextKey MODIFIER_SCALING = core("modifier_scaling");
    public static final FormulaContextKey DAMAGE_BONUS = core("damage_bonus");
    public static final FormulaContextKey HEAL_BONUS = core("heal_bonus");
    public static final FormulaContextKey BUFF_DURATION_MULTIPLIER = core("buff_duration_multiplier");

    public static final Codec<FormulaContextKey> CODEC = Codec.STRING.comapFlatMap(
            FormulaContextKey::parse,
            FormulaContextKey::toString
    );

    public FormulaContextKey {
        Objects.requireNonNull(id, "id");
    }

    public static FormulaContextKey of(ResourceLocation id) {
        return new FormulaContextKey(id);
    }

    @Override
    public String toString() {
        return id.toString();
    }

    private static FormulaContextKey core(String path) {
        return of(MKCore.id(path));
    }

    private static DataResult<FormulaContextKey> parse(String serializedName) {
        String qualifiedName = serializedName.contains(":") ? serializedName : MKCore.MOD_ID + ":" + serializedName;
        return ResourceLocation.read(qualifiedName).map(FormulaContextKey::new);
    }
}
