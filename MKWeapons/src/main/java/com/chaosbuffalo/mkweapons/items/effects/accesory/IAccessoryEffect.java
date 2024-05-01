package com.chaosbuffalo.mkweapons.items.effects.accesory;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.accessories.MKAccessory;
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

public interface IAccessoryEffect extends IItemEffect {
    Codec<IAccessoryEffect> DISPATCH_CODEC = ItemEffects.ACCESSORY_EFFECT_CODEC;


    default float modifyDamageDealt(float damage, MKAccessory accessory, ItemStack stack,
                                    LivingEntity target, LivingEntity attacker) {
        return damage;
    }

    default void livingCompleteAbility(IMKEntityData entityData, MKAccessory accessory,
                                       ItemStack stack, MKAbility ability) {

    }

    default void onMeleeHit(IMKMeleeWeapon weapon, ItemStack stack, IMKEntityData attackerData, LivingEntity target) {

    }

    default <D> D serialize(DynamicOps<D> ops) {
        return DISPATCH_CODEC.encodeStart(ops, this).getOrThrow(false, MKWeapons.LOGGER::error);
    }

    static <D> IAccessoryEffect deserialize(Dynamic<D> dynamic) {
        return DISPATCH_CODEC.parse(dynamic).getOrThrow(false, MKWeapons.LOGGER::error);
    }

    default IAccessoryEffect copy() {
        Tag tag = serialize(NbtOps.INSTANCE);
        return deserialize(new Dynamic<>(NbtOps.INSTANCE, tag));
    }
}
