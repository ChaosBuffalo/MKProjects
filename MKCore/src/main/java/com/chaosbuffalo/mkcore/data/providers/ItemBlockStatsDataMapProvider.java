package com.chaosbuffalo.mkcore.data.providers;

import com.chaosbuffalo.mkcore.init.CoreDataMaps;
import com.chaosbuffalo.mkcore.item.ItemBlockStats;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.DataMapProvider;

import java.util.concurrent.CompletableFuture;

public class ItemBlockStatsDataMapProvider extends DataMapProvider {
    protected ItemBlockStatsDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    public void itemTag(TagKey<Item> itemTag, ItemBlockStats stats) {
        builder(CoreDataMaps.ITEM_BLOCK_STATS)
                .add(itemTag, stats,false)
                .build();
    }

    public void item(ResourceKey<Item> item, ItemBlockStats stats) {
        builder(CoreDataMaps.ITEM_BLOCK_STATS)
                .add(item, stats,false)
                .build();
    }

    public void item(Item item, ItemBlockStats stats) {
        builder(CoreDataMaps.ITEM_BLOCK_STATS)
                .add(item.builtInRegistryHolder(), stats,false)
                .build();
    }
}
