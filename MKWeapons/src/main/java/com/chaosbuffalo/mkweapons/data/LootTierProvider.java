package com.chaosbuffalo.mkweapons.data;

import com.chaosbuffalo.mkcore.data.MKDataProvider;
import com.chaosbuffalo.mkweapons.items.randomization.LootTier;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public abstract class LootTierProvider extends MKDataProvider {

    public LootTierProvider(DataGenerator generator, String modId) {
        super(generator, modId, "Loot Tiers");
    }

    public CompletableFuture<?> writeLootTier(LootTier lootTier, CachedOutput pOutput) {
        Path outputFolder = this.generator.getPackOutput().getOutputFolder();
        ResourceLocation key = lootTier.getName();
        Path path = outputFolder.resolve("data/" + key.getNamespace() + "/loot_tiers/" + key.getPath() + ".json");
        JsonElement element = lootTier.serialize(JsonOps.INSTANCE);
        return DataProvider.saveStable(pOutput, element, path);
    }
}
