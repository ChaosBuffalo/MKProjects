package com.chaosbuffalo.mkcore.item;

import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

public interface ILimitItemTooltip {

    Multimap<Attribute, AttributeModifier> limitTooltip(ItemStack itemStack, EquipmentSlot equipmentSlot, Multimap<Attribute, AttributeModifier> modifiers);
}
