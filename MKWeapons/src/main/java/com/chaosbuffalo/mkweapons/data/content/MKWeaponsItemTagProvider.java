package com.chaosbuffalo.mkweapons.data.content;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.init.MKWeaponsItems;
import com.chaosbuffalo.mkweapons.items.MKBow;
import com.chaosbuffalo.mkweapons.items.MKMeleeWeapon;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class MKWeaponsItemTagProvider extends ItemTagsProvider {

    public MKWeaponsItemTagProvider(DataGenerator dataGenerator,
                                    CompletableFuture<HolderLookup.Provider> lookupProvider,
                                    BlockTagsProvider blockTagProvider,
                                    ExistingFileHelper existingFileHelper) {
        super(dataGenerator.getPackOutput(), lookupProvider, blockTagProvider.contentsGetter(), MKWeapons.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        tag(accessory("ring")).add(MKWeaponsItems.CopperRing.get(),
                MKWeaponsItems.GoldRing.get(), MKWeaponsItems.RoseGoldRing.get(), MKWeaponsItems.SilverRing.get());
        tag(accessory("earring")).add(MKWeaponsItems.GoldEarring.get(), MKWeaponsItems.SilverEarring.get(),
                MKWeaponsItems.CopperEarring.get());
        for (MKMeleeWeapon weapon : MKWeaponsItems.WEAPONS) {
            tag(ItemTags.SWORD_ENCHANTABLE).add(weapon);
        }
        for (MKBow bow : MKWeaponsItems.BOWS) {
            tag(ItemTags.BOW_ENCHANTABLE).add(bow);
        }
    }

    private static TagKey<Item> accessory(String name) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("curios", name));
    }

    @Override
    public String getName() {
        return "MK Weapons Item Tags";
    }
}
