package com.chaosbuffalo.mknpc.entity.ai;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class MovementUtils {

    @Nullable
    public static Vec3 findRandomTargetBlockAwayFromNoWater(PathfinderMob entity, int xz, int y, Vec3 targetPos) {
        return LandRandomPos.getPosAway(entity, xz, y, targetPos);
    }

    @Nullable
    public static Vec3 findRandomTargetBlockTowardsNoWater(PathfinderMob entity, int xz, int y, Vec3 targetPos){
        return LandRandomPos.getPosTowards(entity, xz, y, targetPos);
    }
}
