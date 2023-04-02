package com.chaosbuffalo.mknpc.entity.ai.movement_strategy;

import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.entity.ai.MovementUtils;
import com.chaosbuffalo.mknpc.entity.ai.memory.MKMemoryModuleTypes;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;

public class RandomWanderMovementStrategy extends MovementStrategy{
    private final int positionChance;

    public RandomWanderMovementStrategy(int positionChance){
        this.positionChance = positionChance;
    }



    @Override
    public void update(ServerLevel world, MKEntity entity) {
        Brain<?> brain = entity.getBrain();
        Optional<WalkTarget> walkTargetOptional = brain.getMemory(MemoryModuleType.WALK_TARGET);
        Optional<BlockPos> spawnPointOptional = brain.getMemory(MKMemoryModuleTypes.SPAWN_POINT);
        if (!walkTargetOptional.isPresent() || entity.getRandom().nextInt(positionChance) == 0 || entity.getNavigation().isDone()){
            Vec3 position = spawnPointOptional.map(blockPos -> {
                Vec3 vecPos = Vec3.atLowerCornerOf(blockPos);
                if (entity.distanceToSqr(vecPos) > entity.getWanderRange() * entity.getWanderRange()){
                    return MovementUtils.findRandomTargetBlockTowardsNoWater(
                            entity, entity.getWanderRange() /2, entity.getWanderRange() / 2, vecPos);
                } else {
                    return LandRandomPos.getPos(entity, entity.getWanderRange() / 2,
                            entity.getWanderRange() / 2);
                }
            }).orElse(LandRandomPos.getPos(entity, entity.getWanderRange() / 2,
                    entity.getWanderRange() / 2));
            if (position != null){
                brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(position, 0.5f, 1));
            }
        }
    }
}
