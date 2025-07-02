package com.chaosbuffalo.mknpc.data.providers;


import com.chaosbuffalo.mkcore.data.providers.MKDataProvider;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public abstract class NpcDefinitionProvider extends MKDataProvider {

    public NpcDefinitionProvider(DataGenerator generator, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId) {
        super(generator, lookupProvider, modId, "Npc Definitions");
    }

    public CompletableFuture<?> writeDefinition(NpcDefinition definition, CachedOutput pOutput) {
        return writeDefinition(p -> definition, pOutput);
    }

    public CompletableFuture<?> writeDefinition(Function<HolderLookup.Provider, NpcDefinition> definitionProvider, CachedOutput pOutput) {
        return registries.thenCompose(registries -> {
            NpcDefinition definition = definitionProvider.apply(registries);
            ResourceLocation key = definition.getDefinitionName();
            Path local = Paths.get("data", key.getNamespace(), "mknpc", "mknpcs", key.getPath() + ".json");
            Path path = generator.getPackOutput().getOutputFolder().resolve(local);
            return DataProvider.saveStable(pOutput, registries, NpcDefinition.CODEC, definition, path);
        });
    }
}
