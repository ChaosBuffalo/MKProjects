package com.chaosbuffalo.mkweapons.capabilities;

import com.chaosbuffalo.mkweapons.items.effects.armor.IArmorEffect;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.List;

public interface IArmorData extends INBTSerializable<CompoundTag> {

    ItemStack getItemStack();

    List<IArmorEffect> getArmorEffects();

    boolean hasArmorEffects();

    void markCacheDirty();

    Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot);

    void addArmorEffect(IArmorEffect armorEffect);

    void removeArmorEffect(int index);
}
