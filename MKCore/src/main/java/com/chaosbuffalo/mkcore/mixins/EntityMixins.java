package com.chaosbuffalo.mkcore.mixins;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.entity.EntityRiderModule;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public abstract class EntityMixins {

    @Shadow
    public abstract boolean hasPassenger(Entity pEntity);

    @Shadow
    public abstract double getY();

    @Shadow
    public abstract double getX();

    @Shadow
    public abstract double getZ();

    @Shadow public abstract Vec3 getPassengerRidingPosition(Entity entity);

    @Unique
    private Entity getSelf() {
        return ((Entity)(Object)this);
    }

    /**
     * @author kovak
     * @reason adding some riding logic
     * <p>
     */
    @Overwrite
    protected void positionRider(Entity passenger, Entity.MoveFunction callback) {
        MKCore.getEntityData(getSelf()).ifPresentOrElse(entityData -> {
            if (entityData.getRiders().hasRider(passenger)) {
                EntityRiderModule.EntityRider rider = entityData.getRiders().getRider(passenger);
                Vec2 rot = entityData.getEntity().getRotationVector();
                Vec3 newOffset = rider.getOffset().yRot(-rot.y * ((float)Math.PI / 180F));
                Vec3 newPos = entityData.getEntity().position().add(newOffset);
                callback.accept(passenger, newPos.x, newPos.y, newPos.z);
                if (rider.shouldDoPitch()) {
                    passenger.setXRot(rot.x);
                }
                passenger.setYRot(rot.y + rider.getYawOffset());
            } else {
                Vec3 vec3 = getPassengerRidingPosition(passenger);
                Vec3 vec31 = passenger.getVehicleAttachmentPoint(getSelf());
                callback.accept(passenger, vec3.x - vec31.x, vec3.y - vec31.y, vec3.z - vec31.z);
            }
        }, () -> {
            Vec3 vec3 = getPassengerRidingPosition(passenger);
            Vec3 vec31 = passenger.getVehicleAttachmentPoint(getSelf());
            callback.accept(passenger, vec3.x - vec31.x, vec3.y - vec31.y, vec3.z - vec31.z);
        });
    }
}
