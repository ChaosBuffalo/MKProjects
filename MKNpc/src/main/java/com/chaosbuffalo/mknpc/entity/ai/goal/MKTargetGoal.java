package com.chaosbuffalo.mknpc.entity.ai.goal;


import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.entity.ai.memory.MKMemoryModuleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;

import java.util.Optional;

public class MKTargetGoal extends TargetGoal {
    private final MKEntity entity;

    public MKTargetGoal(MKEntity mobIn, boolean checkSight, boolean nearbyOnlyIn) {
        super(mobIn, checkSight, nearbyOnlyIn);
        this.entity = mobIn;
    }

    @Override
    public boolean canUse() {
        Optional<LivingEntity> opt = mob.getBrain().getMemory(MKMemoryModuleTypes.THREAT_TARGET);
        if (opt.isPresent() && (this.targetMob == null || !this.targetMob.is(opt.get()))) {
            this.targetMob = opt.get();
            return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        Optional<LivingEntity> opt = mob.getBrain().getMemory(MKMemoryModuleTypes.THREAT_TARGET);
        return opt.isPresent() && opt.get().is(targetMob);
    }

    @Override
    public void stop() {
        super.stop();
        entity.setAggressive(false);
    }

    public void start() {
        this.mob.setTarget(this.targetMob);
        entity.setAggressive(true);
        entity.enterCombatMovementState(targetMob);
        super.start();
    }
}
