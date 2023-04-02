package com.chaosbuffalo.mknpc.entity.ai.goal;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Optional;

import net.minecraft.world.entity.ai.goal.Goal.Flag;

public class MovementGoal extends Goal {

    @Nullable
    private Path path;
    @Nullable
    private BlockPos blockPos;
    private float speed;
    private final Mob entity;

    public MovementGoal(PathfinderMob creature) {
        this.entity = creature;
        speed = 1.0f;
        setFlags(EnumSet.of(Flag.MOVE));
    }


    @Override
    public boolean canUse() {
        Brain<?> brain = entity.getBrain();
        Optional<WalkTarget> targetOpt = brain.getMemory(MemoryModuleType.WALK_TARGET);
        if (targetOpt.isPresent()) {
            WalkTarget walkTarget = targetOpt.get();
            if (!this.hasReachedTarget(walkTarget)) {
                this.blockPos = walkTarget.getTarget().currentBlockPosition();
                Path path = entity.getNavigation().createPath(blockPos, 0);
                this.speed = walkTarget.getSpeedModifier();
                if (this.path != path) {
                    this.path = path;
                    entity.getNavigation().moveTo(path, speed);
                    brain.setMemory(MemoryModuleType.PATH, path);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        Brain<?> brain = entity.getBrain();
        Optional<WalkTarget> targetOpt = brain.getMemory(MemoryModuleType.WALK_TARGET);
        if (targetOpt.isPresent()) {
            WalkTarget walkTarget = targetOpt.get();
            if (this.entity.getNavigation().isDone()) {
                return false;
            }
            if (hasReachedTarget(walkTarget)) {
                return false;
            }
            return this.blockPos == null || this.blockPos.equals(walkTarget.getTarget().currentBlockPosition());
        }
        return false;
    }

    @Override
    public void stop() {
        super.stop();
        entity.getNavigation().stop();
        entity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        entity.getBrain().eraseMemory(MemoryModuleType.PATH);
        this.path = null;
        this.blockPos = null;
    }


    public void tick() {

    }


    private boolean hasReachedTarget(WalkTarget target) {
        return target.getTarget().currentBlockPosition().distManhattan(entity.blockPosition()) <= target.getCloseEnoughDist();
    }


}
