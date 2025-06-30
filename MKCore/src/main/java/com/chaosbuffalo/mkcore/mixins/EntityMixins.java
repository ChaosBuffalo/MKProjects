package com.chaosbuffalo.mkcore.mixins;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.entity.EntityRiderModule;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class EntityMixins {

    /**
     * @author kovak
     * @reason adding some riding logic
     * <p>
     */
    @WrapOperation(
            method = "positionRider(Lnet/minecraft/world/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V"
            )
    )
    private void mkcore$positionRider(Entity instance, Entity passenger, Entity.MoveFunction callback, Operation<Void> original) {
        if (instance instanceof LivingEntity livingSelf) {
            var entityData = MKCore.getEntityDataOrThrow(livingSelf);

            EntityRiderModule.EntityRider rider = entityData.getRiders().getRider(passenger);
            if (rider != null) {
                Vec2 rot = livingSelf.getRotationVector();
                Vec3 newOffset = rider.getOffset().yRot(-rot.y * ((float)Math.PI / 180F));
                Vec3 newPos = livingSelf.position().add(newOffset);
                callback.accept(passenger, newPos.x, newPos.y, newPos.z);
                if (rider.shouldDoPitch()) {
                    passenger.setXRot(rot.x);
                }
                passenger.setYRot(rot.y + rider.getYawOffset());
            } else {
                original.call(instance, passenger, callback);
            }
        } else {
            original.call(instance, passenger, callback);
        }
    }
}
