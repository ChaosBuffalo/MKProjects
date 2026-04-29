package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.healing.MKHealSource;
import com.chaosbuffalo.mkcore.core.healing.MKHealing;
import com.chaosbuffalo.mkcore.formulas.BonusFormulaSpec;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameterKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.formulas.StackingBonusFormulaSpec;
import com.chaosbuffalo.mkcore.test.MKTestAbilities;
import com.chaosbuffalo.mkcore.test.MKTestEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(MKCore.MOD_ID)
@PrefixGameTestTemplate(false)
public class MKHealingPipelineCharacterizationGameTests {
    private static final BlockPos CASTER_POS = new BlockPos(1, 2, 1);
    private static final BlockPos TARGET_POS = new BlockPos(1, 2, 3);
    private static final FormulaParameterKey TEST_HEAL_BASE =
            FormulaParameterKey.of(MKCore.id("test.heal.base"));
    private static final FormulaParameterKey TEST_HEAL_PER_LEVEL =
            FormulaParameterKey.of(MKCore.id("test.heal.per_level"));
    private static final FormulaParameterKey TEST_HEAL_MODIFIER_SCALING =
            FormulaParameterKey.of(MKCore.id("test.heal.modifier_scaling"));

    @GameTest(template = "player_data_phase0")
    public static void legacyHealModifierScalingStillAddsConfiguredHealBonus(GameTestHelper helper) {
        Player caster = createMockPlayer(helper, CASTER_POS);
        Player target = createMockPlayer(helper, TARGET_POS);
        target.setHealth(4.0f);
        setBaseValue(caster, MKAttributes.HEAL_BONUS, 8.0);
        setBaseValue(target, MKAttributes.HEAL_EFFICIENCY, 1.0);

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    float startingHealth = target.getHealth();
                    MKHealing.healEntityFrom(target, 6.0f,
                            MKHealSource.getHolyHeal(MKTestAbilities.TEST_HEAL.get().getAbilityId(), caster, caster, 0.5f));
                    float healed = target.getHealth() - startingHealth;

                    assertFloatEquals(helper, healed, 10.0f, 0.001f,
                            "legacy healing modifier scaling should still add the configured heal bonus");
                })
                .thenSucceed();
    }

    @GameTest(template = "player_data_phase0")
    public static void bonusFormulaSpecHealingEffectAppliesRuntimeHealBonus(GameTestHelper helper) {
        Player caster = createMockPlayer(helper, CASTER_POS);
        Player target = createMockPlayer(helper, TARGET_POS);
        target.setHealth(4.0f);
        setBaseValue(caster, MKAttributes.HEAL_BONUS, 4.0);
        setBaseValue(target, MKAttributes.HEAL_EFFICIENCY, 1.0);

        BonusFormulaSpec healingFormula = BonusFormulaSpec.skilledBonusScaled(
                TEST_HEAL_BASE,
                TEST_HEAL_PER_LEVEL,
                FormulaContextKey.HEAL_BONUS,
                TEST_HEAL_MODIFIER_SCALING
        );
        FormulaParameters parameters = FormulaParameters.builder()
                .with(TEST_HEAL_BASE, 2.0f)
                .with(TEST_HEAL_PER_LEVEL, 1.0f)
                .with(TEST_HEAL_MODIFIER_SCALING, 0.5f)
                .build();

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    float startingHealth = target.getHealth();
                    MKCore.getPlayerOrThrow(target).getEffects().addEffect(
                            MKTestEffects.NEW_HEAL.get().builder(caster)
                                    .ability(MKTestAbilities.TEST_NEW_HEAL.get())
                                    .skillLevel(3.0f)
                                    .state(s -> s.setParameterizedHealingFormula(healingFormula, parameters))
                    );
                    float healed = target.getHealth() - startingHealth;

                    assertFloatEquals(helper, healed, 7.0f, 0.001f,
                            "bonus formula spec healing should add runtime heal bonus through the shared pipeline");
                })
                .thenSucceed();
    }

    @GameTest(template = "player_data_phase0")
    public static void stackingBonusFormulaSpecHealingPreservesLegacyStackScaling(GameTestHelper helper) {
        Player caster = createMockPlayer(helper, CASTER_POS);
        Player target = createMockPlayer(helper, TARGET_POS);
        target.setHealth(4.0f);
        setBaseValue(caster, MKAttributes.HEAL_BONUS, 4.0);
        setBaseValue(target, MKAttributes.HEAL_EFFICIENCY, 1.0);

        StackingBonusFormulaSpec healingFormula = StackingBonusFormulaSpec.skilledBonusScaled(
                TEST_HEAL_BASE,
                TEST_HEAL_PER_LEVEL,
                FormulaContextKey.HEAL_BONUS,
                TEST_HEAL_MODIFIER_SCALING
        );
        FormulaParameters parameters = FormulaParameters.builder()
                .with(TEST_HEAL_BASE, 2.0f)
                .with(TEST_HEAL_PER_LEVEL, 1.0f)
                .with(TEST_HEAL_MODIFIER_SCALING, 0.5f)
                .build();

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    float startingHealth = target.getHealth();
                    MKCore.getPlayerOrThrow(target).getEffects().addEffect(
                            MKTestEffects.NEW_HEAL.get().builder(caster)
                                    .ability(MKTestAbilities.TEST_NEW_HEAL.get())
                                    .skillLevel(3.0f)
                                    .amplify(2)
                                    .state(s -> s.setParameterizedHealingFormula(healingFormula, parameters))
                    );
                    float healed = target.getHealth() - startingHealth;

                    assertFloatEquals(helper, healed, 13.0f, 0.001f,
                            "stacking bonus formula spec healing should preserve legacy per-stack scaling");
                })
                .thenSucceed();
    }

    private static Player createMockPlayer(GameTestHelper helper, BlockPos relativePos) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos absolutePos = helper.absolutePos(relativePos);
        player.moveTo(absolutePos.getX() + 0.5, absolutePos.getY(), absolutePos.getZ() + 0.5, 0.0f, 0.0f);
        player.setHealth(player.getMaxHealth());
        return player;
    }

    private static void setBaseValue(Player player, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
                                     double value) {
        var instance = player.getAttribute(attribute);
        if (instance == null) {
            throw new IllegalStateException("Missing attribute " + attribute);
        }
        instance.setBaseValue(value);
    }

    private static void assertFloatEquals(GameTestHelper helper, float actual, float expected, float epsilon, String label) {
        helper.assertTrue(Math.abs(actual - expected) <= epsilon,
                label + ": expected " + expected + " but was " + actual);
    }
}
