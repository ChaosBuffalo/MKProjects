package com.chaosbuffalo.mknpc.entity.ai.controller;

import com.chaosbuffalo.mknpc.entity.ai.memory.MKMemoryModuleTypes;
import com.chaosbuffalo.mknpc.entity.ai.movement_strategy.FollowMovementStrategy;
import com.chaosbuffalo.mknpc.entity.ai.movement_strategy.KiteMovementStrategy;
import com.chaosbuffalo.mknpc.entity.ai.movement_strategy.RandomWanderMovementStrategy;
import com.chaosbuffalo.mknpc.entity.ai.movement_strategy.StationaryMovementStrategy;
import net.minecraft.world.entity.LivingEntity;

public class MovementStrategyController {


    public static void enterMeleeMode(LivingEntity entity, int meleeDistance) {
        entity.getBrain().setMemory(MKMemoryModuleTypes.MOVEMENT_STRATEGY,
                new FollowMovementStrategy(1.0f, meleeDistance));
    }

    public static void enterFollowMode(LivingEntity entity, int followDistance, LivingEntity followTarget) {
        entity.getBrain().setMemory(MKMemoryModuleTypes.MOVEMENT_STRATEGY,
                new FollowMovementStrategy(1.0f, followDistance));
        entity.getBrain().setMemory(MKMemoryModuleTypes.MOVEMENT_TARGET, followTarget);
    }

    public static void enterStationary(LivingEntity entity) {
        entity.getBrain().setMemory(MKMemoryModuleTypes.MOVEMENT_STRATEGY,
                StationaryMovementStrategy.STATIONARY_MOVEMENT_STRATEGY);
    }

    public static void enterCastingMode(LivingEntity entity, double castingDistance) {
        entity.getBrain().setMemory(MKMemoryModuleTypes.MOVEMENT_STRATEGY,
                new KiteMovementStrategy(castingDistance));
    }

    public static void enterRandomWander(LivingEntity entity){
        entity.getBrain().eraseMemory(MKMemoryModuleTypes.MOVEMENT_TARGET);
        entity.getBrain().setMemory(MKMemoryModuleTypes.MOVEMENT_STRATEGY,
                new RandomWanderMovementStrategy(1200));
    }
}
