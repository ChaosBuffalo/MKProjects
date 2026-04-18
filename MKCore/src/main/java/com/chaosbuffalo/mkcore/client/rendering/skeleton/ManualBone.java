package com.chaosbuffalo.mkcore.client.rendering.skeleton;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import javax.annotation.Nullable;

public class ManualBone extends MCBone {
    private float yaw;
    private float pitch;
    private float roll;

    public ManualBone(String boneName, Vec3 boneLocation, float yaw, float pitch, float roll, @Nullable MCBone parent) {
        super(boneName, boneLocation, parent);
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
    }

    public ManualBone(String boneName, Vec3 boneLocation, @Nullable MCBone parent) {
        this(boneName, boneLocation, 0.0f, 0.0f, 0.0f, parent);
    }

    @Override
    public float getPitch() {
        return pitch;
    }

    @Override
    public float getYaw() {
        return yaw;
    }

    @Override
    public float getRoll() {
        return roll;
    }

    @Override
    public void applyLocalTransform(PoseStack poseStack) {
        Vec3 location = getBoneLocation();
        poseStack.translate(location.x, location.y, location.z);
        if (pitch != 0.0F || yaw != 0.0F || roll != 0.0F) {
            poseStack.mulPose(new Quaternionf().rotationZYX(roll, yaw, pitch));
        }
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }
}
