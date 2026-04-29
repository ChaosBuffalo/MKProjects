package com.chaosbuffalo.mkcore.formulas;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nullable;
import java.text.NumberFormat;

public class FormulaTextRenderer {
    private static final NumberFormat PERCENT_FORMATTER = NumberFormat.getPercentInstance();
    private static final NumberFormat INTEGER_FORMATTER = NumberFormat.getIntegerInstance();
    private static final NumberFormat NUMBER_FORMATTER = NumberFormat.getNumberInstance();

    public static MutableComponent render(AbilityFormula formula, FormulaContext context, FormulaTextStyle style) {
        return render(formula, (AbilityFormula) null, context, style);
    }

    public static MutableComponent render(AbilityFormula formula, FormulaParameters parameters,
                                          FormulaContext context, FormulaTextStyle style) {
        return render(formula.bindParametersStrict(parameters), context, style);
    }

    public static MutableComponent render(BonusFormulaSpec formula, FormulaParameters parameters,
                                          FormulaContext context, FormulaTextStyle style) {
        BonusFormulaSpec bound = formula.bindStrict(parameters);
        return render(bound.baseFormula(), bound.bonusFormula(), context, style);
    }

    public static MutableComponent render(StackingBonusFormulaSpec formula, FormulaParameters parameters,
                                          FormulaContext context, FormulaTextStyle style) {
        return render(formula.singleStackSpec(), parameters, context, style);
    }

    public static MutableComponent render(AbilityFormula baseFormula, @Nullable AbilityFormula bonusFormula,
                                          FormulaContext context, FormulaTextStyle style) {
        float baseValue = baseFormula.evaluate(context);
        float bonusValue = bonusFormula != null ? bonusFormula.evaluate(context) : 0.0f;
        float totalValue = baseValue + bonusValue;

        MutableComponent component = style(Component.literal(formatValue(totalValue, style)), style);
        if (bonusValue != 0.0f) {
            component.append(style(Component.literal(" (%s%s)".formatted(
                    bonusValue > 0.0f ? "+" : "",
                    formatValue(bonusValue, style))), style));
        }
        return component;
    }

    public static int round(AbilityFormula formula, FormulaContext context) {
        return Math.round(formula.evaluate(context));
    }

    public static int round(AbilityFormula formula, FormulaParameters parameters, FormulaContext context) {
        return Math.round(formula.bindParametersStrict(parameters).evaluate(context));
    }

    public static String format(AbilityFormula formula, FormulaContext context, FormulaTextStyle style) {
        return formatValue(formula.evaluate(context), style);
    }

    public static String format(AbilityFormula formula, FormulaParameters parameters,
                                FormulaContext context, FormulaTextStyle style) {
        return formatValue(formula.bindParametersStrict(parameters).evaluate(context), style);
    }

    private static MutableComponent style(MutableComponent component, FormulaTextStyle style) {
        if (style.isBoldValue()) {
            component.withStyle(ChatFormatting.BOLD);
        }
        if (style.getColor() != null) {
            component.withStyle(style.getColor());
        }
        return component;
    }

    private static String formatValue(float value, FormulaTextStyle style) {
        return switch (style) {
            case PERCENT -> PERCENT_FORMATTER.format(value);
            case INTEGER, TICKS -> INTEGER_FORMATTER.format(Math.round(value));
            default -> NUMBER_FORMATTER.format(value);
        };
    }
}
