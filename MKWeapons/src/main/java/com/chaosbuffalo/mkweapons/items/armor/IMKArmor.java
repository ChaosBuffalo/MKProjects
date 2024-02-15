package com.chaosbuffalo.mkweapons.items.armor;

import com.chaosbuffalo.mkcore.item.IMKEquipment;
import com.chaosbuffalo.mkweapons.items.effects.armor.IArmorEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IMKArmor extends IMKEquipment {

    List<? extends IArmorEffect> getArmorEffects(ItemStack item);

    List<? extends IArmorEffect> getArmorEffects();

    @Override
    default void onEntityEquip(LivingEntity entity, EquipmentSlot equipmentSlot, ItemStack itemStack) {
        getArmorEffects(itemStack).forEach(eff -> eff.onEntityEquip(entity));
    }

    @Override
    default void onEntityUnequip(LivingEntity entity, EquipmentSlot equipmentSlot, ItemStack itemStack) {
        getArmorEffects(itemStack).forEach(eff -> eff.onEntityUnequip(entity));
    }
}
