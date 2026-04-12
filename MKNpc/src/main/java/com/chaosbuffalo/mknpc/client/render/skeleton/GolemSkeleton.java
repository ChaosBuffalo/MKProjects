package com.chaosbuffalo.mknpc.client.render.skeleton;

import com.chaosbuffalo.mkcore.client.rendering.skeleton.BipedSkeleton;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.MCBone;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.MCSkeleton;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.ManualBone;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.ModelRendererBone;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class GolemSkeleton<T extends LivingEntity, M extends HumanoidModel<T>> extends MCSkeleton {
    private static final Vec3 PELVIS_OFFSET = new Vec3(0.0, -5.5 / 16.0, 0.0);
    private static final Vec3 CHEST_OFFSET = new Vec3(0.0, 8.5 / 16.0, 0.0);
    private static final Vec3 LEFT_HAND_OFFSET = new Vec3(11.0 / 16.0, -19.0 / 16.0, 0.0);
    private static final Vec3 RIGHT_HAND_OFFSET = new Vec3(-11.0 / 16.0, -19.0 / 16.0, 0.0);
    private static final Vec3 FOOT_OFFSET = new Vec3(0.0, -13.0 / 16.0, 0.0);
    private static final Vec3 HEAD_OFFSET = new Vec3(0.0, 7.0 / 16.0, 0.0);

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

    public GolemSkeleton(M model) {
        this.model = model;
        this.boneMap = new HashMap<>();

        MCBone rootBone = new ManualBone(BipedSkeleton.ROOT_BONE_NAME, Vec3.ZERO, null);
        root = rootBone;
        addBone(rootBone);

        this.body = new ModelRendererBone(BipedSkeleton.BODY_BONE_NAME, model.body, root, true, true, true);
        addBone(body);
        this.pelvis = new ManualBone(BipedSkeleton.PELVIS_BONE_NAME, PELVIS_OFFSET, root);
        addBone(pelvis);
        this.chest = new ManualBone(BipedSkeleton.CHEST_BONE_NAME, CHEST_OFFSET, pelvis);
        addBone(chest);

        this.rightArm = new ModelRendererBone(BipedSkeleton.RIGHT_ARM_BONE_NAME, model.rightArm, chest, true, false, false);
        addBone(rightArm);
        this.leftArm = new ModelRendererBone(BipedSkeleton.LEFT_ARM_BONE_NAME, model.leftArm, chest, true, false, false);
        addBone(leftArm);
        this.rightLeg = new ModelRendererBone(BipedSkeleton.RIGHT_LEG_BONE_NAME, model.rightLeg, root, false, false, true);
        addBone(rightLeg);
        this.leftLeg = new ModelRendererBone(BipedSkeleton.LEFT_LEG_BONE_NAME, model.leftLeg, root, false, false, true);
        addBone(leftLeg);

        this.leftHand = new ManualBone(BipedSkeleton.LEFT_HAND_BONE_NAME, LEFT_HAND_OFFSET, leftArm);
        addBone(leftHand);
        this.rightHand = new ManualBone(BipedSkeleton.RIGHT_HAND_BONE_NAME, RIGHT_HAND_OFFSET, rightArm);
        addBone(rightHand);

        this.leftFoot = new ManualBone(BipedSkeleton.LEFT_FOOT_BONE_NAME, FOOT_OFFSET, leftLeg);
        addBone(leftFoot);
        this.rightFoot = new ManualBone(BipedSkeleton.RIGHT_FOOT_BONE_NAME, FOOT_OFFSET, rightLeg);
        addBone(rightFoot);
        this.neck = new ModelRendererBone(BipedSkeleton.NECK_BONE_NAME, model.head, chest, true, false, false);
        addBone(neck);
        this.head = new ManualBone(BipedSkeleton.HEAD_BONE_NAME, HEAD_OFFSET, neck);
        addBone(head);
    }

    public M getModel() {
        return model;
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
