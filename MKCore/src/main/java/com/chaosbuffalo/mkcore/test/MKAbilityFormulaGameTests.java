package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.projectiles.BurstProjectileBehavior;
import com.chaosbuffalo.mkcore.abilities.projectiles.ProjectileCastBehavior;
import com.chaosbuffalo.mkcore.data.providers.MKAbilityProvider;
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.BonusFormulaSpec;
import com.chaosbuffalo.mkcore.formulas.FormulaContext;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaEvaluationContext;
import com.chaosbuffalo.mkcore.formulas.FormulaParameterKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.formulas.FormulaTextRenderer;
import com.chaosbuffalo.mkcore.formulas.FormulaTextStyle;
import com.chaosbuffalo.mkcore.formulas.StackingBonusFormulaSpec;
import com.chaosbuffalo.mkcore.utils.location.CircularLocationProvider;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.minecraft.world.phys.Vec3;

@GameTestHolder(MKCore.MOD_ID)
@PrefixGameTestTemplate(false)
public class MKAbilityFormulaGameTests {
    private static final FormulaParameterKey TEST_BASE = FormulaParameterKey.of(MKCore.id("test.base"));
    private static final FormulaParameterKey TEST_SCALE = FormulaParameterKey.of(MKCore.id("test.scale"));
    private static final FormulaParameterKey TEST_MODIFIER_SCALING =
            FormulaParameterKey.of(MKCore.id("test.modifier_scaling"));

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
        AbilityFormula formula = AbilityFormula.skilledLinear(TEST_BASE, TEST_SCALE);
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
                .with(TEST_MODIFIER_SCALING, 0.5f)
                .build();
        JsonElement encoded = FormulaParameters.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
        FormulaParameters decoded = FormulaParameters.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        String encodedString = encoded.toString();

