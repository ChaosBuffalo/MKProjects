package com.chaosbuffalo.mkweapons.items.effects.melee;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.effects.IItemEffect;
import com.chaosbuffalo.mkweapons.items.effects.ItemEffects;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;


public interface IMeleeWeaponEffect extends IItemEffect {
    Codec<IMeleeWeaponEffect> DISPATCH_CODEC = ItemEffects.MELEE_EFFECT_CODEC;

    default void onHit(IMKMeleeWeapon weapon, ItemStack stack,
                       IMKEntityData attackerData, LivingEntity target) {

    }

    default float modifyDamageDealt(float damage, IMKMeleeWeapon weapon, ItemStack stack,
                                    LivingEntity target, LivingEntity attacker) {
        return damage;
    }

    default void postAttack(IMKMeleeWeapon weapon, ItemStack stack, IMKEntityData attackerData) {

    }

    default void onHurt(float damage, IMKMeleeWeapon weapon, ItemStack stack,
                        LivingEntity target, LivingEntity attacker) {

    }

    default IMeleeWeaponEffect copy() {
        Tag tag = serialize(NbtOps.INSTANCE);
        return deserialize(new Dynamic<>(NbtOps.INSTANCE, tag));
    }

    default <D> D serialize(DynamicOps<D> ops) {
        return DISPATCH_CODEC.encodeStart(ops, this).getOrThrow(false, MKWeapons.LOGGER::error);
    }

    static <D> IMeleeWeaponEffect deserialize(Dynamic<D> dynamic) {
        return DISPATCH_CODEC.parse(dynamic).getOrThrow(false, MKWeapons.LOGGER::error);
    }
}
