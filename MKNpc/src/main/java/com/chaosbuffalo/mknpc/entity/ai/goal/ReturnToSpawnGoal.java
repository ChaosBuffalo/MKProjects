package com.chaosbuffalo.mknpc.entity.ai.goal;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.entity.ai.memory.MKMemoryModuleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.core.BlockPos;

import java.util.EnumSet;
import java.util.Optional;

import net.minecraft.world.entity.ai.goal.Goal.Flag;

public class ReturnToSpawnGoal extends Goal {
    private final MKEntity entity;
    private static final int LEASH_RANGE = 30;
    private static final int MIN_RANGE = 1;
    private int ticksReturning;
    private static final int TICKS_TO_TELEPORT = 15 * GameConstants.TICKS_PER_SECOND;

    public ReturnToSpawnGoal(MKEntity entity) {
        this.entity = entity;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        this.ticksReturning = 0;
    }

    @Override
    public void tick() {
        super.tick();
        ticksReturning++;
        if (ticksReturning > TICKS_TO_TELEPORT){
            Optional<BlockPos> blockPosOpt = entity.getBrain().getMemory(MKMemoryModuleTypes.SPAWN_POINT);
            blockPosOpt.ifPresent(blockPos -> entity.teleportTo(blockPos.getX() + 0.5,
                    blockPos.getY(), blockPos.getZ() + 0.5));
        }
        if (this.entity.getNavigation().isDone()){
            Optional<BlockPos> blockPosOpt = entity.getBrain().getMemory(MKMemoryModuleTypes.SPAWN_POINT);
            if (blockPosOpt.isPresent()) {
                BlockPos spawn = blockPosOpt.get();
                Path path = entity.getNavigation().createPath(spawn, 1);
                entity.getNavigation().moveTo(path, 1.0);
                entity.getBrain().setMemory(MemoryModuleType.PATH, path);
            }
        }
        entity.returnToSpawnTick();
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public void stop() {
        super.stop();
        entity.getNavigation().stop();
        entity.getBrain().eraseMemory(MemoryModuleType.PATH);
        entity.getBrain().eraseMemory(MKMemoryModuleTypes.IS_RETURNING);
        entity.enterNonCombatMovementState();
    }

    private boolean needsToReturnHome(BlockPos spawn){
        Optional<LivingEntity> targetOpt = entity.getBrain().getMemory(MKMemoryModuleTypes.THREAT_TARGET);
        int distFromSpawn = spawn.distManhattan(entity.blockPosition());
        if (distFromSpawn <= MIN_RANGE * 2){
            return false;
        }
        if (targetOpt.isPresent()){
            return distFromSpawn > LEASH_RANGE;
        } else {
            if (entity.getNonCombatMoveType() == MKEntity.NonCombatMoveType.RANDOM_WANDER ){
                return distFromSpawn > LEASH_RANGE;
            }
            return true;
        }
    }

    public boolean canUse() {
        Optional<BlockPos> blockPosOpt = entity.getBrain().getMemory(MKMemoryModuleTypes.SPAWN_POINT);
        if (blockPosOpt.isPresent()){
            BlockPos spawn = blockPosOpt.get();
            if (needsToReturnHome(spawn)){
                Path path = entity.getNavigation().createPath(spawn, 1);
                entity.getNavigation().moveTo(path, 1.0);
                entity.getBrain().setMemory(MemoryModuleType.PATH, path);
                return true;
            }
        }
        return false;
    }

    public boolean canContinueToUse() {
        Optional<BlockPos> blockPosOpt = entity.getBrain().getMemory(MKMemoryModuleTypes.SPAWN_POINT);
        return blockPosOpt.map((pos) -> pos.distManhattan(entity.blockPosition()) > MIN_RANGE).orElse(false);
    }

    public void start() {
        ticksReturning = 0;
        entity.setTarget(null);
        entity.setLastHurtByMob(null);
        entity.getBrain().eraseMemory(MKMemoryModuleTypes.THREAT_TARGET);
        entity.getBrain().setMemory(MKMemoryModuleTypes.IS_RETURNING, true);
    }

}
