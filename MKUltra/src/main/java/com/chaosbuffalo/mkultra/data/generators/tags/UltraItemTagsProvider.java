package com.chaosbuffalo.mkultra.data.generators.tags;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.init.CoreTags;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.init.MKUItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class UltraItemTagsProvider extends ItemTagsProvider {
    public UltraItemTagsProvider(DataGenerator dataGenerator,
                                 CompletableFuture<HolderLookup.Provider> lookupProvider,
                                 BlockTagsProvider blockTagProvider,
                                 ExistingFileHelper existingFileHelper) {
        super(dataGenerator.getPackOutput(), lookupProvider,  blockTagProvider.contentsGetter(), MKUltra.MODID, existingFileHelper);
    }

    @Nonnull
    @Override
    public String getName() {
        return "MKUltra armor class item tags";
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        tag(CoreTags.Items.LIGHT_ARMOR)
                .add(MKUItems.seawovenBoots.get(), MKUItems.seawovenHelmet.get(), MKUItems.seawovenChestplate.get(), MKUItems.seawovenLeggings.get());
        tag(CoreTags.Items.MEDIUM_ARMOR)
                .add(MKUItems.trooperKnightLeggings.get(), MKUItems.trooperKnightBoots.get(), MKUItems.trooperKnightHelmet.get(), MKUItems.trooperKnightChestplate.get())
                .add(MKUItems.ancientBronzeBoots.get(), MKUItems.ancientBronzeChestplate.get(), MKUItems.ancientBronzeLeggings.get(), MKUItems.ancientBronzeHelmet.get());
        tag(CoreTags.Items.HEAVY_ARMOR)
                .add(MKUItems.greenKnightHelmet.get(), MKUItems.greenKnightChestplate.get(), MKUItems.greenKnightBoots.get(), MKUItems.greenKnightLeggings.get());

    }
}
