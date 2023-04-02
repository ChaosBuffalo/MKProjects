package com.chaosbuffalo.mknpc.entity.ai.goal;

import com.chaosbuffalo.mknpc.entity.ai.MovementUtils;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.phys.Vec3;

public class MKSwimGoal extends FloatGoal {
    private PathfinderMob entity;
    private boolean walkOutPath;

    public MKSwimGoal(PathfinderMob entityIn) {
        super(entityIn);
        this.entity = entityIn;
        this.walkOutPath = false;
    }

    @Override
    public void start() {
        super.start();
        setWalkTargetOut();
    }



    private void setWalkTargetOut(){
        Vec3 targetPos = MovementUtils.findRandomTargetBlockAwayFromNoWater(
                entity, 8, 5, entity.position());
        if (targetPos != null){
            entity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(targetPos, 1.0f, 1));
            this.walkOutPath = true;
        } else {
            walkOutPath = false;
        }
    }

    @Override
    public void tick() {
        if (this.entity.getRandom().nextFloat() < 0.01F) {
            this.entity.getJumpControl().jump();
        }
        if (!walkOutPath || entity.getNavigation().isDone()){
            setWalkTargetOut();
        }
    }
}
