package com.chaosbuffalo.mkweapons.items.weapon;

import com.chaosbuffalo.mkweapons.items.effects.melee.IMeleeWeaponEffect;
import com.chaosbuffalo.mkweapons.items.weapon.types.IMeleeWeaponType;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IMKMeleeWeapon extends IMKWeapon {

    IMeleeWeaponType getWeaponType();

    default float getDamageForTier() {
        return getWeaponType().getDamageForTier(getMKTier());
    }

    List<IMeleeWeaponEffect> getWeaponEffects(ItemStack item);

    default boolean allowSweep(){
        return false;
    }

    List<IMeleeWeaponEffect> getWeaponEffects();
}
