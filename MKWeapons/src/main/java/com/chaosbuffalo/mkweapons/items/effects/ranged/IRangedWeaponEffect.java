package com.chaosbuffalo.mkweapons.items.effects.ranged;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.effects.IItemEffect;
import com.chaosbuffalo.mkweapons.items.effects.ItemEffects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public interface IRangedWeaponEffect extends IItemEffect {
    Codec<IRangedWeaponEffect> DISPATCH_CODEC = ItemEffects.RANGED_EFFECT_CODEC;

    default void onProjectileHit(LivingHurtEvent event, DamageSource source, LivingEntity livingTarget,
                                 IMKEntityData attackerData, AbstractArrow arrow, ItemStack bow) {

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

    default IRangedWeaponEffect copy() {
        Tag tag = serialize(NbtOps.INSTANCE);
        return deserialize(new Dynamic<>(NbtOps.INSTANCE, tag));
    }

    default <D> D serialize(DynamicOps<D> ops) {
        return DISPATCH_CODEC.encodeStart(ops, this).getOrThrow(false, MKWeapons.LOGGER::error);
    }

    static <D> IRangedWeaponEffect deserialize(Dynamic<D> dynamic) {
        return DISPATCH_CODEC.parse(dynamic).getOrThrow(false, MKWeapons.LOGGER::error);
    }
}
