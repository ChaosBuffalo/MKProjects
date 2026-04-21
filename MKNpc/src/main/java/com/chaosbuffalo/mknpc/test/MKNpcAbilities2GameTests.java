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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
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

    @GameTest(template = "player_data_phase0")
    public static void abilitySensorSelectsDefinitionBackedAiActivation(GameTestHelper helper) {
        MKSkeletonEntity skeleton = helper.spawn(MKNpcEntityTypes.SKELETON_TYPE.get(), new BlockPos(1, 2, 1));
        Player target = createTestPlayer(helper, new BlockPos(4, 2, 1));

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

    private static class TestAbilityUseSensor extends AbilityUseSensor {
        private void runTick(ServerLevel level, MKEntity entity) {
            doTick(level, entity);
        }
    }
}
