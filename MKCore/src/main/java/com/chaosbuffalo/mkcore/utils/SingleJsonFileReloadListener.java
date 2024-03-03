package com.chaosbuffalo.mkcore.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;


public abstract class SingleJsonFileReloadListener extends SimplePreparableReloadListener<JsonObject> {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Gson gson;
    private final ResourceLocation resourcePath;

    public SingleJsonFileReloadListener(Gson gson, String modid, String path) {
        this.gson = gson;
        this.resourcePath = new ResourceLocation(modid, path + ".json");
    }

    @Override
    protected JsonObject prepare(ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
        return resourceManagerIn.getResource(resourcePath).map(x -> {
            try (BufferedReader reader = x.openAsReader()) {
                return GsonHelper.fromJson(this.gson, reader, JsonObject.class);
            } catch (IllegalArgumentException | IOException | JsonParseException jsonparseexception) {
                LOGGER.error("Couldn't parse data file {}", resourcePath, jsonparseexception);
            }
            return null;
        }).orElse(null);
    }
}