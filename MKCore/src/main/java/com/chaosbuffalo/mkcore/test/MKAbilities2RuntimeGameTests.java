package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities2.AbilityRuntimeService;
import com.chaosbuffalo.mkcore.abilities2.actions.AbilityAction;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityActivationDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityDefinitionData;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityDeliveryDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityPresentation;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityScalar;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityTargetResolverDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.ActivationBehavior;
import com.chaosbuffalo.mkcore.abilities2.definition.ActivationKind;
import com.chaosbuffalo.mkcore.abilities2.definition.DeliveryKind;
import com.chaosbuffalo.mkcore.abilities2.definition.InterruptPolicy;
import com.chaosbuffalo.mkcore.abilities2.definition.InterruptRefundPolicy;
import com.chaosbuffalo.mkcore.abilities2.runtime.AbilityDefinitionResolver;
import com.chaosbuffalo.mkcore.abilities2.runtime.AbilityEventSnapshot;
import com.chaosbuffalo.mkcore.abilities2.runtime.AbilityEventType;
import com.chaosbuffalo.mkcore.abilities2.runtime.AbilityReference;
import com.chaosbuffalo.mkcore.abilities2.runtime.ActivationRequest;
import com.chaosbuffalo.mkcore.abilities2.runtime.InvocationResult;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.AbilityGroupId;
import com.chaosbuffalo.mkcore.entities.AbilityProjectileEntity;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.init.CoreEntities;
import com.chaosbuffalo.mkcore.item.ItemGrantedAbility;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

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
    private static final ResourceLocation SPELL_CRIT_PASSIVE_ABILITY =
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "test_abilities2_spell_crit_passive");
    private static final ResourceLocation SPELL_SOURCE_ABILITY =
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "test_abilities2_firebolt");
    private static final ResourceLocation SELF_HEAL_ABILITY =
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "test_abilities2_self_heal");
    private static final ResourceLocation RESTORING_AURA_ABILITY =
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "test_abilities2_restoring_aura");
    private static final InterruptPolicy NO_INTERRUPT = new InterruptPolicy(false, 0.0f, false, 0.0, true);

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
        return new AbilityRuntimeService(new AbilityDefinitionResolver(definitions::get));
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
                        "ground_proc"
                ))
        );
    }

    private static AbilityActivationDefinition activation(ActivationKind kind, String entryPoint) {
        return new AbilityActivationDefinition(
                kind,
                entryPoint,
                new AbilityTargetResolverDefinition("none"),
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

    private static AbilityPresentation presentation(String name) {
        return new AbilityPresentation(name, name, null, null, null, null, null);
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
}
