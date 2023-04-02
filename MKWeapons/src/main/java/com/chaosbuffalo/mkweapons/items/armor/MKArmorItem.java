package com.chaosbuffalo.mkweapons.items.armor;

import com.chaosbuffalo.mkweapons.capabilities.WeaponsCapabilities;
import com.chaosbuffalo.mkweapons.items.effects.armor.IArmorEffect;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import net.minecraft.world.item.Item.Properties;

public class MKArmorItem extends ArmorItem implements IMKArmor {
    public static final UUID CHEST_UUID = UUID.fromString("77ab4b54-5885-4f7f-ab41-71af536309d1");
    public static final UUID LEGGINGS_UUID = UUID.fromString("8d827c58-8f61-4c77-8dcd-f62f0e69121b");
    public static final UUID HELMET_UUID = UUID.fromString("fb16408c-0421-4138-a283-8da7038e5970");
    public static final UUID FEET_UUID = UUID.fromString("fb16408c-0421-4138-a283-8da7038e5972");
    public static final UUID[] ARMOR_MODIFIERS = new UUID[]{FEET_UUID, LEGGINGS_UUID, CHEST_UUID, HELMET_UUID};
    private final List<IArmorEffect> armorEffects;

    private final Multimap<Attribute, AttributeModifier> attributeMap;


    public MKArmorItem(ArmorMaterial materialIn, EquipmentSlot slot, Properties builderIn,
                       IArmorEffect... armorEffects) {
        super(materialIn, slot, builderIn);
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        UUID uuid = ARMOR_MODIFIERS[slot.getIndex()];
        builder.put(Attributes.ARMOR, new AttributeModifier(uuid, "Armor modifier", getDefense(),
                AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(uuid, "Armor toughness", getToughness(),
                AttributeModifier.Operation.ADDITION));
        if (this.knockbackResistance > 0) {
            builder.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(uuid, "Armor knockback resistance",
                    knockbackResistance, AttributeModifier.Operation.ADDITION));
        }
        buildAttributes(builder, slot, materialIn, uuid);
        this.attributeMap = builder.build();
        this.armorEffects = Arrays.asList(armorEffects);
    }

    protected void buildAttributes(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder,
                                   EquipmentSlot slot, ArmorMaterial material, UUID slotUUID){

    }

    @Nullable
    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        // See comment in MKMeleeWeapon#getShareTag
        CompoundTag newTag = new CompoundTag();
        CompoundTag original = super.getShareTag(stack);
        if (original != null) {
            newTag.put("share", original);
        }
        stack.getCapability(WeaponsCapabilities.ARMOR_DATA_CAPABILITY).ifPresent(armorData ->
                newTag.put("armorCap", armorData.serializeNBT()));
        return newTag;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag shareTag) {
        if (shareTag == null)
            return;

        if (shareTag.contains("share")) {
            super.readShareTag(stack, shareTag.getCompound("share"));
        }
        if (shareTag.contains("armorCap")) {
            stack.getCapability(WeaponsCapabilities.ARMOR_DATA_CAPABILITY).ifPresent(armorData ->
                    armorData.deserializeNBT(shareTag.getCompound("armorCap")));
        }
    }

    public void addToTooltip(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip){
        for (IArmorEffect armorEffect : getArmorEffects(stack)){
            armorEffect.addInformation(stack, worldIn, tooltip);
        }
    }

    @Override
    public List<IArmorEffect> getArmorEffects() {
        return armorEffects;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return stack.getCapability(WeaponsCapabilities.ARMOR_DATA_CAPABILITY).map(x -> x.getAttributeModifiers(slot))
                .orElse(getDefaultAttributeModifiers(slot));
    }


    @Override
    public List<IArmorEffect> getArmorEffects(ItemStack item) {
        return item.getCapability(WeaponsCapabilities.ARMOR_DATA_CAPABILITY).map(cap -> {
            if (cap.hasArmorEffects()){
                return cap.getArmorEffects();
            } else {
                return armorEffects;
            }
        }).orElse(armorEffects);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
        return equipmentSlot == this.slot ? this.attributeMap : super.getDefaultAttributeModifiers(equipmentSlot);
    }
}
