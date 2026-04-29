package com.chaosbuffalo.mkcore.formulas;

import net.minecraft.ChatFormatting;

import javax.annotation.Nullable;

public enum FormulaTextStyle {
    NUMBER(false, false, null),
    INTEGER(false, true, null),
    PERCENT(false, false, null),
    DAMAGE(true, false, null),
    HEAL(true, false, ChatFormatting.GREEN),
    MANA(true, false, ChatFormatting.BLUE),
    SECONDS(false, false, null),
    TICKS(false, true, null);

    private final boolean boldValue;
    private final boolean integerValue;
    @Nullable
    private final ChatFormatting color;

    FormulaTextStyle(boolean boldValue, boolean integerValue, @Nullable ChatFormatting color) {
        this.boldValue = boldValue;
        this.integerValue = integerValue;
        this.color = color;
    }

    public boolean isBoldValue() {
        return boldValue;
    }

    public boolean isIntegerValue() {
        return integerValue;
    }

    @Nullable
    public ChatFormatting getColor() {
        return color;
    }
}
