package com.chaosbuffalo.mknpc.client.render.skeleton;

import com.chaosbuffalo.mkcore.client.rendering.skeleton.BipedSkeleton;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.MCBone;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.MCSkeleton;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.ManualBone;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.ModelRendererBone;
import com.chaosbuffalo.mknpc.client.render.models.MKFireElementalModel;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class FireElementalSkeleton<T extends MKEntity, M extends MKFireElementalModel<T>> extends MCSkeleton {
    private static final Vec3 MODEL_ROOT_OFFSET = Vec3.ZERO;
    public static final String VORTEX_TOP_BONE_NAME = "vortex_top";
    public static final String VORTEX_MID_BONE_NAME = "vortex_mid";
    public static final String VORTEX_BOTTOM_BONE_NAME = "vortex_bottom";

    private final M model;
    private final Map<String, MCBone> boneMap;
    public final MCBone root;
    public final MCBone rightArm;
    public final MCBone leftArm;
    public final MCBone rightLeg;
    public final MCBone leftLeg;
    public final MCBone rightHand;
    public final MCBone leftHand;
    public final MCBone chest;
    public final MCBone leftFoot;
    public final MCBone rightFoot;
    public final MCBone neck;
    public final MCBone pelvis;
    public final MCBone body;
    public final MCBone head;
    public final MCBone vortexTop;
    public final MCBone vortexMid;
    public final MCBone vortexBottom;

    public FireElementalSkeleton(M model) {
        this.model = model;
        this.boneMap = new HashMap<>();

        MCBone rootBone = new ManualBone(BipedSkeleton.ROOT_BONE_NAME, MODEL_ROOT_OFFSET, null);
        root = rootBone;
        addBone(rootBone);

        this.body = new ModelRendererBone(BipedSkeleton.BODY_BONE_NAME, model.body, root, true, true, true);
        addBone(body);
        this.pelvis = new ManualBone(BipedSkeleton.PELVIS_BONE_NAME, new Vec3(0.0, 12.0 / 16.0, 0.0), root);
        addBone(pelvis);
        this.chest = new ManualBone(BipedSkeleton.CHEST_BONE_NAME, new Vec3(0.0, 12.0 / 16.0, 0.0), pelvis);
        addBone(chest);

        this.rightArm = new ModelRendererBone(BipedSkeleton.RIGHT_ARM_BONE_NAME, model.rightArm, root, true, false, false);
        addBone(rightArm);
        this.leftArm = new ModelRendererBone(BipedSkeleton.LEFT_ARM_BONE_NAME, model.leftArm, root, true, false, false);
        addBone(leftArm);
        this.rightLeg = new ModelRendererBone(BipedSkeleton.RIGHT_LEG_BONE_NAME, model.rightLeg, root, false, false, true);
        addBone(rightLeg);
        this.leftLeg = new ModelRendererBone(BipedSkeleton.LEFT_LEG_BONE_NAME, model.leftLeg, root, false, false, true);
        addBone(leftLeg);

        this.leftHand = new ManualBone(BipedSkeleton.LEFT_HAND_BONE_NAME, new Vec3(0.5 / 16.0, 10.0 / 16.0, -2.0 / 16.0), leftArm);
        addBone(leftHand);
        this.rightHand = new ManualBone(BipedSkeleton.RIGHT_HAND_BONE_NAME, new Vec3(-0.5 / 16.0, 10.0 / 16.0, -2.0 / 16.0), rightArm);
        addBone(rightHand);

        this.leftFoot = new ManualBone(BipedSkeleton.LEFT_FOOT_BONE_NAME, new Vec3(0.0, -12.0 / 16.0, 0.0), leftLeg);
        addBone(leftFoot);
        this.rightFoot = new ManualBone(BipedSkeleton.RIGHT_FOOT_BONE_NAME, new Vec3(0.0, -12.0 / 16.0, 0.0), rightLeg);
        addBone(rightFoot);
        this.neck = new ModelRendererBone(BipedSkeleton.NECK_BONE_NAME, model.head, root, true, false, false);
        addBone(neck);
        this.head = new ManualBone(BipedSkeleton.HEAD_BONE_NAME, new Vec3(0.0, 0.25, 0.0), neck);
        addBone(head);

        this.vortexTop = new ModelRendererBone(VORTEX_TOP_BONE_NAME, model.getVortexTop(), root, false, false, false);
        addBone(vortexTop);
        this.vortexMid = new ModelRendererBone(VORTEX_MID_BONE_NAME, model.getVortexMid(), root, false, false, false);
        addBone(vortexMid);
        this.vortexBottom = new ModelRendererBone(VORTEX_BOTTOM_BONE_NAME, model.getVortexBottom(), root, false, false, false);
        addBone(vortexBottom);
    }

    public M getModel() {
        return model;
    }

    public void addBone(MCBone bone) {
        this.boneMap.put(bone.getBoneName(), bone);
    }

    @Nullable
    @Override
    public MCBone getBone(String boneName) {
        return boneMap.get(boneName);
    }
}
