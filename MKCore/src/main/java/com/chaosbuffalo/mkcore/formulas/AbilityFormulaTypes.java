package com.chaosbuffalo.mkcore.formulas;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class AbilityFormulaTypes {
    public static final DeferredRegister<AbilityFormulaType<?>> REGISTRY =
            DeferredRegister.create(MKCoreRegistry.ABILITY_FORMULA_TYPE_REGISTRY_KEY, MKCore.MOD_ID);

    public static final Supplier<AbilityFormulaType<AbilityFormula.Constant>> CONSTANT =
            REGISTRY.register("constant", () -> () -> AbilityFormula.Constant.MAP_CODEC);
    public static final Supplier<AbilityFormulaType<AbilityFormula.ContextValue>> CONTEXT_VALUE =
            REGISTRY.register("context_value", () -> () -> AbilityFormula.ContextValue.MAP_CODEC);
    public static final Supplier<AbilityFormulaType<AbilityFormula.ParameterValue>> PARAMETER_VALUE =
            REGISTRY.register("parameter_value", () -> () -> AbilityFormula.ParameterValue.MAP_CODEC);
    public static final Supplier<AbilityFormulaType<AbilityFormula.Linear>> LINEAR =
            REGISTRY.register("linear", () -> () -> AbilityFormula.Linear.MAP_CODEC);
    public static final Supplier<AbilityFormulaType<AbilityFormula.Add>> ADD =
            REGISTRY.register("add", () -> () -> AbilityFormula.Add.MAP_CODEC);
    public static final Supplier<AbilityFormulaType<AbilityFormula.Multiply>> MULTIPLY =
            REGISTRY.register("multiply", () -> () -> AbilityFormula.Multiply.MAP_CODEC);
    public static final Supplier<AbilityFormulaType<AbilityFormula.Fraction>> FRACTION =
            REGISTRY.register("fraction", () -> () -> AbilityFormula.Fraction.MAP_CODEC);
    public static final Supplier<AbilityFormulaType<AbilityFormula.Clamped>> CLAMPED =
            REGISTRY.register("clamped", () -> () -> AbilityFormula.Clamped.MAP_CODEC);

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
