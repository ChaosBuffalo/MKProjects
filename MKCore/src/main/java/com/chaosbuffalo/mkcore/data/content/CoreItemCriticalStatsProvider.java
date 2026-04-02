package com.chaosbuffalo.mkcore.data.content;

import com.chaosbuffalo.mkcore.data.providers.ItemCriticalStatsDataMapProvider;
import com.chaosbuffalo.mkcore.item.ItemCriticalStats;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;

import java.util.concurrent.CompletableFuture;

public class CoreItemCriticalStatsProvider extends ItemCriticalStatsDataMapProvider {
    protected CoreItemCriticalStatsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {

        ItemCriticalStats sword = new ItemCriticalStats(0.05f, 1.0f);
        itemTag(ItemTags.SWORDS, sword);

        ItemCriticalStats axe = new ItemCriticalStats(0.15f, 1.0f);
        itemTag(ItemTags.AXES, axe);

        ItemCriticalStats pickaxe = new ItemCriticalStats(0.05f, 0.5f);
        itemTag(ItemTags.PICKAXES, pickaxe);

        ItemCriticalStats shovel = new ItemCriticalStats(0.05f, 0.5f);
        itemTag(ItemTags.SHOVELS, shovel);

        ItemCriticalStats hoe = new ItemCriticalStats(0.05f, 0.5f);
        itemTag(ItemTags.HOES, hoe);
    }

    @Override
    public String getName() {
        return "MKCore item critical stats provider";
    }
}
