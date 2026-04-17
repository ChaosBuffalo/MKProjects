package com.chaosbuffalo.mkcore.client.rendering.skeleton;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;

import javax.annotation.Nullable;
import java.util.Optional;

public abstract class MCBone {

    private final String boneName;
    private final MCBone parent;
    private final Vec3 boneLocation;

    public MCBone(String boneName, Vec3 boneLocation, @Nullable MCBone parent) {
        this.boneName = boneName;
        this.parent = parent;
        this.boneLocation = boneLocation;
    }

    public boolean hasParent() {
        return this.parent != null;
    }

    @Nullable
    public MCBone getParent() {
        return parent;
    }

    public String getBoneName() {
        return boneName;
    }

    public Vec3 getBoneLocation() {
        return boneLocation;
    }

    public abstract float getPitch();

    public abstract float getYaw();

    public abstract float getRoll();

    public abstract void applyLocalTransform(PoseStack poseStack);

    private static void applyTransformChain(PoseStack poseStack, MCBone bone) {
        if (bone.hasParent()) {
            MCBone parent = bone.getParent();
            if (parent != null) {
                applyTransformChain(poseStack, parent);
            }
        }
        bone.applyLocalTransform(poseStack);
    }

    private static Vec3 getPositionFromPoseStack(PoseStack poseStack) {
        Vector4f position = new Vector4f(0.0F, 0.0F, 0.0F, 1.0F);
        poseStack.last().pose().transform(position);
        return new Vec3(position.x(), position.y(), position.z());
    }

    public static Vec3 getOffsetForStopAt(MCBone bone, MCBone stopAt) {
        PoseStack poseStack = new PoseStack();
        MCBone currentBone = bone;
        while (currentBone != null) {
            currentBone.applyLocalTransform(poseStack);
            if (currentBone.equals(stopAt)) {
                break;
            }
            currentBone = currentBone.getParent();
        }
        return getPositionFromPoseStack(poseStack);
    }

    public static Vec3 getOffsetForBone(MCBone bone) {
        PoseStack poseStack = new PoseStack();
        applyTransformChain(poseStack, bone);
        return getPositionFromPoseStack(poseStack);
    }

    private static float sleepDirectionToRotation(Direction facing) {
        return switch (facing) {
            case SOUTH -> 90.0F;
            case WEST -> 0.0F;
            case NORTH -> 270.0F;
            case EAST -> 180.0F;
            default -> 0.0F;
        };
    }

    private static boolean isEntityUpsideDown(LivingEntity entity) {
        if (entity instanceof Player || entity.hasCustomName()) {
            String strippedName = ChatFormatting.stripFormatting(entity.getName().getString());
            if ("Dinnerbone".equals(strippedName) || "Grumm".equals(strippedName)) {
                return !(entity instanceof Player player) || player.isModelPartShown(PlayerModelPart.CAPE);
            }
        }
        return false;
    }

