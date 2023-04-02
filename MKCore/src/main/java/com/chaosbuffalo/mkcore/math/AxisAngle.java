package com.chaosbuffalo.mkcore.math;

import com.chaosbuffalo.mkcore.utils.MathUtils;
import net.minecraft.world.phys.Vec3;

public class AxisAngle extends Vec3 {
    protected double angle;

    public AxisAngle(double angle, double xIn, double yIn, double zIn) {
        super(xIn, yIn, zIn);
        this.angle = (angle < 0.0 ? Math.PI + Math.PI + angle % (Math.PI + Math.PI) : angle) % (Math.PI + Math.PI);
    }

    public double getAngle() {
        return angle;
    }

    public Vec3 transform(Vec3 vector) {
        double sin = Math.sin(angle);
        double cos = MathUtils.cosFromSin(sin, angle);
        double dot = x * vector.x() + y * vector.y() + z * vector.z();
        return new Vec3(
                vector.x() * cos + sin * (y * vector.z() - z * vector.y()) + (1.0 - cos) * dot * x,
                vector.y() * cos + sin * (z * vector.x() - x * vector.z()) + (1.0 - cos) * dot * y,
                vector.z() * cos + sin * (x * vector.y() - y * vector.x()) + (1.0 - cos) * dot * z
        );
    }
}
