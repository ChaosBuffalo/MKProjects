package com.chaosbuffalo.mkweapons.items.randomization.options;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.capabilities.WeaponsCapabilities;
import com.chaosbuffalo.mkweapons.items.accessories.MKAccessory;
import com.chaosbuffalo.mkweapons.items.armor.IMKArmor;
import com.chaosbuffalo.mkweapons.items.effects.accesory.AccessoryModifierEffect;
import com.chaosbuffalo.mkweapons.items.effects.armor.ArmorModifierEffect;
import com.chaosbuffalo.mkweapons.items.effects.melee.MeleeModifierEffect;
import com.chaosbuffalo.mkweapons.items.effects.ranged.RangedModifierEffect;
import com.chaosbuffalo.mkweapons.items.randomization.slots.IRandomizationSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.RandomizationSlotManager;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import com.chaosbuffalo.mkweapons.items.weapon.IMKRangedWeapon;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AttributeOption extends BaseRandomizationOption {

    private final List<AttributeOptionEntry> modifiers;
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "attributes");


    public AttributeOption(){
        this(RandomizationSlotManager.ATTRIBUTE_SLOT);
    }

    public AttributeOption(IRandomizationSlot slot){
        super(NAME, slot);
        modifiers = new ArrayList<>();
    }

    public List<AttributeOptionEntry> getModifiers(double difficulty) {
        return modifiers.stream().map(mod -> mod.getModifier().getId().equals(Util.NIL_UUID) ? mod.copy(difficulty) : mod).collect(Collectors.toList());
    }

    public void addFixedAttributeModifier(Attribute attribute, AttributeModifier attributeModifier){
        modifiers.add(new AttributeOptionEntry(attribute, attributeModifier, attributeModifier.getAmount(), attributeModifier.getAmount()));
    }

    public void addAttributeModifier(Attribute attribute, String name, double minAmount, double maxAmount, AttributeModifier.Operation op){
        modifiers.add(new AttributeOptionEntry(attribute, new AttributeModifier(Util.NIL_UUID, name, minAmount, op), minAmount, maxAmount));
    }

    @Override
    public void applyToItemStackForSlot(ItemStack stack, LootSlot slot, double difficulty) {
        if (stack.getItem() instanceof IMKMeleeWeapon){
            stack.getCapability(WeaponsCapabilities.WEAPON_DATA_CAPABILITY).ifPresent(
                    cap -> cap.addMeleeWeaponEffect(new MeleeModifierEffect(getModifiers(difficulty))));
        } else if (stack.getItem() instanceof IMKRangedWeapon){
            stack.getCapability(WeaponsCapabilities.WEAPON_DATA_CAPABILITY).ifPresent(
                    cap -> cap.addRangedWeaponEffect(new RangedModifierEffect(getModifiers(difficulty))));
        } else if (stack.getItem() instanceof IMKArmor){
            stack.getCapability(WeaponsCapabilities.ARMOR_DATA_CAPABILITY).ifPresent(
                    cap -> cap.addArmorEffect(new ArmorModifierEffect(getModifiers(difficulty)))
            );
        } else if (stack.getItem() instanceof MKAccessory){
            MKAccessory.getAccessoryHandler(stack).ifPresent(
                    cap -> cap.addEffect(new AccessoryModifierEffect(getModifiers(difficulty)))
            );
        }
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("modifiers"),
                ops.createList(modifiers.stream().map(mod -> mod.serialize(ops))));
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        List<AttributeOptionEntry> deserialized = dynamic.get("modifiers").asList(dyn -> {
            AttributeOptionEntry entry = new AttributeOptionEntry();
            entry.deserialize(dyn);
            return entry;
        });
        modifiers.clear();
        for (AttributeOptionEntry mod : deserialized){
            if (mod != null){
                modifiers.add(mod);
            }
        }
    }
}
