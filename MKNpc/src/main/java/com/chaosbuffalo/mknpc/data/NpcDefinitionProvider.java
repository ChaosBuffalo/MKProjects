package com.chaosbuffalo.mknpc.data;

import com.chaosbuffalo.mkcore.data.MKDataProvider;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;


import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public abstract class NpcDefinitionProvider extends MKDataProvider {

    public NpcDefinitionProvider(DataGenerator generator, String modId) {
        super(generator, modId, "Npc Definitions");
    }

    public CompletableFuture<?> writeDefinition(NpcDefinition definition, CachedOutput pOutput) {
        Path outputFolder = this.generator.getPackOutput().getOutputFolder();
        ResourceLocation key = definition.getDefinitionName();
        Path path = outputFolder.resolve("data/" + key.getNamespace() + "/mknpcs/" + key.getPath() + ".json");
        JsonElement element = definition.serialize(JsonOps.INSTANCE);
        return DataProvider.saveStable(pOutput, element, path);
    }
}
