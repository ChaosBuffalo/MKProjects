package com.chaosbuffalo.mkweapons.items.weapon;

import com.chaosbuffalo.mkcore.core.IMKAbilityProvider;
import com.chaosbuffalo.mkcore.item.IMKEquipment;
import com.chaosbuffalo.mkweapons.items.effects.IItemEffect;
import com.chaosbuffalo.mkweapons.items.weapon.tier.IMKTier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IMKWeapon extends IMKEquipment, IMKAbilityProvider {

    IMKTier getMKTier();

    default void reload() {

    }

    List<? extends IItemEffect> getWeaponEffects(ItemStack item);

    List<? extends IItemEffect> getWeaponEffects();

    @Override
    default void onEntityEquip(LivingEntity entity, EquipmentSlot equipmentSlot, ItemStack itemStack) {
        getWeaponEffects(itemStack).forEach(eff -> eff.onEntityEquip(entity));
    }

    @Override
    default void onEntityUnequip(LivingEntity entity, EquipmentSlot equipmentSlot, ItemStack itemStack) {
        getWeaponEffects(itemStack).forEach(eff -> eff.onEntityUnequip(entity));
    }
}
