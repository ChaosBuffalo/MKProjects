package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.effects.instant.MKAbilityDamageEffect;
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameterKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.test.effects.DamagePipelineProbeEffect;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(MKCore.MOD_ID)
@PrefixGameTestTemplate(false)
public class MKDamagePipelineCharacterizationGameTests {
    private static final BlockPos ATTACKER_POS = new BlockPos(1, 2, 1);
    private static final BlockPos TARGET_POS = new BlockPos(1, 2, 3);
    private static final BlockPos ATTACKER_ALT_POS = new BlockPos(3, 2, 1);
    private static final BlockPos TARGET_ALT_POS = new BlockPos(3, 2, 3);
    private static final BlockPos ATTACKER_RANGED_POS = new BlockPos(5, 2, 1);
    private static final BlockPos TARGET_RANGED_POS = new BlockPos(5, 2, 3);
    private static final BlockPos TARGET_RANGED_ALT_POS = new BlockPos(7, 2, 3);
    private static final float DAMAGE_AMOUNT = 10.0f;
    private static final float STARTING_POISE = 20.0f;
    private static final FormulaParameterKey TEST_DAMAGE_BASE =
            FormulaParameterKey.of(MKCore.id("test.damage.base"));
    private static final FormulaParameterKey TEST_DAMAGE_PER_LEVEL =
            FormulaParameterKey.of(MKCore.id("test.damage.per_level"));
    private static final FormulaParameterKey TEST_DAMAGE_MODIFIER_SCALING =
            FormulaParameterKey.of(MKCore.id("test.damage.modifier_scaling"));

    @GameTest(template = "player_data_phase0")
    public static void fullShieldBlockSkipsAttackerAndVictimDamageTriggers(GameTestHelper helper) {
        Player attacker = createMockPlayer(helper, ATTACKER_POS);
        Player target = createMockPlayer(helper, TARGET_POS);
        prepareAttacker(attacker);
        prepareBlockingTarget(attacker, target, 1.0, STARTING_POISE, STARTING_POISE, 0.0);

        helper.startSequence()
                .thenExecute(() -> beginBlocking(target))
                .thenExecuteAfter(2, () -> {
                    float startingHealth = target.getHealth();
                    target.hurt(attacker.damageSources().playerAttack(attacker), DAMAGE_AMOUNT);

                    DamagePipelineProbeEffect.State attackerState = getProbeState(MKCore.getPlayerOrThrow(attacker));
                    DamagePipelineProbeEffect.State targetState = getProbeState(MKCore.getPlayerOrThrow(target));

                    assertFloatEquals(helper, target.getHealth(), startingHealth, 0.001f, "full block should prevent health loss");
                    helper.assertValueEqual(attackerState.getAttackerMeleeCount(), 0, "full block melee trigger count");
                    helper.assertValueEqual(attackerState.getAttackerPostCount(), 0, "full block attacker post trigger count");
                    helper.assertValueEqual(targetState.getVictimPreScaleCount(), 0, "full block victim pre trigger count");
                    helper.assertValueEqual(targetState.getVictimPostScaleCount(), 0, "full block victim post trigger count");
                })
                .thenSucceed();
    }

