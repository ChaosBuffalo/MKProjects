package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities2.AbilityRuntimeService;
import com.chaosbuffalo.mkcore.abilities2.actions.AbilityAction;
import com.chaosbuffalo.mkcore.abilities2.actions.AbilityConditionDefinition;
import com.chaosbuffalo.mkcore.abilities2.datagen.AbilityDatagenKeys;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityActivationDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityDefinitionData;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityDeliveryDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityParameterDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityPresentation;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityScalar;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityTargetResolverDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityValue;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityValueKind;
import com.chaosbuffalo.mkcore.abilities2.definition.ActivationBehavior;
import com.chaosbuffalo.mkcore.abilities2.definition.ActivationKind;
import com.chaosbuffalo.mkcore.abilities2.definition.DeliveryKind;
import com.chaosbuffalo.mkcore.abilities2.definition.InterruptPolicy;
import com.chaosbuffalo.mkcore.abilities2.definition.InterruptRefundPolicy;
import com.chaosbuffalo.mkcore.abilities2.definition.StateScope;
import com.chaosbuffalo.mkcore.abilities2.runtime.AbilityDefinitionResolver;
import com.chaosbuffalo.mkcore.abilities2.runtime.AbilityEventSnapshot;
import com.chaosbuffalo.mkcore.abilities2.runtime.AbilityEventType;
import com.chaosbuffalo.mkcore.abilities2.runtime.AbilityReference;
import com.chaosbuffalo.mkcore.abilities2.runtime.AbilityResolvedTargets;
import com.chaosbuffalo.mkcore.abilities2.runtime.ActivationRequest;
import com.chaosbuffalo.mkcore.abilities2.runtime.FailureReason;
import com.chaosbuffalo.mkcore.abilities2.runtime.InvocationResult;
import com.chaosbuffalo.mkcore.abilities2.runtime.MKAbilityPowerResolver;
import com.chaosbuffalo.mkcore.abilities2.runtime.MemoryAbilityStateStore;
import com.chaosbuffalo.mkcore.abilities2.runtime.PersistedAbilityRuntimeState;
import com.chaosbuffalo.mkcore.abilities2.runtime.SimpleAbilityEngine;
import com.chaosbuffalo.mkcore.core.EntityAnimationModule;
import com.chaosbuffalo.mkcore.core.MKEntityData;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.AbilityGroupId;
import com.chaosbuffalo.mkcore.entities.AbilityProjectileEntity;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.init.CoreEntities;
import com.chaosbuffalo.mkcore.init.CoreEffects;
import com.chaosbuffalo.mkcore.item.ItemGrantedAbility;
import com.chaosbuffalo.mkcore.test.MKTestAbilities;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@GameTestHolder(MKCore.MOD_ID)
@PrefixGameTestTemplate(false)
public class MKAbilities2RuntimeGameTests {
    private static final ResourceLocation PROJECTILE_IMPACT_ABILITY =
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "test_abilities2_projectile_impact");
    private static final ResourceLocation PROJECTILE_GROUND_ABILITY =
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "test_abilities2_projectile_ground");
    private static final ResourceLocation INT_BRANCH_CONDITION_ABILITY =
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "test_abilities2_branch_int_conditions");
    private static final ResourceLocation FLOAT_BRANCH_CONDITION_ABILITY =
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "test_abilities2_branch_float_conditions");
    private static final ResourceLocation EVENT_ACTOR_BRANCH_CONDITION_ABILITY =
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "test_abilities2_branch_event_actor_conditions");
    private static final ResourceLocation EVENT_PAYLOAD_BRANCH_CONDITION_ABILITY =
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "test_abilities2_branch_event_payload_conditions");
    private static final ResourceLocation INTERRUPT_REASON_PROBE_ABILITY =
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "test_abilities2_interrupt_reason_probe");
    private static final ResourceLocation DELAYED_BURST_ABILITY =
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "test_abilities2_delayed_burst");
    private static final ResourceLocation HEALING_CLOUD_ABILITY =
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "test_abilities2_healing_cloud");
    private static final ResourceLocation FIRE_CLOUD_ABILITY =
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "test_abilities2_fire_cloud");
    private static final ResourceLocation SPELL_CRIT_PASSIVE_ABILITY =
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "test_abilities2_spell_crit_passive");
    private static final ResourceLocation COOLDOWN_PROBE_ABILITY =
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "test_abilities2_cooldown_probe");
    private static final ResourceLocation COST_PROBE_ABILITY =
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "test_abilities2_cost_probe");
    private static final ResourceLocation GCD_PROBE_SHARED_A_ABILITY =
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "test_abilities2_gcd_probe_shared_a");
    private static final ResourceLocation GCD_PROBE_SHARED_B_ABILITY =
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "test_abilities2_gcd_probe_shared_b");
    private static final ResourceLocation GCD_PROBE_OTHER_ABILITY =
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "test_abilities2_gcd_probe_other");
    private static final ResourceLocation SPELL_SOURCE_ABILITY =
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "test_abilities2_firebolt");
    private static final ResourceLocation AI_FIREBOLT_ABILITY =
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "test_abilities2_ai_firebolt");
    private static final ResourceLocation FRIENDLY_HEAL_ABILITY =
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "test_abilities2_friendly_heal");
    private static final ResourceLocation SELF_HEAL_ABILITY =
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "test_abilities2_self_heal");
    private static final UUID EVENT_PAYLOAD_ENTITY_REF_PROBE_ID =
            UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final ResourceLocation MENDING_CHANNEL_ABILITY =
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "test_abilities2_mending_channel");
    private static final ResourceLocation RESTORING_AURA_ABILITY =
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "test_abilities2_restoring_aura");
    private static final InterruptPolicy NO_INTERRUPT = new InterruptPolicy(false, 0.0f, false, 0.0, true);
    private static final InterruptPolicy FULL_INTERRUPT_PROBE = new InterruptPolicy(true, 0.0f, true, 0.1, true);

    @GameTest(template = "player_data_phase0")
    public static void projectileImpactCallbackDamagesEntityTarget(GameTestHelper helper) {
        Player caster = createTestPlayer(helper, new BlockPos(1, 2, 1));
        Player target = createTestPlayer(helper, new BlockPos(3, 2, 1));
        AbilityRuntimeService service = createTestRuntimeService();
        var casterData = MKCore.getEntityDataOrThrow(caster);

        InvocationResult result = service.getEngine().activate(new ActivationRequest(
                casterData,
                casterData,
                new AbilityReference(PROJECTILE_IMPACT_ABILITY, null),
                "cast",
                null,
                null,
                null,
                false,
                false
        ));
        helper.assertTrue(result.started(), "projectile impact test activation should start");

        AbilityProjectileEntity projectile = findProjectile(helper, caster);
        float startingHealth = target.getHealth();
        boolean handled = service.handleProjectileImpact(projectile, caster, new EntityHitResult(target));

        helper.assertTrue(handled, "entity impact should complete the projectile delivery");
        helper.assertTrue(target.getHealth() < startingHealth, "impact callback should damage the target");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void generatedProjectileDefinitionAssignsRenderItem(GameTestHelper helper) {
        Player caster = createTestPlayer(helper, new BlockPos(1, 2, 1));
        var casterData = MKCore.getEntityDataOrThrow(caster);

        helper.assertTrue(
                MKCore.getAbilityDefinitionService().getResolver().resolvePatched(SPELL_SOURCE_ABILITY) != null,
                "generated projectile definition should be loaded for the integration test"
        );

        InvocationResult result = MKCore.getAbilityRuntimeService().getEngine().activate(new ActivationRequest(
                casterData,
                casterData,
                new AbilityReference(SPELL_SOURCE_ABILITY, null),
                "cast",
                null,
                null,
                null,
                false,
                false
        ));
        helper.assertTrue(result.started(), "generated projectile activation should start");

        AbilityProjectileEntity projectile = findProjectile(helper, caster);
        helper.assertTrue(!projectile.getItem().isEmpty(),
                "generated abilities2 projectile should carry a non-empty render item");
        helper.assertValueEqual(projectile.getItem().getItem(), Items.FIRE_CHARGE,
                "generated firebolt projectile render item");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void blockImpactKeepsProjectileAliveForGroundTickCallback(GameTestHelper helper) {
        Player caster = createTestPlayer(helper, new BlockPos(1, 2, 1));
        AbilityRuntimeService service = createTestRuntimeService();
        var casterData = MKCore.getEntityDataOrThrow(caster);

        InvocationResult result = service.getEngine().activate(new ActivationRequest(
                casterData,
                casterData,
                new AbilityReference(PROJECTILE_GROUND_ABILITY, null),
                "cast",
                null,
                null,
                null,
                false,
                false
        ));
        helper.assertTrue(result.started(), "projectile ground test activation should start");

        AbilityProjectileEntity projectile = findProjectile(helper, caster);
        float startingHealth = caster.getHealth();
        boolean impactHandled = service.handleProjectileImpact(
                projectile,
                caster,
                new BlockHitResult(projectile.position(), Direction.UP, projectile.blockPosition(), false)
        );
        boolean groundHandled = service.handleProjectileGroundTick(projectile, caster);

        helper.assertFalse(impactHandled, "block impact should keep the projectile active for ground callbacks");
        helper.assertFalse(groundHandled, "ground callbacks should not remove the projectile directly");
        helper.assertTrue(caster.getHealth() < startingHealth, "ground callback should damage the caster");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void delayedBurstDefinitionDetonatesAfterConfiguredDelay(GameTestHelper helper) {
        Player caster = createTestPlayer(helper, new BlockPos(1, 2, 1));
        Zombie target = createTestZombie(helper, new BlockPos(4, 2, 1));
        var casterData = MKCore.getEntityDataOrThrow(caster);

        helper.assertTrue(
                MKCore.getAbilityDefinitionService().getResolver().resolvePatched(DELAYED_BURST_ABILITY) != null,
                "generated delayed burst definition should be loaded for the integration test"
        );

        float startingHealth = target.getHealth();
        AbilityResolvedTargets forcedTargets = new AbilityResolvedTargets(
                target.getUUID(),
                List.of(target.getUUID()),
                target.position(),
                null,
                null
        );

        helper.startSequence()
                .thenExecute(() -> {
                    InvocationResult result = MKCore.getAbilityRuntimeService().getEngine().activate(new ActivationRequest(
                            casterData,
                            casterData,
                            new AbilityReference(DELAYED_BURST_ABILITY, null),
                            "cast",
                            null,
                            forcedTargets,
                            null,
                            false,
                            false
                    ));
                    helper.assertTrue(result.started(), "delayed burst activation should start");
                })
                .thenExecuteAfter(3, () -> helper.assertValueEqual(target.getHealth(), startingHealth,
                        "delayed burst should not detonate before the configured delay expires"))
                .thenExecuteAfter(3, () -> {
                    helper.assertTrue(target.getHealth() < startingHealth,
                            "delayed burst should damage the selected target once the delay expires");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void healingCloudDefinitionPulsesAfterConfiguredInterval(GameTestHelper helper) {
        Player caster = createTestPlayer(helper, new BlockPos(1, 2, 1));
        var casterData = MKCore.getEntityDataOrThrow(caster);

        helper.assertTrue(
                MKCore.getAbilityDefinitionService().getResolver().resolvePatched(HEALING_CLOUD_ABILITY) != null,
                "generated healing cloud definition should be loaded for the integration test"
        );

        caster.setHealth(caster.getMaxHealth() - 8.0f);
        float startingHealth = caster.getHealth();

        helper.startSequence()
                .thenExecute(() -> {
                    InvocationResult result = MKCore.getAbilityRuntimeService().getEngine().activate(new ActivationRequest(
                            casterData,
                            casterData,
                            new AbilityReference(HEALING_CLOUD_ABILITY, null),
                            "cast",
                            null,
                            null,
                            null,
                            false,
                            false
                    ));
                    helper.assertTrue(result.started(), "healing cloud activation should start");
                })
                .thenExecuteAfter(3, () -> helper.assertValueEqual(caster.getHealth(), startingHealth,
                        "healing cloud should wait until its first ground pulse interval"))
                .thenExecuteAfter(3, () -> {
                    helper.assertTrue(caster.getHealth() > startingHealth,
                            "healing cloud should heal the caster once its first ground pulse fires");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void intBranchConditionsReadVarsParamsAndState(GameTestHelper helper) {
        Player caster = createTestPlayer(helper, new BlockPos(1, 2, 1));
        AbilityRuntimeService service = createTestRuntimeService();
        var casterData = MKCore.getEntityDataOrThrow(caster);

        caster.setHealth(caster.getMaxHealth() - 8.0f);
        float startingHealth = caster.getHealth();

        InvocationResult result = service.getEngine().activate(new ActivationRequest(
                casterData,
                casterData,
                new AbilityReference(INT_BRANCH_CONDITION_ABILITY, null),
                "cast",
                null,
                null,
                null,
                false,
                false
        ));
        helper.assertTrue(result.started(), "int branch condition probe should start");

        PersistedAbilityRuntimeState snapshot = service.captureOwnerRuntime(casterData);
        helper.assertTrue(caster.getHealth() > startingHealth,
                "state-driven int branch conditions should allow the follow-up heal");
        helper.assertTrue(stateBool(snapshot, "charges_gate"),
                "var_int branch should mark the boolean gate state");
        helper.assertValueEqual(stateInt(snapshot, "bonus_count"), 1,
                "param_float branch should store the expected int state value");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void floatBranchConditionsReadVarsParamsAndState(GameTestHelper helper) {
        Player caster = createTestPlayer(helper, new BlockPos(1, 2, 1));
        AbilityRuntimeService service = createTestRuntimeService();
        var casterData = MKCore.getEntityDataOrThrow(caster);

        caster.setHealth(caster.getMaxHealth() - 6.0f);
        float startingHealth = caster.getHealth();

        InvocationResult result = service.getEngine().activate(new ActivationRequest(
                casterData,
                casterData,
                new AbilityReference(FLOAT_BRANCH_CONDITION_ABILITY, null),
                "cast",
                null,
                null,
                null,
                false,
                false
        ));
        helper.assertTrue(result.started(), "float branch condition probe should start");

        PersistedAbilityRuntimeState snapshot = service.captureOwnerRuntime(casterData);
        helper.assertTrue(caster.getHealth() > startingHealth,
                "state-driven float branch conditions should allow the follow-up heal");
        helper.assertTrue(stateBool(snapshot, "param_gate"),
                "param_int branch should mark the boolean gate state");
        helper.assertValueEqual(stateInt(snapshot, "float_gate"), 1,
                "var_float branch should store the expected int gate state");
        helper.assertValueEqual(stateFloat(snapshot, "rating"), 1.25f,
                "modify_state should preserve the float value used by the state_float condition");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void eventHasActorConditionBranchesOnSnapshotActor(GameTestHelper helper) {
        Player actorPresentCaster = createTestPlayer(helper, new BlockPos(1, 2, 1));
        Player actorMissingCaster = createTestPlayer(helper, new BlockPos(4, 2, 1));
        AbilityRuntimeService service = createTestRuntimeService();
        var actorPresentData = MKCore.getEntityDataOrThrow(actorPresentCaster);
        var actorMissingData = MKCore.getEntityDataOrThrow(actorMissingCaster);

        actorPresentCaster.setHealth(actorPresentCaster.getMaxHealth() - 4.0f);
        actorMissingCaster.setHealth(actorMissingCaster.getMaxHealth() - 4.0f);
        float actorPresentStartingHealth = actorPresentCaster.getHealth();
        float actorMissingStartingHealth = actorMissingCaster.getHealth();

        InvocationResult actorPresentResult = service.getEngine().activate(new ActivationRequest(
                actorPresentData,
                actorPresentData,
                new AbilityReference(EVENT_ACTOR_BRANCH_CONDITION_ABILITY, null),
                "cast",
                null,
                null,
                new AbilityEventSnapshot(
                        AbilityEventType.SPELL_HIT,
                        null,
                        UUID.randomUUID(),
                        0,
                        actorPresentCaster.getUUID(),
                        EVENT_ACTOR_BRANCH_CONDITION_ABILITY,
                        "cast",
                        actorPresentCaster.getUUID(),
                        null,
                        Map.of()
                ),
                false,
                false
        ));
        helper.assertTrue(actorPresentResult.started(), "event_has_actor positive probe should start");

        InvocationResult actorMissingResult = service.getEngine().activate(new ActivationRequest(
                actorMissingData,
                actorMissingData,
                new AbilityReference(EVENT_ACTOR_BRANCH_CONDITION_ABILITY, null),
                "cast",
                null,
                null,
                new AbilityEventSnapshot(
                        AbilityEventType.SPELL_HIT,
                        null,
                        UUID.randomUUID(),
                        0,
                        actorMissingCaster.getUUID(),
                        EVENT_ACTOR_BRANCH_CONDITION_ABILITY,
                        "cast",
                        null,
                        null,
                        Map.of()
                ),
                false,
                false
        ));
        helper.assertTrue(actorMissingResult.started(), "event_has_actor negative probe should start");

        PersistedAbilityRuntimeState actorPresentSnapshot = service.captureOwnerRuntime(actorPresentData);
        PersistedAbilityRuntimeState actorMissingSnapshot = service.captureOwnerRuntime(actorMissingData);
        helper.assertTrue(actorPresentCaster.getHealth() > actorPresentStartingHealth,
                "event_has_actor should take the true branch when the snapshot includes an actor");
        helper.assertValueEqual(actorMissingCaster.getHealth(), actorMissingStartingHealth,
                "event_has_actor should skip the heal when the snapshot omits an actor");
        helper.assertTrue(stateBool(actorPresentSnapshot, "actor_present"),
                "event_has_actor true branch should mark the state as present");
        helper.assertFalse(stateBool(actorMissingSnapshot, "actor_present"),
                "event_has_actor false branch should mark the state as absent");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void eventPayloadBranchConditionsReadTypedPayloadValues(GameTestHelper helper) {
        Player caster = createTestPlayer(helper, new BlockPos(1, 2, 1));
        AbilityRuntimeService service = createTestRuntimeService();
        var casterData = MKCore.getEntityDataOrThrow(caster);

        InvocationResult result = service.getEngine().activate(new ActivationRequest(
                casterData,
                casterData,
                new AbilityReference(EVENT_PAYLOAD_BRANCH_CONDITION_ABILITY, null),
                "cast",
                null,
                null,
                new AbilityEventSnapshot(
                        AbilityEventType.SPELL_HIT,
                        null,
                        UUID.randomUUID(),
                        0,
                        caster.getUUID(),
                        EVENT_PAYLOAD_BRANCH_CONDITION_ABILITY,
                        "cast",
                        caster.getUUID(),
                        caster.getUUID(),
                        Map.of(
                                "stack_count", new AbilityValue.IntValue(2),
                                "impact_rating", new AbilityValue.FloatValue(2.0f),
                                "critical", new AbilityValue.BoolValue(true),
                                "phase", new AbilityValue.StringValue("burst"),
                                "damage_type", new AbilityValue.ResourceLocationValue(CoreDamageTypes.FireDamage.getId()),
                                "ability_tag_probe", new AbilityValue.ResourceLocationValue(EVENT_PAYLOAD_BRANCH_CONDITION_ABILITY),
                                "actor_ref", new AbilityValue.EntityRefValue(EVENT_PAYLOAD_ENTITY_REF_PROBE_ID)
                        )
                ),
                false,
                false
        ));
        helper.assertTrue(result.started(), "event payload branch probe should start");

        PersistedAbilityRuntimeState snapshot = service.captureOwnerRuntime(casterData);
        helper.assertTrue(stateBool(snapshot, "has_stack_payload"),
                "event_has_payload should detect a present payload entry");
        helper.assertTrue(stateBool(snapshot, "stack_gate"),
                "event_payload_int should evaluate numeric payload comparisons");
        helper.assertTrue(stateBool(snapshot, "rating_gate"),
                "event_payload_float should evaluate float payload comparisons");
        helper.assertTrue(stateBool(snapshot, "source_tag_gate"),
                "event_source_tag should evaluate tagged source ability ids");
        helper.assertTrue(stateBool(snapshot, "critical_gate"),
                "event_payload_bool should evaluate boolean payload values");
        helper.assertTrue(stateBool(snapshot, "phase_gate"),
                "event_payload_string should evaluate string payload values");
        helper.assertTrue(stateBool(snapshot, "damage_type_gate"),
                "event_payload_resource_location should evaluate resource location payload values");
        helper.assertTrue(stateBool(snapshot, "fire_tag_gate"),
                "event_payload_tag should evaluate tagged resource-location payload values");
        helper.assertTrue(stateBool(snapshot, "actor_ref_gate"),
                "event_payload_entity_ref should evaluate entity-ref payload values");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void eventPayloadBranchConditionsTreatMissingOrMismatchedValuesAsFalse(GameTestHelper helper) {
        Player caster = createTestPlayer(helper, new BlockPos(1, 2, 1));
        AbilityRuntimeService service = createTestRuntimeService();
        var casterData = MKCore.getEntityDataOrThrow(caster);

        InvocationResult result = service.getEngine().activate(new ActivationRequest(
                casterData,
                casterData,
                new AbilityReference(EVENT_PAYLOAD_BRANCH_CONDITION_ABILITY, null),
                "cast",
                null,
                null,
                new AbilityEventSnapshot(
                        AbilityEventType.SPELL_HIT,
                        null,
                        UUID.randomUUID(),
                        0,
                        caster.getUUID(),
                        INT_BRANCH_CONDITION_ABILITY,
                        "cast",
                        caster.getUUID(),
                        caster.getUUID(),
                        Map.of(
                                "impact_rating", new AbilityValue.FloatValue(0.5f),
                                "critical", new AbilityValue.BoolValue(false),
                                "phase", new AbilityValue.StringValue("fizzle"),
                                "damage_type", new AbilityValue.ResourceLocationValue(CoreDamageTypes.FrostDamage.getId()),
                                "ability_tag_probe", new AbilityValue.ResourceLocationValue(INT_BRANCH_CONDITION_ABILITY),
                                "actor_ref", new AbilityValue.EntityRefValue(UUID.randomUUID())
                        )
                ),
                false,
                false
        ));
        helper.assertTrue(result.started(), "event payload branch negative probe should start");

        PersistedAbilityRuntimeState snapshot = service.captureOwnerRuntime(casterData);
        helper.assertFalse(stateBool(snapshot, "has_stack_payload"),
                "event_has_payload should be false when the payload key is missing");
        helper.assertFalse(stateBool(snapshot, "stack_gate"),
                "event_payload_int should be false when the numeric payload is missing");
        helper.assertFalse(stateBool(snapshot, "rating_gate"),
                "event_payload_float should be false when the comparison fails");
        helper.assertFalse(stateBool(snapshot, "source_tag_gate"),
                "event_source_tag should be false when the source ability id is missing the tag");
        helper.assertFalse(stateBool(snapshot, "critical_gate"),
                "event_payload_bool should be false when the boolean payload does not match");
        helper.assertFalse(stateBool(snapshot, "phase_gate"),
                "event_payload_string should be false when the string payload does not match");
        helper.assertFalse(stateBool(snapshot, "damage_type_gate"),
                "event_payload_resource_location should be false when the resource location does not match");
        helper.assertFalse(stateBool(snapshot, "fire_tag_gate"),
                "event_payload_tag should be false when the resource-location payload is missing the tag");
        helper.assertFalse(stateBool(snapshot, "actor_ref_gate"),
                "event_payload_entity_ref should be false when the entity ref does not match");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void friendlyResolvedActivationRejectsEnemyForcedTarget(GameTestHelper helper) {
        Player caster = createTestPlayer(helper, new BlockPos(1, 2, 1));
        Player ally = createTestPlayer(helper, new BlockPos(3, 2, 1));
        Zombie enemy = createTestZombie(helper, new BlockPos(5, 2, 1));
        var casterData = MKCore.getEntityDataOrThrow(caster);

        helper.assertTrue(
                MKCore.getAbilityDefinitionService().getResolver().resolvePatched(FRIENDLY_HEAL_ABILITY) != null,
                "generated friendly heal definition should be loaded for the integration test"
        );

        InvocationResult invalidResult = MKCore.getAbilityRuntimeService().getEngine().activate(new ActivationRequest(
                casterData,
                casterData,
                new AbilityReference(FRIENDLY_HEAL_ABILITY, null),
                "cast",
                null,
                singleTarget(enemy),
                null,
                false,
                false
        ));
        helper.assertFalse(invalidResult.started(),
                "friendly-targeted activations should reject enemy forced targets");
        helper.assertValueEqual(invalidResult.failureReason(), FailureReason.INVALID_TARGETS,
                "friendly-targeted activation enemy rejection reason");

        ally.setHealth(ally.getMaxHealth() - 6.0f);
        float allyStartingHealth = ally.getHealth();
        InvocationResult validResult = MKCore.getAbilityRuntimeService().getEngine().activate(new ActivationRequest(
                casterData,
                casterData,
                new AbilityReference(FRIENDLY_HEAL_ABILITY, null),
                "cast",
                null,
                singleTarget(ally),
                null,
                false,
                false
        ));
        helper.assertTrue(validResult.started(),
                "friendly-targeted activations should accept allied forced targets");
        helper.assertTrue(ally.getHealth() > allyStartingHealth,
                "friendly-targeted activation should heal the allied target");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void enemyResolvedAiActivationRejectsFriendlyForcedTarget(GameTestHelper helper) {
        Player caster = createTestPlayer(helper, new BlockPos(1, 2, 1));
        Player friendly = createTestPlayer(helper, new BlockPos(3, 2, 1));
        Zombie enemy = createTestZombie(helper, new BlockPos(5, 2, 1));
        var casterData = MKCore.getEntityDataOrThrow(caster);

        helper.assertTrue(
                MKCore.getAbilityDefinitionService().getResolver().resolvePatched(AI_FIREBOLT_ABILITY) != null,
                "generated AI firebolt definition should be loaded for the integration test"
        );

        InvocationResult invalidResult = MKCore.getAbilityRuntimeService().activateAiAbility(
                casterData,
                casterData,
                new AbilityReference(AI_FIREBOLT_ABILITY, null),
                "cast",
                singleTarget(friendly)
        );
        helper.assertFalse(invalidResult.started(),
                "enemy-targeted AI activations should reject friendly forced targets");
        helper.assertValueEqual(invalidResult.failureReason(), FailureReason.INVALID_TARGETS,
                "enemy-targeted AI activation friendly rejection reason");

        float enemyStartingHealth = enemy.getHealth();
        InvocationResult validResult = MKCore.getAbilityRuntimeService().activateAiAbility(
                casterData,
                casterData,
                new AbilityReference(AI_FIREBOLT_ABILITY, null),
                "cast",
                singleTarget(enemy)
        );
        helper.assertTrue(validResult.started(),
                "enemy-targeted AI activations should accept enemy forced targets");

        AbilityProjectileEntity projectile = findProjectile(helper, caster);
        boolean handled = MKCore.getAbilityRuntimeService().handleProjectileImpact(
                projectile,
                caster,
                new EntityHitResult(enemy)
        );
        helper.assertTrue(handled, "enemy-targeted AI projectile impact should complete normally");
        helper.assertTrue(enemy.getHealth() < enemyStartingHealth,
                "enemy-targeted AI projectile should damage the enemy target");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void delayedBurstEnemyCallbackIgnoresCloserFriendlyTarget(GameTestHelper helper) {
        Player caster = createTestPlayer(helper, new BlockPos(1, 2, 1));
        Player friendly = createTestPlayer(helper, new BlockPos(8, 2, 1));
        Zombie enemy = createTestZombie(helper, new BlockPos(4, 2, 1));
        var casterData = MKCore.getEntityDataOrThrow(caster);

        float friendlyStartingHealth = friendly.getHealth();
        float enemyStartingHealth = enemy.getHealth();
        Vec3[] burstCenter = new Vec3[1];

        helper.startSequence()
                .thenExecute(() -> {
                    burstCenter[0] = enemy.position();
                    InvocationResult result = MKCore.getAbilityRuntimeService().getEngine().activate(new ActivationRequest(
                            casterData,
                            casterData,
                            new AbilityReference(DELAYED_BURST_ABILITY, null),
                            "cast",
                            null,
                            singleTarget(enemy),
                            null,
                            false,
                            false
                    ));
                    helper.assertTrue(result.started(), "delayed burst enemy callback probe should start");

                    friendly.moveTo(burstCenter[0].x + 0.2, burstCenter[0].y, burstCenter[0].z, 0.0f, 0.0f);
                    enemy.moveTo(burstCenter[0].x + 1.1, burstCenter[0].y, burstCenter[0].z, 0.0f, 0.0f);
                })
                .thenExecuteAfter(6, () -> {
                    helper.assertValueEqual(friendly.getHealth(), friendlyStartingHealth,
                            "enemy-targeted burst callbacks should ignore closer friendly targets");
                    helper.assertTrue(enemy.getHealth() < enemyStartingHealth,
                            "enemy-targeted burst callbacks should still damage a valid enemy in radius");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void fireCloudEnemyCallbackTargetsEnemyInsteadOfCaster(GameTestHelper helper) {
        Player caster = createTestPlayer(helper, new BlockPos(1, 2, 1));
        Zombie enemy = createTestZombie(helper, new BlockPos(3, 2, 1));
        var casterData = MKCore.getEntityDataOrThrow(caster);

        helper.assertTrue(
                MKCore.getAbilityDefinitionService().getResolver().resolvePatched(FIRE_CLOUD_ABILITY) != null,
                "generated fire cloud definition should be loaded for the integration test"
        );

        float casterStartingHealth = caster.getHealth();
        float enemyStartingHealth = enemy.getHealth();

        helper.startSequence()
                .thenExecute(() -> {
                    InvocationResult result = MKCore.getAbilityRuntimeService().getEngine().activate(new ActivationRequest(
                            casterData,
                            casterData,
                            new AbilityReference(FIRE_CLOUD_ABILITY, null),
                            "cast",
                            null,
                            null,
                            null,
                            false,
                            false
                    ));
                    helper.assertTrue(result.started(), "fire cloud activation should start");
                })
                .thenExecuteAfter(6, () -> {
                    helper.assertValueEqual(caster.getHealth(), casterStartingHealth,
                            "enemy-targeted area clouds should not treat the caster as the selected target");
                    helper.assertTrue(enemy.getHealth() < enemyStartingHealth,
                            "enemy-targeted area clouds should damage a valid enemy in range");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void slottedPassiveDefinitionInstallsReactionRuntime(GameTestHelper helper) {
        Player owner = createTestPlayer(helper, new BlockPos(1, 2, 1));
        Player target = createTestPlayer(helper, new BlockPos(3, 2, 1));
        MKPlayerData ownerData = MKCore.getPlayerOrThrow(owner);

        helper.assertTrue(
                MKCore.getAbilityDefinitionService().getResolver().resolvePatched(SPELL_CRIT_PASSIVE_ABILITY) != null,
                "generated passive definition should be loaded for the integration test"
        );

        helper.assertTrue(ownerData.getAbilities().learnAbilityDefinition(SPELL_CRIT_PASSIVE_ABILITY, AbilitySource.ADMIN),
                "slotted passive runtime test should learn the passive definition first");
        ownerData.getLoadout().getPassiveAbilityGroup().setSlots(1);
        ownerData.getLoadout().getPassiveAbilityGroup().setSlot(0, SPELL_CRIT_PASSIVE_ABILITY);

        float startingHealth = target.getHealth();
        MKCore.getAbilityRuntimeService().getReactionBus().emit(new AbilityEventSnapshot(
                AbilityEventType.SPELL_CRIT,
                null,
                UUID.randomUUID(),
                0,
                owner.getUUID(),
                SPELL_SOURCE_ABILITY,
                "cast",
                owner.getUUID(),
                target.getUUID(),
                Map.of()
        ));

        helper.assertTrue(target.getHealth() < startingHealth,
                "slotted passive definition should install a reaction that damages the crit target");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void slottedBasicDefinitionExecutesDefaultManualActivation(GameTestHelper helper) {
        Player owner = createTestPlayer(helper, new BlockPos(1, 2, 1));
        MKPlayerData ownerData = MKCore.getPlayerOrThrow(owner);

        helper.assertTrue(ownerData.getAbilities().learnAbilityDefinition(SELF_HEAL_ABILITY, AbilitySource.ADMIN),
                "slotted basic runtime test should learn the definition first");
        ownerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic).setSlots(1);
        ownerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic).setSlot(0, SELF_HEAL_ABILITY);

        owner.setHealth(owner.getMaxHealth() - 8.0f);
        ownerData.getStats().setMana(ownerData.getStats().getMaxMana());
        float startingHealth = owner.getHealth();

        helper.startSequence()
                .thenExecute(() -> ownerData.getAbilityExecutor().executeLoadoutAbility(AbilityGroupId.Basic, 0))
                .thenExecuteAfter(25, () -> {
                    helper.assertTrue(owner.getHealth() > startingHealth,
                            "loadout execution should start the default abilities2 manual activation");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void slottedDefinitionMirrorsCooldownIntoSyncedTimer(GameTestHelper helper) {
        Player owner = createTestPlayer(helper, new BlockPos(1, 2, 1));
        MKPlayerData ownerData = MKCore.getPlayerOrThrow(owner);

        helper.assertTrue(ownerData.getAbilities().learnAbilityDefinition(COOLDOWN_PROBE_ABILITY, AbilitySource.ADMIN),
                "cooldown probe test should learn the definition first");
        ownerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic).setSlots(1);
        ownerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic).setSlot(0, COOLDOWN_PROBE_ABILITY);

        helper.startSequence()
                .thenExecute(() -> ownerData.getAbilityExecutor().executeLoadoutAbility(AbilityGroupId.Basic, 0))
                .thenExecuteAfter(1, () -> {
                    var abilityGroup = ownerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic);
                    var ability = abilityGroup.getExecutionAbilityReference(0);
                    helper.assertTrue(ability != null, "cooldown probe slot should resolve an execution reference");

                    int cooldownTicks = MKCore.getAbilityRuntimeService().getLoadoutCooldownTicks(
                            ownerData,
                            ability,
                            abilityGroup.getExecutionSourceId(0)
                    );
                    helper.assertTrue(cooldownTicks > 0,
                            "loadout execution should mirror abilities2 cooldowns into synced player timers");
                    helper.assertFalse(ownerData.getAbilityExecutor().clientSimulateAbility(AbilityGroupId.Basic, 0),
                            "client loadout simulation should reject definition-backed slots while the synced cooldown timer is active");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void slottedDefinitionClientSimulationRejectsUnaffordableManaCost(GameTestHelper helper) {
        Player owner = createTestPlayer(helper, new BlockPos(1, 2, 1));
        MKPlayerData ownerData = MKCore.getPlayerOrThrow(owner);

        helper.assertTrue(ownerData.getAbilities().learnAbilityDefinition(COST_PROBE_ABILITY, AbilitySource.ADMIN),
                "cost probe test should learn the definition first");
        ownerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic).setSlots(1);
        ownerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic).setSlot(0, COST_PROBE_ABILITY);

        ownerData.getStats().setMana(0.0f);
        helper.assertFalse(ownerData.getAbilityExecutor().clientSimulateAbility(AbilityGroupId.Basic, 0),
                "client loadout simulation should reject definition-backed slots when current mana is below the activation cost");

        ownerData.getStats().setMana(ownerData.getStats().getMaxMana());
        helper.assertTrue(ownerData.getAbilityExecutor().clientSimulateAbility(AbilityGroupId.Basic, 0),
                "client loadout simulation should allow definition-backed slots again once the mana cost is affordable");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void slottedFriendlyDefinitionUsesLookedAtFriendlyTarget(GameTestHelper helper) {
        Player owner = createTestPlayer(helper, new BlockPos(1, 2, 1));
        Player ally = createTestPlayer(helper, new BlockPos(1, 2, 4));
        MKPlayerData ownerData = MKCore.getPlayerOrThrow(owner);

        helper.assertTrue(ownerData.getAbilities().learnAbilityDefinition(FRIENDLY_HEAL_ABILITY, AbilitySource.ADMIN),
                "friendly heal loadout test should learn the definition first");
        ownerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic).setSlots(1);
        ownerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic).setSlot(0, FRIENDLY_HEAL_ABILITY);

        owner.setHealth(owner.getMaxHealth() - 4.0f);
        ally.setHealth(ally.getMaxHealth() - 8.0f);
        float ownerStartingHealth = owner.getHealth();
        float allyStartingHealth = ally.getHealth();
        lookAtEntity(owner, ally);

        InvocationResult result = MKCore.getAbilityRuntimeService().executeLoadoutAbility(
                ownerData,
                ownerData,
                AbilityGroupId.Basic,
                FRIENDLY_HEAL_ABILITY
        );
        helper.assertTrue(result.started(), "friendly-targeted loadout execution should start with a looked-at ally");
        helper.assertValueEqual(owner.getHealth(), ownerStartingHealth,
                "friendly-targeted loadout execution should not fall back to self while an allied target is selected");
        helper.assertTrue(ally.getHealth() > allyStartingHealth,
                "friendly-targeted loadout execution should heal the looked-at allied target");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void slottedEnemyDefinitionClientSimulationRequiresLookedAtEnemy(GameTestHelper helper) {
        Player owner = createTestPlayer(helper, new BlockPos(1, 2, 1));
        Player friendly = createTestPlayer(helper, new BlockPos(1, 2, 4));
        Zombie enemy = createTestZombie(helper, new BlockPos(4, 2, 1));
        MKPlayerData ownerData = MKCore.getPlayerOrThrow(owner);

        helper.assertTrue(ownerData.getAbilities().learnAbilityDefinition(DELAYED_BURST_ABILITY, AbilitySource.ADMIN),
                "enemy loadout simulation test should learn the definition first");
        ownerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic).setSlots(1);
        ownerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic).setSlot(0, DELAYED_BURST_ABILITY);

        lookAtEntity(owner, friendly);
        helper.assertFalse(ownerData.getAbilityExecutor().clientSimulateAbility(AbilityGroupId.Basic, 0),
                "client loadout simulation should reject enemy-targeted definitions when a friendly target is selected");

        lookAtEntity(owner, enemy);
        helper.assertTrue(ownerData.getAbilityExecutor().clientSimulateAbility(AbilityGroupId.Basic, 0),
                "client loadout simulation should allow enemy-targeted definitions when an enemy target is selected");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void slottedEnemyDefinitionExecutesAgainstLookedAtEnemy(GameTestHelper helper) {
        Player owner = createTestPlayer(helper, new BlockPos(1, 2, 1));
        Player friendly = createTestPlayer(helper, new BlockPos(1, 2, 4));
        Zombie enemy = createTestZombie(helper, new BlockPos(4, 2, 1));
        MKPlayerData ownerData = MKCore.getPlayerOrThrow(owner);

        helper.assertTrue(ownerData.getAbilities().learnAbilityDefinition(DELAYED_BURST_ABILITY, AbilitySource.ADMIN),
                "enemy loadout execution test should learn the definition first");
        ownerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic).setSlots(1);
        ownerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic).setSlot(0, DELAYED_BURST_ABILITY);

        float enemyStartingHealth = enemy.getHealth();
        float friendlyStartingHealth = friendly.getHealth();
        lookAtEntity(owner, enemy);

        helper.startSequence()
                .thenExecute(() -> {
                    InvocationResult result = MKCore.getAbilityRuntimeService().executeLoadoutAbility(
                            ownerData,
                            ownerData,
                            AbilityGroupId.Basic,
                            DELAYED_BURST_ABILITY
                    );
                    helper.assertTrue(result.started(),
                            "enemy-targeted loadout execution should start with a looked-at enemy target");
                })
                .thenExecuteAfter(5, () -> {
                    helper.assertTrue(enemy.getHealth() < enemyStartingHealth,
                            "enemy-targeted loadout execution should damage the looked-at enemy target");
                    helper.assertValueEqual(friendly.getHealth(), friendlyStartingHealth,
                            "enemy-targeted loadout execution should not affect a friendly target");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void slottedDefinitionMirrorsGroupScopedGcdTimers(GameTestHelper helper) {
        Player owner = createTestPlayer(helper, new BlockPos(1, 2, 1));
        MKPlayerData ownerData = MKCore.getPlayerOrThrow(owner);

        helper.assertTrue(ownerData.getAbilities().learnAbilityDefinition(GCD_PROBE_SHARED_A_ABILITY, AbilitySource.ADMIN),
                "shared gcd probe A should be learned first");
        helper.assertTrue(ownerData.getAbilities().learnAbilityDefinition(GCD_PROBE_SHARED_B_ABILITY, AbilitySource.ADMIN),
                "shared gcd probe B should be learned first");
        helper.assertTrue(ownerData.getAbilities().learnAbilityDefinition(GCD_PROBE_OTHER_ABILITY, AbilitySource.ADMIN),
                "other gcd probe should be learned first");
        ownerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic).setSlots(3);
        ownerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic).setSlot(0, GCD_PROBE_SHARED_A_ABILITY);
        ownerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic).setSlot(1, GCD_PROBE_SHARED_B_ABILITY);
        ownerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic).setSlot(2, GCD_PROBE_OTHER_ABILITY);

        helper.startSequence()
                .thenExecute(() -> ownerData.getAbilityExecutor().executeLoadoutAbility(AbilityGroupId.Basic, 0))
                .thenExecuteAfter(1, () -> {
                    var abilityGroup = ownerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic);
                    var sharedA = abilityGroup.getExecutionAbilityReference(0);
                    helper.assertTrue(sharedA != null, "shared gcd probe A should resolve an execution reference");

                    int gcdTicks = MKCore.getAbilityRuntimeService().getLoadoutGcdTicks(
                            ownerData,
                            AbilityGroupId.Basic,
                            sharedA
                    );
                    helper.assertTrue(gcdTicks > 0,
                            "loadout execution should mirror abilities2 gcd groups into synced player timers");
                    helper.assertFalse(ownerData.getAbilityExecutor().clientSimulateAbility(AbilityGroupId.Basic, 1),
                            "client simulation should reject another definition in the same synced gcd group");
                    helper.assertTrue(ownerData.getAbilityExecutor().clientSimulateAbility(AbilityGroupId.Basic, 2),
                            "client simulation should still allow a definition in a different synced gcd group");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void slottedDefinitionClientSimulationRejectsWhileAnotherDefinitionCastIsPending(GameTestHelper helper) {
        Player owner = createTestPlayer(helper, new BlockPos(1, 2, 1));
        MKPlayerData ownerData = MKCore.getPlayerOrThrow(owner);

        helper.assertTrue(ownerData.getAbilities().learnAbilityDefinition(SELF_HEAL_ABILITY, AbilitySource.ADMIN),
                "busy-state probe should learn the cast-time definition first");
        helper.assertTrue(ownerData.getAbilities().learnAbilityDefinition(COST_PROBE_ABILITY, AbilitySource.ADMIN),
                "busy-state probe should learn the follow-up definition first");
        ownerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic).setSlots(2);
        ownerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic).setSlot(0, SELF_HEAL_ABILITY);
        ownerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic).setSlot(1, COST_PROBE_ABILITY);

        owner.setHealth(owner.getMaxHealth() - 8.0f);
        ownerData.getStats().setMana(ownerData.getStats().getMaxMana());
        float startingHealth = owner.getHealth();

        helper.startSequence()
                .thenExecute(() -> ownerData.getAbilityExecutor().executeLoadoutAbility(AbilityGroupId.Basic, 0))
                .thenExecuteAfter(1, () -> {
                    helper.assertTrue(MKCore.getAbilityRuntimeService().hasPendingActivation(ownerData),
                            "abilities2 cast-time activation should register as a pending runtime cast");
                    helper.assertFalse(ownerData.getAbilityExecutor().clientSimulateAbility(AbilityGroupId.Basic, 1),
                            "client simulation should reject another definition-backed slot while an abilities2 cast is pending");
                    InvocationResult busyResult = MKCore.getAbilityRuntimeService().executeLoadoutAbility(
                            ownerData,
                            ownerData,
                            AbilityGroupId.Basic,
                            COST_PROBE_ABILITY
                    );
                    helper.assertFalse(busyResult.started(),
                            "server-side loadout execution should also reject another definition while an abilities2 cast is pending");
                    helper.assertValueEqual(busyResult.failureReason(), FailureReason.BUSY,
                            "definition-backed loadout busy failure reason");
                })
                .thenExecuteAfter(25, () -> {
                    helper.assertTrue(owner.getHealth() > startingHealth,
                            "the original cast-time definition should still complete once the pending cast finishes");
                    helper.assertFalse(MKCore.getAbilityRuntimeService().hasPendingActivation(ownerData),
                            "the pending cast should clear once the original definition finishes");
                    ownerData.getStats().setMana(ownerData.getStats().getMaxMana());
                    InvocationResult readyResult = MKCore.getAbilityRuntimeService().executeLoadoutAbility(
                            ownerData,
                            ownerData,
                            AbilityGroupId.Basic,
                            COST_PROBE_ABILITY
                    );
                    helper.assertTrue(readyResult.started(),
                            "the second definition should become executable again once the pending cast clears");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void slottedDefinitionExecutionRejectsWhileLegacyCastIsPending(GameTestHelper helper) {
        Player owner = createTestPlayer(helper, new BlockPos(1, 2, 1));
        MKPlayerData ownerData = MKCore.getPlayerOrThrow(owner);

        helper.assertTrue(ownerData.getAbilities().learnAbilityDefinition(COST_PROBE_ABILITY, AbilitySource.ADMIN),
                "legacy busy-state probe should learn the abilities2 definition first");
        helper.assertTrue(ownerData.getAbilities().learnAbility(MKTestAbilities.TEST_HEAL.get(), AbilitySource.ADMIN),
                "legacy busy-state probe should learn the legacy heal first");
        ownerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic).setSlots(1);
        ownerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic).setSlot(0, COST_PROBE_ABILITY);
        ownerData.getStats().setMana(ownerData.getStats().getMaxMana());

        helper.startSequence()
                .thenExecute(() -> ownerData.getAbilityExecutor().executeAbility(MKTestAbilities.TEST_HEAL.get().getAbilityId()))
                .thenExecuteAfter(1, () -> {
                    helper.assertTrue(ownerData.getAbilityExecutor().isCasting(),
                            "legacy heal should be in a casting state for the cross-runtime busy check");
                    InvocationResult result = MKCore.getAbilityRuntimeService().executeLoadoutAbility(
                            ownerData,
                            ownerData,
                            AbilityGroupId.Basic,
                            COST_PROBE_ABILITY
                    );
                    helper.assertFalse(result.started(),
                            "definition-backed loadout execution should reject while a legacy cast is pending");
                    helper.assertValueEqual(result.failureReason(), FailureReason.BUSY,
                            "definition-backed loadout execution failure reason");
                    helper.assertFalse(ownerData.getAbilityExecutor().clientSimulateAbility(AbilityGroupId.Basic, 0),
                            "client simulation should also reject a definition-backed slot while a legacy cast is pending");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void legacyAbilityActivationRejectsWhileDefinitionCastIsPending(GameTestHelper helper) {
        Player owner = createTestPlayer(helper, new BlockPos(1, 2, 1));
        MKPlayerData ownerData = MKCore.getPlayerOrThrow(owner);

        helper.assertTrue(ownerData.getAbilities().learnAbilityDefinition(SELF_HEAL_ABILITY, AbilitySource.ADMIN),
                "cross-runtime busy probe should learn the abilities2 definition first");
        helper.assertTrue(ownerData.getAbilities().learnAbility(MKTestAbilities.TEST_HEAL.get(), AbilitySource.ADMIN),
                "cross-runtime busy probe should learn the legacy heal first");
        ownerData.getStats().setMana(ownerData.getStats().getMaxMana());

        InvocationResult result = MKCore.getAbilityRuntimeService().getEngine().activate(new ActivationRequest(
                ownerData,
                ownerData,
                new AbilityReference(SELF_HEAL_ABILITY, null),
                "cast",
                null,
                null,
                null,
                false,
                false
        ));
        helper.assertTrue(result.started(), "abilities2 cast-time activation should start for the busy-state probe");

        helper.startSequence()
                .thenExecuteAfter(1, () -> {
                    helper.assertTrue(MKCore.getAbilityRuntimeService().hasPendingActivation(ownerData),
                            "abilities2 cast-time activation should still be pending during the legacy cross-check");
                    helper.assertFalse(ownerData.getAbilityExecutor().canActivateAbility(
                                    ownerData.getAbilities().getAbilityInfo(MKTestAbilities.TEST_HEAL.get().getAbilityId())),
                            "legacy ability activation should reject while an abilities2 cast is pending");
                })
                .thenExecuteAfter(25, () -> {
                    helper.assertFalse(MKCore.getAbilityRuntimeService().hasPendingActivation(ownerData),
                            "abilities2 pending cast should clear once the cast completes");
                    ownerData.getStats().setMana(ownerData.getStats().getMaxMana());
                    helper.assertTrue(ownerData.getAbilityExecutor().canActivateAbility(
                                    ownerData.getAbilities().getAbilityInfo(MKTestAbilities.TEST_HEAL.get().getAbilityId())),
                            "legacy ability activation should be allowed again once the abilities2 cast completes");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void blockingInterruptsDefinitionCastBeforeCompletion(GameTestHelper helper) {
        Player owner = createTestPlayer(helper, new BlockPos(1, 2, 1));
        MKPlayerData ownerData = MKCore.getPlayerOrThrow(owner);

        owner.setHealth(owner.getMaxHealth() - 8.0f);
        float startingHealth = owner.getHealth();

        InvocationResult result = MKCore.getAbilityRuntimeService().getEngine().activate(new ActivationRequest(
                ownerData,
                ownerData,
                new AbilityReference(SELF_HEAL_ABILITY, null),
                "cast",
                null,
                null,
                null,
                false,
                false
        ));
        helper.assertTrue(result.started(), "abilities2 cast-time activation should start for the blocking interrupt probe");

        helper.startSequence()
                .thenExecuteAfter(1, () -> {
                    helper.assertTrue(MKCore.getAbilityRuntimeService().hasPendingActivation(ownerData),
                            "abilities2 cast should be pending before the blocking interrupt starts");
                    beginBlocking(owner);
                })
                .thenExecuteAfter(1, () -> {
                    helper.assertFalse(MKCore.getAbilityRuntimeService().hasPendingActivation(ownerData),
                            "starting to block should interrupt pending abilities2 casts");
                })
                .thenExecuteAfter(25, () -> {
                    helper.assertTrue(owner.getHealth() < owner.getMaxHealth(),
                            "interrupted abilities2 cast should not complete its full heal after blocking");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void movingInterruptsDefinitionCastBeforeCompletion(GameTestHelper helper) {
        Player owner = createTestPlayer(helper, new BlockPos(1, 2, 1));
        MKPlayerData ownerData = MKCore.getPlayerOrThrow(owner);

        owner.setHealth(owner.getMaxHealth() - 8.0f);
        float startingHealth = owner.getHealth();

        InvocationResult result = MKCore.getAbilityRuntimeService().getEngine().activate(new ActivationRequest(
                ownerData,
                ownerData,
                new AbilityReference(SELF_HEAL_ABILITY, null),
                "cast",
                null,
                null,
                null,
                false,
                false
        ));
        helper.assertTrue(result.started(), "abilities2 cast-time activation should start for the movement interrupt probe");

        helper.startSequence()
                .thenExecuteAfter(1, () -> {
                    helper.assertTrue(MKCore.getAbilityRuntimeService().hasPendingActivation(ownerData),
                            "abilities2 cast should be pending before the movement interrupt starts");
                    owner.moveTo(owner.getX() + 0.5, owner.getY(), owner.getZ(), owner.getYRot(), owner.getXRot());
                })
                .thenExecuteAfter(1, () -> helper.assertFalse(MKCore.getAbilityRuntimeService().hasPendingActivation(ownerData),
                        "moving beyond the interrupt threshold should cancel pending abilities2 casts"))
                .thenExecuteAfter(25, () -> {
                    helper.assertTrue(owner.getHealth() < owner.getMaxHealth(),
                            "movement-interrupted abilities2 cast should not complete its full heal");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void jumpingInterruptsDefinitionCastBeforeCompletion(GameTestHelper helper) {
        Player owner = createTestPlayer(helper, new BlockPos(1, 2, 1));
        MKPlayerData ownerData = MKCore.getPlayerOrThrow(owner);

        owner.setHealth(owner.getMaxHealth() - 8.0f);

        InvocationResult result = MKCore.getAbilityRuntimeService().getEngine().activate(new ActivationRequest(
                ownerData,
                ownerData,
                new AbilityReference(SELF_HEAL_ABILITY, null),
                "cast",
                null,
                null,
                null,
                false,
                false
        ));
        helper.assertTrue(result.started(), "abilities2 cast-time activation should start for the jump interrupt probe");

        helper.startSequence()
                .thenExecuteAfter(1, () -> {
                    helper.assertTrue(MKCore.getAbilityRuntimeService().hasPendingActivation(ownerData),
                            "abilities2 cast should be pending before the jump interrupt starts");
                    MKCore.getAbilityRuntimeService().onLivingJump(new LivingEvent.LivingJumpEvent(owner));
                })
                .thenExecuteAfter(1, () -> helper.assertFalse(MKCore.getAbilityRuntimeService().hasPendingActivation(ownerData),
                        "jumping should interrupt pending abilities2 casts"))
                .thenExecuteAfter(25, () -> {
                    helper.assertTrue(owner.getHealth() < owner.getMaxHealth(),
                            "jump-interrupted abilities2 cast should not complete its full heal");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void stunInterruptsDefinitionCastBeforeCompletion(GameTestHelper helper) {
        Player owner = createTestPlayer(helper, new BlockPos(1, 2, 1));
        MKPlayerData ownerData = MKCore.getPlayerOrThrow(owner);

        owner.setHealth(owner.getMaxHealth() - 8.0f);

        InvocationResult result = MKCore.getAbilityRuntimeService().getEngine().activate(new ActivationRequest(
                ownerData,
                ownerData,
                new AbilityReference(SELF_HEAL_ABILITY, null),
                "cast",
                null,
                null,
                null,
                false,
                false
        ));
        helper.assertTrue(result.started(), "abilities2 cast-time activation should start for the stun interrupt probe");

        helper.startSequence()
                .thenExecuteAfter(1, () -> {
                    helper.assertTrue(MKCore.getAbilityRuntimeService().hasPendingActivation(ownerData),
                            "abilities2 cast should be pending before the stun interrupt starts");
                    ownerData.getEffects().addEffect(CoreEffects.STUN.get().builder(owner).infinite());
                })
                .thenExecuteAfter(1, () -> helper.assertFalse(MKCore.getAbilityRuntimeService().hasPendingActivation(ownerData),
                        "stun should interrupt pending abilities2 casts"))
                .thenExecuteAfter(25, () -> {
                    helper.assertTrue(owner.getHealth() < owner.getMaxHealth(),
                            "stun-interrupted abilities2 cast should not complete its full heal");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void deathInterruptsDefinitionCastBeforeCompletion(GameTestHelper helper) {
        Player owner = createTestPlayer(helper, new BlockPos(1, 2, 1));
        MKPlayerData ownerData = MKCore.getPlayerOrThrow(owner);

        InvocationResult result = MKCore.getAbilityRuntimeService().getEngine().activate(new ActivationRequest(
                ownerData,
                ownerData,
                new AbilityReference(SELF_HEAL_ABILITY, null),
                "cast",
                null,
                null,
                null,
                false,
                false
        ));
        helper.assertTrue(result.started(), "abilities2 cast-time activation should start for the death interrupt probe");

        helper.startSequence()
                .thenExecuteAfter(1, () -> {
                    helper.assertTrue(MKCore.getAbilityRuntimeService().hasPendingActivation(ownerData),
                            "abilities2 cast should be pending before the death interrupt starts");
                    owner.kill();
                })
                .thenExecuteAfter(1, () -> {
                    helper.assertFalse(MKCore.getAbilityRuntimeService().hasPendingActivation(ownerData),
                            "death should clear pending abilities2 casts immediately");
                    helper.assertFalse(owner.isAlive(), "death interrupt probe should leave the caster dead");
                })
                .thenExecuteAfter(10, helper::succeed);
    }

    @GameTest(template = "player_data_phase0")
    public static void movementInterruptReportsSpecificFailureReason(GameTestHelper helper) {
        Player owner = createTestPlayer(helper, new BlockPos(1, 2, 1));
        MKPlayerData ownerData = MKCore.getPlayerOrThrow(owner);
        List<FailureReason> reasons = new ArrayList<>();
        SimpleAbilityEngine engine = createInterruptReasonTestEngine(new SimpleAbilityEngine.LifecycleListener() {
            @Override
            public void onInvocationInterrupted(com.chaosbuffalo.mkcore.abilities2.runtime.AbilityInvocation invocation,
                                                FailureReason failureReason,
                                                int castTicksSpent) {
                reasons.add(failureReason);
            }
        });

        InvocationResult result = engine.activate(new ActivationRequest(
                ownerData,
                ownerData,
                new AbilityReference(INTERRUPT_REASON_PROBE_ABILITY, null),
                "cast",
                null,
                null,
                null,
                false,
                false
        ));
        helper.assertTrue(result.started(), "interrupt reason probe should start for movement");

        helper.startSequence()
                .thenExecuteAfter(1, () -> {
                    helper.assertTrue(engine.hasPendingActivation(ownerData),
                            "interrupt reason probe should be pending before movement");
                    owner.moveTo(owner.getX() + 0.5, owner.getY(), owner.getZ(), owner.getYRot(), owner.getXRot());
                    engine.tickEntity(ownerData);
                })
                .thenExecuteAfter(1, () -> {
                    helper.assertFalse(engine.hasPendingActivation(ownerData),
                            "movement should clear the interrupt reason probe");
                    helper.assertValueEqual(reasons.size(), 1,
                            "movement interrupt should report exactly one failure reason");
                    helper.assertValueEqual(reasons.get(0), FailureReason.INTERRUPTED_BY_MOVE,
                            "movement interrupt should report the move-specific failure reason");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void blockingInterruptReportsSpecificFailureReason(GameTestHelper helper) {
        Player owner = createTestPlayer(helper, new BlockPos(1, 2, 1));
        MKPlayerData ownerData = MKCore.getPlayerOrThrow(owner);
        List<FailureReason> reasons = new ArrayList<>();
        SimpleAbilityEngine engine = createInterruptReasonTestEngine(new SimpleAbilityEngine.LifecycleListener() {
            @Override
            public void onInvocationInterrupted(com.chaosbuffalo.mkcore.abilities2.runtime.AbilityInvocation invocation,
                                                FailureReason failureReason,
                                                int castTicksSpent) {
                reasons.add(failureReason);
            }
        });

        InvocationResult result = engine.activate(new ActivationRequest(
                ownerData,
                ownerData,
                new AbilityReference(INTERRUPT_REASON_PROBE_ABILITY, null),
                "cast",
                null,
                null,
                null,
                false,
                false
        ));
        helper.assertTrue(result.started(), "interrupt reason probe should start for blocking");

        helper.startSequence()
                .thenExecuteAfter(1, () -> {
                    helper.assertTrue(engine.hasPendingActivation(ownerData),
                            "interrupt reason probe should be pending before blocking");
                    engine.interruptPendingActivations(ownerData, FailureReason.INTERRUPTED_BY_BLOCK);
                })
                .thenExecuteAfter(1, () -> {
                    helper.assertFalse(engine.hasPendingActivation(ownerData),
                            "blocking should clear the interrupt reason probe");
                    helper.assertValueEqual(reasons.size(), 1,
                            "blocking interrupt should report exactly one failure reason");
                    helper.assertValueEqual(reasons.get(0), FailureReason.INTERRUPTED_BY_BLOCK,
                            "blocking interrupt should report the block-specific failure reason");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void jumpInterruptReportsSpecificFailureReason(GameTestHelper helper) {
        Player owner = createTestPlayer(helper, new BlockPos(1, 2, 1));
        MKPlayerData ownerData = MKCore.getPlayerOrThrow(owner);
        List<FailureReason> reasons = new ArrayList<>();
        SimpleAbilityEngine engine = createInterruptReasonTestEngine(new SimpleAbilityEngine.LifecycleListener() {
            @Override
            public void onInvocationInterrupted(com.chaosbuffalo.mkcore.abilities2.runtime.AbilityInvocation invocation,
                                                FailureReason failureReason,
                                                int castTicksSpent) {
                reasons.add(failureReason);
            }
        });

        InvocationResult result = engine.activate(new ActivationRequest(
                ownerData,
                ownerData,
                new AbilityReference(INTERRUPT_REASON_PROBE_ABILITY, null),
                "cast",
                null,
                null,
                null,
                false,
                false
        ));
        helper.assertTrue(result.started(), "interrupt reason probe should start for jump");

        helper.startSequence()
                .thenExecuteAfter(1, () -> {
                    helper.assertTrue(engine.hasPendingActivation(ownerData),
                            "interrupt reason probe should be pending before jump");
                    engine.interruptPendingActivations(ownerData, FailureReason.INTERRUPTED_BY_JUMP);
                })
                .thenExecuteAfter(1, () -> {
                    helper.assertFalse(engine.hasPendingActivation(ownerData),
                            "jump should clear the interrupt reason probe");
                    helper.assertValueEqual(reasons.size(), 1,
                            "jump interrupt should report exactly one failure reason");
                    helper.assertValueEqual(reasons.get(0), FailureReason.INTERRUPTED_BY_JUMP,
                            "jump interrupt should report the jump-specific failure reason");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void damageInterruptReportsSpecificFailureReason(GameTestHelper helper) {
        Player owner = createTestPlayer(helper, new BlockPos(1, 2, 1));
        MKPlayerData ownerData = MKCore.getPlayerOrThrow(owner);
        List<FailureReason> reasons = new ArrayList<>();
        SimpleAbilityEngine engine = createInterruptReasonTestEngine(new SimpleAbilityEngine.LifecycleListener() {
            @Override
            public void onInvocationInterrupted(com.chaosbuffalo.mkcore.abilities2.runtime.AbilityInvocation invocation,
                                                FailureReason failureReason,
                                                int castTicksSpent) {
                reasons.add(failureReason);
            }
        });

        InvocationResult result = engine.activate(new ActivationRequest(
                ownerData,
                ownerData,
                new AbilityReference(INTERRUPT_REASON_PROBE_ABILITY, null),
                "cast",
                null,
                null,
                null,
                false,
                false
        ));
        helper.assertTrue(result.started(), "interrupt reason probe should start for damage");

        helper.startSequence()
                .thenExecuteAfter(1, () -> {
                    helper.assertTrue(engine.hasPendingActivation(ownerData),
                            "interrupt reason probe should be pending before damage");
                    engine.queueDamageInterrupt(ownerData, 1.0f);
                    engine.tickEntity(ownerData);
                })
                .thenExecuteAfter(1, () -> {
                    helper.assertFalse(engine.hasPendingActivation(ownerData),
                            "damage should clear the interrupt reason probe");
                    helper.assertValueEqual(reasons.size(), 1,
                            "damage interrupt should report exactly one failure reason");
                    helper.assertValueEqual(reasons.get(0), FailureReason.INTERRUPTED_BY_DAMAGE,
                            "damage interrupt should report the damage-specific failure reason");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void stunInterruptReportsSpecificFailureReason(GameTestHelper helper) {
        Player owner = createTestPlayer(helper, new BlockPos(1, 2, 1));
        MKPlayerData ownerData = MKCore.getPlayerOrThrow(owner);
        List<FailureReason> reasons = new ArrayList<>();
        SimpleAbilityEngine engine = createInterruptReasonTestEngine(new SimpleAbilityEngine.LifecycleListener() {
            @Override
            public void onInvocationInterrupted(com.chaosbuffalo.mkcore.abilities2.runtime.AbilityInvocation invocation,
                                                FailureReason failureReason,
                                                int castTicksSpent) {
                reasons.add(failureReason);
            }
        });

        InvocationResult result = engine.activate(new ActivationRequest(
                ownerData,
                ownerData,
                new AbilityReference(INTERRUPT_REASON_PROBE_ABILITY, null),
                "cast",
                null,
                null,
                null,
                false,
                false
        ));
        helper.assertTrue(result.started(), "interrupt reason probe should start for stun");

        helper.startSequence()
                .thenExecuteAfter(1, () -> {
                    helper.assertTrue(engine.hasPendingActivation(ownerData),
                            "interrupt reason probe should be pending before stun");
                    engine.interruptPendingActivations(ownerData, FailureReason.INTERRUPTED_BY_STUN);
                })
                .thenExecuteAfter(1, () -> {
                    helper.assertFalse(engine.hasPendingActivation(ownerData),
                            "stun should clear the interrupt reason probe");
                    helper.assertValueEqual(reasons.size(), 1,
                            "stun interrupt should report exactly one failure reason");
                    helper.assertValueEqual(reasons.get(0), FailureReason.INTERRUPTED_BY_STUN,
                            "stun interrupt should report the stun-specific failure reason");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void deathInterruptReportsSpecificFailureReason(GameTestHelper helper) {
        Player owner = createTestPlayer(helper, new BlockPos(1, 2, 1));
        MKPlayerData ownerData = MKCore.getPlayerOrThrow(owner);
        List<FailureReason> reasons = new ArrayList<>();
        SimpleAbilityEngine engine = createInterruptReasonTestEngine(new SimpleAbilityEngine.LifecycleListener() {
            @Override
            public void onInvocationInterrupted(com.chaosbuffalo.mkcore.abilities2.runtime.AbilityInvocation invocation,
                                                FailureReason failureReason,
                                                int castTicksSpent) {
                reasons.add(failureReason);
            }
        });

        InvocationResult result = engine.activate(new ActivationRequest(
                ownerData,
                ownerData,
                new AbilityReference(INTERRUPT_REASON_PROBE_ABILITY, null),
                "cast",
                null,
                null,
                null,
                false,
                false
        ));
        helper.assertTrue(result.started(), "interrupt reason probe should start for death");

        helper.startSequence()
                .thenExecuteAfter(1, () -> {
                    helper.assertTrue(engine.hasPendingActivation(ownerData),
                            "interrupt reason probe should be pending before death");
                    owner.kill();
                    engine.tickEntity(ownerData);
                })
                .thenExecuteAfter(1, () -> {
                    helper.assertFalse(engine.hasPendingActivation(ownerData),
                            "death should clear the interrupt reason probe");
                    helper.assertFalse(owner.isAlive(), "death interrupt probe should leave the caster dead");
                    helper.assertValueEqual(reasons.size(), 1,
                            "death interrupt should report exactly one failure reason");
                    helper.assertValueEqual(reasons.get(0), FailureReason.INTERRUPTED_BY_DEATH,
                            "death interrupt should report the death-specific failure reason");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void logoutInterruptReportsSpecificFailureReason(GameTestHelper helper) {
        Player owner = createTestPlayer(helper, new BlockPos(1, 2, 1));
        MKPlayerData ownerData = MKCore.getPlayerOrThrow(owner);
        List<FailureReason> reasons = new ArrayList<>();
        SimpleAbilityEngine engine = createInterruptReasonTestEngine(new SimpleAbilityEngine.LifecycleListener() {
            @Override
            public void onInvocationInterrupted(com.chaosbuffalo.mkcore.abilities2.runtime.AbilityInvocation invocation,
                                                FailureReason failureReason,
                                                int castTicksSpent) {
                reasons.add(failureReason);
            }
        });

        InvocationResult result = engine.activate(new ActivationRequest(
                ownerData,
                ownerData,
                new AbilityReference(INTERRUPT_REASON_PROBE_ABILITY, null),
                "cast",
                null,
                null,
                null,
                false,
                false
        ));
        helper.assertTrue(result.started(), "interrupt reason probe should start for logout");

        helper.startSequence()
                .thenExecuteAfter(1, () -> {
                    helper.assertTrue(engine.hasPendingActivation(ownerData),
                            "interrupt reason probe should be pending before logout");
                    engine.interruptPendingActivations(ownerData, FailureReason.INTERRUPTED_BY_LOGOUT);
                })
                .thenExecuteAfter(1, () -> {
                    helper.assertFalse(engine.hasPendingActivation(ownerData),
                            "logout should clear the interrupt reason probe");
                    helper.assertValueEqual(reasons.size(), 1,
                            "logout interrupt should report exactly one failure reason");
                    helper.assertValueEqual(reasons.get(0), FailureReason.INTERRUPTED_BY_LOGOUT,
                            "logout interrupt should report the logout-specific failure reason");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void unloadInterruptReportsSpecificFailureReason(GameTestHelper helper) {
        Player owner = createTestPlayer(helper, new BlockPos(1, 2, 1));
        MKPlayerData ownerData = MKCore.getPlayerOrThrow(owner);
        List<FailureReason> reasons = new ArrayList<>();
        SimpleAbilityEngine engine = createInterruptReasonTestEngine(new SimpleAbilityEngine.LifecycleListener() {
            @Override
            public void onInvocationInterrupted(com.chaosbuffalo.mkcore.abilities2.runtime.AbilityInvocation invocation,
                                                FailureReason failureReason,
                                                int castTicksSpent) {
                reasons.add(failureReason);
            }
        });

        InvocationResult result = engine.activate(new ActivationRequest(
                ownerData,
                ownerData,
                new AbilityReference(INTERRUPT_REASON_PROBE_ABILITY, null),
                "cast",
                null,
                null,
                null,
                false,
                false
        ));
        helper.assertTrue(result.started(), "interrupt reason probe should start for unload");

        helper.startSequence()
                .thenExecuteAfter(1, () -> {
                    helper.assertTrue(engine.hasPendingActivation(ownerData),
                            "interrupt reason probe should be pending before unload");
                    engine.interruptPendingActivations(ownerData, FailureReason.INTERRUPTED_BY_UNLOAD);
                })
                .thenExecuteAfter(1, () -> {
                    helper.assertFalse(engine.hasPendingActivation(ownerData),
                            "unload should clear the interrupt reason probe");
                    helper.assertValueEqual(reasons.size(), 1,
                            "unload interrupt should report exactly one failure reason");
                    helper.assertValueEqual(reasons.get(0), FailureReason.INTERRUPTED_BY_UNLOAD,
                            "unload interrupt should report the unload-specific failure reason");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void slottedBasicToggleDefinitionUsesLoadoutTogglePath(GameTestHelper helper) {
        Player owner = createTestPlayer(helper, new BlockPos(1, 2, 1));
        MKPlayerData ownerData = MKCore.getPlayerOrThrow(owner);

        helper.assertTrue(ownerData.getAbilities().learnAbilityDefinition(RESTORING_AURA_ABILITY, AbilitySource.ADMIN),
                "slotted toggle runtime test should learn the definition first");
        ownerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic).setSlots(1);
        ownerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic).setSlot(0, RESTORING_AURA_ABILITY);

        owner.setHealth(owner.getMaxHealth() - 4.0f);
        float startingHealth = owner.getHealth();

        helper.startSequence()
                .thenExecute(() -> ownerData.getAbilityExecutor().executeLoadoutAbility(AbilityGroupId.Basic, 0))
                .thenExecuteAfter(2, () -> {
                    helper.assertTrue(owner.getHealth() > startingHealth,
                            "loadout execution should route toggle abilities through the abilities2 toggle runtime");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void serializedDefinitionCooldownBlocksReactivationAfterJoin(GameTestHelper helper) {
        Player source = createTestPlayer(helper, new BlockPos(1, 2, 1));
        MKPlayerData sourceData = MKCore.getPlayerOrThrow(source);

        helper.assertTrue(sourceData.getAbilities().learnAbilityDefinition(COOLDOWN_PROBE_ABILITY, AbilitySource.ADMIN),
                "serialized cooldown restore test should learn the cooldown probe first");
        sourceData.getLoadout().getAbilityGroup(AbilityGroupId.Basic).setSlots(1);
        sourceData.getLoadout().getAbilityGroup(AbilityGroupId.Basic).setSlot(0, COOLDOWN_PROBE_ABILITY);

        Player[] restoredHolder = new Player[1];
        helper.startSequence()
                .thenExecute(() -> {
                    InvocationResult initialResult = MKCore.getAbilityRuntimeService().executeLoadoutAbility(
                            sourceData,
                            sourceData,
                            AbilityGroupId.Basic,
                            COOLDOWN_PROBE_ABILITY
                    );
                    helper.assertTrue(initialResult.started(),
                            "initial cooldown probe execution should start before serialization");
                    PersistedAbilityRuntimeState sourceSnapshot = MKCore.getAbilityRuntimeService()
                            .capturePersonaRuntime(sourceData.getPersonaManager().getActivePersona());
                    helper.assertTrue(sourceSnapshot.cooldowns().stream()
                                    .anyMatch(entry -> COOLDOWN_PROBE_ABILITY.equals(entry.abilityId()) && "ability".equals(entry.key())),
                            "source player should build a live abilities2 cooldown entry before serialization");
                    restoredHolder[0] = createDeserializedTestPlayer(
                            helper,
                            new BlockPos(3, 2, 1),
                            serializePlayerData(sourceData),
                            sourceData.getEntity().registryAccess()
                    );
                })
                .thenExecuteAfter(5, () -> {
                    MKPlayerData restoredData = MKCore.getPlayerOrThrow(restoredHolder[0]);
                    PersistedAbilityRuntimeState restoredSnapshot = MKCore.getAbilityRuntimeService()
                            .capturePersonaRuntime(restoredData.getPersonaManager().getActivePersona());
                    helper.assertTrue(restoredSnapshot.cooldowns().stream()
                                    .anyMatch(entry -> COOLDOWN_PROBE_ABILITY.equals(entry.abilityId()) && "ability".equals(entry.key())),
                            "restored player should rebuild the abilities2 cooldown entry in live runtime state");
                    InvocationResult restoredResult = MKCore.getAbilityRuntimeService().executeLoadoutAbility(
                            restoredData,
                            restoredData,
                            AbilityGroupId.Basic,
                            COOLDOWN_PROBE_ABILITY
                    );
                    helper.assertFalse(restoredResult.started(),
                            "restored player should still be server-gated by the serialized abilities2 cooldown");
                    helper.assertValueEqual(restoredResult.failureReason(), FailureReason.ON_COOLDOWN,
                            "serialized abilities2 cooldown restore failure reason");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void serializedToggleDefinitionRestoresAuraPulseAfterJoin(GameTestHelper helper) {
        Player source = createTestPlayer(helper, new BlockPos(1, 2, 1));
        MKPlayerData sourceData = MKCore.getPlayerOrThrow(source);

        helper.assertTrue(sourceData.getAbilities().learnAbilityDefinition(RESTORING_AURA_ABILITY, AbilitySource.ADMIN),
                "serialized toggle restore test should learn the restoring aura first");
        sourceData.getLoadout().getAbilityGroup(AbilityGroupId.Basic).setSlots(1);
        sourceData.getLoadout().getAbilityGroup(AbilityGroupId.Basic).setSlot(0, RESTORING_AURA_ABILITY);

        Player[] restoredHolder = new Player[1];
        float[] restoredStartingHealth = new float[1];
        helper.startSequence()
                .thenExecute(() -> {
                    source.setHealth(source.getMaxHealth() - 6.0f);
                    InvocationResult enableResult = MKCore.getAbilityRuntimeService().executeLoadoutAbility(
                            sourceData,
                            sourceData,
                            AbilityGroupId.Basic,
                            RESTORING_AURA_ABILITY
                    );
                    helper.assertTrue(enableResult.started(),
                            "restoring aura should enable before serialization");
                })
                .thenExecuteAfter(35, () -> {
                    Player restored = createDeserializedTestPlayer(
                            helper,
                            new BlockPos(3, 2, 1),
                            serializePlayerData(sourceData),
                            sourceData.getEntity().registryAccess()
                    );
                    restored.setHealth(restored.getMaxHealth() - 4.0f);
                    restoredHolder[0] = restored;
                    restoredStartingHealth[0] = restored.getHealth();
                })
                .thenExecuteAfter(10, () -> {
                    helper.assertTrue(restoredHolder[0].getHealth() > restoredStartingHealth[0],
                            "serialized toggle restore should resume aura pulses after the restored player joins");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void serializedPendingDefinitionCastCompletesAfterJoin(GameTestHelper helper) {
        Player source = createTestPlayer(helper, new BlockPos(1, 2, 1));
        MKPlayerData sourceData = MKCore.getPlayerOrThrow(source);

        Player[] restoredHolder = new Player[1];
        float[] restoredStartingHealth = new float[1];
        helper.startSequence()
                .thenExecute(() -> {
                    InvocationResult result = MKCore.getAbilityRuntimeService().getEngine().activate(new ActivationRequest(
                            sourceData,
                            sourceData,
                            new AbilityReference(SELF_HEAL_ABILITY, null),
                            "cast",
                            null,
                            null,
                            null,
                            false,
                            false
                    ));
                    helper.assertTrue(result.started(), "serialized cast restore probe should start the cast-time activation");
                    helper.assertTrue(MKCore.getAbilityRuntimeService().hasPendingActivation(sourceData),
                            "serialized cast restore probe should snapshot a live pending cast");

                    Player restored = createDeserializedTestPlayer(
                            helper,
                            new BlockPos(3, 2, 1),
                            serializePlayerData(sourceData),
                            sourceData.getEntity().registryAccess()
                    );
                    restored.setHealth(restored.getMaxHealth() - 8.0f);
                    restoredHolder[0] = restored;
                    restoredStartingHealth[0] = restored.getHealth();
                })
                .thenExecuteAfter(2, () -> {
                    MKPlayerData restoredData = MKCore.getPlayerOrThrow(restoredHolder[0]);
                    helper.assertTrue(MKCore.getAbilityRuntimeService().hasPendingActivation(restoredData),
                            "serialized cast restore should rebuild the pending cast runtime after join");
                })
                .thenExecuteAfter(25, () -> {
                    MKPlayerData restoredData = MKCore.getPlayerOrThrow(restoredHolder[0]);
                    helper.assertTrue(restoredHolder[0].getHealth() > restoredStartingHealth[0],
                            "serialized cast restore should let the pending cast complete after the restored player joins");
                    helper.assertFalse(MKCore.getAbilityRuntimeService().hasPendingActivation(restoredData),
                            "serialized cast restore should clear the pending cast after completion");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void serializedPendingDefinitionCastForNonPlayerCompletesAfterJoin(GameTestHelper helper) {
        Zombie source = createTestZombie(helper, new BlockPos(1, 2, 1));
        MKEntityData sourceData = MKCore.getEntitySpecificData(source).orElseThrow();
        helper.assertTrue(sourceData.getAbilities().learnAbilityDefinition(SELF_HEAL_ABILITY, 1, null),
                "serialized non-player cast restore test should learn the cast-time definition first");

        Zombie[] restoredHolder = new Zombie[1];
        float[] restoredStartingHealth = new float[1];
        helper.startSequence()
                .thenExecute(() -> {
                    source.setHealth(source.getMaxHealth() - 8.0f);
                    InvocationResult result = MKCore.getAbilityRuntimeService().getEngine().activate(new ActivationRequest(
                            sourceData,
                            sourceData,
                            new AbilityReference(SELF_HEAL_ABILITY, null),
                            "cast",
                            null,
                            null,
                            null,
                            false,
                            false
                    ));
                    helper.assertTrue(result.started(),
                            "serialized non-player cast restore probe should start the cast-time activation");
                    helper.assertTrue(MKCore.getAbilityRuntimeService().hasPendingActivation(sourceData),
                            "serialized non-player cast restore probe should snapshot a live pending cast");

                    CompoundTag serialized = serializeEntityData(sourceData);
                    UUID sourceId = source.getUUID();
                    source.discard();

                    Zombie restored = createDeserializedTestZombie(
                            helper,
                            new BlockPos(3, 2, 1),
                            sourceId,
                            serialized,
                            sourceData.getEntity().registryAccess()
                    );
                    restored.setHealth(restored.getMaxHealth() - 8.0f);
                    restoredHolder[0] = restored;
                    restoredStartingHealth[0] = restored.getHealth();
                })
                .thenExecuteAfter(2, () -> {
                    MKEntityData restoredData = MKCore.getEntitySpecificData(restoredHolder[0]).orElseThrow();
                    helper.assertTrue(MKCore.getAbilityRuntimeService().hasPendingActivation(restoredData),
                            "serialized non-player cast restore should rebuild the pending cast runtime after join");
                })
                .thenExecuteAfter(25, () -> {
                    MKEntityData restoredData = MKCore.getEntitySpecificData(restoredHolder[0]).orElseThrow();
                    helper.assertTrue(restoredHolder[0].getHealth() > restoredStartingHealth[0],
                            "serialized non-player cast restore should let the pending cast complete after join");
                    helper.assertFalse(MKCore.getAbilityRuntimeService().hasPendingActivation(restoredData),
                            "serialized non-player cast restore should clear the pending cast after completion");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void definitionCastUpdatesAnimationModule(GameTestHelper helper) {
        Player owner = createTestPlayer(helper, new BlockPos(1, 2, 1));
        MKPlayerData ownerData = MKCore.getPlayerOrThrow(owner);

        InvocationResult result = MKCore.getAbilityRuntimeService().getEngine().activate(new ActivationRequest(
                ownerData,
                ownerData,
                new AbilityReference(SELF_HEAL_ABILITY, null),
                "cast",
                null,
                null,
                null,
                false,
                false
        ));
        helper.assertTrue(result.started(), "animation probe should start the cast-time definition");
        helper.assertValueEqual(ownerData.getAnimationModule().getVisualCastState(),
                EntityAnimationModule.VisualCastState.CASTING,
                "abilities2 cast should enter the server-side casting animation state");

        helper.startSequence()
                .thenExecuteAfter(25, () -> helper.assertValueEqual(
                        ownerData.getAnimationModule().getVisualCastState(),
                        EntityAnimationModule.VisualCastState.RELEASE,
                        "abilities2 cast completion should transition into the release animation state"
                ))
                .thenExecuteAfter(20, () -> {
                    helper.assertValueEqual(ownerData.getAnimationModule().getVisualCastState(),
                            EntityAnimationModule.VisualCastState.NONE,
                            "abilities2 release animation should expire after the standard release window");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void serializedPendingDefinitionCastRestoresAnimationModule(GameTestHelper helper) {
        Player source = createTestPlayer(helper, new BlockPos(1, 2, 1));
        MKPlayerData sourceData = MKCore.getPlayerOrThrow(source);

        Player[] restoredHolder = new Player[1];
        helper.startSequence()
                .thenExecute(() -> {
                    InvocationResult result = MKCore.getAbilityRuntimeService().getEngine().activate(new ActivationRequest(
                            sourceData,
                            sourceData,
                            new AbilityReference(SELF_HEAL_ABILITY, null),
                            "cast",
                            null,
                            null,
                            null,
                            false,
                            false
                    ));
                    helper.assertTrue(result.started(), "animation restore probe should start the cast-time definition");
                    helper.assertValueEqual(sourceData.getAnimationModule().getVisualCastState(),
                            EntityAnimationModule.VisualCastState.CASTING,
                            "source player should enter the casting animation before serialization");

                    restoredHolder[0] = createDeserializedTestPlayer(
                            helper,
                            new BlockPos(3, 2, 1),
                            serializePlayerData(sourceData),
                            sourceData.getEntity().registryAccess()
                    );
                })
                .thenExecuteAfter(2, () -> {
                    MKPlayerData restoredData = MKCore.getPlayerOrThrow(restoredHolder[0]);
                    helper.assertValueEqual(restoredData.getAnimationModule().getVisualCastState(),
                            EntityAnimationModule.VisualCastState.CASTING,
                            "restored pending casts should rebuild the casting animation state after join");
                })
                .thenExecuteAfter(25, () -> {
                    MKPlayerData restoredData = MKCore.getPlayerOrThrow(restoredHolder[0]);
                    helper.assertValueEqual(restoredData.getAnimationModule().getVisualCastState(),
                            EntityAnimationModule.VisualCastState.RELEASE,
                            "restored pending casts should still transition into the release animation state");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void serializedPendingDefinitionChannelResumesPulsesAfterJoin(GameTestHelper helper) {
        Player source = createTestPlayer(helper, new BlockPos(1, 2, 1));
        MKPlayerData sourceData = MKCore.getPlayerOrThrow(source);

        Player[] restoredHolder = new Player[1];
        float[] restoredStartingHealth = new float[1];
        helper.startSequence()
                .thenExecute(() -> {
                    InvocationResult result = MKCore.getAbilityRuntimeService().getEngine().activate(new ActivationRequest(
                            sourceData,
                            sourceData,
                            new AbilityReference(MENDING_CHANNEL_ABILITY, null),
                            "cast",
                            null,
                            null,
                            null,
                            false,
                            false
                    ));
                    helper.assertTrue(result.started(), "serialized channel restore probe should start the channel activation");
                    helper.assertTrue(MKCore.getAbilityRuntimeService().hasPendingActivation(sourceData),
                            "serialized channel restore probe should snapshot a live pending channel");

                    Player restored = createDeserializedTestPlayer(
                            helper,
                            new BlockPos(3, 2, 1),
                            serializePlayerData(sourceData),
                            sourceData.getEntity().registryAccess()
                    );
                    restored.setHealth(restored.getMaxHealth() - 6.0f);
                    restoredHolder[0] = restored;
                    restoredStartingHealth[0] = restored.getHealth();
                })
                .thenExecuteAfter(2, () -> {
                    MKPlayerData restoredData = MKCore.getPlayerOrThrow(restoredHolder[0]);
                    helper.assertTrue(MKCore.getAbilityRuntimeService().hasPendingActivation(restoredData),
                            "serialized channel restore should rebuild the pending channel runtime after join");
                    helper.assertTrue(restoredHolder[0].getHealth() == restoredStartingHealth[0],
                            "serialized channel restore should not replay the initial channel entry point on join");
                })
                .thenExecuteAfter(25, () -> {
                    helper.assertTrue(restoredHolder[0].getHealth() > restoredStartingHealth[0],
                            "serialized channel restore should resume periodic channel pulses after the restored player joins");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void serializedAreaCloudDeliveryResumesRemainingPulseDelayAfterJoin(GameTestHelper helper) {
        Player source = createTestPlayer(helper, new BlockPos(1, 2, 1));
        MKPlayerData sourceData = MKCore.getPlayerOrThrow(source);

        source.setHealth(source.getMaxHealth() - 8.0f);
        Player[] restoredHolder = new Player[1];
        float[] restoredStartingHealth = new float[1];
        helper.startSequence()
                .thenExecute(() -> {
                    InvocationResult result = MKCore.getAbilityRuntimeService().getEngine().activate(new ActivationRequest(
                            sourceData,
                            sourceData,
                            new AbilityReference(HEALING_CLOUD_ABILITY, null),
                            "cast",
                            null,
                            null,
                            null,
                            false,
                            false
                    ));
                    helper.assertTrue(result.started(), "serialized cloud restore probe should start the healing cloud");
                })
                .thenExecuteAfter(2, () -> {
                    PersistedAbilityRuntimeState sourceSnapshot = MKCore.getAbilityRuntimeService()
                            .capturePersonaRuntime(sourceData.getPersonaManager().getActivePersona());
                    helper.assertTrue(sourceSnapshot.deliveries().stream()
                                    .anyMatch(entry -> entry.abilityId().equals(HEALING_CLOUD_ABILITY)
                                            && entry.kind() == DeliveryKind.AREA_CLOUD
                                            && entry.ticksUntilNextGroundTick() == 2),
                            "serialized cloud restore probe should snapshot the remaining cloud pulse delay");

                    CompoundTag serialized = serializePlayerData(sourceData);
                    source.discard();

                    Player restored = createDeserializedTestPlayer(
                            helper,
                            new BlockPos(1, 2, 1),
                            serialized,
                            sourceData.getEntity().registryAccess()
                    );
                    restored.setHealth(restored.getMaxHealth() - 8.0f);
                    restoredHolder[0] = restored;
                    restoredStartingHealth[0] = restored.getHealth();
                })
                .thenExecuteAfter(1, () -> {
                    MKPlayerData restoredData = MKCore.getPlayerOrThrow(restoredHolder[0]);
                    PersistedAbilityRuntimeState restoredSnapshot = MKCore.getAbilityRuntimeService()
                            .capturePersonaRuntime(restoredData.getPersonaManager().getActivePersona());
                    helper.assertTrue(restoredSnapshot.deliveries().stream()
                                    .anyMatch(entry -> entry.abilityId().equals(HEALING_CLOUD_ABILITY)
                                            && entry.kind() == DeliveryKind.AREA_CLOUD
                                            && entry.ticksUntilNextGroundTick() == 1),
                            "serialized cloud restore should rebuild the area cloud runtime with the remaining pulse delay");
                    helper.assertValueEqual(restoredHolder[0].getHealth(), restoredStartingHealth[0],
                            "serialized cloud restore should not replay the cloud pulse early on join");
                })
                .thenExecuteAfter(1, () -> {
                    helper.assertTrue(restoredHolder[0].getHealth() > restoredStartingHealth[0],
                            "serialized cloud restore should resume the next cloud pulse using the remaining delay");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void serializedProjectileDeliveryRestoresMidFlightImpactCallbacksAfterJoin(GameTestHelper helper) {
        Player source = createTestPlayer(helper, new BlockPos(1, 2, 1));
        Zombie target = createTestZombie(helper, new BlockPos(12, 2, 1));
        MKPlayerData sourceData = MKCore.getPlayerOrThrow(source);
        float startingHealth = target.getHealth();

        Player[] restoredHolder = new Player[1];
        helper.startSequence()
                .thenExecute(() -> {
                    InvocationResult result = MKCore.getAbilityRuntimeService().activateAiAbility(
                            sourceData,
                            sourceData,
                            new AbilityReference(AI_FIREBOLT_ABILITY, null),
                            "cast",
                            new AbilityResolvedTargets(target.getUUID(), List.of(target.getUUID()), null, null, null)
                    );
                    helper.assertTrue(result.started(), "serialized projectile restore probe should start the AI firebolt");
                })
                .thenExecuteAfter(1, () -> {
                    PersistedAbilityRuntimeState sourceSnapshot = MKCore.getAbilityRuntimeService()
                            .capturePersonaRuntime(sourceData.getPersonaManager().getActivePersona());
                    helper.assertTrue(sourceSnapshot.deliveries().stream()
                                    .anyMatch(entry -> entry.abilityId().equals(AI_FIREBOLT_ABILITY)
                                            && entry.kind() == DeliveryKind.PROJECTILE
                                            && entry.velocity() != null),
                            "serialized projectile restore probe should snapshot the mid-flight projectile delivery");

                    AbilityProjectileEntity projectile = findProjectile(helper, source);
                    CompoundTag serialized = serializePlayerData(sourceData);
                    projectile.discard();
                    source.discard();

                    restoredHolder[0] = createDeserializedTestPlayer(
                            helper,
                            new BlockPos(1, 2, 1),
                            serialized,
                            sourceData.getEntity().registryAccess()
                    );
                })
                .thenExecuteAfter(1, () -> {
                    MKPlayerData restoredData = MKCore.getPlayerOrThrow(restoredHolder[0]);
                    PersistedAbilityRuntimeState restoredSnapshot = MKCore.getAbilityRuntimeService()
                            .capturePersonaRuntime(restoredData.getPersonaManager().getActivePersona());
                    helper.assertTrue(restoredSnapshot.deliveries().stream()
                                    .anyMatch(entry -> entry.abilityId().equals(AI_FIREBOLT_ABILITY)
                                            && entry.kind() == DeliveryKind.PROJECTILE
                                            && entry.velocity() != null),
                            "serialized projectile restore should rebuild the mid-flight projectile runtime after join");
                    helper.assertValueEqual(target.getHealth(), startingHealth,
                            "serialized projectile restore should not replay the impact early on join");

                    AbilityProjectileEntity restoredProjectile = findProjectile(helper, restoredHolder[0]);
                    boolean handled = MKCore.getAbilityRuntimeService().handleProjectileImpact(
                            restoredProjectile,
                            restoredHolder[0],
                            new EntityHitResult(target)
                    );
                    helper.assertTrue(handled,
                            "serialized projectile restore should preserve the delivery callback runtime on the respawned projectile");
                    helper.assertTrue(target.getHealth() < startingHealth,
                            "serialized projectile restore should let the restored projectile impact callback damage the target");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void serializedProjectileDeliveryRestoresGroundedCallbacksAfterJoin(GameTestHelper helper) {
        Player source = createTestPlayer(helper, new BlockPos(1, 2, 1));
        MKPlayerData sourceData = MKCore.getPlayerOrThrow(source);

        Player[] restoredHolder = new Player[1];
        float[] restoredStartingHealth = new float[1];
        helper.startSequence()
                .thenExecute(() -> {
                    InvocationResult result = MKCore.getAbilityRuntimeService().getEngine().activate(new ActivationRequest(
                            sourceData,
                            sourceData,
                            new AbilityReference(PROJECTILE_GROUND_ABILITY, null),
                            "cast",
                            null,
                            null,
                            null,
                            false,
                            false
                    ));
                    helper.assertTrue(result.started(), "serialized grounded projectile restore probe should start");

                    AbilityProjectileEntity projectile = findProjectile(helper, source);
                    BlockPos groundedPos = helper.absolutePos(new BlockPos(4, 2, 1));
                    helper.getLevel().setBlock(groundedPos, Blocks.STONE.defaultBlockState(), 3);
                    projectile.moveTo(groundedPos.getX() + 0.5, groundedPos.getY() + 0.5, groundedPos.getZ() + 0.5,
                            projectile.getYRot(), projectile.getXRot());
                    projectile.setDeltaMovement(Vec3.ZERO);
                    projectile.restoreGroundedState(0);

                    PersistedAbilityRuntimeState sourceSnapshot = MKCore.getAbilityRuntimeService()
                            .capturePersonaRuntime(sourceData.getPersonaManager().getActivePersona());
                    helper.assertTrue(sourceSnapshot.deliveries().stream()
                                    .anyMatch(entry -> entry.abilityId().equals(PROJECTILE_GROUND_ABILITY)
                                            && entry.kind() == DeliveryKind.PROJECTILE
                                            && entry.projectileInGround()),
                            "serialized grounded projectile restore probe should snapshot the grounded projectile delivery");

                    CompoundTag serialized = serializePlayerData(sourceData);
                    projectile.discard();
                    source.discard();

                    restoredHolder[0] = createDeserializedTestPlayer(
                            helper,
                            new BlockPos(1, 2, 1),
                            serialized,
                            sourceData.getEntity().registryAccess()
                    );
                    restoredStartingHealth[0] = restoredHolder[0].getHealth();
                })
                .thenExecuteAfter(1, () -> {
                    MKPlayerData restoredData = MKCore.getPlayerOrThrow(restoredHolder[0]);
                    PersistedAbilityRuntimeState restoredSnapshot = MKCore.getAbilityRuntimeService()
                            .capturePersonaRuntime(restoredData.getPersonaManager().getActivePersona());
                    helper.assertTrue(restoredSnapshot.deliveries().stream()
                                    .anyMatch(entry -> entry.abilityId().equals(PROJECTILE_GROUND_ABILITY)
                                            && entry.kind() == DeliveryKind.PROJECTILE
                                            && entry.projectileInGround()),
                            "serialized grounded projectile restore should rebuild the grounded projectile runtime after join");

                    AbilityProjectileEntity restoredProjectile = findProjectile(helper, restoredHolder[0]);
                    helper.assertTrue(restoredProjectile.isInGround(),
                            "serialized grounded projectile restore should respawn the projectile in its grounded state");
                    helper.assertTrue(restoredHolder[0].getHealth() < restoredStartingHealth[0],
                            "serialized grounded projectile restore should resume the grounded callback after join");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void serializedProjectileDeliveryRestoresRemainingLifetimeAfterJoin(GameTestHelper helper) {
        Player source = createTestPlayer(helper, new BlockPos(1, 2, 1));
        MKPlayerData sourceData = MKCore.getPlayerOrThrow(source);

        Player[] restoredHolder = new Player[1];
        helper.startSequence()
                .thenExecute(() -> {
                    InvocationResult result = MKCore.getAbilityRuntimeService().getEngine().activate(new ActivationRequest(
                            sourceData,
                            sourceData,
                            new AbilityReference(PROJECTILE_GROUND_ABILITY, null),
                            "cast",
                            null,
                            null,
                            null,
                            false,
                            false
                    ));
                    helper.assertTrue(result.started(), "serialized projectile lifetime restore probe should start");

                    AbilityProjectileEntity projectile = findProjectile(helper, source);
                    BlockPos groundedPos = helper.absolutePos(new BlockPos(4, 2, 1));
                    helper.getLevel().setBlock(groundedPos, Blocks.STONE.defaultBlockState(), 3);
                    projectile.moveTo(groundedPos.getX() + 0.5, groundedPos.getY() + 0.5, groundedPos.getZ() + 0.5,
                            projectile.getYRot(), projectile.getXRot());
                    projectile.setDeltaMovement(Vec3.ZERO);
                    projectile.restoreGroundedState(0);
                    projectile.restoreTickCount(99);

                    PersistedAbilityRuntimeState sourceSnapshot = MKCore.getAbilityRuntimeService()
                            .capturePersonaRuntime(sourceData.getPersonaManager().getActivePersona());
                    helper.assertTrue(sourceSnapshot.deliveries().stream()
                                    .anyMatch(entry -> entry.abilityId().equals(PROJECTILE_GROUND_ABILITY)
                                            && entry.kind() == DeliveryKind.PROJECTILE
                                            && entry.projectileEntityTickCount() == 99),
                            "serialized projectile lifetime restore probe should snapshot the remaining projectile lifetime");

                    CompoundTag serialized = serializePlayerData(sourceData);
                    projectile.discard();
                    source.discard();

                    restoredHolder[0] = createDeserializedTestPlayer(
                            helper,
                            new BlockPos(1, 2, 1),
                            serialized,
                            sourceData.getEntity().registryAccess()
                    );
                })
                .thenExecuteAfter(1, () -> {
                    List<AbilityProjectileEntity> restoredProjectiles = helper.getLevel().getEntitiesOfClass(
                            AbilityProjectileEntity.class,
                            restoredHolder[0].getBoundingBox().inflate(8.0)
                    );
                    helper.assertTrue(restoredProjectiles.isEmpty(),
                            "serialized projectile lifetime restore should preserve the remaining lifetime instead of resetting the projectile age");

                    PersistedAbilityRuntimeState restoredSnapshot = MKCore.getAbilityRuntimeService()
                            .capturePersonaRuntime(MKCore.getPlayerOrThrow(restoredHolder[0]).getPersonaManager().getActivePersona());
                    helper.assertTrue(restoredSnapshot.deliveries().stream()
                                    .noneMatch(entry -> entry.abilityId().equals(PROJECTILE_GROUND_ABILITY)),
                            "serialized projectile lifetime restore should clear the delivery runtime once the restored projectile expires");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void equippedItemPassiveDefinitionInstallsAndRemovesReactionRuntime(GameTestHelper helper) {
        Player owner = createTestPlayer(helper, new BlockPos(1, 2, 1));
        Player target = createTestPlayer(helper, new BlockPos(3, 2, 1));

        helper.startSequence()
                .thenExecute(() -> owner.setItemSlot(
                        EquipmentSlot.FEET,
                        createGrantedItem(Items.LEATHER_BOOTS, SPELL_CRIT_PASSIVE_ABILITY)
                ))
                .thenExecuteAfter(1, () -> {
                    float startingHealth = target.getHealth();
                    emitSpellCrit(owner, target);
                    helper.assertTrue(target.getHealth() < startingHealth,
                            "equipped abilities2 passive item should install its reaction runtime");
                })
                .thenExecute(() -> owner.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY))
                .thenExecuteAfter(1, () -> {
                    float removedHealth = target.getHealth();
                    emitSpellCrit(owner, target);
                    helper.assertTrue(target.getHealth() == removedHealth,
                            "unequipped abilities2 passive item should remove its reaction runtime");
                    helper.succeed();
                });
    }

    @GameTest(template = "player_data_phase0")
    public static void equippedMainhandDefinitionExecutesItemLoadoutAbility(GameTestHelper helper) {
        Player owner = createTestPlayer(helper, new BlockPos(1, 2, 1));
        MKPlayerData ownerData = MKCore.getPlayerOrThrow(owner);

        owner.setItemSlot(EquipmentSlot.MAINHAND, createGrantedItem(Items.IRON_SWORD, SELF_HEAL_ABILITY));
        owner.setHealth(owner.getMaxHealth() - 8.0f);
        float startingHealth = owner.getHealth();

        helper.startSequence()
                .thenExecuteAfter(1, () -> ownerData.getAbilityExecutor().executeLoadoutAbility(AbilityGroupId.Item, 0))
                .thenExecuteAfter(25, () -> {
                    helper.assertTrue(owner.getHealth() > startingHealth,
                            "mainhand abilities2 item should execute through the item loadout bridge");
                    helper.succeed();
                });
    }

    private static AbilityRuntimeService createTestRuntimeService() {
        Map<ResourceLocation, AbilityDefinitionData> definitions = new LinkedHashMap<>();
        definitions.put(PROJECTILE_IMPACT_ABILITY, createProjectileImpactDefinition());
        definitions.put(PROJECTILE_GROUND_ABILITY, createProjectileGroundDefinition());
        definitions.put(INT_BRANCH_CONDITION_ABILITY, createIntBranchConditionDefinition());
        definitions.put(FLOAT_BRANCH_CONDITION_ABILITY, createFloatBranchConditionDefinition());
        definitions.put(EVENT_ACTOR_BRANCH_CONDITION_ABILITY, createEventActorBranchConditionDefinition());
        definitions.put(EVENT_PAYLOAD_BRANCH_CONDITION_ABILITY, createEventPayloadBranchConditionDefinition());
        return new AbilityRuntimeService(new AbilityDefinitionResolver(definitions::get));
    }

    private static SimpleAbilityEngine createInterruptReasonTestEngine(SimpleAbilityEngine.LifecycleListener listener) {
        Map<ResourceLocation, AbilityDefinitionData> definitions = new LinkedHashMap<>();
        definitions.put(INTERRUPT_REASON_PROBE_ABILITY, createInterruptReasonProbeDefinition());
        return new SimpleAbilityEngine(
                new AbilityDefinitionResolver(definitions::get),
                new MKAbilityPowerResolver(),
                new MemoryAbilityStateStore(),
                event -> {
                },
                SimpleAbilityEngine.ReactionController.NOOP,
                SimpleAbilityEngine.DeliveryController.NOOP,
                listener
        );
    }

    private static AbilityDefinitionData createProjectileImpactDefinition() {
        Map<String, AbilityActivationDefinition> activations = new LinkedHashMap<>();
        activations.put("cast", activation(ActivationKind.MANUAL, "cast"));
        activations.put("impact_proc", activation(ActivationKind.PROC, "impact_proc"));

        Map<String, List<AbilityAction>> entryPoints = new LinkedHashMap<>();
        entryPoints.put("cast", List.of(new AbilityAction.SpawnProjectileAction(
                "projectile",
                AbilityAction.ActionTarget.SELF,
                new AbilityScalar.ConstantScalar(1.0),
                new AbilityScalar.ConstantScalar(0.0)
        )));
        entryPoints.put("impact_proc", List.of(new AbilityAction.DamageAction(
                AbilityAction.ActionTarget.PRIMARY_ENTITY,
                new AbilityScalar.ConstantScalar(4.0),
                CoreDamageTypes.FireDamage.get().getId()
        )));

        return new AbilityDefinitionData(
                PROJECTILE_IMPACT_ABILITY,
                presentation("Projectile Impact Test"),
                MKCore.id("test"),
                Set.of(),
                Set.of(),
                Map.of(),
                activations,
                entryPoints,
                Map.of(),
                Map.of("projectile", new AbilityDeliveryDefinition(
                        DeliveryKind.PROJECTILE,
                        CoreEntities.ABILITY_PROJECTILE_TYPE.getId(),
                        Items.SNOWBALL.builtInRegistryHolder().key().location(),
                        List.of(),
                        null,
                        null,
                        null,
                        null,
                        "impact_proc",
                        null,
                        null
                ))
        );
    }

    private static AbilityDefinitionData createProjectileGroundDefinition() {
        Map<String, AbilityActivationDefinition> activations = new LinkedHashMap<>();
        activations.put("cast", activation(ActivationKind.MANUAL, "cast"));
        activations.put("ground_proc", activation(ActivationKind.PROC, "ground_proc"));

        Map<String, List<AbilityAction>> entryPoints = new LinkedHashMap<>();
        entryPoints.put("cast", List.of(new AbilityAction.SpawnProjectileAction(
                "projectile",
                AbilityAction.ActionTarget.SELF,
                new AbilityScalar.ConstantScalar(1.0),
                new AbilityScalar.ConstantScalar(0.0)
        )));
        entryPoints.put("ground_proc", List.of(new AbilityAction.DamageAction(
                AbilityAction.ActionTarget.SELF,
                new AbilityScalar.ConstantScalar(2.0),
                CoreDamageTypes.FireDamage.get().getId()
        )));

        return new AbilityDefinitionData(
                PROJECTILE_GROUND_ABILITY,
                presentation("Projectile Ground Test"),
                MKCore.id("test"),
                Set.of(),
                Set.of(),
                Map.of(),
                activations,
                entryPoints,
                Map.of(),
                Map.of("projectile", new AbilityDeliveryDefinition(
                        DeliveryKind.PROJECTILE,
                        CoreEntities.ABILITY_PROJECTILE_TYPE.getId(),
                        Items.SNOWBALL.builtInRegistryHolder().key().location(),
                        List.of(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "ground_proc"
                ))
        );
    }

    private static AbilityDefinitionData createIntBranchConditionDefinition() {
        Map<String, AbilityActivationDefinition> activations = new LinkedHashMap<>();
        activations.put("cast", activation(ActivationKind.MANUAL, "cast", new AbilityTargetResolverDefinition("self")));

        Map<String, List<AbilityAction>> entryPoints = new LinkedHashMap<>();
        entryPoints.put("cast", List.of(
                new AbilityAction.SetVarAction("charges", new AbilityValue.IntValue(2)),
                new AbilityAction.BranchAction(
                        condition("var_int", Map.of(
                                "name", stringConditionValue("charges"),
                                "operator", stringConditionValue("gte"),
                                "value", numberConditionValue(2)
                        )),
                        List.of(new AbilityAction.ModifyStateAction(
                                StateScope.SELF,
                                "charges_gate",
                                AbilityAction.ModifyStateOperation.SET_BOOL,
                                new AbilityValue.BoolValue(true)
                        )),
                        List.of(new AbilityAction.ModifyStateAction(
                                StateScope.SELF,
                                "charges_gate",
                                AbilityAction.ModifyStateOperation.SET_BOOL,
                                new AbilityValue.BoolValue(false)
                        ))
                ),
                new AbilityAction.BranchAction(
                        condition("param_float", Map.of(
                                "parameter", stringConditionValue("bonus_scale"),
                                "operator", stringConditionValue("gt"),
                                "value", numberConditionValue(1.5f)
                        )),
                        List.of(new AbilityAction.ModifyStateAction(
                                StateScope.SELF,
                                "bonus_count",
                                AbilityAction.ModifyStateOperation.SET_INT,
                                new AbilityValue.IntValue(1)
                        )),
                        List.of(new AbilityAction.ModifyStateAction(
                                StateScope.SELF,
                                "bonus_count",
                                AbilityAction.ModifyStateOperation.SET_INT,
                                new AbilityValue.IntValue(0)
                        ))
                ),
                new AbilityAction.BranchAction(
                        condition("state_int", Map.of(
                                "scope", stringConditionValue("self"),
                                "state_key", stringConditionValue("bonus_count"),
                                "operator", stringConditionValue("eq"),
                                "value", numberConditionValue(1)
                        )),
                        List.of(new AbilityAction.HealAction(
                                AbilityAction.ActionTarget.PRIMARY_ENTITY,
                                new AbilityScalar.ConstantScalar(4.0)
                        )),
                        List.of()
                )
        ));

        return new AbilityDefinitionData(
                INT_BRANCH_CONDITION_ABILITY,
                presentation("Int Branch Conditions Test"),
                MKCore.id("test"),
                Set.of(),
                Set.of(),
                Map.of("bonus_scale", floatParameter("bonus_scale", 2.0f)),
                activations,
                entryPoints,
                Map.of(),
                Map.of()
        );
    }

    private static AbilityDefinitionData createFloatBranchConditionDefinition() {
        Map<String, AbilityActivationDefinition> activations = new LinkedHashMap<>();
        activations.put("cast", activation(ActivationKind.MANUAL, "cast", new AbilityTargetResolverDefinition("self")));

        Map<String, List<AbilityAction>> entryPoints = new LinkedHashMap<>();
        entryPoints.put("cast", List.of(
                new AbilityAction.ModifyStateAction(
                        StateScope.SELF,
                        "rating",
                        AbilityAction.ModifyStateOperation.SET_FLOAT,
                        new AbilityValue.FloatValue(1.25f)
                ),
                new AbilityAction.SetVarAction("bonus", new AbilityValue.FloatValue(1.5f)),
                new AbilityAction.BranchAction(
                        condition("param_int", Map.of(
                                "parameter", stringConditionValue("required_charges"),
                                "operator", stringConditionValue("eq"),
                                "value", numberConditionValue(2)
                        )),
                        List.of(new AbilityAction.ModifyStateAction(
                                StateScope.SELF,
                                "param_gate",
                                AbilityAction.ModifyStateOperation.SET_BOOL,
                                new AbilityValue.BoolValue(true)
                        )),
                        List.of(new AbilityAction.ModifyStateAction(
                                StateScope.SELF,
                                "param_gate",
                                AbilityAction.ModifyStateOperation.SET_BOOL,
                                new AbilityValue.BoolValue(false)
                        ))
                ),
                new AbilityAction.BranchAction(
                        condition("var_float", Map.of(
                                "name", stringConditionValue("bonus"),
                                "operator", stringConditionValue("gte"),
                                "value", numberConditionValue(1.5f)
                        )),
                        List.of(new AbilityAction.ModifyStateAction(
                                StateScope.SELF,
                                "float_gate",
                                AbilityAction.ModifyStateOperation.SET_INT,
                                new AbilityValue.IntValue(1)
                        )),
                        List.of(new AbilityAction.ModifyStateAction(
                                StateScope.SELF,
                                "float_gate",
                                AbilityAction.ModifyStateOperation.SET_INT,
                                new AbilityValue.IntValue(0)
                        ))
                ),
                new AbilityAction.BranchAction(
                        condition("state_float", Map.of(
                                "scope", stringConditionValue("self"),
                                "state_key", stringConditionValue("rating"),
                                "operator", stringConditionValue("lt"),
                                "value", numberConditionValue(2.0f)
                        )),
                        List.of(new AbilityAction.HealAction(
                                AbilityAction.ActionTarget.PRIMARY_ENTITY,
                                new AbilityScalar.ConstantScalar(3.0)
                        )),
                        List.of()
                )
        ));

        return new AbilityDefinitionData(
                FLOAT_BRANCH_CONDITION_ABILITY,
                presentation("Float Branch Conditions Test"),
                MKCore.id("test"),
                Set.of(),
                Set.of(),
                Map.of("required_charges", intParameter("required_charges", 2)),
                activations,
                entryPoints,
                Map.of(),
                Map.of()
        );
    }

    private static AbilityDefinitionData createEventActorBranchConditionDefinition() {
        Map<String, AbilityActivationDefinition> activations = new LinkedHashMap<>();
        activations.put("cast", activation(ActivationKind.MANUAL, "cast", new AbilityTargetResolverDefinition("self")));

        Map<String, List<AbilityAction>> entryPoints = new LinkedHashMap<>();
        entryPoints.put("cast", List.of(
                new AbilityAction.BranchAction(
                        new AbilityConditionDefinition("event_has_actor"),
                        List.of(
                                new AbilityAction.ModifyStateAction(
                                        StateScope.SELF,
                                        "actor_present",
                                        AbilityAction.ModifyStateOperation.SET_BOOL,
                                        new AbilityValue.BoolValue(true)
                                ),
                                new AbilityAction.HealAction(
                                        AbilityAction.ActionTarget.PRIMARY_ENTITY,
                                        new AbilityScalar.ConstantScalar(2.0)
                                )
                        ),
                        List.of(new AbilityAction.ModifyStateAction(
                                StateScope.SELF,
                                "actor_present",
                                AbilityAction.ModifyStateOperation.SET_BOOL,
                                new AbilityValue.BoolValue(false)
                        ))
                )
        ));

        return new AbilityDefinitionData(
                EVENT_ACTOR_BRANCH_CONDITION_ABILITY,
                presentation("Event Actor Branch Condition Test"),
                MKCore.id("test"),
                Set.of(),
                Set.of(),
                Map.of(),
                activations,
                entryPoints,
                Map.of(),
                Map.of()
        );
    }

    private static AbilityDefinitionData createEventPayloadBranchConditionDefinition() {
        Map<String, AbilityActivationDefinition> activations = new LinkedHashMap<>();
        activations.put("cast", activation(ActivationKind.MANUAL, "cast", new AbilityTargetResolverDefinition("self")));

        Map<String, List<AbilityAction>> entryPoints = new LinkedHashMap<>();
        entryPoints.put("cast", List.of(
                payloadStateBranch("event_has_payload", "has_stack_payload", Map.of(
                        "key", stringConditionValue("stack_count")
                )),
                payloadStateBranch("event_payload_int", "stack_gate", Map.of(
                        "key", stringConditionValue("stack_count"),
                        "operator", stringConditionValue("gte"),
                        "value", numberConditionValue(2)
                )),
                payloadStateBranch("event_payload_float", "rating_gate", Map.of(
                        "key", stringConditionValue("impact_rating"),
                        "operator", stringConditionValue("gt"),
                        "value", numberConditionValue(1.5f)
                )),
                payloadStateBranch("event_source_tag", "source_tag_gate", Map.of(
                        "tag", stringConditionValue(AbilityDatagenKeys.TAG_FIRE.toString())
                )),
                payloadStateBranch("event_payload_bool", "critical_gate", Map.of(
                        "key", stringConditionValue("critical"),
                        "value", new JsonPrimitive(true)
                )),
                payloadStateBranch("event_payload_string", "phase_gate", Map.of(
                        "key", stringConditionValue("phase"),
                        "value", stringConditionValue("burst")
                )),
                payloadStateBranch("event_payload_resource_location", "damage_type_gate", Map.of(
                        "key", stringConditionValue("damage_type"),
                        "value", stringConditionValue(CoreDamageTypes.FireDamage.getId().toString())
                )),
                payloadStateBranch("event_payload_tag", "fire_tag_gate", Map.of(
                        "key", stringConditionValue("ability_tag_probe"),
                        "tag", stringConditionValue(AbilityDatagenKeys.TAG_FIRE.toString())
                )),
                payloadStateBranch("event_payload_entity_ref", "actor_ref_gate", Map.of(
                        "key", stringConditionValue("actor_ref"),
                        "value", stringConditionValue(EVENT_PAYLOAD_ENTITY_REF_PROBE_ID.toString())
                ))
        ));

        return new AbilityDefinitionData(
                EVENT_PAYLOAD_BRANCH_CONDITION_ABILITY,
                presentation("Event Payload Branch Condition Test"),
                MKCore.id("test"),
                Set.of(),
                Set.of(AbilityDatagenKeys.TAG_FIRE),
                Map.of(),
                activations,
                entryPoints,
                Map.of(),
                Map.of()
        );
    }

    private static AbilityDefinitionData createInterruptReasonProbeDefinition() {
        Map<String, AbilityActivationDefinition> activations = new LinkedHashMap<>();
        activations.put("cast", activation(
                ActivationKind.MANUAL,
                "cast",
                new AbilityTargetResolverDefinition("self"),
                20,
                false,
                FULL_INTERRUPT_PROBE
        ));

        Map<String, List<AbilityAction>> entryPoints = new LinkedHashMap<>();
        entryPoints.put("cast", List.of(new AbilityAction.HealAction(
                AbilityAction.ActionTarget.SELF,
                new AbilityScalar.ConstantScalar(4.0)
        )));

        return new AbilityDefinitionData(
                INTERRUPT_REASON_PROBE_ABILITY,
                presentation("Interrupt Reason Probe"),
                MKCore.id("test"),
                Set.of(),
                Set.of(),
                Map.of(),
                activations,
                entryPoints,
                Map.of(),
                Map.of()
        );
    }

    private static AbilityActivationDefinition activation(ActivationKind kind, String entryPoint) {
        return activation(kind, entryPoint, new AbilityTargetResolverDefinition("none"));
    }

    private static AbilityActivationDefinition activation(ActivationKind kind,
                                                          String entryPoint,
                                                          AbilityTargetResolverDefinition targeting) {
        return new AbilityActivationDefinition(
                kind,
                entryPoint,
                targeting,
                List.of(),
                List.of(),
                null,
                0,
                false,
                NO_INTERRUPT,
                InterruptRefundPolicy.NONE,
                new ActivationBehavior.InstantBehavior()
        );
    }

    private static AbilityActivationDefinition activation(ActivationKind kind,
                                                          String entryPoint,
                                                          AbilityTargetResolverDefinition targeting,
                                                          int castTicks,
                                                          boolean affectedByCastSpeed,
                                                          InterruptPolicy interruptPolicy) {
        return new AbilityActivationDefinition(
                kind,
                entryPoint,
                targeting,
                List.of(),
                List.of(),
                null,
                castTicks,
                affectedByCastSpeed,
                interruptPolicy,
                InterruptRefundPolicy.NONE,
                new ActivationBehavior.InstantBehavior()
        );
    }

    private static AbilityPresentation presentation(String name) {
        return new AbilityPresentation(name, name, null, null, null, null, null);
    }

    private static AbilityParameterDefinition floatParameter(String id, float defaultValue) {
        return new AbilityParameterDefinition(
                id,
                new AbilityValue.FloatValue(defaultValue),
                AbilityValueKind.FLOAT,
                true,
                true,
                id
        );
    }

    private static AbilityParameterDefinition intParameter(String id, int defaultValue) {
        return new AbilityParameterDefinition(
                id,
                new AbilityValue.IntValue(defaultValue),
                AbilityValueKind.INT,
                true,
                true,
                id
        );
    }

    private static AbilityConditionDefinition condition(String type, Map<String, JsonElement> data) {
        return new AbilityConditionDefinition(type, data);
    }

    private static JsonPrimitive stringConditionValue(String value) {
        return new JsonPrimitive(value);
    }

    private static JsonPrimitive numberConditionValue(Number value) {
        return new JsonPrimitive(value);
    }

    private static AbilityAction.BranchAction payloadStateBranch(String conditionType,
                                                                String stateKey,
                                                                Map<String, JsonElement> data) {
        return new AbilityAction.BranchAction(
                condition(conditionType, data),
                List.of(new AbilityAction.ModifyStateAction(
                        StateScope.SELF,
                        stateKey,
                        AbilityAction.ModifyStateOperation.SET_BOOL,
                        new AbilityValue.BoolValue(true)
                )),
                List.of(new AbilityAction.ModifyStateAction(
                        StateScope.SELF,
                        stateKey,
                        AbilityAction.ModifyStateOperation.SET_BOOL,
                        new AbilityValue.BoolValue(false)
                ))
        );
    }

    private static void emitSpellCrit(Player owner, Player target) {
        MKCore.getAbilityRuntimeService().getReactionBus().emit(new AbilityEventSnapshot(
                AbilityEventType.SPELL_CRIT,
                null,
                UUID.randomUUID(),
                0,
                owner.getUUID(),
                SPELL_SOURCE_ABILITY,
                "cast",
                owner.getUUID(),
                target.getUUID(),
                Map.of()
        ));
    }

    private static ItemStack createGrantedItem(Item item, ResourceLocation abilityId) {
        ItemStack stack = new ItemStack(item);
        ItemGrantedAbility.setAbility(stack, abilityId);
        return stack;
    }

    private static CompoundTag serializePlayerData(MKPlayerData playerData) {
        return playerData.serializeNBT(playerData.getEntity().registryAccess());
    }

    private static CompoundTag serializeEntityData(MKEntityData entityData) {
        return entityData.serializeNBT(entityData.getEntity().registryAccess());
    }

    private static AbilityResolvedTargets singleTarget(LivingEntity target) {
        UUID targetId = target.getUUID();
        return new AbilityResolvedTargets(targetId, List.of(targetId), null, null, null);
    }

    private static AbilityValue stateValue(PersistedAbilityRuntimeState snapshot, String stateKey) {
        return snapshot.states().stream()
                .filter(entry -> stateKey.equals(entry.stateKey()))
                .map(PersistedAbilityRuntimeState.StateEntry::value)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing runtime state " + stateKey));
    }

    private static boolean stateBool(PersistedAbilityRuntimeState snapshot, String stateKey) {
        AbilityValue value = stateValue(snapshot, stateKey);
        if (!(value instanceof AbilityValue.BoolValue boolValue)) {
            throw new IllegalStateException("State " + stateKey + " should be a bool");
        }
        return boolValue.value();
    }

    private static int stateInt(PersistedAbilityRuntimeState snapshot, String stateKey) {
        AbilityValue value = stateValue(snapshot, stateKey);
        if (!(value instanceof AbilityValue.IntValue intValue)) {
            throw new IllegalStateException("State " + stateKey + " should be an int");
        }
        return intValue.value();
    }

    private static float stateFloat(PersistedAbilityRuntimeState snapshot, String stateKey) {
        AbilityValue value = stateValue(snapshot, stateKey);
        if (!(value instanceof AbilityValue.FloatValue floatValue)) {
            throw new IllegalStateException("State " + stateKey + " should be a float");
        }
        return floatValue.value();
    }

    private static Player createDeserializedTestPlayer(GameTestHelper helper,
                                                       BlockPos relativePos,
                                                       CompoundTag serialized,
                                                       HolderLookup.Provider provider) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos absolutePos = helper.absolutePos(relativePos);
        player.moveTo(absolutePos.getX() + 0.5, absolutePos.getY(), absolutePos.getZ() + 0.5, 0.0f, 0.0f);
        MKPlayerData playerData = MKCore.getPlayerOrThrow(player);
        playerData.deserializeNBT(provider, serialized);
        if (helper.getLevel().getEntity(player.getUUID()) == null) {
            helper.getLevel().addFreshEntity(player);
        }
        playerData.getPersonaManager().onJoinLevel();
        return player;
    }

    private static Zombie createDeserializedTestZombie(GameTestHelper helper,
                                                       BlockPos relativePos,
                                                       UUID entityId,
                                                       CompoundTag serialized,
                                                       HolderLookup.Provider provider) {
        Zombie zombie = EntityType.ZOMBIE.create(helper.getLevel());
        if (zombie == null) {
            throw new IllegalStateException("test zombie should construct");
        }
        BlockPos absolutePos = helper.absolutePos(relativePos);
        zombie.setUUID(entityId);
        zombie.moveTo(absolutePos.getX() + 0.5, absolutePos.getY(), absolutePos.getZ() + 0.5, 0.0f, 0.0f);
        MKCore.getEntitySpecificData(zombie).orElseThrow().deserializeNBT(provider, serialized);
        if (helper.getLevel().getEntity(zombie.getUUID()) == null) {
            helper.getLevel().addFreshEntity(zombie);
        }
        return zombie;
    }

    private static void beginBlocking(Player player) {
        player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
        player.startUsingItem(InteractionHand.OFF_HAND);
        player.tick();
        player.tick();
    }

    private static void lookAtEntity(Player player, LivingEntity target) {
        player.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
        player.yHeadRot = player.getYRot();
        player.yBodyRot = player.getYRot();
    }

    private static AbilityProjectileEntity findProjectile(GameTestHelper helper, LivingEntity caster) {
        List<AbilityProjectileEntity> projectiles = helper.getLevel().getEntitiesOfClass(
                AbilityProjectileEntity.class,
                caster.getBoundingBox().inflate(8.0)
        );
        helper.assertTrue(!projectiles.isEmpty(), "test ability should have spawned a projectile");
        return projectiles.getFirst();
    }

    private static Player createTestPlayer(GameTestHelper helper, BlockPos relativePos) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos absolutePos = helper.absolutePos(relativePos);
        if (helper.getLevel().getEntity(player.getUUID()) == null) {
            helper.getLevel().addFreshEntity(player);
        }
        player.moveTo(absolutePos.getX() + 0.5, absolutePos.getY(), absolutePos.getZ() + 0.5, 0.0f, 0.0f);
        player.setHealth(player.getMaxHealth());
        return player;
    }

    private static Zombie createTestZombie(GameTestHelper helper, BlockPos relativePos) {
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, relativePos);
        zombie.setHealth(zombie.getMaxHealth());
        return zombie;
    }
}
