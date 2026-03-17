package com.chaosbuffalo.mkcore.data.content;

import com.chaosbuffalo.mkcore.data.providers.ItemBlockStatsDataMapProvider;
import com.chaosbuffalo.mkcore.item.ItemBlockStats;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class CoreItemBlockStatsProvider extends ItemBlockStatsDataMapProvider {
    protected CoreItemBlockStatsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {

        ItemBlockStats sword = new ItemBlockStats(20.0f, 0.75f);
        itemTag(ItemTags.SWORDS, sword);

        ItemBlockStats diamond = new ItemBlockStats(25.0f, 0.80f);
        item(Items.DIAMOND_SWORD, diamond);

        ItemBlockStats shield = new  ItemBlockStats(50.0f, 1.0f);
        item(Items.SHIELD, shield);
    }


    @Override
    public String getName() {
        return "MKCore item block stats provider";
    }
}
