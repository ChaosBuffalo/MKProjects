package com.chaosbuffalo.mkfaction.util;

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

        return resourceManagerIn.getResource(getLoc()).map(x -> {
            try (
                    InputStream inputstream = x.open();
            ) {
                Reader reader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8));
                return GsonHelper.fromJson(this.gson, reader, JsonObject.class);
            } catch (IllegalArgumentException | IOException | JsonParseException jsonparseexception) {
                LOGGER.error("Couldn't parse data file {}", loc, jsonparseexception);
            }
            return null;
        }).orElse(null);


    }
}