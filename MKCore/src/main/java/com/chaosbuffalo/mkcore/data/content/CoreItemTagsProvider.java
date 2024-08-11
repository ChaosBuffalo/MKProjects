package com.chaosbuffalo.mkcore.data.content;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.init.CoreTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class CoreItemTagsProvider extends ItemTagsProvider {
    public CoreItemTagsProvider(DataGenerator dataGenerator,
                                CompletableFuture<HolderLookup.Provider> lookupProvider,
                                BlockTagsProvider blockTagProvider,
                                ExistingFileHelper existingFileHelper) {
        super(dataGenerator.getPackOutput(), lookupProvider, blockTagProvider.contentsGetter(), MKCore.MOD_ID, existingFileHelper);
    }

    @Nonnull
    @Override
    public String getName() {
        return "MKCore item tags";
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        tag(CoreTags.Items.ROBES_ARMOR);
        tag(CoreTags.Items.LIGHT_ARMOR)
                .add(Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS);
        tag(CoreTags.Items.MEDIUM_ARMOR)
                .add(Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS)
                .add(Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS)
                .add(Items.TURTLE_HELMET);
        tag(CoreTags.Items.HEAVY_ARMOR)
                .add(Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS)
                .add(Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS)
                .add(Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS);
        tag(CoreTags.Items.ARMOR)
                .addTag(CoreTags.Items.ROBES_ARMOR)
                .addTag(CoreTags.Items.LIGHT_ARMOR)
                .addTag(CoreTags.Items.MEDIUM_ARMOR)
                .addTag(CoreTags.Items.HEAVY_ARMOR);
    }
}