    @GameTest(template = "player_data_phase0")
    public static void unblockedMeleeHitRunsAttackerAndVictimDamageTriggers(GameTestHelper helper) {
        Player attacker = createMockPlayer(helper, ATTACKER_POS);
        Player target = createMockPlayer(helper, TARGET_POS);
        prepareAttacker(attacker);

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    float startingHealth = target.getHealth();
                    target.hurt(attacker.damageSources().playerAttack(attacker), DAMAGE_AMOUNT);

                    DamagePipelineProbeEffect.State attackerState = getProbeState(MKCore.getPlayerOrThrow(attacker));
                    DamagePipelineProbeEffect.State targetState = getProbeState(MKCore.getPlayerOrThrow(target));

                    helper.assertTrue(target.getHealth() < startingHealth,
                            "unblocked melee should damage target"
                                    + " attackerMelee=" + attackerState.getAttackerMeleeCount()
                                    + " attackerPost=" + attackerState.getAttackerPostCount()
                                    + " victimPre=" + targetState.getVictimPreScaleCount()
                                    + " victimPost=" + targetState.getVictimPostScaleCount());
                    helper.assertValueEqual(attackerState.getAttackerMeleeCount(), 1, "unblocked melee trigger count");
                    helper.assertValueEqual(attackerState.getAttackerPostCount(), 1, "unblocked attacker post trigger count");
                    helper.assertValueEqual(targetState.getVictimPreScaleCount(), 1, "unblocked victim pre trigger count");
                    helper.assertValueEqual(targetState.getVictimPostScaleCount(), 1, "unblocked victim post trigger count");
                })
                .thenSucceed();
    }

    @GameTest(template = "player_data_phase0")
    public static void unblockedProjectileHitRunsAttackerAndVictimDamageTriggers(GameTestHelper helper) {
        Player attacker = createMockPlayer(helper, ATTACKER_POS);
        Player target = createMockPlayer(helper, TARGET_POS);

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    float startingHealth = target.getHealth();
                    Arrow arrow = helper.spawn(net.minecraft.world.entity.EntityType.ARROW, ATTACKER_POS);
                    arrow.setOwner(attacker);
                    target.hurt(attacker.damageSources().arrow(arrow, attacker), DAMAGE_AMOUNT);

                    DamagePipelineProbeEffect.State attackerState = getProbeState(MKCore.getPlayerOrThrow(attacker));
                    DamagePipelineProbeEffect.State targetState = getProbeState(MKCore.getPlayerOrThrow(target));

                    helper.assertTrue(target.getHealth() < startingHealth,
                            "unblocked projectile should damage target"
                                    + " attackerProjectile=" + attackerState.getAttackerProjectileCount()
                                    + " attackerPost=" + attackerState.getAttackerPostCount()
                                    + " victimPre=" + targetState.getVictimPreScaleCount()
                                    + " victimPost=" + targetState.getVictimPostScaleCount());
                    helper.assertValueEqual(attackerState.getAttackerProjectileCount(), 1, "unblocked projectile trigger count");
                    helper.assertValueEqual(attackerState.getAttackerPostCount(), 1, "unblocked projectile attacker post trigger count");
                    helper.assertValueEqual(targetState.getVictimPreScaleCount(), 1, "unblocked projectile victim pre trigger count");
                    helper.assertValueEqual(targetState.getVictimPostScaleCount(), 1, "unblocked projectile victim post trigger count");
                })
                .thenSucceed();
    }

    @GameTest(template = "player_data_phase0")
    public static void shieldedMeleeHitConsumesPoiseWithoutRunningDamageTriggers(GameTestHelper helper) {
        Player attacker = createMockPlayer(helper, ATTACKER_POS);
        Player target = createMockPlayer(helper, TARGET_POS);
        prepareAttacker(attacker);
        prepareBlockingTarget(attacker, target, 0.5, STARTING_POISE, STARTING_POISE, 0.0);

        helper.startSequence()
                .thenExecute(() -> beginBlocking(target))
                .thenExecuteAfter(2, () -> {
                    float startingHealth = target.getHealth();
                    target.hurt(attacker.damageSources().playerAttack(attacker), DAMAGE_AMOUNT);

                    DamagePipelineProbeEffect.State attackerState = getProbeState(MKCore.getPlayerOrThrow(attacker));
                    DamagePipelineProbeEffect.State targetState = getProbeState(MKCore.getPlayerOrThrow(target));
                    float endingPoise = MKCore.getPlayerOrThrow(target).getStats().getPoise();

                    // This test is intentionally characterizing the current pipeline behavior:
                    // once the shield path is taken, these mock-player hits no longer reach LivingDamageEvent.Pre.
                    assertFloatEquals(helper, target.getHealth(), startingHealth, 0.001f,
                            "shielded melee currently prevents all health loss");
                    helper.assertTrue(endingPoise < STARTING_POISE, "shielded melee should still consume poise");
                    helper.assertValueEqual(attackerState.getAttackerMeleeCount(), 0, "shielded melee should skip melee triggers");
                    helper.assertValueEqual(attackerState.getAttackerPostCount(), 0, "shielded melee attacker post trigger count");
                    helper.assertValueEqual(targetState.getVictimPreScaleCount(), 0, "shielded melee victim pre trigger count");
                    helper.assertValueEqual(targetState.getVictimPostScaleCount(), 0, "shielded melee victim post trigger count");
                })
                .thenSucceed();
    }

    @GameTest(template = "player_data_phase0")
    public static void shieldedProjectileHitConsumesPoiseWithoutRunningDamageTriggers(GameTestHelper helper) {
        Player attacker = createMockPlayer(helper, ATTACKER_POS);
        Player target = createMockPlayer(helper, TARGET_POS);
        prepareBlockingTarget(attacker, target, 0.5, STARTING_POISE, STARTING_POISE, 0.2);

        helper.startSequence()
                .thenExecute(() -> beginBlocking(target))
                .thenExecuteAfter(2, () -> {
                    float startingHealth = target.getHealth();
                    Arrow arrow = helper.spawn(net.minecraft.world.entity.EntityType.ARROW, ATTACKER_POS);
                    arrow.setOwner(attacker);
                    target.hurt(attacker.damageSources().arrow(arrow, attacker), DAMAGE_AMOUNT);

                    DamagePipelineProbeEffect.State attackerState = getProbeState(MKCore.getPlayerOrThrow(attacker));
                    DamagePipelineProbeEffect.State targetState = getProbeState(MKCore.getPlayerOrThrow(target));
                    float endingPoise = MKCore.getPlayerOrThrow(target).getStats().getPoise();

                    // Projectile hits currently follow the same blocked-hit behavior in this harness:
                    // poise is consumed, but the attacker/victim damage trigger pipeline is skipped.
                    assertFloatEquals(helper, target.getHealth(), startingHealth, 0.001f,
                            "shielded projectile currently prevents all health loss");
                    helper.assertTrue(endingPoise < STARTING_POISE, "shielded projectile should still consume poise");
                    helper.assertValueEqual(attackerState.getAttackerProjectileCount(), 0, "shielded projectile should skip projectile triggers");
                    helper.assertValueEqual(attackerState.getAttackerPostCount(), 0, "shielded projectile attacker post trigger count");
                    helper.assertValueEqual(targetState.getVictimPreScaleCount(), 0, "shielded projectile victim pre trigger count");
                    helper.assertValueEqual(targetState.getVictimPostScaleCount(), 0, "shielded projectile victim post trigger count");
                })
                .thenSucceed();
    }

    @GameTest(template = "player_data_phase0")
    public static void meleeHitOnlyCountsForAttackerWhenVictimLacksProbeEffect(GameTestHelper helper) {
        Player attacker = createMockPlayer(helper, ATTACKER_POS);
        Player target = createMockPlayer(helper, TARGET_POS, false);
        prepareAttacker(attacker);

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    target.hurt(attacker.damageSources().playerAttack(attacker), DAMAGE_AMOUNT);

                    DamagePipelineProbeEffect.State attackerState = getProbeState(MKCore.getPlayerOrThrow(attacker));

                    helper.assertValueEqual(attackerState.getAttackerMeleeCount(), 1, "attacker melee trigger count");
                    helper.assertValueEqual(attackerState.getAttackerPostCount(), 1, "attacker post trigger count");
                    assertProbeEffectAbsent(MKCore.getPlayerOrThrow(target), "victim should not have probe effect");
                })
                .thenSucceed();
    }

    @GameTest(template = "player_data_phase0")
    public static void meleeHitOnlyCountsForVictimWhenAttackerLacksProbeEffect(GameTestHelper helper) {
        Player attacker = createMockPlayer(helper, ATTACKER_POS, false);
        Player target = createMockPlayer(helper, TARGET_POS);
        prepareAttacker(attacker);

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    target.hurt(attacker.damageSources().playerAttack(attacker), DAMAGE_AMOUNT);

                    DamagePipelineProbeEffect.State targetState = getProbeState(MKCore.getPlayerOrThrow(target));

                    helper.assertValueEqual(targetState.getVictimPreScaleCount(), 1, "victim pre trigger count");
                    helper.assertValueEqual(targetState.getVictimPostScaleCount(), 1, "victim post trigger count");
                    assertProbeEffectAbsent(MKCore.getPlayerOrThrow(attacker), "attacker should not have probe effect");
                })
                .thenSucceed();
    }

    @GameTest(template = "player_data_phase0")
    public static void removingProbeEffectStopsFurtherTriggerDispatch(GameTestHelper helper) {
        Player attacker = createMockPlayer(helper, ATTACKER_POS);
        Player target = createMockPlayer(helper, TARGET_POS);
        prepareAttacker(attacker);

        helper.startSequence()
                .thenExecute(() -> {
                    MKCore.getPlayerOrThrow(attacker).getEffects().removeEffect(MKTestEffects.DAMAGE_PIPELINE_PROBE.get());
                    MKCore.getPlayerOrThrow(target).getEffects().removeEffect(MKTestEffects.DAMAGE_PIPELINE_PROBE.get());
                })
                .thenExecuteAfter(2, () -> {
                    target.hurt(attacker.damageSources().playerAttack(attacker), DAMAGE_AMOUNT);

                    assertProbeEffectAbsent(MKCore.getPlayerOrThrow(attacker), "attacker probe effect should be removed");
                    assertProbeEffectAbsent(MKCore.getPlayerOrThrow(target), "target probe effect should be removed");
                })
                .thenSucceed();
    }

    @GameTest(template = "player_data_phase0")
    public static void reapplyingProbeEffectDoesNotDuplicateTriggerDispatch(GameTestHelper helper) {
        Player attacker = createMockPlayer(helper, ATTACKER_POS);
        Player target = createMockPlayer(helper, TARGET_POS);
        prepareAttacker(attacker);

        helper.startSequence()
                .thenExecute(() -> {
                    applyProbeEffect(MKCore.getPlayerOrThrow(attacker));
                    applyProbeEffect(MKCore.getPlayerOrThrow(target));
                })
                .thenExecuteAfter(2, () -> {
                    target.hurt(attacker.damageSources().playerAttack(attacker), DAMAGE_AMOUNT);

                    DamagePipelineProbeEffect.State attackerState = getProbeState(MKCore.getPlayerOrThrow(attacker));
                    DamagePipelineProbeEffect.State targetState = getProbeState(MKCore.getPlayerOrThrow(target));

                    helper.assertValueEqual(attackerState.getAttackerMeleeCount(), 1, "reapplied attacker melee trigger count");
                    helper.assertValueEqual(attackerState.getAttackerPostCount(), 1, "reapplied attacker post trigger count");
                    helper.assertValueEqual(targetState.getVictimPreScaleCount(), 1, "reapplied victim pre trigger count");
                    helper.assertValueEqual(targetState.getVictimPostScaleCount(), 1, "reapplied victim post trigger count");
                })
                .thenSucceed();
    }

    @GameTest(template = "player_data_phase0")
    public static void repeatedHitsIncrementExactlyOncePerHit(GameTestHelper helper) {
        Player attacker = createMockPlayer(helper, ATTACKER_POS);
        Player target = createMockPlayer(helper, TARGET_POS);
        prepareAttacker(attacker);

        helper.startSequence()
                .thenExecuteAfter(2, () -> target.hurt(attacker.damageSources().playerAttack(attacker), DAMAGE_AMOUNT))
                .thenExecuteAfter(2, () -> target.hurt(attacker.damageSources().playerAttack(attacker), DAMAGE_AMOUNT))
                .thenExecuteAfter(2, () -> {
                    DamagePipelineProbeEffect.State attackerState = getProbeState(MKCore.getPlayerOrThrow(attacker));
                    DamagePipelineProbeEffect.State targetState = getProbeState(MKCore.getPlayerOrThrow(target));

                    helper.assertValueEqual(attackerState.getAttackerMeleeCount(), 2, "repeated attacker melee trigger count");
                    helper.assertValueEqual(attackerState.getAttackerPostCount(), 2, "repeated attacker post trigger count");
                    helper.assertValueEqual(targetState.getVictimPreScaleCount(), 2, "repeated victim pre trigger count");
                    helper.assertValueEqual(targetState.getVictimPostScaleCount(), 2, "repeated victim post trigger count");
                })
                .thenSucceed();
    }

    @GameTest(template = "player_data_phase0")
    public static void meleeCritChanceOfOneAppliesConfiguredCritMultiplier(GameTestHelper helper) {
        final float baseDamage = 6.0f;

        Player normalAttacker = createMockPlayer(helper, ATTACKER_POS, false);
        Player critAttacker = createMockPlayer(helper, ATTACKER_ALT_POS, false);
        Player normalTarget = createMockPlayer(helper, TARGET_POS, false);
        Player critTarget = createMockPlayer(helper, TARGET_ALT_POS, false);
        prepareAttacker(normalAttacker);
        prepareAttacker(critAttacker);
        setBaseValue(normalAttacker, MKAttributes.MELEE_CRIT, 0.0);
        setBaseValue(critAttacker, MKAttributes.MELEE_CRIT, 1.0);
        setBaseValue(critAttacker, MKAttributes.MELEE_CRIT_MULTIPLIER, 2.0);

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    float normalDamage = dealMeleeDamage(normalAttacker, normalTarget, baseDamage);
                    float critDamage = dealMeleeDamage(critAttacker, critTarget, baseDamage);

                    assertFloatEquals(helper, normalDamage, baseDamage, 0.001f, "non-crit melee damage");
                    assertFloatEquals(helper, critDamage, baseDamage * 2.0f, 0.001f, "crit melee damage");
                })
                .thenSucceed();
    }

    @GameTest(template = "player_data_phase0")
    public static void rangedResistanceAppliesAfterRangedBonusAndCritScaling(GameTestHelper helper) {
        final float baseDamage = 6.0f;
        final float rangedBonus = 4.0f;

        Player attacker = createMockPlayer(helper, ATTACKER_RANGED_POS, false);
        Player unresistedTarget = createMockPlayer(helper, TARGET_RANGED_POS, false);
        Player resistedTarget = createMockPlayer(helper, TARGET_RANGED_ALT_POS, false);
        setBaseValue(attacker, MKAttributes.RANGED_DAMAGE, rangedBonus);
        setBaseValue(attacker, MKAttributes.RANGED_CRIT, 1.0);
        setBaseValue(attacker, MKAttributes.RANGED_CRIT_MULTIPLIER, 2.0);
        setBaseValue(resistedTarget, MKAttributes.RANGED_RESISTANCE, 0.5);

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    float unresistedDamage = dealProjectileDamage(helper, attacker, unresistedTarget, baseDamage);
                    float resistedDamage = dealProjectileDamage(helper, attacker, resistedTarget, baseDamage);

                    assertFloatEquals(helper, unresistedDamage, (baseDamage + rangedBonus) * 2.0f,
                            0.001f, "unresisted ranged crit damage");
                    assertFloatEquals(helper, resistedDamage, (baseDamage + rangedBonus),
                            0.001f, "resisted ranged crit damage");
                })
                .thenSucceed();
    }

    @GameTest(template = "player_data_phase0")
    public static void mkAbilityDamageCritUsesSpellCritMultiplier(GameTestHelper helper) {
        final float baseDamage = 6.0f;

        Player normalAttacker = createMockPlayer(helper, ATTACKER_POS, false);
        Player critAttacker = createMockPlayer(helper, ATTACKER_ALT_POS, false);
        Player normalTarget = createMockPlayer(helper, TARGET_POS, false);
        Player critTarget = createMockPlayer(helper, TARGET_ALT_POS, false);
        setBaseValue(normalAttacker, MKAttributes.SPELL_CRIT, 0.0);
        setBaseValue(critAttacker, MKAttributes.SPELL_CRIT, 1.0);
        setBaseValue(critAttacker, MKAttributes.SPELL_CRIT_MULTIPLIER, 2.0);

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    float normalDamage = dealMKAbilityDamage(normalAttacker, normalTarget, baseDamage);
                    float critDamage = dealMKAbilityDamage(critAttacker, critTarget, baseDamage);

                    assertFloatEquals(helper, normalDamage, baseDamage, 0.001f, "non-crit MK ability damage");
                    assertFloatEquals(helper, critDamage, baseDamage * 2.0f, 0.001f, "crit MK ability damage");
                })
                .thenSucceed();
    }

    @GameTest(template = "player_data_phase0")
    public static void mkEffectDamageCritUsesSpellCritMultiplier(GameTestHelper helper) {
        final float baseDamage = 6.0f;

        Player normalAttacker = createMockPlayer(helper, ATTACKER_POS, false);
        Player critAttacker = createMockPlayer(helper, ATTACKER_ALT_POS, false);
        Player normalTarget = createMockPlayer(helper, TARGET_POS, false);
        Player critTarget = createMockPlayer(helper, TARGET_ALT_POS, false);
        setBaseValue(normalAttacker, MKAttributes.SPELL_CRIT, 0.0);
        setBaseValue(critAttacker, MKAttributes.SPELL_CRIT, 1.0);
        setBaseValue(critAttacker, MKAttributes.SPELL_CRIT_MULTIPLIER, 2.0);

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    float normalDamage = dealMKEffectDamage(normalAttacker, normalTarget, baseDamage);
                    float critDamage = dealMKEffectDamage(critAttacker, critTarget, baseDamage);

                    assertFloatEquals(helper, normalDamage, baseDamage, 0.001f, "non-crit MK effect damage");
                    assertFloatEquals(helper, critDamage, baseDamage * 2.0f, 0.001f, "crit MK effect damage");
                })
                .thenSucceed();
    }

    @GameTest(template = "player_data_phase0")
    public static void mkAbilityDamageModifierScalingStillAddsConfiguredDamageBonus(GameTestHelper helper) {
        final float baseDamage = 6.0f;
        final float fireBonus = 8.0f;
        final float modifierScaling = 0.5f;

        Player attacker = createMockPlayer(helper, ATTACKER_POS, false);
        Player target = createMockPlayer(helper, TARGET_POS, false);
        setBaseValue(attacker, MKAttributes.FIRE_DAMAGE, fireBonus);
        setBaseValue(attacker, MKAttributes.SPELL_CRIT, 0.0);

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    float damage = dealMKAbilityDamage(attacker, target, baseDamage, modifierScaling);

                    assertFloatEquals(helper, damage, baseDamage + fireBonus * modifierScaling,
                            0.001f, "legacy modifier scaling should still add the configured fire bonus");
                })
                .thenSucceed();
    }

    @GameTest(template = "player_data_phase0")
    public static void parameterizedMkAbilityDamageEffectAppliesRuntimeDamageBonus(GameTestHelper helper) {
        final float skillLevel = 3.0f;
        final float fireBonus = 4.0f;

        Player attacker = createMockPlayer(helper, ATTACKER_POS, false);
        Player target = createMockPlayer(helper, TARGET_POS, false);
        setBaseValue(attacker, MKAttributes.FIRE_DAMAGE, fireBonus);
        setBaseValue(attacker, MKAttributes.SPELL_CRIT, 0.0);

        AbilityFormula damageFormula = AbilityFormula.bonusScaledLinear(
                TEST_DAMAGE_BASE,
                TEST_DAMAGE_PER_LEVEL,
                FormulaContextKey.DAMAGE_BONUS,
                TEST_DAMAGE_MODIFIER_SCALING
        );
        FormulaParameters parameters = FormulaParameters.builder()
                .with(TEST_DAMAGE_BASE, 2.0f)
                .with(TEST_DAMAGE_PER_LEVEL, 1.0f)
                .with(TEST_DAMAGE_MODIFIER_SCALING, 0.5f)
                .build();

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    float startingHealth = target.getHealth();
                    MKCore.getPlayerOrThrow(target).getEffects().addEffect(
                            MKAbilityDamageEffect.from(attacker, CoreDamageTypes.FireDamage.get(), damageFormula, parameters)
                                    .ability(MKTestAbilities.TEST_EMBER.get())
                                    .skillLevel(skillLevel)
                    );
                    float damage = startingHealth - target.getHealth();

                    assertFloatEquals(helper, damage, 7.0f, 0.001f,
                            "parameterized ability damage should add runtime fire bonus through the damage pipeline");
                })
                .thenSucceed();
    }

    @GameTest(template = "player_data_phase0")
    public static void mkAbilityDamageCurrentlyIgnoresFireResistanceWithoutBypassesArmorTag(GameTestHelper helper) {
        final float baseDamage = 6.0f;

        Player attacker = createMockPlayer(helper, ATTACKER_POS, false);
        Player unresistedTarget = createMockPlayer(helper, TARGET_POS, false);
        Player resistedTarget = createMockPlayer(helper, TARGET_ALT_POS, false);
        setBaseValue(resistedTarget, MKAttributes.FIRE_RESISTANCE, 0.8);

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    float unresistedDamage = dealMKAbilityDamage(attacker, unresistedTarget, baseDamage);
                    float resistedDamage = dealMKAbilityDamage(attacker, resistedTarget, baseDamage);

                    // Characterizes the current EntityHurtTriggers behavior: MKDamageType resistance
                    // is only applied for BYPASSES_ARMOR-tagged MKDamageSource hits.
                    assertFloatEquals(helper, unresistedDamage, baseDamage, 0.001f, "baseline MK ability damage");
                    assertFloatEquals(helper, resistedDamage, baseDamage, 0.001f, "fire resistance is currently ignored");
                })
                .thenSucceed();
    }

    private static Player createMockPlayer(GameTestHelper helper, BlockPos relativePos) {
        return createMockPlayer(helper, relativePos, true);
    }

    private static Player createMockPlayer(GameTestHelper helper, BlockPos relativePos, boolean applyProbeEffect) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos absolutePos = helper.absolutePos(relativePos);
        player.moveTo(absolutePos.getX() + 0.5, absolutePos.getY(), absolutePos.getZ() + 0.5, 0.0f, 0.0f);
        player.setHealth(player.getMaxHealth());
        if (applyProbeEffect) {
            applyProbeEffect(MKCore.getPlayerOrThrow(player));
        }
        return player;
    }

    private static void prepareAttacker(Player attacker) {
        attacker.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
    }

    private static void prepareBlockingTarget(Player attacker, Player target, double blockEfficiency,
                                              double maxPoise, double currentPoise, double rangedResistance) {
        target.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
        target.lookAt(EntityAnchorArgument.Anchor.EYES, attacker.position());
        target.setYHeadRot(target.getYRot());
        setBaseValue(target, MKAttributes.BLOCK_EFFICIENCY, blockEfficiency);
        setBaseValue(target, MKAttributes.MAX_POISE, maxPoise);
        setBaseValue(target, MKAttributes.RANGED_RESISTANCE, rangedResistance);
        MKCore.getPlayerOrThrow(target).getStats().setPoise((float) currentPoise);
    }

    private static void beginBlocking(Player target) {
        target.startUsingItem(InteractionHand.OFF_HAND);
        target.tick();
        target.tick();
    }

    private static float dealMeleeDamage(Player attacker, Player target, float damageAmount) {
        float startingHealth = target.getHealth();
        target.hurt(attacker.damageSources().playerAttack(attacker), damageAmount);
        return startingHealth - target.getHealth();
    }

    private static float dealProjectileDamage(GameTestHelper helper, Player attacker, Player target, float damageAmount) {
        float startingHealth = target.getHealth();
        Arrow arrow = helper.spawn(net.minecraft.world.entity.EntityType.ARROW, helper.absolutePos(ATTACKER_RANGED_POS));
        arrow.setOwner(attacker);
        target.hurt(attacker.damageSources().arrow(arrow, attacker), damageAmount);
        return startingHealth - target.getHealth();
    }

    private static float dealMKAbilityDamage(Player attacker, Player target, float damageAmount) {
        return dealMKAbilityDamage(attacker, target, damageAmount, 1.0f);
    }

    private static float dealMKAbilityDamage(Player attacker, Player target, float damageAmount, float modifierScaling) {
        float startingHealth = target.getHealth();
        target.hurt(MKDamageSource.causeAbilityDamage(target.level(), CoreDamageTypes.FireDamage.get(),
                MKTestAbilities.TEST_EMBER.get().getAbilityId(), attacker, attacker, modifierScaling), damageAmount);
        return startingHealth - target.getHealth();
    }

    private static float dealMKEffectDamage(Player attacker, Player target, float damageAmount) {
        float startingHealth = target.getHealth();
        target.hurt(MKDamageSource.causeEffectDamage(target.level(), CoreDamageTypes.FireDamage.get(),
                "mkcore.test.effect.damage", attacker, attacker), damageAmount);
        return startingHealth - target.getHealth();
    }

    private static void applyProbeEffect(MKPlayerData playerData) {
        playerData.getEffects().addEffect(MKTestEffects.DAMAGE_PIPELINE_PROBE.get().builder(playerData.getEntity()).infinite());
    }

    private static DamagePipelineProbeEffect.State getProbeState(MKPlayerData playerData) {
        return playerData.getEffects().effects(MKTestEffects.DAMAGE_PIPELINE_PROBE.get()).stream()
                .findFirst()
                .map(effect -> effect.getState(DamagePipelineProbeEffect.STATE))
                .orElseThrow(() -> new IllegalStateException("Missing probe effect"));
    }

    private static void assertProbeEffectAbsent(MKPlayerData playerData, String label) {
        if (!playerData.getEffects().effects(MKTestEffects.DAMAGE_PIPELINE_PROBE.get()).isEmpty()) {
            throw new IllegalStateException(label);
        }
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
