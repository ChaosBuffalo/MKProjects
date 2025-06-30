package com.chaosbuffalo.mkcore.mixins;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.utils.DamageUtils;
import com.chaosbuffalo.mkcore.utils.EntityUtils;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixins {

    @Shadow
    public abstract boolean isDamageSourceBlocked(DamageSource damageSourceIn);

    // disable player blocking as we handle it ourselves
    @Redirect(
            method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;isDamageSourceBlocked(Lnet/minecraft/world/damagesource/DamageSource;)Z"
            )
    )
    private boolean mkcore$proxyIsDamageSourceBlocked(LivingEntity entity, DamageSource damageSourceIn) {
        if (entity instanceof Player) {
            return false;
        } else {
            return isDamageSourceBlocked(damageSourceIn);
        }
    }

    @WrapOperation(
            method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"
            )
    )
    private void mkcore$knockback(LivingEntity instance, double strength, double x, double z,
                                  Operation<Void> original, @Local(argsOnly = true) DamageSource damageSource) {
        if (DamageUtils.isMeleeDamage(damageSource) && damageSource.getDirectEntity() != null
                && !DamageUtils.wasAlreadyPartiallyBlocked(damageSource)
                && !EntityUtils.isInFrontOf(instance, damageSource.getDirectEntity())) {
            MKCore.LOGGER.info("Performing knockback");
            original.call(instance, strength, x, z);
        } else {
            MKCore.LOGGER.info("skipping knockback");
        }
    }

    @ModifyConstant(
            method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            constant = @Constant(floatValue = 10.0f)
    )
    private float mkcore$calculateInvulnerability(float value, @Local(argsOnly = true) DamageSource damageSource) {
        if (DamageUtils.isMKDamage(damageSource) ||
                DamageUtils.isMinecraftPhysicalDamage(damageSource) ||
                DamageUtils.isProjectileDamage(damageSource)) {
            return 100.0f;
        }
        return value;
    }

    @ModifyConstant(
            method = "isBlocking()Z",
            constant = @Constant(intValue = 5)
    )
    private int mkcore$calculateBlockDelay(int value) {
        return 1;
    }
}
