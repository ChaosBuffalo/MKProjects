package com.chaosbuffalo.mkcore.data.providers;

import com.chaosbuffalo.mkcore.init.CoreDataMaps;
import com.chaosbuffalo.mkcore.item.ItemCriticalStats;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.DataMapProvider;

import java.util.concurrent.CompletableFuture;

public class ItemCriticalStatsDataMapProvider extends DataMapProvider {
    protected ItemCriticalStatsDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    public void itemTag(TagKey<Item> itemTag, ItemCriticalStats stats) {
        builder(CoreDataMaps.ITEM_CRITICAL_STATS)
                .add(itemTag, stats,false)
                .build();
    }
}
