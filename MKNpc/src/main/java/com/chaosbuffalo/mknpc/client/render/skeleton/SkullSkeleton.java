package com.chaosbuffalo.mknpc.client.render.skeleton;

import com.chaosbuffalo.mkcore.client.rendering.skeleton.MCBone;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.MCSkeleton;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.ManualBone;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.ModelRendererBone;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class SkullSkeleton extends MCSkeleton {
    public static final String ROOT_BONE_NAME = "root";
    public static final String HEAD_BONE_NAME = "head";
    public static final String JAW_BONE_NAME = "jaw";

    private final Map<String, MCBone> boneMap = new HashMap<>();

    public SkullSkeleton(ModelPart head, ModelPart jaw) {
        MCBone root = new ManualBone(ROOT_BONE_NAME, new Vec3(0.0, 0.0, 0.0), null);
        addBone(root);
        MCBone headBone = new ModelRendererBone(HEAD_BONE_NAME, head, root, true, false, false);
        addBone(headBone);
        addBone(new ModelRendererBone(JAW_BONE_NAME, jaw, headBone, true, false, false));
    }

    private void addBone(MCBone bone) {
        boneMap.put(bone.getBoneName(), bone);
    }

    @Nullable
    @Override
    public MCBone getBone(String boneName) {
        return boneMap.get(boneName);
    }
}
