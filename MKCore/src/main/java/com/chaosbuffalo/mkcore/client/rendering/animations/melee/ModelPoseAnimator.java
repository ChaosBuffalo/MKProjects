package com.chaosbuffalo.mkcore.client.rendering.animations.melee;

import com.chaosbuffalo.mkcore.client.rendering.skeleton.MCSkeleton;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;

import java.util.function.BiPredicate;

public class ModelPoseAnimator {
    public record Context(float swing, float windup, float ageInTicks, float netHeadYaw, float headPitch,
                          HumanoidArm mainArm, InteractionHand attackHand, boolean dualWielding) {
        public float handedness() {
            return mainArm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        }

        public static Context strike(float swing, float ageInTicks, HumanoidArm mainArm, InteractionHand attackHand) {
            return strike(swing, ageInTicks, mainArm, attackHand, false);
        }

        public static Context strike(float swing, float ageInTicks, HumanoidArm mainArm, InteractionHand attackHand,
                                     boolean dualWielding) {
            return new Context(swing, 0.0F, ageInTicks, 0.0F, 0.0F, mainArm, attackHand, dualWielding);
        }

        public static Context strike(float swing, float ageInTicks, float netHeadYaw, float headPitch, HumanoidArm mainArm,
                                     InteractionHand attackHand) {
            return strike(swing, ageInTicks, netHeadYaw, headPitch, mainArm, attackHand, false);
        }

        public static Context strike(float swing, float ageInTicks, float netHeadYaw, float headPitch, HumanoidArm mainArm,
                                     InteractionHand attackHand, boolean dualWielding) {
            return new Context(swing, 0.0F, ageInTicks, netHeadYaw, headPitch, mainArm, attackHand, dualWielding);
        }

        public static Context windup(float windup, HumanoidArm mainArm, InteractionHand attackHand) {
            return windup(windup, mainArm, attackHand, false);
        }

        public static Context windup(float windup, HumanoidArm mainArm, InteractionHand attackHand, boolean dualWielding) {
            return new Context(0.0F, windup, 0.0F, 0.0F, 0.0F, mainArm, attackHand, dualWielding);
        }

        public static Context windup(float windup, float ageInTicks, float netHeadYaw, float headPitch, HumanoidArm mainArm,
                                     InteractionHand attackHand) {
            return windup(windup, ageInTicks, netHeadYaw, headPitch, mainArm, attackHand, false);
        }

        public static Context windup(float windup, float ageInTicks, float netHeadYaw, float headPitch, HumanoidArm mainArm,
                                     InteractionHand attackHand, boolean dualWielding) {
            return new Context(0.0F, windup, ageInTicks, netHeadYaw, headPitch, mainArm, attackHand, dualWielding);
        }

        public static Context idle(float ageInTicks, float netHeadYaw, float headPitch, HumanoidArm mainArm) {
            return new Context(0.0F, 0.0F, ageInTicks, netHeadYaw, headPitch, mainArm, InteractionHand.MAIN_HAND, false);
        }
    }

    public static void apply(MCSkeleton skeleton, ResourceLocation family, MeleeAnimationPose pose, Context context) {
        apply(skeleton, family, pose, context, (originalTarget, resolvedTarget) -> true);
    }

    public static void apply(MCSkeleton skeleton, ResourceLocation family, MeleeAnimationPose pose, Context context,
                             BiPredicate<String, String> targetFilter) {
        MeleeAnimationFamilyAdapter adapter = MeleeAnimationManager.getFamilyAdapter(family);
        for (PoseChannel channel : pose.channels()) {
            String resolvedTarget = adapter.resolveTarget(channel.target(), context);
            if (!targetFilter.test(channel.target(), resolvedTarget)) {
                continue;
            }
            if (!adapter.shouldApplyTarget(channel.target(), resolvedTarget, context)) {
                continue;
            }
            ModelPart part = skeleton.getModelPart(resolvedTarget);
            if (part == null) {
                continue;
            }
            float value = evaluate(channel.value(), context);
            value *= evaluateSign(channel.sign(), pose.options(), context);
            applyValue(part, channel.property(), channel.operation(), value);
        }
        adapter.afterApply(skeleton, pose, context);
    }

