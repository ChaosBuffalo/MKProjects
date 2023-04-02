package com.chaosbuffalo.mknpc.data;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.quest.QuestDefinition;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;

public abstract class QuestDefinitionProvider implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final DataGenerator generator;

    public QuestDefinitionProvider(DataGenerator generator) {
        this.generator = generator;
    }


    public void writeDefinition(QuestDefinition definition, @Nonnull HashCache cache){
        Path outputFolder = this.generator.getOutputFolder();
        ResourceLocation key = definition.getName();
        Path path = outputFolder.resolve("data/" + key.getNamespace() + "/mkquests/" + key.getPath() + ".json");
        try {
            JsonElement element = definition.serialize(JsonOps.INSTANCE);
            DataProvider.save(GSON, cache, element, path);
        } catch (IOException e){
            MKNpc.LOGGER.error("Couldn't write quest {}", path, e);
        }
    }

    @Override
    public String getName() {
        return "MKNpc Quest Definitions";
    }
}
