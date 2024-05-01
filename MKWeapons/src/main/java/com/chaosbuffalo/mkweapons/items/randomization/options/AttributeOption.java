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
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AttributeOption extends BaseRandomizationOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "attributes");
    public static final Codec<AttributeOption> CODEC = RecordCodecBuilder.<AttributeOption>mapCodec(builder -> {
        return builder.group(
                IRandomizationSlot.CODEC.optionalFieldOf("slot", RandomizationSlotManager.ATTRIBUTE_SLOT).forGetter(BaseRandomizationOption::getSlot),
                Codec.DOUBLE.optionalFieldOf("weight", 1.0).forGetter(BaseRandomizationOption::getWeight),
                AttributeOptionEntry.CODEC.listOf().fieldOf("modifiers").forGetter(i -> i.modifiers)
        ).apply(builder, AttributeOption::new);
    }).codec();

    private final List<AttributeOptionEntry> modifiers;

    private AttributeOption(IRandomizationSlot slot, double weight, List<AttributeOptionEntry> modifiers) {
        super(NAME, slot, weight);
        this.modifiers = modifiers;
    }

    public AttributeOption() {
        this(RandomizationSlotManager.ATTRIBUTE_SLOT);
    }

    public AttributeOption(IRandomizationSlot slot) {
        super(NAME, slot);
        this.modifiers = new ArrayList<>();
    }

    public List<AttributeOptionEntry> getModifiers(double difficulty) {
        return modifiers.stream().map(mod -> mod.createScaledModifier(difficulty)).collect(Collectors.toList());
    }

    public void addFixedAttributeModifier(Attribute attribute, AttributeModifier attributeModifier) {
        modifiers.add(new AttributeOptionEntry(attribute, attributeModifier, attributeModifier.getAmount(), attributeModifier.getAmount()));
    }

    public static AttributeOption withModifier(Attribute attribute, String name, double minAmount, double maxAmount, AttributeModifier.Operation op) {
        return withModifier(RandomizationSlotManager.ATTRIBUTE_SLOT, attribute, name, minAmount, maxAmount, op);
    }

    public static AttributeOption withModifier(IRandomizationSlot slot, Attribute attribute, String name, double minAmount, double maxAmount, AttributeModifier.Operation op) {
        AttributeOption opt = new AttributeOption(slot);
        opt.addAttributeModifier(attribute, name, minAmount, maxAmount, op);
        return opt;
    }

    public void addAttributeModifier(Attribute attribute, String name, double minAmount, double maxAmount, AttributeModifier.Operation op) {
        modifiers.add(new AttributeOptionEntry(attribute, new AttributeModifier(Util.NIL_UUID, name, minAmount, op), minAmount, maxAmount));
    }

    @Override
    public void applyToItemStackForSlot(ItemStack stack, LootSlot slot, double difficulty) {
        if (stack.getItem() instanceof IMKMeleeWeapon) {
            stack.getCapability(WeaponsCapabilities.WEAPON_DATA_CAPABILITY).ifPresent(
                    cap -> cap.addMeleeWeaponEffect(new MeleeModifierEffect(getModifiers(difficulty))));
        } else if (stack.getItem() instanceof IMKRangedWeapon) {
            stack.getCapability(WeaponsCapabilities.WEAPON_DATA_CAPABILITY).ifPresent(
                    cap -> cap.addRangedWeaponEffect(new RangedModifierEffect(getModifiers(difficulty))));
        } else if (stack.getItem() instanceof IMKArmor) {
            stack.getCapability(WeaponsCapabilities.ARMOR_DATA_CAPABILITY).ifPresent(
                    cap -> cap.addArmorEffect(new ArmorModifierEffect(getModifiers(difficulty)))
            );
        } else if (stack.getItem() instanceof MKAccessory) {
            MKAccessory.getAccessoryHandler(stack).ifPresent(
                    cap -> cap.addEffect(new AccessoryModifierEffect(getModifiers(difficulty)))
            );
        }
    }
}
