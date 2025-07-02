package com.chaosbuffalo.mkweapons.data.providers;

import com.chaosbuffalo.mkcore.data.providers.MKDataProvider;
import com.chaosbuffalo.mkweapons.items.randomization.LootTier;
import com.chaosbuffalo.mkweapons.items.randomization.LootTierManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public abstract class LootTierProvider extends MKDataProvider {

    public LootTierProvider(DataGenerator generator, CompletableFuture<HolderLookup.Provider> registries, String modId) {
        super(generator, registries, modId, "Loot Tiers");
    }

    public CompletableFuture<?> writeLootTier(LootTier lootTier, CachedOutput pOutput) {
        return writeLootTier(p -> lootTier, pOutput);
    }

    public CompletableFuture<?> writeLootTier(Function<HolderLookup.Provider, LootTier> lootTierProvider, CachedOutput pOutput) {
        return registries.thenCompose(registries -> {
            LootTier lootTier = lootTierProvider.apply(registries);
            ResourceLocation key = lootTier.getName();

            Path local = Paths.get("data", key.getNamespace(), LootTierManager.DEFINITION_FOLDER, key.getPath() + ".json");
            Path path = generator.getPackOutput().getOutputFolder().resolve(local);

            return DataProvider.saveStable(pOutput, registries, LootTier.CODEC, lootTier, path);
        });
    }
}
