package com.chaosbuffalo.mkweapons.items.randomization.options;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.components.AccessoryEffectsComponent;
import com.chaosbuffalo.mkweapons.components.ArmorEffectsComponent;
import com.chaosbuffalo.mkweapons.components.MeleeEffectsComponent;
import com.chaosbuffalo.mkweapons.components.RangedEffectsComponent;
import com.chaosbuffalo.mkweapons.items.accessories.IMKAccessory;
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
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AttributeOption extends BaseRandomizationOption {
    public static final ResourceLocation NAME = MKWeapons.id("attributes");
    public static final MapCodec<AttributeOption> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            IRandomizationSlot.CODEC.optionalFieldOf("slot", RandomizationSlotManager.ATTRIBUTE_SLOT).forGetter(BaseRandomizationOption::getSlot),
            Codec.DOUBLE.optionalFieldOf("weight", 1.0).forGetter(BaseRandomizationOption::getWeight),
            AttributeOptionEntry.CODEC.listOf().fieldOf("modifiers").forGetter(i -> i.modifiers)
    ).apply(builder, AttributeOption::new));
    public static final Codec<AttributeOption> CODEC = MAP_CODEC.codec();

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

    private List<AttributeOptionEntry> createStackModifiers(LootSlot lootSlot, int slotIndex, double difficulty) {
        return modifiers.stream().map(mod -> {
            // Give each modifier a unique name in case an item with multiple attribute slots rolls the same attribute twice
            return mod.createModifierInstance(difficulty, id -> {
                return id.withSuffix(String.format("/%s/%d", lootSlot.getName().toLanguageKey(), slotIndex));
            });
        }).collect(Collectors.toList());
    }

    public void addFixedAttributeModifier(Holder<Attribute> attribute, AttributeModifier attributeModifier) {
        modifiers.add(new AttributeOptionEntry(attribute, attributeModifier, attributeModifier.amount(), attributeModifier.amount()));
    }

    public static AttributeOption withModifier(Holder<Attribute> attribute, ResourceLocation name, double minAmount, double maxAmount, AttributeModifier.Operation op, EquipmentSlotGroup slotGroup) {
        return withModifier(RandomizationSlotManager.ATTRIBUTE_SLOT, attribute, name, minAmount, maxAmount, op, slotGroup);
    }

    public static AttributeOption withModifier(IRandomizationSlot slot, Holder<Attribute> attribute, ResourceLocation name, double minAmount, double maxAmount, AttributeModifier.Operation op, EquipmentSlotGroup slotGroup) {
        AttributeOption opt = new AttributeOption(slot);
        opt.addAttributeModifier(attribute, name, minAmount, maxAmount, op, slotGroup);
        return opt;
    }

    public void addAttributeModifier(Holder<Attribute> attribute, ResourceLocation name, double minAmount, double maxAmount, AttributeModifier.Operation op, EquipmentSlotGroup slotGroup) {
        modifiers.add(new AttributeOptionEntry(attribute, new AttributeModifier(name, minAmount, op), slotGroup, minAmount, maxAmount));
    }

    @Override
    public void applyToItemStackForSlot(ItemStack stack, LootSlot slot, int slotIndex, double difficulty) {
        var stackModifiers = createStackModifiers(slot, slotIndex, difficulty);
        switch (stack.getItem()) {
            case IMKMeleeWeapon meleeWeapon ->
                    MeleeEffectsComponent.addEffect(stack, new MeleeModifierEffect(stackModifiers));
            case IMKRangedWeapon rangedWeapon ->
                    RangedEffectsComponent.addEffect(stack, new RangedModifierEffect(stackModifiers));
            case IMKArmor armor ->
                    ArmorEffectsComponent.addEffect(stack, new ArmorModifierEffect(stackModifiers));
            case IMKAccessory accessory ->
                    AccessoryEffectsComponent.addEffect(stack, new AccessoryModifierEffect(stackModifiers));
            default -> {
            }
        }
    }
}
