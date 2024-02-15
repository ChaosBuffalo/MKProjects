package com.chaosbuffalo.mkcore.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface IMKEquipment {

    default void onEntityEquip(LivingEntity entity, EquipmentSlot equipmentSlot, ItemStack itemStack) {

    }

    default void onEntityUnequip(LivingEntity entity, EquipmentSlot equipmentSlot, ItemStack itemStack) {

    }
}
