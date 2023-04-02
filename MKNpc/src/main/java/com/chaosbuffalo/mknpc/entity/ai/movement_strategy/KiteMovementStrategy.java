package com.chaosbuffalo.mknpc.entity.ai.movement_strategy;

import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.entity.ai.MovementUtils;
import com.chaosbuffalo.mknpc.entity.ai.memory.MKMemoryModuleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;

public class KiteMovementStrategy extends MovementStrategy {

    private double dist;

    public KiteMovementStrategy(double dist) {
        this.dist = dist;
    }

    @Override
    public void update(ServerLevel world, MKEntity entity) {
        Brain<?> brain = entity.getBrain();
        Optional<LivingEntity> targetOpt = brain.getMemory(MKMemoryModuleTypes.MOVEMENT_TARGET);
        Optional<WalkTarget> walkTargetOptional = brain.getMemory(MemoryModuleType.WALK_TARGET);
        if (entity.getRandom().nextInt(20) == 0){
            return;
        }
        if (targetOpt.isPresent()) {
            LivingEntity target = targetOpt.get();
            if (target.is(entity)) {
                brain.eraseMemory(MemoryModuleType.WALK_TARGET);
                return;
            }
            WalkTarget walkTarget = walkTargetOptional.orElse(null);
            Vec3 targetPos = null;
            double distToWalkTarget = 0.0;
            double distanceTo = entity.distanceTo(target);
            if (walkTarget != null) {
                distToWalkTarget = target.distanceToSqr(walkTarget.getTarget().currentPosition());
            }
            double threeQuarterDist = .75 * dist;
            if (distanceTo < threeQuarterDist && distToWalkTarget < (threeQuarterDist * threeQuarterDist)) {
                targetPos = MovementUtils.findRandomTargetBlockAwayFromNoWater(
                        entity, (int) Math.round(dist), 3, target.position());
            } else if (distanceTo > 1.1 * dist) {
                targetPos = target.position();
            }
            if (targetPos != null) {
                brain.setMemory(MemoryModuleType.WALK_TARGET,
                        new WalkTarget(targetPos, 1.0f, 1));
            }
        }

    }
}
