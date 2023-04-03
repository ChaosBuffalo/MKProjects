package com.chaosbuffalo.mkweapons.data;

import com.chaosbuffalo.mkweapons.init.MKWeaponsItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

public class MKWeaponsItemTagProvider extends ItemTagsProvider {

    public MKWeaponsItemTagProvider(DataGenerator dataGenerator, BlockTagsProvider blockTagProvider, String modId,
                                    @Nullable ExistingFileHelper existingFileHelper) {
        super(dataGenerator, blockTagProvider, modId, existingFileHelper);
    }

    @Override
    protected void addTags() {
        tag(accessory("ring")).add(MKWeaponsItems.CopperRing.get(),
                MKWeaponsItems.GoldRing.get(), MKWeaponsItems.RoseGoldRing.get(), MKWeaponsItems.SilverRing.get());
        tag(accessory("earring")).add(MKWeaponsItems.GoldEarring.get(), MKWeaponsItems.SilverEarring.get());
    }

    private static TagKey<Item> accessory(String name) {
        return ItemTags.create(new ResourceLocation("curios", name));
    }

    @Override
    public String getName() {
        return "MK Weapons Item Tags";
    }
}
