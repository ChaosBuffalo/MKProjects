package com.chaosbuffalo.mkcore.client.rendering.skeleton;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

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

    public static Vec3 getOffsetForStopAt(MCBone bone, MCBone stopAt) {
        MCBone currentBone = bone;
        Vec3 finalLoc = bone.getBoneLocation();
        while (currentBone != null && currentBone.hasParent()) {
            MCBone parent = currentBone.getParent();
            if (parent != null) {
                finalLoc = finalLoc.xRot(-parent.getPitch());
                finalLoc = finalLoc.yRot(parent.getYaw());
                finalLoc = finalLoc.zRot(parent.getRoll());
                finalLoc = finalLoc.add(parent.getBoneLocation());
            }
            if (currentBone.equals(stopAt)) {
                return finalLoc;
            }
            currentBone = parent;
        }
        return finalLoc;
    }

    public static Vec3 getOffsetForBone(MCBone bone) {
        MCBone currentBone = bone;
        Vec3 finalLoc = bone.getBoneLocation();
        while (currentBone != null && currentBone.hasParent()) {
            MCBone parent = currentBone.getParent();
            if (parent != null) {
                finalLoc = finalLoc.xRot(-parent.getPitch());
                finalLoc = finalLoc.yRot(parent.getYaw());
                finalLoc = finalLoc.zRot(parent.getRoll());
                finalLoc = finalLoc.add(parent.getBoneLocation());
            }
            currentBone = parent;
        }
        return finalLoc;
    }


    public static Optional<Vec3> getPositionOfBoneInWorld(LivingEntity entityIn, MCSkeleton skeleton,
                                                          float partialTicks, Vec3 renderOffset, String boneName) {
        MCBone bone = skeleton.getBone(boneName);
        if (bone != null) {
            double entX = Mth.lerp(partialTicks, entityIn.xo, entityIn.getX());
            double entY = Mth.lerp(partialTicks, entityIn.yo, entityIn.getY());
            double entZ = Mth.lerp(partialTicks, entityIn.zo, entityIn.getZ());
            float yaw = Mth.lerp(partialTicks, entityIn.yBodyRotO, entityIn.yBodyRot) * ((float) Math.PI / 180F);

            //we need to handle swimming rots
            float swimTime = entityIn.getSwimAmount(partialTicks);
            float pitch = 0.0f;
            Vec3 boneOffset = new Vec3(0.0, 0.0, 0.0);


            Vec3 bonePos = MCBone.getOffsetForBone(bone);

            if (swimTime > 0.0f) {
                float entPitch = entityIn.isInWater() ? -90.0F - entityIn.getXRot() : -90.0F;
                float lerpSwim = Mth.lerp(swimTime, 0.0F, entPitch);
                pitch = ((float) Math.PI / 180F) * lerpSwim;
                if (entityIn.isVisuallySwimming()) {
                    boneOffset = new Vec3(0.0, -1.0, -0.3);
                }
                bonePos = bonePos.add(boneOffset);
                bonePos = bonePos.xRot(pitch);
//                bonePos = bonePos.add(boneOffset);
            }


            return Optional.of(new Vec3(entX, entY, entZ).add(renderOffset).add(bonePos.yRot(-yaw)
                    .scale(entityIn.getScale())));
        } else {
            return Optional.empty();
        }


    }

}
