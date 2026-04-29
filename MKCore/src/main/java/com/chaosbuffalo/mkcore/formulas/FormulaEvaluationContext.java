package com.chaosbuffalo.mkcore.formulas;

import java.util.Objects;

public record FormulaEvaluationContext(FormulaContext runtimeContext, FormulaParameters parameters) {
    public FormulaEvaluationContext {
        Objects.requireNonNull(runtimeContext, "runtimeContext");
        Objects.requireNonNull(parameters, "parameters");
    }

    public static FormulaEvaluationContext of(FormulaContext runtimeContext) {
        return new FormulaEvaluationContext(runtimeContext, FormulaParameters.EMPTY);
    }

    public static FormulaEvaluationContext of(FormulaContext runtimeContext, FormulaParameters parameters) {
        return new FormulaEvaluationContext(runtimeContext, parameters);
    }

    public float getContext(FormulaContextKey key) {
        return runtimeContext.get(key);
    }

    public float getParameter(FormulaParameterKey key) {
        return parameters.require(key);
    }
}
