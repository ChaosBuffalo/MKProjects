package com.chaosbuffalo.mknpc.test;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbilityMemories;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityTargetingDecision;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.entity.MKSkeletonEntity;
import com.chaosbuffalo.mknpc.entity.ai.NpcAbilitySelection;
import com.chaosbuffalo.mknpc.entity.ai.goal.UseAbilityGoal;
import com.chaosbuffalo.mknpc.entity.ai.memory.MKMemoryModuleTypes;
import com.chaosbuffalo.mknpc.entity.ai.sensor.AbilityUseSensor;
import com.chaosbuffalo.mknpc.init.MKNpcEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(MKNpc.MODID)
@PrefixGameTestTemplate(false)
public class MKNpcAbilities2GameTests {
    private static final ResourceLocation AI_SELF_HEAL_ABILITY =
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "test_abilities2_ai_self_heal");
    private static final ResourceLocation AI_FIREBOLT_ABILITY =
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "test_abilities2_ai_firebolt");
    private static final ResourceLocation AI_FRIENDLY_HEAL_ABILITY =
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "test_abilities2_ai_friendly_heal");

    @GameTest(template = "player_data_phase0")
    public static void abilitySensorSelectsDefinitionBackedAiActivation(GameTestHelper helper) {
        MKSkeletonEntity skeleton = helper.spawn(MKNpcEntityTypes.SKELETON_TYPE.get(), new BlockPos(1, 2, 1));
        var target = helper.spawn(EntityType.VILLAGER, new BlockPos(4, 2, 1));

        helper.assertTrue(
                skeleton.getEntityDataCap().getAbilities().learnAbilityDefinition(AI_FIREBOLT_ABILITY, 1, null),
                "NPC should learn the definition-backed AI firebolt"
        );
        skeleton.getBrain().setMemory(MKMemoryModuleTypes.THREAT_TARGET.get(), target);
        skeleton.getBrain().setMemory(MKMemoryModuleTypes.ALLIES.get(), List.of());
        skeleton.getBrain().setMemory(MKMemoryModuleTypes.ENEMIES.get(), List.of(target));

        new TestAbilityUseSensor().runTick(helper.getLevel(), skeleton);

        NpcAbilitySelection selection = skeleton.getBrain().getMemory(MKMemoryModuleTypes.CURRENT_ABILITY.get()).orElse(null);
        helper.assertTrue(selection != null, "sensor should select an ability");
        helper.assertTrue(selection.isDefinitionBacked(), "selected ability should be definition-backed");
        helper.assertValueEqual(selection.abilityId(), AI_FIREBOLT_ABILITY, "selected abilities2 AI ability");
        helper.assertValueEqual(selection.movementSuggestion(), AbilityTargetingDecision.MovementSuggestion.MELEE,
                "skeleton combat mode should map resolved-target abilities2 casts to melee movement");
        helper.assertTrue(skeleton.getBrain().getMemory(MKAbilityMemories.ABILITY_TARGET.get())
                        .map(target::equals)
                        .orElse(false),
                "sensor should set the resolved threat target as the active ability target");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void abilitySensorSelectsFriendlyDefinitionBackedAiActivation(GameTestHelper helper) {
        MKSkeletonEntity skeleton = helper.spawn(MKNpcEntityTypes.SKELETON_TYPE.get(), new BlockPos(1, 2, 1));
        MKSkeletonEntity ally = helper.spawn(MKNpcEntityTypes.SKELETON_TYPE.get(), new BlockPos(4, 2, 1));
        var enemy = helper.spawn(EntityType.VILLAGER, new BlockPos(6, 2, 1));
        ally.setHealth(ally.getMaxHealth() - 6.0f);

        helper.assertTrue(
                skeleton.getEntityDataCap().getAbilities().learnAbilityDefinition(AI_FRIENDLY_HEAL_ABILITY, 1, null),
                "NPC should learn the definition-backed AI friendly heal"
        );
        skeleton.getBrain().setMemory(MKMemoryModuleTypes.ALLIES.get(), List.of(ally));
        skeleton.getBrain().setMemory(MKMemoryModuleTypes.ENEMIES.get(), List.of(enemy));

        new TestAbilityUseSensor().runTick(helper.getLevel(), skeleton);

        NpcAbilitySelection selection = skeleton.getBrain().getMemory(MKMemoryModuleTypes.CURRENT_ABILITY.get()).orElse(null);
        helper.assertTrue(selection != null, "sensor should select a friendly-targeted ability");
        helper.assertTrue(selection.isDefinitionBacked(), "selected friendly ability should be definition-backed");
        helper.assertValueEqual(selection.abilityId(), AI_FRIENDLY_HEAL_ABILITY, "selected abilities2 AI friendly heal");
        helper.assertValueEqual(selection.movementSuggestion(), AbilityTargetingDecision.MovementSuggestion.STATIONARY,
                "friendly-targeted abilities2 casts should keep the skeleton stationary");
        helper.assertTrue(skeleton.getBrain().getMemory(MKAbilityMemories.ABILITY_TARGET.get())
                        .map(ally::equals)
                        .orElse(false),
                "sensor should set the allied target for friendly-targeted abilities2 casts");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void useAbilityGoalExecutesDefinitionBackedAiActivation(GameTestHelper helper) {
        MKSkeletonEntity skeleton = helper.spawn(MKNpcEntityTypes.SKELETON_TYPE.get(), new BlockPos(1, 2, 1));
        skeleton.setHealth(skeleton.getMaxHealth() - 6.0f);
        float startingHealth = skeleton.getHealth();

        helper.assertTrue(
                skeleton.getEntityDataCap().getAbilities().learnAbilityDefinition(AI_SELF_HEAL_ABILITY, 1, null),
                "NPC should learn the definition-backed AI self-heal"
        );
        skeleton.getBrain().setMemory(MKMemoryModuleTypes.ALLIES.get(), List.of());
        skeleton.getBrain().setMemory(MKMemoryModuleTypes.ENEMIES.get(), List.of());

        new TestAbilityUseSensor().runTick(helper.getLevel(), skeleton);

        UseAbilityGoal goal = new UseAbilityGoal(skeleton, false);
        helper.assertTrue(goal.canUse(), "goal should accept the selected abilities2 AI activation");
        goal.start();

        helper.assertTrue(skeleton.getHealth() > startingHealth,
                "starting the abilities2 AI self-heal should restore health immediately");
        goal.stop();
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void useAbilityGoalExecutesFriendlyDefinitionBackedAiActivation(GameTestHelper helper) {
        MKSkeletonEntity skeleton = helper.spawn(MKNpcEntityTypes.SKELETON_TYPE.get(), new BlockPos(1, 2, 1));
        MKSkeletonEntity ally = helper.spawn(MKNpcEntityTypes.SKELETON_TYPE.get(), new BlockPos(4, 2, 1));
        var enemy = helper.spawn(EntityType.VILLAGER, new BlockPos(6, 2, 1));
        ally.setHealth(ally.getMaxHealth() - 6.0f);
        float allyStartingHealth = ally.getHealth();

        helper.assertTrue(
                skeleton.getEntityDataCap().getAbilities().learnAbilityDefinition(AI_FRIENDLY_HEAL_ABILITY, 1, null),
                "NPC should learn the definition-backed AI friendly heal"
        );
        skeleton.getBrain().setMemory(MKMemoryModuleTypes.ALLIES.get(), List.of(ally));
        skeleton.getBrain().setMemory(MKMemoryModuleTypes.ENEMIES.get(), List.of(enemy));

        new TestAbilityUseSensor().runTick(helper.getLevel(), skeleton);

        UseAbilityGoal goal = new UseAbilityGoal(skeleton, false);
        helper.assertTrue(goal.canUse(), "goal should accept the selected friendly-targeted abilities2 AI activation");
        goal.start();

        helper.assertTrue(ally.getHealth() > allyStartingHealth,
                "starting the abilities2 AI friendly heal should restore health to the allied target");
        goal.stop();
        helper.succeed();
    }

    private static class TestAbilityUseSensor extends AbilityUseSensor {
        private void runTick(ServerLevel level, MKEntity entity) {
            doTick(level, entity);
        }
    }
}
