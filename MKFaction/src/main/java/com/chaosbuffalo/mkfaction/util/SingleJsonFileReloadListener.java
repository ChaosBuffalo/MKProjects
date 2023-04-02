package com.chaosbuffalo.mkfaction.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;


public abstract class SingleJsonFileReloadListener extends SimplePreparableReloadListener<JsonObject> {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Gson gson;
    private final ResourceLocation loc;

    public SingleJsonFileReloadListener(Gson gson, String modid, String path) {
        this.gson = gson;
        this.loc = new ResourceLocation(modid, path + ".json");
    }

    public ResourceLocation getLoc() {
        return loc;
    }

    @Override
    protected JsonObject prepare(ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
        try (
                Resource iresource = resourceManagerIn.getResource(getLoc());
                InputStream inputstream = iresource.getInputStream();
                Reader reader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8));
        ) {
            JsonObject jsonobject = GsonHelper.fromJson(this.gson, reader, JsonObject.class);
            if (jsonobject != null) {
                return jsonobject;
            } else {
                LOGGER.error("Couldn't load data file {} as it's null or empty", loc);
            }
        } catch (IllegalArgumentException | IOException | JsonParseException jsonparseexception) {
            LOGGER.error("Couldn't parse data file {}", loc, jsonparseexception);
        }
        return null;
    }
}