    private static void applyEntityRootTransform(LivingEntity entity, PoseStack poseStack, float partialTicks) {
        float bodyYaw = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        float headYaw = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
        float headBodyDelta = Mth.wrapDegrees(headYaw - bodyYaw);
        if (entity.isPassenger() && entity.getVehicle() instanceof LivingEntity vehicle && entity.getVehicle().shouldRiderSit()) {
            bodyYaw = Mth.rotLerp(partialTicks, vehicle.yBodyRotO, vehicle.yBodyRot);
            headBodyDelta = Mth.wrapDegrees(headYaw - bodyYaw);
            headBodyDelta = Mth.clamp(headBodyDelta, -85.0F, 85.0F);
            bodyYaw = headYaw - headBodyDelta;
            if (headBodyDelta * headBodyDelta > 2500.0F) {
                bodyYaw += headBodyDelta * 0.2F;
            }
        }

        float pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        if (isEntityUpsideDown(entity)) {
            pitch *= -1.0F;
        }

        if (entity.hasPose(Pose.SLEEPING)) {
            Direction direction = entity.getBedOrientation();
            if (direction != null) {
                float eyeOffset = entity.getEyeHeight(Pose.STANDING) - 0.1F;
                poseStack.translate((float) (-direction.getStepX()) * eyeOffset, 0.0F, (float) (-direction.getStepZ()) * eyeOffset);
            }
        }

        float scale = entity.getScale();
        poseStack.scale(scale, scale, scale);

        if (entity instanceof AbstractClientPlayer player) {
            float swimAmount = player.getSwimAmount(partialTicks);
            float viewXRot = player.getViewXRot(partialTicks);
            if (player.isFallFlying()) {
                if (entity.isFullyFrozen()) {
                    bodyYaw += (float) (Math.cos((double) entity.tickCount * 3.25) * Math.PI * 0.4F);
                }
                if (!entity.hasPose(Pose.SLEEPING)) {
                    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYaw));
                }
                poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F - viewXRot));
                poseStack.mulPose(Axis.YP.rotationDegrees(((float) entity.tickCount + partialTicks) * -75.0F));
            } else if (swimAmount > 0.0F) {
                if (entity.isFullyFrozen()) {
                    bodyYaw += (float) (Math.cos((double) entity.tickCount * 3.25) * Math.PI * 0.4F);
                }
                if (!entity.hasPose(Pose.SLEEPING)) {
                    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYaw));
                }
                float swimPitch = player.isInWater() || player.isInFluidType((fluidType, height) -> player.canSwimInFluidType(fluidType))
                        ? -90.0F - viewXRot
                        : -90.0F;
                poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(swimAmount, 0.0F, swimPitch)));
                if (player.isVisuallySwimming()) {
                    poseStack.translate(0.0F, -1.0F, 0.3F);
                }
            } else {
                applyGenericSetupRotations(entity, poseStack, partialTicks, bodyYaw, pitch, scale);
            }
        } else {
            applyGenericSetupRotations(entity, poseStack, partialTicks, bodyYaw, pitch, scale);
        }

        poseStack.scale(-1.0F, -1.0F, 1.0F);
        if (entity instanceof AbstractClientPlayer) {
            poseStack.scale(0.9375F, 0.9375F, 0.9375F);
        }
        poseStack.translate(0.0F, -1.501F, 0.0F);
    }

    private static void applyGenericSetupRotations(LivingEntity entity, PoseStack poseStack, float partialTicks,
                                                   float bodyYaw, float pitch, float scale) {
        if (entity.isFullyFrozen()) {
            bodyYaw += (float) (Math.cos((double) entity.tickCount * 3.25) * Math.PI * 0.4F);
        }

        if (!entity.hasPose(Pose.SLEEPING)) {
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYaw));
        }

        if (entity.deathTime > 0) {
            float deathProgress = ((float) entity.deathTime + partialTicks - 1.0F) / 20.0F * 1.6F;
            deathProgress = Mth.sqrt(deathProgress);
            deathProgress = Math.min(deathProgress, 1.0F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(deathProgress * 90.0F));
        } else if (entity.isAutoSpinAttack()) {
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F - pitch));
            poseStack.mulPose(Axis.YP.rotationDegrees(((float) entity.tickCount + partialTicks) * -75.0F));
        } else if (entity.hasPose(Pose.SLEEPING)) {
            Direction direction = entity.getBedOrientation();
            float sleepYaw = direction != null ? sleepDirectionToRotation(direction) : bodyYaw;
            poseStack.mulPose(Axis.YP.rotationDegrees(sleepYaw));
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
        } else if (isEntityUpsideDown(entity)) {
            poseStack.translate(0.0F, (entity.getBbHeight() + 0.1F) / scale, 0.0F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        }
    }

    public static Optional<Vec3> getPositionOfBoneInWorld(LivingEntity entityIn, MCSkeleton skeleton,
                                                          float partialTicks, Vec3 renderOffset, String boneName) {
        MCBone bone = skeleton.getBone(boneName);
        if (bone != null) {
            double entX = Mth.lerp(partialTicks, entityIn.xo, entityIn.getX());
            double entY = Mth.lerp(partialTicks, entityIn.yo, entityIn.getY());
            double entZ = Mth.lerp(partialTicks, entityIn.zo, entityIn.getZ());
            PoseStack poseStack = new PoseStack();
            poseStack.translate(entX + renderOffset.x, entY + renderOffset.y, entZ + renderOffset.z);
            applyEntityRootTransform(entityIn, poseStack, partialTicks);
            applyTransformChain(poseStack, bone);
            return Optional.of(getPositionFromPoseStack(poseStack));
        } else {
            return Optional.empty();
        }


    }

}
