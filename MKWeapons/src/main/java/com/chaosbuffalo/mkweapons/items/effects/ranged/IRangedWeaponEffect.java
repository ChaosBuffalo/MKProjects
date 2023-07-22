package com.chaosbuffalo.mkweapons.items.effects.ranged;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkweapons.items.effects.IItemEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public interface IRangedWeaponEffect extends IItemEffect {

    default void onProjectileHit(LivingHurtEvent event, IMKEntityData attackerData, IMKEntityData victimData,
                                 AbstractArrow arrow, ItemStack bow) {

    }

    default float modifyDrawTime(float inTime, ItemStack item, LivingEntity entity) {
        return inTime;
    }

    default float modifyLaunchVelocity(float inVel, ItemStack item, LivingEntity entity) {
        return inVel;
    }

    default double modifyArrowDamage(double inDamage, LivingEntity shooter, AbstractArrow arrow) {
        return inDamage;
    }

    @Override
    default IRangedWeaponEffect copy() {
        return (IRangedWeaponEffect) IItemEffect.super.copy();
    }
}
