package com.chaosbuffalo.mkweapons.data;

import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.data.MKCoreGenerators;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.init.MKWeaponsItems;
import com.chaosbuffalo.mkweapons.items.MKMeleeWeapon;
import com.chaosbuffalo.mkweapons.items.randomization.LootItemTemplate;
import com.chaosbuffalo.mkweapons.items.randomization.LootTier;
import com.chaosbuffalo.mkweapons.items.randomization.options.AttributeOption;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlotManager;
import com.chaosbuffalo.mkweapons.items.randomization.slots.RandomizationSlotManager;
import com.chaosbuffalo.mkweapons.items.randomization.templates.RandomizationTemplate;
import com.chaosbuffalo.mkweapons.items.weapon.tier.MKWrapperTier;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class MKWeaponsGenerator {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        ExistingFileHelper helper = event.getExistingFileHelper();
        gen.addProvider(event.includeServer(), new MKWeaponRecipeProvider(gen.getPackOutput()));
        gen.addProvider(event.includeServer(), new MKWeaponTypesProvider(gen));
        gen.addProvider(event.includeServer(), new MKWeaponsLootTierProvider(gen));
        MKCoreGenerators.MKBlockTagsProvider blockTagsProvider = new MKCoreGenerators.MKBlockTagsProvider(
                gen.getPackOutput(), event.getLookupProvider(), MKWeapons.MODID, helper);
        gen.addProvider(event.includeServer(), blockTagsProvider);
        gen.addProvider(event.includeServer(), new MKWeaponsItemTagProvider(gen, event.getLookupProvider(),
                blockTagsProvider, helper));
        gen.addProvider(event.includeClient(), new MKWeaponModelProvider(gen.getPackOutput(), helper, MKWeapons.MODID));

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
            LootTier tier = new LootTier(new ResourceLocation(MKWeapons.MODID, "tier_one"));
            Set<MKWrapperTier> weaponTiers = new HashSet<>();
            weaponTiers.add(MKWeaponsItems.STONE_TIER);
            weaponTiers.add(MKWeaponsItems.WOOD_TIER);

            LootItemTemplate weaponTemplate = new LootItemTemplate(LootSlotManager.MAIN_HAND);


            for (MKMeleeWeapon weapon : MKWeaponsItems.WEAPONS) {
                if (weaponTiers.contains(weapon.getMKTier())) {
                    weaponTemplate.addItem(weapon);
                }
            }
            AttributeOption healthAttribute = new AttributeOption();
            healthAttribute.addAttributeModifier(Attributes.MAX_HEALTH, tier.getName().toString(),
                    5, 10, AttributeModifier.Operation.ADDITION);
            AttributeOption manaRegen = new AttributeOption();
            manaRegen.addAttributeModifier(MKAttributes.MANA_REGEN, tier.getName().toString(),
                    0.5, 2.0, AttributeModifier.Operation.ADDITION);

            LootItemTemplate ringTemplate = new LootItemTemplate(LootSlotManager.RINGS);
            ringTemplate.addItem(MKWeaponsItems.CopperRing.get());

            List<LootItemTemplate> templates = Arrays.asList(weaponTemplate, ringTemplate);

            for (LootItemTemplate temp : templates) {
                temp.addRandomizationOption(healthAttribute);
                temp.addRandomizationOption(manaRegen);
                temp.addTemplate(new RandomizationTemplate(new ResourceLocation(MKWeapons.MODID, "simple_template"),
                        RandomizationSlotManager.ATTRIBUTE_SLOT), 10);
                temp.addTemplate(new RandomizationTemplate(new ResourceLocation(MKWeapons.MODID, "simple_template_2x"),
                        RandomizationSlotManager.ATTRIBUTE_SLOT, RandomizationSlotManager.ATTRIBUTE_SLOT), 10);
                tier.addItemTemplate(temp, 1.0);
            }

            return tier;
        }
    }
}