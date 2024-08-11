package com.chaosbuffalo.mkcore.data.providers;

import com.chaosbuffalo.mkcore.core.talents.TalentManager;
import com.chaosbuffalo.mkcore.core.talents.TalentTreeDefinition;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

public abstract class TalentTreeProvider extends MKDataProvider {
    public TalentTreeProvider(DataGenerator generator, String modId) {
        super(generator, modId, "Talent Trees");
    }

    public CompletableFuture<?> writeDefinition(TalentTreeDefinition definition, CachedOutput pOutput) {
        Path outputFolder = this.generator.getPackOutput().getOutputFolder();
        ResourceLocation key = definition.getTreeId();
        Path local = Paths.get("data", key.getNamespace(), TalentManager.DEFINITION_FOLDER, key.getPath() + ".json");
        Path path = outputFolder.resolve(local);
        JsonElement element = definition.serialize(JsonOps.INSTANCE);
        return DataProvider.saveStable(pOutput, element, path);
    }
}
