package com.chaosbuffalo.mkcore.client.rendering.skeleton;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class ModelRendererBone extends MCBone {
    private final ModelPart modelRenderer;
    private final boolean invertY;
    private final boolean invertX;
    private final boolean invertZ;

    public ModelRendererBone(String boneName, ModelPart modelRenderer, @Nullable MCBone parent, boolean invertY, boolean invertX, boolean invertZ) {
        super(boneName, new Vec3(modelRenderer.x / 16.0,
                modelRenderer.y / 16.0, modelRenderer.z / 16.0), parent);
        this.modelRenderer = modelRenderer;
        this.invertY = invertY;
        this.invertX = invertX;
        this.invertZ = invertZ;

    }

    public ModelPart getModelRenderer() {
        return modelRenderer;
    }

    @Override
    public Vec3 getBoneLocation() {

        return new Vec3(
                (invertX ? -1.0 : 1.0) * modelRenderer.x / 16.0,
                (invertY ? -1.0 : 1.0) * modelRenderer.y / 16.0,
                (invertZ ? -1.0 : 1.0) * modelRenderer.z / 16.0
        );
//        return invertY ? new Vector3d(-modelRenderer.rotationPointX / 16.0, -modelRenderer.rotationPointY / 16.0, -modelRenderer.rotationPointZ / 16.0)
//                : new Vector3d(modelRenderer.rotationPointX / 16.0, modelRenderer.rotationPointY / 16.0, modelRenderer.rotationPointZ / 16.0) ;
    }

    @Override
    public float getPitch() {
        return modelRenderer.xRot;
    }

    @Override
    public float getYaw() {
        return modelRenderer.yRot;
    }

    @Override
    public float getRoll() {
        return modelRenderer.zRot;
    }
}
