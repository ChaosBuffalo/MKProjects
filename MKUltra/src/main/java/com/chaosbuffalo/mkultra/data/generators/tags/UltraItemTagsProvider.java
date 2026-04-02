package com.chaosbuffalo.mkultra.data.generators.tags;

import com.chaosbuffalo.mkcore.init.CoreTags;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.init.MKUItems;
import com.chaosbuffalo.mkweapons.init.MKWeaponsItems;
import com.chaosbuffalo.mkweapons.items.MKBow;
import com.chaosbuffalo.mkweapons.items.MKMeleeWeapon;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class UltraItemTagsProvider extends ItemTagsProvider {
    public UltraItemTagsProvider(DataGenerator dataGenerator,
                                 CompletableFuture<HolderLookup.Provider> lookupProvider,
                                 BlockTagsProvider blockTagProvider,
                                 ExistingFileHelper existingFileHelper) {
        super(dataGenerator.getPackOutput(), lookupProvider, blockTagProvider.contentsGetter(), MKUltra.MODID, existingFileHelper);
    }

    @Nonnull
    @Override
    public String getName() {
        return "MKUltra armor class item tags";
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        tag(CoreTags.Items.ROBES_ARMOR)
                .add(MKUItems.seawovenBoots.get(), MKUItems.seawovenHelmet.get(), MKUItems.seawovenChestplate.get(), MKUItems.seawovenLeggings.get())
                .add(MKUItems.ancientPriestChestplate.get(), MKUItems.ancientPriestLeggings.get(), MKUItems.ancientPriestBoots.get(), MKUItems.ancientPriestHelmet.get())
                .add(MKUItems.ancientCardinalChestplate.get(), MKUItems.ancientCardinalLeggings.get(), MKUItems.ancientCardinalBoots.get(), MKUItems.ancientCardinalHelmet.get())
                .add(MKUItems.themnianChestplate.get(), MKUItems.themnianLeggings.get(), MKUItems.themnianBoots.get(), MKUItems.themnianHelmet.get())
                .add(MKUItems.themnianLeaderChestplate.get(), MKUItems.themnianLeaderLeggings.get(), MKUItems.themnianLeaderBoots.get(), MKUItems.themnianLeaderHelmet.get());
        tag(CoreTags.Items.MEDIUM_ARMOR)
                .add(MKUItems.trooperKnightLeggings.get(), MKUItems.trooperKnightBoots.get(), MKUItems.trooperKnightHelmet.get(), MKUItems.trooperKnightChestplate.get())
                .add(MKUItems.ancientBronzeBoots.get(), MKUItems.ancientBronzeChestplate.get(), MKUItems.ancientBronzeLeggings.get(), MKUItems.ancientBronzeHelmet.get());
        tag(CoreTags.Items.HEAVY_ARMOR)
                .add(MKUItems.greenKnightHelmet.get(), MKUItems.greenKnightChestplate.get(), MKUItems.greenKnightBoots.get(), MKUItems.greenKnightLeggings.get());
        tag(accessory("hands"))
                .add(MKUItems.corruptedGauntlets.get());
        tag(accessory("ring"))
                .add(MKUItems.necrotideBand.get())
                .add(MKUItems.themcromancerArchonRing.get());
        for (MKMeleeWeapon weapon : MKWeaponsItems.getMeleeWeaponsFromMod(MKUltra.MODID)) {
            tag(ItemTags.SWORD_ENCHANTABLE).add(weapon);
        }
        for (MKBow bow : MKWeaponsItems.getRangedWeaponsFromMod(MKUltra.MODID)) {
            tag(ItemTags.BOW_ENCHANTABLE).add(bow);
        }
        tag(ItemTags.ARMOR_ENCHANTABLE)
                .add(MKUItems.greenKnightBoots.get(), MKUItems.greenKnightHelmet.get(), MKUItems.greenKnightChestplate.get(), MKUItems.greenKnightLeggings.get())
                .add(MKUItems.seawovenBoots.get(), MKUItems.seawovenHelmet.get(), MKUItems.seawovenChestplate.get(), MKUItems.seawovenLeggings.get())
                .add(MKUItems.ancientPriestChestplate.get(), MKUItems.ancientPriestLeggings.get(), MKUItems.ancientPriestBoots.get(), MKUItems.ancientPriestHelmet.get())
                .add(MKUItems.ancientCardinalChestplate.get(), MKUItems.ancientCardinalLeggings.get(), MKUItems.ancientCardinalBoots.get(), MKUItems.ancientCardinalHelmet.get())
                .add(MKUItems.themnianChestplate.get(), MKUItems.themnianLeggings.get(), MKUItems.themnianBoots.get(), MKUItems.themnianHelmet.get())
                .add(MKUItems.themnianLeaderChestplate.get(), MKUItems.themnianLeaderLeggings.get(), MKUItems.themnianLeaderBoots.get(), MKUItems.themnianLeaderHelmet.get())
                .add(MKUItems.trooperKnightLeggings.get(), MKUItems.trooperKnightBoots.get(), MKUItems.trooperKnightHelmet.get(), MKUItems.trooperKnightChestplate.get())
                .add(MKUItems.ancientBronzeBoots.get(), MKUItems.ancientBronzeChestplate.get(), MKUItems.ancientBronzeLeggings.get(), MKUItems.ancientBronzeHelmet.get());

    }

    private static TagKey<Item> accessory(String name) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("curios", name));
    }
}
