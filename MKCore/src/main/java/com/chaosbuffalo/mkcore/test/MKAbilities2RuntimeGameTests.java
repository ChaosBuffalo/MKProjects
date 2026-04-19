package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.MKCore;
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
import com.chaosbuffalo.mkcore.abilities2.runtime.AbilityReference;
import com.chaosbuffalo.mkcore.abilities2.runtime.ActivationRequest;
import com.chaosbuffalo.mkcore.abilities2.runtime.InvocationResult;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.entities.AbilityProjectileEntity;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.init.CoreEntities;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@GameTestHolder(MKCore.MOD_ID)
@PrefixGameTestTemplate(false)
public class MKAbilities2RuntimeGameTests {
    private static final ResourceLocation PROJECTILE_IMPACT_ABILITY =
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "test_abilities2_projectile_impact");
    private static final ResourceLocation PROJECTILE_GROUND_ABILITY =
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "test_abilities2_projectile_ground");
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