        helper.assertTrue(encodedString.contains("\"mkcore:test.base\":6.0"),
                "encoded parameter map should use namespaced parameter keys");
        helper.assertTrue(encodedString.indexOf("\"mkcore:test.base\"")
                        < encodedString.indexOf("\"mkcore:test.scale\"")
                        && encodedString.indexOf("\"mkcore:test.scale\"")
                        < encodedString.indexOf("\"mkcore:test.modifier_scaling\""),
                "encoded parameter map should preserve insertion order");
        assertFloatEquals(helper, decoded.get(TEST_BASE), 6.0f, 0.0001f, "decoded base parameter");
        assertFloatEquals(helper, decoded.get(TEST_SCALE), 1.25f, 0.0001f, "decoded scale parameter");
        assertFloatEquals(helper, decoded.get(TEST_MODIFIER_SCALING), 0.5f, 0.0001f,
                "decoded modifier scaling parameter");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void bindParametersProducesClosedFormula(GameTestHelper helper) {
        AbilityFormula formula = AbilityFormula.skilledLinear(TEST_BASE, TEST_SCALE);
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
    public static void composedParameterizedFormulaEvaluatesAndBinds(GameTestHelper helper) {
        AbilityFormula formula = AbilityFormula.add(
                AbilityFormula.skilledLinear(TEST_BASE, TEST_SCALE),
                AbilityFormula.multiply(
                        AbilityFormula.param(TEST_MODIFIER_SCALING),
                        AbilityFormula.context(FormulaContextKey.HEAL_BONUS)
                )
        );
        FormulaParameters parameters = FormulaParameters.builder()
                .with(TEST_BASE, 5.0f)
                .with(TEST_SCALE, 3.0f)
                .with(TEST_MODIFIER_SCALING, 0.5f)
                .build();
        FormulaContext runtimeContext = FormulaContext.builder()
                .withSkillLevel(2.0f)
                .withHealBonus(4.0f)
                .build();
        JsonElement encoded = AbilityFormula.CODEC.encodeStart(JsonOps.INSTANCE, formula).getOrThrow();
        AbilityFormula boundFormula = formula.bindParameters(parameters);

        helper.assertTrue(encoded.toString().contains("\"type\":\"mkcore:add\""),
                "encoded formula should use ordinary composed formula nodes");
        assertFloatEquals(helper, formula.evaluate(FormulaEvaluationContext.of(runtimeContext, parameters)),
                13.0f, 0.0001f, "composed formula evaluation");
        assertFloatEquals(helper, boundFormula.evaluate(runtimeContext), 13.0f, 0.0001f,
                "bound composed formula evaluation");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void bonusFormulaSpecTextRendererShowsBonusBreakdown(GameTestHelper helper) {
        BonusFormulaSpec formula = BonusFormulaSpec.skilledBonusScaled(
                TEST_BASE,
                TEST_SCALE,
                FormulaContextKey.HEAL_BONUS,
                TEST_MODIFIER_SCALING
        );
        FormulaParameters parameters = FormulaParameters.builder()
                .with(TEST_BASE, 5.0f)
                .with(TEST_SCALE, 3.0f)
                .with(TEST_MODIFIER_SCALING, 0.5f)
                .build();
        FormulaContext context = FormulaContext.builder()
                .withSkillLevel(2.0f)
                .withHealBonus(4.0f)
                .build();

        String rendered = FormulaTextRenderer.render(formula, parameters, context, FormulaTextStyle.HEAL).getString();
        helper.assertTrue(rendered.startsWith("13"), "rendered total should include the full formula value");
        helper.assertTrue(rendered.contains("(+2)"), "rendered value should include the semantic bonus breakdown");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void bonusFormulaSpecCodecRoundTripPreservesEvaluation(GameTestHelper helper) {
        BonusFormulaSpec original = BonusFormulaSpec.skilledBonusScaled(
                TEST_BASE,
                TEST_SCALE,
                FormulaContextKey.HEAL_BONUS,
                TEST_MODIFIER_SCALING
        );
        FormulaParameters parameters = FormulaParameters.builder()
                .with(TEST_BASE, 5.0f)
                .with(TEST_SCALE, 3.0f)
                .with(TEST_MODIFIER_SCALING, 0.5f)
                .build();
        FormulaContext context = FormulaContext.builder()
                .withSkillLevel(2.0f)
                .withHealBonus(4.0f)
                .build();
        JsonElement encoded = BonusFormulaSpec.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
        BonusFormulaSpec decoded = BonusFormulaSpec.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();

        helper.assertTrue(encoded.toString().contains("\"baseFormula\""),
                "encoded bonus formula spec should preserve explicit base and bonus channels");
        assertFloatEquals(helper, decoded.bindStrict(parameters).evaluate(context), 13.0f, 0.0001f,
                "decoded bonus formula spec evaluation");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void stackingBonusFormulaSpecRendererUsesSingleStackValue(GameTestHelper helper) {
        StackingBonusFormulaSpec formula = StackingBonusFormulaSpec.skilledBonusScaled(
                TEST_BASE,
                TEST_SCALE,
                FormulaContextKey.HEAL_BONUS,
                TEST_MODIFIER_SCALING
        );
        FormulaParameters parameters = FormulaParameters.builder()
                .with(TEST_BASE, 5.0f)
                .with(TEST_SCALE, 3.0f)
                .with(TEST_MODIFIER_SCALING, 0.5f)
                .build();
        FormulaContext context = FormulaContext.builder()
                .withSkillLevel(2.0f)
                .withHealBonus(4.0f)
                .withStackCount(3.0f)
                .build();

        String rendered = FormulaTextRenderer.render(formula, parameters, context, FormulaTextStyle.HEAL).getString();
        float stackedValue = formula.bindStrict(parameters).totalFormula().evaluate(context);

        helper.assertTrue(rendered.startsWith("13"),
                "stacking spec renderer should show the single-stack authored value");
        helper.assertTrue(rendered.contains("(+2)"),
                "stacking spec renderer should still show the explicit bonus channel");
        assertFloatEquals(helper, stackedValue, 25.0f, 0.0001f,
                "stacking spec total formula should still evaluate with stack count at runtime");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void parameterizedFormatAndRoundUseProvidedParameters(GameTestHelper helper) {
        AbilityFormula formula = AbilityFormula.skilledLinear(TEST_BASE, TEST_SCALE);
        FormulaParameters parameters = FormulaParameters.builder()
                .with(TEST_BASE, 2.4f)
                .with(TEST_SCALE, 1.6f)
                .build();
        FormulaContext context = FormulaContext.builder()
                .withSkillLevel(2.0f)
                .build();

        helper.assertTrue("5.6".equals(FormulaTextRenderer.format(formula, parameters, context, FormulaTextStyle.NUMBER)),
                "parameterized format should evaluate with the provided parameter values");
        helper.assertTrue(FormulaTextRenderer.round(formula, parameters, context) == 6,
                "parameterized round should evaluate with the provided parameter values");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void strictBindingFailsWhenParametersAreMissing(GameTestHelper helper) {
        AbilityFormula formula = AbilityFormula.skilledLinear(TEST_BASE, TEST_SCALE);
        boolean threw = false;
        try {
            formula.bindParametersStrict(FormulaParameters.builder().with(TEST_BASE, 4.0f).build());
        } catch (IllegalStateException e) {
            threw = e.getMessage().contains(TEST_SCALE.toString());
        }

        helper.assertTrue(threw, "strict parameter binding should fail when a parameter is missing");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void abilityVariantPatchMergesFormulaParametersAndReplacesAttributes(GameTestHelper helper) {
        JsonObject base = JsonParser.parseString("""
                {
                  "cooldown": 120,
                  "manaCost": 4.0,
                  "castTime": 5,
                  "attributes": {
                    "formulaParameters": {
                      "mkcore:test.base": 5.0,
                      "mkcore:test.scale": 3.0
                    },
                    "healingFormula": {
                      "type": "mkcore:linear",
                      "base": 5.0,
                      "scale": 5.0
                    }
                  }
                }
                """).getAsJsonObject();
        MKAbilityProvider.AbilityVariantPatch patch = MKAbilityProvider.AbilityVariantPatch.builder()
                .cooldown(160)
                .manaCost(6.0f)
                .mergeFormulaParameter(TEST_BASE, 8.0f)
                .replaceAttribute("healingFormula", AbilityFormula.linear(9.0f, 1.0f))
                .build();

        JsonObject variant = MKAbilityProvider.applyVariantPatch(base, patch);
        JsonObject attributes = variant.getAsJsonObject("attributes");
        JsonObject formulaParameters = attributes.getAsJsonObject("formulaParameters");

        helper.assertTrue(variant.get("cooldown").getAsInt() == 160,
                "variant patch should replace top-level cooldown");
        helper.assertTrue(Math.abs(variant.get("manaCost").getAsFloat() - 6.0f) < 0.0001f,
                "variant patch should replace top-level mana cost");
        helper.assertTrue(Math.abs(formulaParameters.get(TEST_BASE.toString()).getAsFloat() - 8.0f) < 0.0001f,
                "variant patch should override merged formula parameter keys");
        helper.assertTrue(Math.abs(formulaParameters.get(TEST_SCALE.toString()).getAsFloat() - 3.0f) < 0.0001f,
                "variant patch should preserve formula parameter keys not mentioned in the patch");
        helper.assertTrue(attributes.getAsJsonObject("healingFormula").get("base").getAsFloat() == 9.0f,
                "variant patch should replace whole formula attributes");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void abilityVariantPatchCanReplaceFormulaAndClearParameters(GameTestHelper helper) {
        JsonObject base = JsonParser.parseString("""
                {
                  "attributes": {
                    "formulaParameters": {
                      "mkcore:test.base": 5.0,
                      "mkcore:test.scale": 3.0
                    },
                    "damageFormula": {
                      "type": "mkcore:add",
                      "terms": [
                        {
                          "type": "mkcore:add",
                          "terms": [
                            {
                              "type": "mkcore:parameter_value",
                              "key": "mkcore:test.base"
                            },
                            {
                              "type": "mkcore:multiply",
                              "factors": [
                                {
                                  "type": "mkcore:parameter_value",
                                  "key": "mkcore:test.scale"
                                },
                                {
                                  "type": "mkcore:context_value",
                                  "key": "mkcore:skill_level"
                                }
                              ]
                            }
                          ]
                        },
                        {
                          "type": "mkcore:multiply",
                          "factors": [
                            {
                              "type": "mkcore:parameter_value",
                              "key": "mkcore:test.modifier_scaling"
                            },
                            {
                              "type": "mkcore:context_value",
                              "key": "mkcore:damage_bonus"
                            }
                          ]
                        }
                      ]
                    }
                  }
                }
                """).getAsJsonObject();
        AbilityFormula replacement = AbilityFormula.add(
                AbilityFormula.linear(10.0f, 7.0f),
                AbilityFormula.multiply(
                        AbilityFormula.constant(1.5f),
                        AbilityFormula.context(FormulaContextKey.DAMAGE_BONUS)
                )
        );
        MKAbilityProvider.AbilityVariantPatch patch = MKAbilityProvider.AbilityVariantPatch.builder()
                .replaceAttribute("damageFormula", replacement)
                .clearFormulaParameters()
                .build();

        JsonObject variant = MKAbilityProvider.applyVariantPatch(base, patch);
        JsonObject attributes = variant.getAsJsonObject("attributes");
        JsonObject formulaParameters = attributes.getAsJsonObject("formulaParameters");
        AbilityFormula decoded = AbilityFormula.CODEC.parse(JsonOps.INSTANCE, attributes.get("damageFormula")).getOrThrow();
        FormulaContext context = FormulaContext.builder()
                .withSkillLevel(2.0f)
                .withDamageBonus(4.0f)
                .build();

        helper.assertTrue(formulaParameters.entrySet().isEmpty(),
                "whole-formula replacement should be able to clear inherited formula parameters");
        helper.assertTrue("mkcore:add".equals(attributes.getAsJsonObject("damageFormula").get("type").getAsString()),
                "whole-formula replacement should write the replacement formula structure");
        assertFloatEquals(helper, decoded.evaluate(context), 30.0f, 0.0001f,
                "replacement formula should decode and evaluate independently of cleared parameters");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void abilityVariantPatchCanReplacePrimitiveAndNestedJsonAttributes(GameTestHelper helper) {
        JsonObject base = JsonParser.parseString("""
                {
                  "castTime": 25,
                  "attributes": {
                    "npc_solve_ballistics": 2,
                    "cast_behavior": {
                      "type": "mkcore:simple",
                      "doPitch": true,
                      "location": {
                        "type": "mkcore:single_location",
                        "percentEyeHeight": 0.6
                      }
                    }
                  }
                }
                """).getAsJsonObject();
        JsonObject replacementBehavior = JsonParser.parseString("""
                {
                  "type": "mkcore:burst",
                  "doPitch": true,
                  "location": {
                    "type": "mkcore:circular_location",
                    "count": 12,
                    "distance": 1.0,
                    "inheritPitch": true,
                    "maxDegrees": -60.0,
                    "minDegrees": 60.0,
                    "offset": [
                      0.0,
                      0.0,
                      0.0
                    ],
                    "percentEyeHeight": 1.2
                  }
                }
                """).getAsJsonObject();
        MKAbilityProvider.AbilityVariantPatch patch = MKAbilityProvider.AbilityVariantPatch.builder()
                .castTime(80)
                .replaceAttribute("npc_solve_ballistics", 1)
                .replaceAttribute("cast_behavior", replacementBehavior)
                .build();

        JsonObject variant = MKAbilityProvider.applyVariantPatch(base, patch);
        JsonObject attributes = variant.getAsJsonObject("attributes");
        JsonObject castBehavior = attributes.getAsJsonObject("cast_behavior");
        JsonObject location = castBehavior.getAsJsonObject("location");

        helper.assertTrue(variant.get("castTime").getAsInt() == 80,
                "variant patch should replace top-level cast time");
        helper.assertTrue(attributes.get("npc_solve_ballistics").getAsInt() == 1,
                "variant patch should replace primitive attributes without raw JsonPrimitive boilerplate");
        helper.assertTrue("mkcore:burst".equals(castBehavior.get("type").getAsString()),
                "variant patch should replace nested json attributes");
        helper.assertTrue(location.get("count").getAsInt() == 12,
                "variant patch should preserve nested replacement payload values");
        helper.assertTrue(Math.abs(location.get("percentEyeHeight").getAsFloat() - 1.2f) < 0.0001f,
                "variant patch should preserve nested float payload values");
        helper.assertTrue(base.get("castTime").getAsInt() == 25,
                "variant patch should not mutate the base top-level json");
        helper.assertTrue(base.getAsJsonObject("attributes").get("npc_solve_ballistics").getAsInt() == 2,
                "variant patch should not mutate the base attribute json");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void abilityVariantPatchCanReplaceCodecBackedAttributes(GameTestHelper helper) {
        JsonObject base = JsonParser.parseString("""
                {
                  "attributes": {
                    "cast_behavior": {
                      "type": "mkcore:simple",
                      "doPitch": true,
                      "location": {
                        "type": "mkcore:single_location",
                        "offset": [
                          0.5,
                          0.0,
                          0.5
                        ],
                        "percentEyeHeight": 0.6
                      }
                    }
                  }
                }
                """).getAsJsonObject();
        ProjectileCastBehavior replacement = new BurstProjectileBehavior(
                new CircularLocationProvider(Vec3.ZERO, 1.2f, 12, 1.0f, 60.0f, -60.0f, true), true);
        MKAbilityProvider.AbilityVariantPatch patch = MKAbilityProvider.AbilityVariantPatch.builder()
                .replaceAttribute("cast_behavior", ProjectileCastBehavior.CODEC, replacement)
                .build();

        JsonObject variant = MKAbilityProvider.applyVariantPatch(base, patch);
        JsonObject castBehavior = variant.getAsJsonObject("attributes").getAsJsonObject("cast_behavior");
        JsonObject location = castBehavior.getAsJsonObject("location");
        ProjectileCastBehavior decoded =
                ProjectileCastBehavior.CODEC.parse(JsonOps.INSTANCE, castBehavior).getOrThrow();

        helper.assertTrue("mkcore:burst".equals(castBehavior.get("type").getAsString()),
                "codec-backed replacement should preserve the dispatched cast behavior type");
        helper.assertTrue("mkcore:circular_location".equals(location.get("type").getAsString()),
                "codec-backed replacement should preserve the dispatched location type");
        helper.assertTrue(decoded instanceof BurstProjectileBehavior,
                "codec-backed replacement should decode back to the concrete projectile behavior");
        helper.assertTrue(decoded.getLocationProvider() instanceof CircularLocationProvider,
                "codec-backed replacement should decode back to the concrete location provider");
        helper.assertTrue("mkcore:simple".equals(base.getAsJsonObject("attributes")
                        .getAsJsonObject("cast_behavior").get("type").getAsString()),
                "codec-backed replacement should not mutate the base json");
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
