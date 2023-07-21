package com.chaosbuffalo.mkweapons.items.effects.accesory;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkweapons.items.accessories.MKAccessory;
import com.chaosbuffalo.mkweapons.items.effects.IItemEffect;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface IAccessoryEffect extends IItemEffect {


    default float modifyDamageDealt(float damage, MKAccessory accessory, ItemStack stack,
                                    LivingEntity target, LivingEntity attacker) {
        return damage;
    }

    default void livingCompleteAbility(LivingEntity caster, IMKEntityData entityData, MKAccessory accessory,
                                       ItemStack stack, MKAbility ability) {

    }

    default void onMeleeHit(IMKMeleeWeapon weapon, ItemStack stack, LivingEntity target, LivingEntity attacker) {

    }

    @Override
    default IAccessoryEffect copy() {
        return (IAccessoryEffect) IItemEffect.super.copy();
    }
}
