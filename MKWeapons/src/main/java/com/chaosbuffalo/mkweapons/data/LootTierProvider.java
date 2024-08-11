package com.chaosbuffalo.mkweapons.data;

import com.chaosbuffalo.mkcore.data.providers.MKDataProvider;
import com.chaosbuffalo.mkweapons.items.randomization.LootTier;
import com.chaosbuffalo.mkweapons.items.randomization.LootTierManager;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

public abstract class LootTierProvider extends MKDataProvider {

    public LootTierProvider(DataGenerator generator, String modId) {
        super(generator, modId, "Loot Tiers");
    }

    public CompletableFuture<?> writeLootTier(LootTier lootTier, CachedOutput pOutput) {
        Path outputFolder = this.generator.getPackOutput().getOutputFolder();
        ResourceLocation key = lootTier.getName();

        Path local = Paths.get("data", key.getNamespace(), LootTierManager.DEFINITION_FOLDER, key.getPath() + ".json");
        Path path = outputFolder.resolve(local);

        JsonElement element = lootTier.serialize(JsonOps.INSTANCE);
        return DataProvider.saveStable(pOutput, element, path);
    }
}
