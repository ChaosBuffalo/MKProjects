package com.chaosbuffalo.mknpc.entity.ai.movement_strategy;


import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.entity.ai.MovementUtils;
import com.chaosbuffalo.mknpc.entity.ai.memory.MKMemoryModuleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class KiteMovementStrategy extends MovementStrategy {

    private final double dist;
    private final boolean canFly;

    public KiteMovementStrategy(double dist, boolean canFly) {
        this.dist = dist;
        this.canFly = canFly;
    }

    protected Vec3 getTargetPosition(MKEntity entity, LivingEntity target) {
        Vec3 targetPos = null;
        for (int i = 0; i < 10; i++) {
            if (canFly) {
                Vec3 targetBPos = Vec3.atBottomCenterOf(target.blockPosition());
                int heightOffset = 0;
                BlockPos entityBPos = entity.blockPosition();
                int heightDiff = (int)targetBPos.y - entityBPos.getY();
                if (heightDiff > 2) {
                    heightOffset = 4;
                } else if (heightDiff < -2) {
                    heightOffset = -4;
                }
                targetPos = getAirPosAwayFrom(entity, (int) Math.round(dist),
                        (int) Math.round(dist) / 2, heightOffset, target.position(), (float) (Math.PI / 10));
            } else {
                targetPos = MovementUtils.findRandomTargetBlockAwayFromNoWater(
                        entity, (int) Math.round(dist), 7, target.position());
            }
            if (targetPos != null) {
                break;
            }
        }
        return targetPos;
    }

    public static Vec3 getAirPosAwayFrom(PathfinderMob mob, int radius, int yRange, int y, Vec3 vectorPosition, double amplifier) {
        Vec3 vec3 = mob.position().subtract(vectorPosition);
        boolean flag = GoalUtils.mobRestricted(mob, radius);
        return RandomPos.generateRandomPos(mob, () -> {
            BlockPos blockpos = AirAndWaterRandomPos.generateRandomPos(mob, radius, yRange, y, vec3.x, vec3.z, amplifier, flag);
            return blockpos != null && !GoalUtils.isWater(mob, blockpos) ? blockpos : null;
        });
    }

    @Override
    public void update(ServerLevel world, MKEntity entity) {
        Brain<?> brain = entity.getBrain();
        Optional<LivingEntity> targetOpt = brain.getMemory(MKMemoryModuleTypes.MOVEMENT_TARGET.get());
        Optional<WalkTarget> walkTargetOptional = brain.getMemory(MemoryModuleType.WALK_TARGET);
        if (walkTargetOptional.isPresent() && entity.getRandom().nextInt(40) != 0) {
            return;
        }
        if (targetOpt.isPresent()) {
            LivingEntity target = targetOpt.get();
            if (target.is(entity)) {
                brain.eraseMemory(MemoryModuleType.WALK_TARGET);
                return;
            }
            double distanceTo = entity.distanceTo(target);
            Vec3 targetPos = null;
            if (distanceTo > (1.5 * dist)) {
                targetPos = target.position();
            } else {
                targetPos = getTargetPosition(entity, target);
            }
            if (targetPos != null) {
                brain.setMemory(MemoryModuleType.WALK_TARGET,
                        new WalkTarget(targetPos, 1.0f, 1));

            }
        }

    }
}
