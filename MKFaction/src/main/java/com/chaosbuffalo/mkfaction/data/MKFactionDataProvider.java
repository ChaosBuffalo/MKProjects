package com.chaosbuffalo.mkfaction.data;

import com.chaosbuffalo.mkfaction.faction.FactionManager;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public abstract class MKFactionDataProvider implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final String modId;
    private final DataGenerator generator;

    public MKFactionDataProvider(String modId, DataGenerator generator) {
        this.modId = modId;
        this.generator = generator;
    }

    public CompletableFuture<?> writeFaction(MKFaction faction, CachedOutput cachedOutput) {
        Path outputFolder = generator.getPackOutput().getOutputFolder();
        ResourceLocation key = Objects.requireNonNull(faction.getId());
        Path local = Paths.get("data", key.getNamespace(), FactionManager.DEFINITION_FOLDER, key.getPath() + ".json");
        Path path = outputFolder.resolve(local);
        JsonElement element = faction.serialize(JsonOps.INSTANCE);
        return DataProvider.saveStable(cachedOutput, element, path);
    }


    @Nonnull
    @Override
    public String getName() {
        return String.format("Faction Generator for %s", modId);
    }
}
