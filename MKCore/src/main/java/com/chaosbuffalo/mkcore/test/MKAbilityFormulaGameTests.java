package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.FormulaContext;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaEvaluationContext;
import com.chaosbuffalo.mkcore.formulas.FormulaParameterKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.formulas.FormulaTextRenderer;
import com.chaosbuffalo.mkcore.formulas.FormulaTextStyle;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(MKCore.MOD_ID)
@PrefixGameTestTemplate(false)
public class MKAbilityFormulaGameTests {
    private static final FormulaParameterKey TEST_BASE = FormulaParameterKey.of(MKCore.id("test.base"));
    private static final FormulaParameterKey TEST_SCALE = FormulaParameterKey.of(MKCore.id("test.scale"));

    @GameTest(template = "player_data_phase0")
    public static void nestedFormulaCodecRoundTripPreservesEvaluation(GameTestHelper helper) {
        AbilityFormula original = AbilityFormula.add(
                AbilityFormula.linear(4.0f, 2.0f),
                AbilityFormula.multiply(
                        AbilityFormula.context(FormulaContextKey.MODIFIER_SCALING),
                        AbilityFormula.context(FormulaContextKey.DAMAGE_BONUS)
                )
        );
        JsonElement encoded = AbilityFormula.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
        AbilityFormula decoded = AbilityFormula.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        FormulaContext context = FormulaContext.builder()
                .withSkillLevel(3.0f)
                .withModifierScaling(0.5f)
                .withDamageBonus(6.0f)
                .build();

        helper.assertTrue(encoded.toString().contains("\"mkcore:modifier_scaling\""),
                "encoded formula should use namespaced context keys");
        assertFloatEquals(helper, decoded.evaluate(context), 13.0f, 0.0001f, "decoded formula evaluation");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void parameterFormulaEvaluatesWithProvidedParameters(GameTestHelper helper) {
        AbilityFormula formula = AbilityFormula.add(
                AbilityFormula.param(TEST_BASE),
                AbilityFormula.multiply(
                        AbilityFormula.param(TEST_SCALE),
                        AbilityFormula.context(FormulaContextKey.SKILL_LEVEL)
                )
        );
        FormulaContext runtimeContext = FormulaContext.builder()
                .withSkillLevel(3.0f)
                .build();
        FormulaParameters parameters = FormulaParameters.builder()
                .with(TEST_BASE, 4.0f)
                .with(TEST_SCALE, 2.0f)
                .build();
        JsonElement encoded = AbilityFormula.CODEC.encodeStart(JsonOps.INSTANCE, formula).getOrThrow();

        helper.assertTrue(encoded.toString().contains("\"type\":\"mkcore:parameter_value\""),
                "encoded formula should serialize parameter values");
        assertFloatEquals(helper, formula.evaluate(FormulaEvaluationContext.of(runtimeContext, parameters)),
                10.0f, 0.0001f, "parameterized formula evaluation");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void formulaParametersCodecRoundTripPreservesValues(GameTestHelper helper) {
        FormulaParameters original = FormulaParameters.builder()
                .with(TEST_BASE, 6.0f)
                .with(TEST_SCALE, 1.25f)
                .build();
        JsonElement encoded = FormulaParameters.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
        FormulaParameters decoded = FormulaParameters.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();

        helper.assertTrue(encoded.toString().contains("\"mkcore:test.base\":6.0"),
                "encoded parameter map should use namespaced parameter keys");
        assertFloatEquals(helper, decoded.get(TEST_BASE), 6.0f, 0.0001f, "decoded base parameter");
        assertFloatEquals(helper, decoded.get(TEST_SCALE), 1.25f, 0.0001f, "decoded scale parameter");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void bindParametersProducesClosedFormula(GameTestHelper helper) {
        AbilityFormula formula = AbilityFormula.add(
                AbilityFormula.param(TEST_BASE),
                AbilityFormula.multiply(
                        AbilityFormula.param(TEST_SCALE),
                        AbilityFormula.context(FormulaContextKey.SKILL_LEVEL)
                )
        );
        FormulaParameters parameters = FormulaParameters.builder()
                .with(TEST_BASE, 1.5f)
                .with(TEST_SCALE, 2.5f)
                .build();
        AbilityFormula boundFormula = formula.bindParameters(parameters);
        FormulaContext runtimeContext = FormulaContext.builder()
                .withSkillLevel(4.0f)
                .build();
        JsonElement encoded = AbilityFormula.CODEC.encodeStart(JsonOps.INSTANCE, boundFormula).getOrThrow();

        helper.assertTrue(!encoded.toString().contains("\"type\":\"mkcore:parameter_value\""),
                "bound formula should no longer serialize parameter lookup nodes");
        assertFloatEquals(helper, boundFormula.evaluate(runtimeContext), 11.5f, 0.0001f,
                "bound formula evaluation");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void bonusScaledLinearEvaluatesAndBinds(GameTestHelper helper) {
        AbilityFormula formula = AbilityFormula.bonusScaledLinear(
                TEST_BASE,
                TEST_SCALE,
                FormulaContextKey.HEAL_BONUS,
                FormulaParameterKey.of(MKCore.id("test.modifier_scaling"))
        );
        FormulaParameters parameters = FormulaParameters.builder()
                .with(TEST_BASE, 5.0f)
                .with(TEST_SCALE, 3.0f)
                .with(FormulaParameterKey.of(MKCore.id("test.modifier_scaling")), 0.5f)
                .build();
        FormulaContext runtimeContext = FormulaContext.builder()
                .withSkillLevel(2.0f)
                .withHealBonus(4.0f)
                .build();
        JsonElement encoded = AbilityFormula.CODEC.encodeStart(JsonOps.INSTANCE, formula).getOrThrow();
        AbilityFormula boundFormula = formula.bindParameters(parameters);

        helper.assertTrue(encoded.toString().contains("\"type\":\"mkcore:bonus_scaled_linear\""),
                "encoded formula should use the semantic bonus_scaled_linear node");
        assertFloatEquals(helper, formula.evaluate(FormulaEvaluationContext.of(runtimeContext, parameters)),
                13.0f, 0.0001f, "semantic formula evaluation");
        assertFloatEquals(helper, boundFormula.evaluate(runtimeContext), 13.0f, 0.0001f,
                "bound semantic formula evaluation");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void legacyUnqualifiedContextKeysStillDecode(GameTestHelper helper) {
        String legacyFormula = """
                {
                  "type": "mkcore:context_value",
                  "key": "skill_level"
                }
                """;
        AbilityFormula decoded = AbilityFormula.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(legacyFormula)).getOrThrow();
        FormulaContext context = FormulaContext.builder()
                .withSkillLevel(7.0f)
                .build();

        assertFloatEquals(helper, decoded.evaluate(context), 7.0f, 0.0001f, "legacy context key evaluation");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void formulaTextRendererShowsBonusBreakdown(GameTestHelper helper) {
        AbilityFormula base = AbilityFormula.linear(5.0f, 3.0f);
        AbilityFormula bonus = AbilityFormula.multiply(
                AbilityFormula.context(FormulaContextKey.MODIFIER_SCALING),
                AbilityFormula.context(FormulaContextKey.HEAL_BONUS)
        );
        FormulaContext context = FormulaContext.builder()
                .withSkillLevel(2.0f)
                .withModifierScaling(0.5f)
                .withHealBonus(4.0f)
                .build();

        String rendered = FormulaTextRenderer.render(base, bonus, context, FormulaTextStyle.HEAL).getString();
        helper.assertTrue(rendered.startsWith("13"), "rendered total should include the summed value");
        helper.assertTrue(rendered.contains("(+2)"), "rendered value should include the bonus breakdown");
        helper.succeed();
    }

    private static void assertFloatEquals(GameTestHelper helper, float actual, float expected, float epsilon, String label) {
        helper.assertTrue(Math.abs(actual - expected) <= epsilon,
                "%s expected %s but was %s".formatted(label, expected, actual));
    }
}
