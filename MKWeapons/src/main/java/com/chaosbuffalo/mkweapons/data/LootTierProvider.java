package com.chaosbuffalo.mkweapons.data;

import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.init.MKWeaponsItems;
import com.chaosbuffalo.mkweapons.items.MKMeleeWeapon;
import com.chaosbuffalo.mkweapons.items.randomization.LootItemTemplate;
import com.chaosbuffalo.mkweapons.items.randomization.LootTier;
import com.chaosbuffalo.mkweapons.items.randomization.options.AttributeOption;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlotManager;
import com.chaosbuffalo.mkweapons.items.randomization.slots.RandomizationSlotManager;
import com.chaosbuffalo.mkweapons.items.randomization.templates.RandomizationTemplate;
import com.chaosbuffalo.mkweapons.items.weapon.tier.MKTier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.data.DataProvider;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LootTierProvider implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final DataGenerator generator;

    public LootTierProvider(DataGenerator generator) {
        this.generator = generator;
    }

    @Override
    public void run(@Nonnull HashCache cache) {
        writeLootTier(generateTierOne(), cache);
    }

    private LootTier generateTierOne(){
        LootTier tier = new LootTier(new ResourceLocation(MKWeapons.MODID, "tier_one"));
        Set<MKTier> weaponTiers = new HashSet<>();
        weaponTiers.add(MKWeaponsItems.STONE_TIER);
        weaponTiers.add(MKWeaponsItems.WOOD_TIER);

        LootItemTemplate weaponTemplate = new LootItemTemplate(LootSlotManager.MAIN_HAND);


        for (MKMeleeWeapon weapon : MKWeaponsItems.WEAPONS){
            if (weaponTiers.contains(weapon.getMKTier())){
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
        ringTemplate.addItem(MKWeaponsItems.CopperRing);

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

    public void writeLootTier(LootTier lootTier, @Nonnull HashCache cache){
        Path outputFolder = this.generator.getOutputFolder();
        ResourceLocation key = lootTier.getName();
        Path path = outputFolder.resolve("data/" + key.getNamespace() + "/loot_tiers/" + key.getPath() + ".json");
        try {
            JsonElement element = lootTier.serialize(JsonOps.INSTANCE);
            DataProvider.save(GSON, cache, element, path);
        } catch (IOException e){
            MKWeapons.LOGGER.error("Couldn't write loot tier {}", path, e);
        }
    }

    @Nonnull
    @Override
    public String getName() {
        return "MKWeapons Loot Tiers";
    }
}
