package com.chaosbuffalo.mkcore.mixins;

import com.chaosbuffalo.mkcore.utils.DamageUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;

import javax.annotation.Nullable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixins {

    @Unique
    private DamageSource damageSource;

    @Shadow
    public abstract boolean isDamageSourceBlocked(DamageSource damageSourceIn);

    @Shadow
    public abstract boolean isUsingItem();

    @Shadow
    protected ItemStack useItem;

    @Shadow
    protected int useItemRemaining;

    @Shadow
    @Nullable
    public abstract DamageSource getLastDamageSource();

    @Shadow
    public abstract boolean hurt(DamageSource damageSrc, float damageAmount);

    // disable player blocking as we handle it ourselves
    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isDamageSourceBlocked(Lnet/minecraft/world/damagesource/DamageSource;)Z"),
            method = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
    )
    private boolean proxyIsDamageSourceBlocked(LivingEntity entity, DamageSource damageSourceIn) {
        if (entity instanceof Player) {
            return false;
        } else {
            return isDamageSourceBlocked(damageSourceIn);
        }
    }

    @ModifyVariable(
            method = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At("HEAD"),
            index = 1,
            ordinal = 0,
            argsOnly = true
    )
    private DamageSource captureSource(DamageSource source) {
        this.damageSource = source;
        return source;
    }

    @ModifyConstant(
            method = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            constant = @Constant(floatValue = 10.0f)
    )
    private float calculateInvulnerability(float value) {
        if (DamageUtils.isMKDamage(damageSource) ||
                DamageUtils.isMinecraftPhysicalDamage(damageSource) ||
                DamageUtils.isProjectileDamage(damageSource)) {
            return 100.0f;
        }
        return value;
    }

    @ModifyConstant(
            method = "Lnet/minecraft/world/entity/LivingEntity;isBlocking()Z",
            constant = @Constant(intValue = 5)
    )
    private int calculateBlockDelay(int value) {
        return 1;
    }
}
