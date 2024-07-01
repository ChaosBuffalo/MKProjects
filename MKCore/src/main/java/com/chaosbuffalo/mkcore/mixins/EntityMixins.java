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

    @Shadow
    public abstract double getPassengersRidingOffset();

    @Unique
    private Entity getSelf() {
        return ((Entity)(Object)this);
    }

    //copy of the original vanilla logic
    @Unique
    private void mkPositionRider(Entity pPassenger, Entity.MoveFunction pCallback) {
        if (hasPassenger(pPassenger)) {
            double d0 = getY() + getPassengersRidingOffset() + pPassenger.getMyRidingOffset();
            pCallback.accept(pPassenger, getX(), d0, getZ());
        }
    }

    /**
     * @author kovak
     * @reason adding some riding logic
     * <p>
     */
    @Overwrite
    public void positionRider(Entity pPassenger) {
        mkPositionRider(pPassenger, Entity::setPos);
        MKCore.getEntityData(getSelf()).ifPresent(entityData -> {
            if (entityData.getRiders().hasRider(pPassenger)) {
                EntityRiderModule.EntityRider rider = entityData.getRiders().getRider(pPassenger);
                Vec2 rot = entityData.getEntity().getRotationVector();
                Vec3 newOffset = rider.getOffset().yRot(-rot.y * ((float)Math.PI / 180F));
                pPassenger.setPos(entityData.getEntity().position().add(newOffset));
                if (rider.shouldDoPitch()) {
                    pPassenger.setXRot(rot.x);
                }
                pPassenger.setYRot(rot.y + rider.getYawOffset());
            }
        });

    }
}
