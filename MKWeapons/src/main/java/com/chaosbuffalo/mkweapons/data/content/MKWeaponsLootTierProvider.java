package com.chaosbuffalo.mkweapons.data.content;

import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.MKWeaponsRegistry;
import com.chaosbuffalo.mkweapons.init.MKWeaponsItems;
import com.chaosbuffalo.mkweapons.items.MKMeleeWeapon;
import com.chaosbuffalo.mkweapons.items.randomization.LootItemTemplate;
import com.chaosbuffalo.mkweapons.items.randomization.LootTier;
import com.chaosbuffalo.mkweapons.items.randomization.options.AttributeOption;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlotManager;
import com.chaosbuffalo.mkweapons.items.randomization.slots.RandomizationSlotManager;
import com.chaosbuffalo.mkweapons.items.randomization.templates.RandomizationTemplate;
import com.chaosbuffalo.mkweapons.items.weapon.tier.IMKTier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class MKWeaponsLootTierProvider {


    static ResourceKey<LootTier> key(String name) {
        return ResourceKey.create(MKWeaponsRegistry.LOOT_TIER_REGISTRY_KEY, MKWeapons.id(name));
    }

    public static final ResourceKey<LootTier> TIER_ONE = key("tier_one");


    public static void bootstrap(BootstrapContext<LootTier> context) {
        context.register(TIER_ONE, generateTierOne(TIER_ONE));
    }

    private static LootTier generateTierOne(ResourceKey<LootTier> tierKey) {
        LootTier tier = new LootTier();
        List<IMKTier> weaponTiers = List.of(MKWeaponsItems.STONE_TIER, MKWeaponsItems.WOOD_TIER);

        LootItemTemplate weaponTemplate = new LootItemTemplate(LootSlotManager.MAIN_HAND);

        // Sort the items for stable datagen output
        List<MKMeleeWeapon> sorted = new ArrayList<>(MKWeaponsItems.WEAPONS);
        Comparator<MKMeleeWeapon> comp = Comparator.comparing((MKMeleeWeapon w) -> w.getMKTier().getName())
                .thenComparing(BuiltInRegistries.ITEM::getKey);
        sorted.sort(comp);

        for (MKMeleeWeapon weapon : sorted) {
            if (weaponTiers.contains(weapon.getMKTier())) {
                weaponTemplate.addItem(weapon);
            }
        }

        ResourceLocation modifierId = tierKey.location().withPrefix("mod_");

        AttributeOption healthAttribute = new AttributeOption();
        healthAttribute.addAttributeModifier(Attributes.MAX_HEALTH, modifierId,
                5, 10, AttributeModifier.Operation.ADD_VALUE, EquipmentSlotGroup.MAINHAND);
        AttributeOption manaRegen = new AttributeOption();
        manaRegen.addAttributeModifier(MKAttributes.MANA_REGEN, modifierId,
                0.5, 2.0, AttributeModifier.Operation.ADD_VALUE, EquipmentSlotGroup.MAINHAND);

        LootItemTemplate ringTemplate = new LootItemTemplate(LootSlotManager.RINGS);
        ringTemplate.addItem(MKWeaponsItems.CopperRing.get());

        List<LootItemTemplate> templates = Arrays.asList(weaponTemplate, ringTemplate);

        for (LootItemTemplate temp : templates) {
            temp.addRandomizationOption(healthAttribute);
            temp.addRandomizationOption(manaRegen);
            temp.addTemplate(new RandomizationTemplate(MKWeapons.id("simple_template"),
                    RandomizationSlotManager.ATTRIBUTE_SLOT), 10);
            temp.addTemplate(new RandomizationTemplate(MKWeapons.id("simple_template_2x"),
                    RandomizationSlotManager.ATTRIBUTE_SLOT, RandomizationSlotManager.ATTRIBUTE_SLOT), 10);
            tier.addItemTemplate(temp, 1.0);
        }

        return tier;
    }
}
