package com.chaosbuffalo.mknpc.data.providers;

import com.chaosbuffalo.mkcore.data.providers.MKDataProvider;
import com.chaosbuffalo.mknpc.quest.QuestDefinition;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public abstract class QuestDefinitionProvider extends MKDataProvider {

    CompletableFuture<HolderLookup.Provider> provider;

    public QuestDefinitionProvider(DataGenerator generator, CompletableFuture<HolderLookup.Provider> provider, String modId) {
        super(generator, modId, "Quest Definitions");
        this.provider = provider;
    }

    public CompletableFuture<?> writeDefinition(QuestDefinition definition, CachedOutput pOutput) {
        Path outputFolder = this.generator.getPackOutput().getOutputFolder();
        ResourceLocation key = definition.getName();
        Path path = outputFolder.resolve("data/" + key.getNamespace() + "/mkquests/" + key.getPath() + ".json");
        return provider.thenCompose(registries -> {
            JsonElement element =  definition.serialize(JsonOps.INSTANCE, registries);
            return DataProvider.saveStable(pOutput, element, path);
        });
    }

    public CompletableFuture<?> writeDefinition(Function<HolderLookup.Provider, QuestDefinition> definitionProvider, CachedOutput pOutput) {
        Path outputFolder = this.generator.getPackOutput().getOutputFolder();

        return provider.thenCompose(registries -> {
            var definition = definitionProvider.apply(registries);
            var regOps = registries.createSerializationContext(JsonOps.INSTANCE);
            ResourceLocation key = definition.getName();
            Path path = outputFolder.resolve("data/" + key.getNamespace() + "/mkquests/" + key.getPath() + ".json");
            JsonElement element =  definition.serialize(regOps, registries);
            return DataProvider.saveStable(pOutput, element, path);
        });
    }
}
