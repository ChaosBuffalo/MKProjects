package com.chaosbuffalo.mkcore.client.rendering.animations;

import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

public class BipedMeleeSwingAnimation {
    public static void apply(HumanoidModel<?> model, HumanoidArm mainArm, float swing, int swingVariant, float ageInTicks) {
        ModelPart weaponArm = mainArm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;
        ModelPart offArm = mainArm == HumanoidArm.RIGHT ? model.leftArm : model.rightArm;
        float swingSin = Mth.sin(swing * (float) Math.PI);
        float impactCurve = Mth.sin((1.0F - (1.0F - swing) * (1.0F - swing)) * (float) Math.PI);
        float followThrough = Mth.sin(Mth.clamp((swing - 0.45F) / 0.55F, 0.0F, 1.0F) * ((float) Math.PI / 2.0F));
        float handedness = mainArm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        float bodyYawBase;
        float bodyYawSwing;
        float bodyYawFollow;
        float weaponYawBase;
        float weaponYawSwing;
        float weaponYawFollow;
        float weaponPitchBase;
        float weaponPitchSwing;
        float weaponPitchImpact;
        float weaponPitchFollow;
        float weaponRollBase;
        float weaponRollSwing;
        float weaponRollFollow;
        float offArmYawBase;
        float offArmYawSwing;
        float offArmPitchBase;
        float offArmPitchSwing;
        float offArmPitchImpact;
        float offArmRollBase;
        float offArmRollSwing;
        float headPitchSwing;
        float headPitchFollow;
        float headYawFollowScale;
        float weaponRollDirection;
        float offArmRollDirection;
        float swingDirection;

        switch (Math.floorMod(swingVariant, 3)) {
            case 1 -> {
                bodyYawBase = 0.03F;
                bodyYawSwing = 0.05F;
                bodyYawFollow = 0.04F;
                weaponYawBase = 0.04F;
                weaponYawSwing = 0.06F;
                weaponYawFollow = 0.04F;
                weaponPitchBase = -2.3F;
                weaponPitchSwing = 1.0F;
                weaponPitchImpact = 0.75F;
                weaponPitchFollow = 2.85F;
                weaponRollBase = 0.08F;
                weaponRollSwing = 0.06F;
                weaponRollFollow = 0.06F;
                offArmYawBase = 0.05F;
                offArmYawSwing = 0.08F;
                offArmPitchBase = -1.05F;
                offArmPitchSwing = 0.05F;
                offArmPitchImpact = 0.04F;
                offArmRollBase = 0.05F;
                offArmRollSwing = 0.04F;
                headPitchSwing = 0.04F;
                headPitchFollow = 0.1F;
                headYawFollowScale = 0.12F;
                weaponRollDirection = 1.0F;
                offArmRollDirection = -1.0F;
                swingDirection = 1.0F;
            }
            case 2 -> {
                bodyYawBase = 0.12F;
                bodyYawSwing = 0.16F;
                bodyYawFollow = 0.11F;
                weaponYawBase = 0.24F;
                weaponYawSwing = 0.22F;
                weaponYawFollow = 0.12F;
                weaponPitchBase = -1.7F;
                weaponPitchSwing = 0.85F;
                weaponPitchImpact = 0.55F;
                weaponPitchFollow = 2.45F;
                weaponRollBase = 0.4F;
                weaponRollSwing = 0.3F;
                weaponRollFollow = 0.18F;
                offArmYawBase = 0.1F;
                offArmYawSwing = 0.12F;
                offArmPitchBase = -0.9F;
                offArmPitchSwing = 0.08F;
                offArmPitchImpact = 0.06F;
                offArmRollBase = 0.08F;
                offArmRollSwing = 0.06F;
                headPitchSwing = 0.06F;
                headPitchFollow = 0.08F;
                headYawFollowScale = 0.24F;
                weaponRollDirection = -1.0F;
                offArmRollDirection = 1.0F;
                swingDirection = -1.0F;
            }
            default -> {
                bodyYawBase = 0.12F;
                bodyYawSwing = 0.16F;
                bodyYawFollow = 0.11F;
                weaponYawBase = 0.24F;
                weaponYawSwing = 0.22F;
                weaponYawFollow = 0.12F;
                weaponPitchBase = -1.7F;
                weaponPitchSwing = 0.85F;
                weaponPitchImpact = 0.55F;
                weaponPitchFollow = 2.45F;
                weaponRollBase = 0.4F;
                weaponRollSwing = 0.3F;
                weaponRollFollow = 0.18F;
                offArmYawBase = 0.08F;
                offArmYawSwing = 0.1F;
                offArmPitchBase = -0.95F;
                offArmPitchSwing = 0.1F;
                offArmPitchImpact = 0.08F;
                offArmRollBase = 0.06F;
                offArmRollSwing = 0.06F;
                headPitchSwing = 0.06F;
                headPitchFollow = 0.08F;
                headYawFollowScale = 0.2F;
                weaponRollDirection = 1.0F;
                offArmRollDirection = -1.0F;
                swingDirection = 1.0F;
            }
        }

        model.rightArm.zRot = 0.0F;
        model.leftArm.zRot = 0.0F;

        model.body.yRot = -handedness * swingDirection * (bodyYawBase + swingSin * bodyYawSwing + followThrough * bodyYawFollow);
        weaponArm.yRot = -handedness * swingDirection * (weaponYawBase + swingSin * weaponYawSwing + followThrough * weaponYawFollow);
        offArm.yRot = handedness * swingDirection * (offArmYawBase + swingSin * offArmYawSwing);

        weaponArm.xRot = weaponPitchBase - swingSin * weaponPitchSwing - impactCurve * weaponPitchImpact + followThrough * weaponPitchFollow;
        offArm.xRot = offArmPitchBase + swingSin * offArmPitchSwing - impactCurve * offArmPitchImpact;

        weaponArm.zRot = handedness * weaponRollDirection * (weaponRollBase + swingSin * weaponRollSwing + followThrough * weaponRollFollow);
        offArm.zRot = handedness * offArmRollDirection * (offArmRollBase + swingSin * offArmRollSwing);

        model.head.xRot += swingSin * headPitchSwing + followThrough * headPitchFollow;
        model.head.yRot += model.body.yRot * headYawFollowScale;

        AnimationUtils.bobArms(model.rightArm, model.leftArm, ageInTicks);
    }
}
