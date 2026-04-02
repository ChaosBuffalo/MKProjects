package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
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
    private static final float DAMAGE_AMOUNT = 10.0f;
    private static final float STARTING_POISE = 20.0f;

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

    private static Player createMockPlayer(GameTestHelper helper, BlockPos relativePos) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos absolutePos = helper.absolutePos(relativePos);
        player.moveTo(absolutePos.getX() + 0.5, absolutePos.getY(), absolutePos.getZ() + 0.5, 0.0f, 0.0f);
        player.setHealth(player.getMaxHealth());
        applyProbeEffect(MKCore.getPlayerOrThrow(player));
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

    private static void applyProbeEffect(MKPlayerData playerData) {
        playerData.getEffects().addEffect(MKTestEffects.DAMAGE_PIPELINE_PROBE.get().builder(playerData.getEntity()).infinite());
    }

    private static DamagePipelineProbeEffect.State getProbeState(MKPlayerData playerData) {
        return playerData.getEffects().effects(MKTestEffects.DAMAGE_PIPELINE_PROBE.get()).stream()
                .findFirst()
                .map(effect -> effect.getState(DamagePipelineProbeEffect.STATE))
                .orElseThrow(() -> new IllegalStateException("Missing probe effect"));
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
