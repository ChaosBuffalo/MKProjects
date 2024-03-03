package com.chaosbuffalo.mkcore.item;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.init.CoreTags;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ArmorClass {

    public static final ArmorClass ROBES = new ArmorClass(MKCore.makeRL("armor_class.robes"), CoreTags.Items.ROBES_ARMOR)
            .addPositiveEffect(Attributes.MOVEMENT_SPEED, 0.025, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addPositiveEffect(MKAttributes.CASTING_SPEED, 0.025, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addPositiveEffect(MKAttributes.MANA_REGEN, 0.025, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addNegativeEffect(Attributes.ARMOR, -0.04, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addNegativeEffect(Attributes.MAX_HEALTH, -0.02, AttributeModifier.Operation.MULTIPLY_TOTAL);

    public static final ArmorClass LIGHT = new ArmorClass(MKCore.makeRL("armor_class.light"), CoreTags.Items.LIGHT_ARMOR)
            .addPositiveEffect(MKAttributes.MELEE_CRIT, 0.02, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addPositiveEffect(MKAttributes.SPELL_CRIT, 0.02, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addPositiveEffect(MKAttributes.RANGED_CRIT, 0.02, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addPositiveEffect(MKAttributes.COOLDOWN, 0.02, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addNegativeEffect(MKAttributes.CASTING_SPEED, -0.02, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addNegativeEffect(Attributes.MAX_HEALTH, -0.04, AttributeModifier.Operation.MULTIPLY_TOTAL);
    public static final ArmorClass MEDIUM = new ArmorClass(MKCore.makeRL("armor_class.medium"), CoreTags.Items.MEDIUM_ARMOR)
            .addPositiveEffect(MKAttributes.HEAL_BONUS, 0.04, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addPositiveEffect(Attributes.ATTACK_SPEED, 0.03, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addPositiveEffect(Attributes.MAX_HEALTH, 0.02, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addNegativeEffect(MKAttributes.COOLDOWN, -0.02, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addNegativeEffect(MKAttributes.CASTING_SPEED, -0.03, AttributeModifier.Operation.MULTIPLY_TOTAL);
    public static final ArmorClass HEAVY = new ArmorClass(MKCore.makeRL("armor_class.heavy"), CoreTags.Items.HEAVY_ARMOR)
            .addPositiveEffect(Attributes.ATTACK_DAMAGE, 0.025, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addPositiveEffect(Attributes.MAX_HEALTH, 0.04, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addPositiveEffect(MKAttributes.MAX_POISE, 0.03, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addPositiveEffect(MKAttributes.ARCANE_RESISTANCE, 0.015, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addPositiveEffect(MKAttributes.FIRE_RESISTANCE, 0.015, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addPositiveEffect(MKAttributes.FROST_RESISTANCE, 0.015, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addPositiveEffect(MKAttributes.NATURE_RESISTANCE, 0.015, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addPositiveEffect(MKAttributes.POISON_RESISTANCE, 0.015, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addPositiveEffect(Attributes.ARMOR_TOUGHNESS, 0.025, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addNegativeEffect(Attributes.MOVEMENT_SPEED, -0.025, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addNegativeEffect(MKAttributes.COOLDOWN, -0.04, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addNegativeEffect(MKAttributes.CASTING_SPEED, -0.04, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addNegativeEffect(Attributes.ATTACK_SPEED, -0.025, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final List<ArmorClass> CHECK_ORDER = Arrays.asList(ROBES, LIGHT, MEDIUM, HEAVY);

    private final ResourceLocation location;
    private final Map<Attribute, AttributeModifier> positiveModifierMap = new HashMap<>();
    private final Map<Attribute, AttributeModifier> negativeModifierMap = new HashMap<>();
    private final TagKey<Item> tag;

    public static ArmorClass getItemArmorClass(ItemStack item) {
        for (ArmorClass armorClass : CHECK_ORDER) {
            if (item.is(armorClass.tag)) {
                return armorClass;
            }
        }
        return null;
    }

    public ArmorClass(ResourceLocation location, TagKey<Item> tag) {
        this.location = location;
        this.tag = tag;
    }

    public ArmorClass addNegativeEffect(Attribute attributeIn, double amount, AttributeModifier.Operation operation) {
        AttributeModifier attributemodifier = new AttributeModifier(getTranslationKey(), amount, operation);
        this.negativeModifierMap.put(attributeIn, attributemodifier);
        return this;
    }

    public ArmorClass addPositiveEffect(Attribute attributeIn, double amount, AttributeModifier.Operation operation) {
        AttributeModifier attributemodifier = new AttributeModifier(getTranslationKey(), amount, operation);
        this.positiveModifierMap.put(attributeIn, attributemodifier);
        return this;
    }

    public Map<Attribute, AttributeModifier> getPositiveModifierMap(EquipmentSlot slot) {
        return this.positiveModifierMap;
    }

    public Map<Attribute, AttributeModifier> getNegativeModifierMap(EquipmentSlot slot) {
        return this.negativeModifierMap;
    }

    private String getTranslationKey() {
        return String.format("%s.%s.name", location.getNamespace(), location.getPath());
    }

    public Component getName() {
        return Component.translatable(getTranslationKey());
    }

    public ResourceLocation getLocation() {
        return location;
    }
}
