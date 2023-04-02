package com.chaosbuffalo.mkcore.utils;

import com.chaosbuffalo.mkcore.GameConstants;
import net.minecraft.world.phys.Vec3;

public class ProjectileUtils {

    public static class BallisticResult {
        public Vec3 lowArc;
        public Vec3 highArc;
        public boolean hasHighArc;
        public boolean foundSolution;

        public BallisticResult(Vec3 lowArc, Vec3 highArc) {
            this.lowArc = lowArc;
            this.highArc = highArc;
            this.hasHighArc = true;
            this.foundSolution = true;
        }

        public BallisticResult(Vec3 lowArc) {
            this.lowArc = lowArc;
            this.hasHighArc = false;
            this.foundSolution = true;
        }

        public BallisticResult() {
            this.foundSolution = false;
            this.hasHighArc = false;
        }
    }

    public static BallisticResult solveBallisticArcStationaryTarget(Vec3 projPos, Vec3 target,
                                                                    float tickVelocity, float tickGravity) {
        Vec3 diff = target.subtract(projPos);
        Vec3 diffXZ = new Vec3(diff.x, 0.0, diff.z);
        double groundDist = diffXZ.length();

        double vel = tickVelocity * GameConstants.TICKS_PER_SECOND;
        double seconds = groundDist / vel;
        double heightLostToGravity = tickGravity * GameConstants.TICKS_PER_SECOND * seconds;

        double yDiff = diff.y;
        double yWithGravity = yDiff + heightLostToGravity;

        Vec3 targetPos = new Vec3(diff.x(), yWithGravity, diff.z());
        return new BallisticResult(targetPos);
    }
}
