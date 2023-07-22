package com.chaosbuffalo.mkweapons.items.effects.melee;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkweapons.items.effects.IItemEffect;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;


public interface IMeleeWeaponEffect extends IItemEffect {

    default void onHit(IMKMeleeWeapon weapon, ItemStack stack,
                       LivingEntity target, LivingEntity attacker) {
    }

    default void onHit(IMKMeleeWeapon weapon, ItemStack stack,
                       LivingEntity target, IMKEntityData attackerData) {
        onHit(weapon, stack, target, attackerData.getEntity());
    }

    default float modifyDamageDealt(float damage, IMKMeleeWeapon weapon, ItemStack stack,
                                    LivingEntity target, IMKEntityData attackerData) {
        return damage;
    }

    default void postAttack(IMKMeleeWeapon weapon, ItemStack stack, IMKEntityData attackerData) {

    }

    default void onHurt(float damage, IMKMeleeWeapon weapon, ItemStack stack,
                        LivingEntity target, IMKEntityData attackerData) {

    }

    @Override
    default IMeleeWeaponEffect copy() {
        return (IMeleeWeaponEffect) IItemEffect.super.copy();
    }
}
