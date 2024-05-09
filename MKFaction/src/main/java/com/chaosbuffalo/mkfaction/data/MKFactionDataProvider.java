package com.chaosbuffalo.mkfaction.data;

import com.chaosbuffalo.mkcore.data.providers.MKDataProvider;
import com.chaosbuffalo.mkfaction.faction.FactionManager;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public abstract class MKFactionDataProvider extends MKDataProvider {
    public MKFactionDataProvider(DataGenerator generator, String modId) {
        super(generator, modId, "Factions");
    }

    public CompletableFuture<?> writeFaction(MKFaction faction, CachedOutput cachedOutput) {
        Path outputFolder = generator.getPackOutput().getOutputFolder();
        ResourceLocation key = Objects.requireNonNull(faction.getId());
        Path local = Paths.get("data", key.getNamespace(), FactionManager.DEFINITION_FOLDER, key.getPath() + ".json");
        Path path = outputFolder.resolve(local);
        JsonElement element = faction.serialize(JsonOps.INSTANCE);
        return DataProvider.saveStable(cachedOutput, element, path);
    }
}
