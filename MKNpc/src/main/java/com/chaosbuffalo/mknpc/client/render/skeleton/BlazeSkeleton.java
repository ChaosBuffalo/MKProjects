package com.chaosbuffalo.mknpc.client.render.skeleton;

import com.chaosbuffalo.mkcore.client.rendering.skeleton.MCBone;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.MCSkeleton;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.ManualBone;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.ModelRendererBone;
import net.minecraft.client.model.BlazeModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class BlazeSkeleton<T extends Entity, M extends BlazeModel<T>> extends MCSkeleton {
    public static final String ROOT_BONE_NAME = "root";
    public static final String HEAD_BONE_NAME = "head";
    public static final String PART_BONE_PREFIX = "part";
    public static final String RIGHT_HAND_BONE_NAME = "rightHand";
    public static final String LEFT_HAND_BONE_NAME = "leftHand";

    private final M model;
    private final Map<String, MCBone> boneMap;

    public final MCBone root;
    public final MCBone head;
    public final MCBone[] upperBodyParts;
    public final MCBone rightHand;
    public final MCBone leftHand;

    public BlazeSkeleton(M model) {
        this.model = model;
        this.boneMap = new HashMap<>();

        this.root = new ManualBone(ROOT_BONE_NAME, Vec3.ZERO, null);
        addBone(root);

        this.head = new ModelRendererBone(HEAD_BONE_NAME, model.root().getChild(HEAD_BONE_NAME), root, false, false, false);
        addBone(head);

        this.upperBodyParts = new MCBone[12];
        for (int i = 0; i < upperBodyParts.length; i++) {
            String partName = PART_BONE_PREFIX + i;
            MCBone partBone = new ModelRendererBone(partName, model.root().getChild(partName), root, false, false, false);
            upperBodyParts[i] = partBone;
            addBone(partBone);
        }

        this.rightHand = upperBodyParts[8];
        this.leftHand = upperBodyParts[10];
        addAliasBone(RIGHT_HAND_BONE_NAME, rightHand);
        addAliasBone(LEFT_HAND_BONE_NAME, leftHand);
    }

    public M getModel() {
        return model;
    }

    public void addBone(MCBone bone) {
        this.boneMap.put(bone.getBoneName(), bone);
    }

    public void addAliasBone(String aliasName, MCBone bone) {
        this.boneMap.put(aliasName, bone);
    }

    @Nullable
    @Override
    public MCBone getBone(String boneName) {
        return boneMap.get(boneName);
    }
}
