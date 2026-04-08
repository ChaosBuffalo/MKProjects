package com.chaosbuffalo.mkcore.client.rendering.animations.melee;

import com.chaosbuffalo.mkcore.client.rendering.skeleton.BipedSkeleton;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.MCSkeleton;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.HumanoidArm;

public interface MeleeAnimationFamilyAdapter {
    MeleeAnimationFamilyAdapter DEFAULT = new MeleeAnimationFamilyAdapter() {
    };

    MeleeAnimationFamilyAdapter BIPED = new MeleeAnimationFamilyAdapter() {
        @Override
        public String resolveTarget(String target, ModelPoseAnimator.Context context) {
            return switch (target) {
                case "main_arm" -> context.mainArm() == HumanoidArm.RIGHT ?
                        BipedSkeleton.RIGHT_ARM_BONE_NAME : BipedSkeleton.LEFT_ARM_BONE_NAME;
                case "off_arm" -> context.mainArm() == HumanoidArm.RIGHT ?
                        BipedSkeleton.LEFT_ARM_BONE_NAME : BipedSkeleton.RIGHT_ARM_BONE_NAME;
                default -> target;
            };
        }

        @Override
        public void afterApply(MCSkeleton skeleton, MeleeAnimationPose pose, ModelPoseAnimator.Context context) {
            if (pose.options().bobArms() && skeleton instanceof BipedSkeleton<?, ?> bipedSkeleton) {
                HumanoidModel<?> model = bipedSkeleton.getModel();
                AnimationUtils.bobArms(model.rightArm, model.leftArm, context.ageInTicks());
            }
        }
    };

    default String resolveTarget(String target, ModelPoseAnimator.Context context) {
        return target;
    }

    default void afterApply(MCSkeleton skeleton, MeleeAnimationPose pose, ModelPoseAnimator.Context context) {
    }
}
