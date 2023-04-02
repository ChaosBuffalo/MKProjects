package com.chaosbuffalo.mknpc.entity.ai.movement_strategy;

import com.chaosbuffalo.mknpc.entity.MKEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;

public class StationaryMovementStrategy extends MovementStrategy {

    public static final StationaryMovementStrategy STATIONARY_MOVEMENT_STRATEGY = new StationaryMovementStrategy();

    @Override
    public void update(ServerLevel world, MKEntity entity) {
        Brain<?> brain = entity.getBrain();
        Optional<WalkTarget> walkTargetOptional = brain.getMemory(MemoryModuleType.WALK_TARGET);
        if (walkTargetOptional.isPresent()) {
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        }
    }
}
