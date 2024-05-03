package com.chaosbuffalo.mknpc.quest;

import com.chaosbuffalo.mknpc.MKNpc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;

import java.util.HashMap;
import java.util.Map;

public class QuestDefinitionManager extends SimpleJsonResourceReloadListener {
    public static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    public static final String DEFINITION_FOLDER = "mkquests";

    public static final Map<ResourceLocation, QuestDefinition> DEFINITIONS = new HashMap<>();

    public QuestDefinitionManager() {
        super(GSON, DEFINITION_FOLDER);
        MinecraftForge.EVENT_BUS.addListener(this::addReloadListener);
    }

    private void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(this);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
        DEFINITIONS.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet()) {
            ResourceLocation definitionId = entry.getKey();
            MKNpc.LOGGER.info("Found Quest Definition file: {}", definitionId);
            QuestDefinition def = new QuestDefinition(definitionId);
            def.deserialize(new Dynamic<>(JsonOps.INSTANCE, entry.getValue()));
            DEFINITIONS.put(def.getName(), def);
        }
    }

    public static QuestDefinition getDefinition(ResourceLocation questName) {
        return DEFINITIONS.get(questName);
    }
}
