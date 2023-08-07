package com.chaosbuffalo.mkcore.mixins;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.utils.DamageUtils;
import com.chaosbuffalo.mkcore.utils.EntityUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixins {

    @Unique
    private DamageSource mkcore$damageSource;

    @Shadow
    public abstract boolean isDamageSourceBlocked(DamageSource damageSourceIn);

    @Shadow
    public abstract void knockback(double pStrength, double pX, double pZ);

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

    @Redirect(
            method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"
            )
    )
    private void mkcore$knockbackProxy(LivingEntity entity, double strength, double x, double z) {
        if (DamageUtils.isMeleeDamage(mkcore$damageSource) && mkcore$damageSource.getDirectEntity() != null
                && !DamageUtils.wasAlreadyPartiallyBlocked(mkcore$damageSource)
                && !EntityUtils.isInFrontOf(entity, mkcore$damageSource.getDirectEntity())) {
            knockback(strength, x, z);
            MKCore.LOGGER.info("Performing knockback");
        } else {
            MKCore.LOGGER.info("skipping knockback");
        }
    }

    @ModifyVariable(
            method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At("HEAD"),
            index = 1,
            ordinal = 0,
            argsOnly = true
    )
    private DamageSource mkcore$captureSource(DamageSource source) {
        this.mkcore$damageSource = source;
        return source;
    }

    @ModifyConstant(
            method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            constant = @Constant(floatValue = 10.0f)
    )
    private float mkcore$calculateInvulnerability(float value) {
        if (DamageUtils.isMKDamage(mkcore$damageSource) ||
                DamageUtils.isMinecraftPhysicalDamage(mkcore$damageSource) ||
                DamageUtils.isProjectileDamage(mkcore$damageSource)) {
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
