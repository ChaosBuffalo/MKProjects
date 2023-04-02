package com.chaosbuffalo.mknpc.entity.ai.movement_strategy;


import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.entity.ai.memory.MKMemoryModuleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;

public class FollowMovementStrategy extends MovementStrategy {

    private final float movementScale;
    private final int dist;

    public FollowMovementStrategy(float movementScale, int manhattanDist) {
        this.movementScale = movementScale;
        this.dist = manhattanDist;
    }

    @Override
    public void update(ServerLevel world, MKEntity entity) {
        Brain<?> brain = entity.getBrain();
        Optional<LivingEntity> targetOpt = brain.getMemory(MKMemoryModuleTypes.MOVEMENT_TARGET);
        if (targetOpt.isPresent()) {
            LivingEntity target = targetOpt.get();
            if (target.is(entity)) {
                brain.eraseMemory(MemoryModuleType.WALK_TARGET);
                return;
            }
            brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(target.blockPosition(),
                    movementScale, dist));
        }
    }
}
