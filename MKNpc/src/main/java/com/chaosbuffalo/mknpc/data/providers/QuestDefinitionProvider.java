package com.chaosbuffalo.mknpc.data.providers;

import com.chaosbuffalo.mkcore.data.providers.MKDataProvider;
import com.chaosbuffalo.mknpc.quest.QuestDefinition;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public abstract class QuestDefinitionProvider extends MKDataProvider {

    public QuestDefinitionProvider(DataGenerator generator, CompletableFuture<HolderLookup.Provider> provider, String modId) {
        super(generator, provider, modId, "Quest Definitions");
    }

    public CompletableFuture<?> writeDefinition(QuestDefinition definition, CachedOutput pOutput) {
        return writeDefinition(p -> definition, pOutput);
    }

    public CompletableFuture<?> writeDefinition(Function<HolderLookup.Provider, QuestDefinition> definitionProvider, CachedOutput pOutput) {
        Path outputFolder = this.generator.getPackOutput().getOutputFolder();

        return registries.thenCompose(registries -> {
            var definition = definitionProvider.apply(registries);
            ResourceLocation key = definition.getName();
            Path local = Paths.get("data", key.getNamespace(), "mknpc", "mkquests", key.getPath() + ".json");
            Path path = generator.getPackOutput().getOutputFolder().resolve(local);
            return DataProvider.saveStable(pOutput, registries, QuestDefinition.CODEC, definition, path);
        });
    }
}
