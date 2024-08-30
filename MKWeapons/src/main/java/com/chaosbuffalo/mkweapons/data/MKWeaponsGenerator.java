package com.chaosbuffalo.mkweapons.data;

import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.data.content.MKCoreGenerators;
import com.chaosbuffalo.mkweapons.MKWeapons;
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
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class MKWeaponsGenerator {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        ExistingFileHelper helper = event.getExistingFileHelper();
        gen.addProvider(event.includeServer(), new MKWeaponRecipeProvider(gen.getPackOutput(), event.getLookupProvider()));
        gen.addProvider(event.includeServer(), new MKWeaponTypesProvider(gen));
        gen.addProvider(event.includeServer(), new MKWeaponsLootTierProvider(gen));
        MKCoreGenerators.MKBlockTagsProvider blockTagsProvider = new MKCoreGenerators.MKBlockTagsProvider(
                gen.getPackOutput(), event.getLookupProvider(), MKWeapons.MODID, helper);
        gen.addProvider(event.includeServer(), blockTagsProvider);
        gen.addProvider(event.includeServer(), new MKWeaponsItemTagProvider(gen, event.getLookupProvider(),
                blockTagsProvider, helper));
        gen.addProvider(event.includeClient(), new MKWeaponModelProvider(gen.getPackOutput(), helper, MKWeapons.MODID));
        gen.addProvider(event.includeServer(), new MKWeaponCurioGenerator(MKWeapons.MODID, gen.getPackOutput(), event.getExistingFileHelper(), event.getLookupProvider()));

    }

    public static class MKWeaponsLootTierProvider extends LootTierProvider {

        public MKWeaponsLootTierProvider(DataGenerator generator) {
            super(generator, MKWeapons.MODID);
        }

        @Override
        public CompletableFuture<?> run(CachedOutput pOutput) {
            return writeLootTier(generateTierOne(), pOutput);
        }

        private LootTier generateTierOne() {
            LootTier tier = new LootTier(MKWeapons.id("tier_one"));
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

            ResourceLocation modifierId = MKWeapons.id("mod_" + tier.getName().getNamespace() + "_" + tier.getName().getPath());

            AttributeOption healthAttribute = new AttributeOption();
            healthAttribute.addAttributeModifier(Attributes.MAX_HEALTH, modifierId,
                    5, 10, AttributeModifier.Operation.ADD_VALUE, EquipmentSlotGroup.MAINHAND);
            AttributeOption manaRegen = new AttributeOption();
            manaRegen.addAttributeModifier(MKAttributes.MANA_REGEN, modifierId,
                    0.5, 2.0, AttributeModifier.Operation.ADD_VALUE);

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
}