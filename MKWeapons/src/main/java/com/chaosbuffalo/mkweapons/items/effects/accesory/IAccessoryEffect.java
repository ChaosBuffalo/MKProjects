package com.chaosbuffalo.mkweapons.items.effects.accesory;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkweapons.items.accessories.IMKAccessory;
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


    default float modifyDamageDealt(float damage, IMKAccessory accessory, ItemStack stack,
                                    LivingEntity target, LivingEntity attacker) {
        return damage;
    }

    default void livingCompleteAbility(IMKEntityData entityData, IMKAccessory accessory,
                                       ItemStack stack, MKAbility ability) {

    }

    default void onMeleeHit(IMKMeleeWeapon weapon, ItemStack stack, IMKEntityData attackerData, LivingEntity target) {

    }

    default <D> D serialize(DynamicOps<D> ops) {
        return DISPATCH_CODEC.encodeStart(ops, this).getOrThrow();
    }

    static <D> IAccessoryEffect deserialize(Dynamic<D> dynamic) {
        return DISPATCH_CODEC.parse(dynamic).getOrThrow();
    }

    default IAccessoryEffect copy() {
        Tag tag = serialize(NbtOps.INSTANCE);
        return deserialize(new Dynamic<>(NbtOps.INSTANCE, tag));
    }

    default IAccessoryEffect createTunedEffect(double difficultyPercentage) {
        return this;
    }
}
