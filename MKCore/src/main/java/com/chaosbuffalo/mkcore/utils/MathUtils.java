package com.chaosbuffalo.mkcore.utils;

import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;
import net.minecraft.world.phys.Vec3;

public class MathUtils {

    public static float lerp(float v0, float v1, float t) {
        return (1.0f - t) * v0 + t * v1;
    }

    public static double lerpDouble(double v0, double v1, double t) {
        return (1.0 - t) * v0 + t * v1;
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(Math.min(max, value), min);
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    public static boolean isInteger(String str) {
        return str.matches("^([+-]?[1-9]\\d*|0)$");
    }

    public static Vec3 getTranslation(Matrix4f mat, Vec3 vecIn) {
        Vector4f vec = new Vector4f((float) vecIn.x(), (float) vecIn.y(), (float) vecIn.z(), 1.0f);
        vec.transform(mat);
        return new Vec3(vec.x(), vec.y(), vec.z());
    }

    public static double getAtanOffset(double y, double x) {
//        If the first argument is positive zero and the second argument is negative, or the first argument is positive and finite and the second argument is negative infinity, then the result is the double value closest to pi.
//        If the first argument is negative zero and the second argument is negative, or the first argument is negative and finite and the second argument is negative infinity, then the result is the double value closest to -pi.
//        If the first argument is positive and the second argument is positive zero or negative zero, or the first argument is positive infinity and the second argument is finite, then the result is the double value closest to pi/2.
//        If the first argument is negative and the second argument is positive zero or negative zero, or the first argument is negative infinity and the second argument is finite, then the result is the double value closest to -pi/2.

        if (x > 0) {
            return 0.0;
        } else if (y >= 0 && x < 0) {
            return Math.PI;
        } else if (y < 0 && x < 0) {
            return -Math.PI;
        } else if (y > 0 && x == 0) {
            return Math.PI / 2.0;
        } else if (y < 0 && x == 0) {
            return -Math.PI / 2.0;
        }
        return 0.0;
    }

    public static double getAngleAroundYAxis(double zCoord, double xCoord) {
        double value = Math.atan2(zCoord, xCoord);
        if (value < 0) {
            value += (2.0 * Math.PI);
        }
        return value;
    }

    public static double cosFromSin(double sin, double angle) {
        double cos = Math.sqrt(1.0 - sin * sin);
        double a = angle + (Math.PI / 2.0);
        double b = a - (int) (a / (2.0 * Math.PI)) * 2.0 * Math.PI;
        if (b < 0.0)
            b = 2.0 * Math.PI + b;
        if (b >= Math.PI)
            return -cos;
        return cos;
    }
}
