package com.chaosbuffalo.mknpc.entity.ai.goal;

import com.chaosbuffalo.mknpc.entity.ai.memory.MKMemoryModuleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.Optional;

import net.minecraft.world.entity.ai.goal.Goal.Flag;

public class LookAtThreatTargetGoal extends Goal {
    private final Mob entity;
    private LivingEntity target;

    public LookAtThreatTargetGoal(Mob entity) {
        this.entity = entity;
        setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        Optional<LivingEntity> target = entity.getBrain().getMemory(MKMemoryModuleTypes.THREAT_TARGET);
        if (target.isPresent()) {
            this.target = target.get();
            return true;
        }
        return false;
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public boolean canContinueToUse() {
        Optional<LivingEntity> target = entity.getBrain().getMemory(MKMemoryModuleTypes.THREAT_TARGET);
        return target.isPresent() && this.target != null && this.target.is(target.get());
    }

    @Override
    public void stop() {
        this.target = null;
    }

    @Override
    public void tick() {
        this.entity.getLookControl().setLookAt(this.target.getX(), this.target.getEyeY(),
                this.target.getZ());
    }
}
