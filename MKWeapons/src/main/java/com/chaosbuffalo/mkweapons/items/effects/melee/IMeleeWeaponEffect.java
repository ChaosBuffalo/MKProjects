package com.chaosbuffalo.mkweapons.items.effects.melee;

import com.chaosbuffalo.mkweapons.items.effects.IItemEffect;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;


public interface IMeleeWeaponEffect extends IItemEffect {

    default void onHit(IMKMeleeWeapon weapon, ItemStack stack,
                       LivingEntity target, LivingEntity attacker) { }

    default float modifyDamageDealt(float damage, IMKMeleeWeapon weapon, ItemStack stack,
                                    LivingEntity target, LivingEntity attacker){
        return damage;
    }

    default void postAttack(IMKMeleeWeapon weapon, ItemStack stack, LivingEntity attacker){

    }


}
