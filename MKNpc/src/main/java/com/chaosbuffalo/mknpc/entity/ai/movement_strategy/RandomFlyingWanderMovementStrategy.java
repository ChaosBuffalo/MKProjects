package com.chaosbuffalo.mknpc.entity.ai.movement_strategy;

import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.entity.ai.memory.MKMemoryModuleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class RandomFlyingWanderMovementStrategy extends MovementStrategy {
    private final int positionChance;

    public RandomFlyingWanderMovementStrategy(int positionChance) {
        this.positionChance = positionChance;
    }


    @Override
    public void update(ServerLevel world, MKEntity entity) {
        Brain<?> brain = entity.getBrain();
        Optional<WalkTarget> walkTargetOptional = brain.getMemory(MemoryModuleType.WALK_TARGET);
        Optional<BlockPos> spawnPointOptional = brain.getMemory(MKMemoryModuleTypes.SPAWN_POINT.get());
        if (walkTargetOptional.isEmpty() || entity.getRandom().nextInt(positionChance) == 0 || entity.getNavigation().isDone()) {

            Vec3 desiredStart = spawnPointOptional.map(blockPos -> {
                Vec3 vecPos = Vec3.atCenterOf(blockPos);
                if (entity.distanceToSqr(vecPos) > entity.getWanderRange() * entity.getWanderRange()) {
                    return vecPos.subtract(entity.position()).normalize();
                } else {
                    return entity.getViewVector(0.0f);
                }
            }).orElse(entity.getViewVector(0.0f));

            Vec3 position = HoverRandomPos.getPos(entity, entity.getWanderRange(), entity.getWanderRange(),
                    desiredStart.x, desiredStart.z, ((float)Math.PI / 2F), 3, 1);
            if (position == null) {
                position = AirAndWaterRandomPos.getPos(entity, entity.getWanderRange(), entity.getWanderRange() / 2, -2, desiredStart.x, desiredStart.z, (double) ((float) Math.PI / 2F));
            }

            if (position != null) {
                brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(position, 0.5f, 1));
            }
        }
    }
}
