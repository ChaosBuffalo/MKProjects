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

public abstract class NpcDefinitionProvider extends MKDataProvider {

    protected final CompletableFuture<HolderLookup.Provider> lookupProvider;

    public NpcDefinitionProvider(DataGenerator generator, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId) {
        super(generator, modId, "Npc Definitions");
        this.lookupProvider = lookupProvider;
    }

    public CompletableFuture<?> writeDefinition(NpcDefinition definition, CachedOutput pOutput) {
        Path outputFolder = this.generator.getPackOutput().getOutputFolder();
        ResourceLocation key = definition.getDefinitionName();
        Path local = Paths.get("data", key.getNamespace(), "mknpc", "mknpcs", key.getPath() + ".json");
        Path path = outputFolder.resolve(local);
        return lookupProvider.thenCompose(registries -> DataProvider.saveStable(pOutput, registries, NpcDefinition.CODEC, definition, path));
    }
}
