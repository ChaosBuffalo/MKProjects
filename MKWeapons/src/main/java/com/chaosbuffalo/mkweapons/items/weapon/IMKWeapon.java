package com.chaosbuffalo.mkweapons.items.weapon;

import com.chaosbuffalo.mkcore.core.IMKAbilityProvider;
import com.chaosbuffalo.mkweapons.items.effects.IItemEffect;
import com.chaosbuffalo.mkweapons.items.weapon.tier.MKTier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IMKWeapon extends IMKAbilityProvider {

    MKTier getMKTier();

    default void reload(){

    }

    List<? extends IItemEffect> getWeaponEffects(ItemStack item);

    List<? extends IItemEffect> getWeaponEffects();
}
