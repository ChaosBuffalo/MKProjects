package com.chaosbuffalo.mkcore.client.rendering.skeleton;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class BipedSkeleton<T extends LivingEntity, M extends HumanoidModel<T>> extends MCSkeleton {
    public static final String ROOT_BONE_NAME = "root";
    public static final String RIGHT_ARM_BONE_NAME = "rightArm";
    public static final String LEFT_ARM_BONE_NAME = "leftArm";
    public static final String RIGHT_LEG_BONE_NAME = "rightLeg";
    public static final String LEFT_LEG_BONE_NAME = "leftLeg";
    public static final String LEFT_HAND_BONE_NAME = "leftHand";
    public static final String RIGHT_HAND_BONE_NAME = "rightHand";
    public static final String RIGHT_FOOT_BONE_NAME = "rightFoot";
    public static final String LEFT_FOOT_BONE_NAME = "leftFoot";
    public static final String CHEST_BONE_NAME = "chest";
    public static final String NECK_BONE_NAME = "neck";
    public static final String PELVIS_BONE_NAME = "pelvis";
    public static final String BODY_BONE_NAME = "body";
    public static final String HEAD_BONE_NAME = "head";

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


    public BipedSkeleton(M model) {
        this.model = model;
        this.boneMap = new HashMap<>();
        MCBone rootBone = new ManualBone(ROOT_BONE_NAME, new Vec3(0, 0, 0), null);
        root = rootBone;
        addBone(rootBone);
        this.body = new ModelRendererBone(BODY_BONE_NAME, model.body, root, true, true, true);
        addBone(body);
        this.pelvis = new ManualBone(PELVIS_BONE_NAME, new Vec3(0.0, 12.0 / 16.0, 0.0), root);
        addBone(pelvis);
        this.chest = new ManualBone(CHEST_BONE_NAME, new Vec3(0.0, 12.0 / 16.0, 0.0), pelvis);
        addBone(chest);
        MCBone rightArm = new ModelRendererBone(RIGHT_ARM_BONE_NAME, model.rightArm, chest, true, false, false);
        addBone(rightArm);
        this.rightArm = rightArm;
        MCBone leftArm = new ModelRendererBone(LEFT_ARM_BONE_NAME, model.leftArm, chest, true, false, false);
        addBone(leftArm);
        this.leftArm = leftArm;
        MCBone rightLeg = new ModelRendererBone(RIGHT_LEG_BONE_NAME, model.rightLeg, root, false, false, true);
        addBone(rightLeg);
        this.rightLeg = rightLeg;
        MCBone leftLeg = new ModelRendererBone(LEFT_LEG_BONE_NAME, model.leftLeg, root, false, false, true);
        addBone(leftLeg);
        this.leftLeg = leftLeg;
        MCBone leftHand = new ManualBone(LEFT_HAND_BONE_NAME, new Vec3(1.0 / 16.0, -10.0 / 16.0, 0), leftArm);
        addBone(leftHand);
        this.leftHand = leftHand;
        MCBone rightHand = new ManualBone(RIGHT_HAND_BONE_NAME, new Vec3(-1.0 / 16.0, -10.0 / 16.0, 0), rightArm);
        addBone(rightHand);
        this.rightHand = rightHand;

        this.leftFoot = new ManualBone(LEFT_FOOT_BONE_NAME, new Vec3(0.0, -12.0 / 16.0, 0.0), leftLeg);
        addBone(leftFoot);
        this.rightFoot = new ManualBone(RIGHT_FOOT_BONE_NAME, new Vec3(0.0, -12.0 / 16.0, 0.0), rightLeg);
        addBone(rightFoot);
        this.neck = new ModelRendererBone(NECK_BONE_NAME, model.head, chest, true, false, false);
        addBone(neck);
        this.head = new ManualBone(HEAD_BONE_NAME, new Vec3(0.0, 0.25, 0.0), neck);
        addBone(head);
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
