package com.chaosbuffalo.mkcore.client.rendering.skeleton;

import net.minecraft.client.model.geom.ModelPart;

import javax.annotation.Nullable;

public abstract class MCSkeleton {

    @Nullable
    public abstract MCBone getBone(String boneName);

    @Nullable
    public ModelPart getModelPart(String boneName) {
        MCBone bone = getBone(boneName);
        if (bone instanceof ModelRendererBone modelBone) {
            return modelBone.getModelRenderer();
        }
        return null;
    }

}