    private static float evaluateSign(PoseChannel.Sign sign, MeleeAnimationPose.PoseOptions options, Context context) {
        float swingDirection = options.swingDirection();
        return switch (sign) {
            case NEGATED_HANDEDNESS_TIMES_SWING_DIRECTION -> -context.handedness() * swingDirection;
            case HANDEDNESS_TIMES_SWING_DIRECTION -> context.handedness() * swingDirection;
            case HANDEDNESS -> context.handedness();
            case NEGATED_HANDEDNESS -> -context.handedness();
            case SWING_DIRECTION -> swingDirection;
            case NEGATED_SWING_DIRECTION -> -swingDirection;
            case NONE -> 1.0F;
        };
    }

    private static float evaluate(PoseChannel.PoseValue value, Context context) {
        float result = value.constant();
        for (PoseChannel.PoseTerm term : value.terms()) {
            float termValue = evaluateTerm(term, context);
            if (term.multiplierCurve() != PoseChannel.Curve.NONE) {
                termValue *= evaluateCurve(term.multiplierCurve(), term, context);
            }
            result += (term.absolute() ? Math.abs(termValue) : termValue) * term.scale();
        }
        return result;
    }

    private static float evaluateTerm(PoseChannel.PoseTerm term, Context context) {
        if (term.input() != PoseChannel.Input.NONE) {
            return switch (term.input()) {
                case HEAD_PITCH -> context.headPitch() * ((float) Math.PI / 180.0F);
                case NET_HEAD_YAW -> context.netHeadYaw() * ((float) Math.PI / 180.0F);
                case NONE -> 0.0F;
            };
        }
        return evaluateCurve(term.curve(), term, context);
    }

    private static float evaluateCurve(PoseChannel.Curve curve, PoseChannel.PoseTerm term, Context context) {
        return switch (curve) {
            case SWING_INVERSE -> 1.0F - context.swing();
            case SWING_SIN -> Mth.sin(context.swing() * (float) Math.PI);
            case IMPACT -> Mth.sin((1.0F - (1.0F - context.swing()) * (1.0F - context.swing())) * (float) Math.PI);
            case FOLLOW_THROUGH -> Mth.sin(Mth.clamp((context.swing() - 0.45F) / 0.55F, 0.0F, 1.0F) * ((float) Math.PI / 2.0F));
            case FOLLOW_THROUGH_SHORT -> Mth.sin(Mth.clamp((context.swing() - 0.35F) / 0.35F, 0.0F, 1.0F) * ((float) Math.PI / 2.0F));
            case WINDUP_SIN -> Mth.sin(context.windup() * ((float) Math.PI / 2.0F));
            case RELEASE_ARC -> Mth.cos((float) (Math.PI / 2.0F + context.windup() * Math.PI)) * ((float) Math.PI / 2.0F);
            case AGE_SIN -> Mth.sin(context.ageInTicks() * term.frequency()) + term.offset();
            case NONE -> 0.0F;
        };
    }

    private static void applyValue(ModelPart part, PoseChannel.Property property, PoseChannel.Operation operation, float value) {
        float current = getValue(part, property);
        float next = switch (operation) {
            case SET -> value;
            case ADD -> current + value;
            case MAX -> Math.max(current, value);
        };
        setValue(part, property, next);
    }

    private static float getValue(ModelPart part, PoseChannel.Property property) {
        return switch (property) {
            case X_ROT -> part.xRot;
            case Y_ROT -> part.yRot;
            case Z_ROT -> part.zRot;
            case X -> part.x;
            case Y -> part.y;
            case Z -> part.z;
        };
    }

    private static void setValue(ModelPart part, PoseChannel.Property property, float value) {
        switch (property) {
            case X_ROT -> part.xRot = value;
            case Y_ROT -> part.yRot = value;
            case Z_ROT -> part.zRot = value;
            case X -> part.x = value;
            case Y -> part.y = value;
            case Z -> part.z = value;
        }
    }
}
