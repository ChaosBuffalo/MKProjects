package com.chaosbuffalo.mknpc.data;

import com.chaosbuffalo.mknpc.MKNpc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;

public abstract class TemplatePoolProvider implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final DataGenerator generator;

    public TemplatePoolProvider(DataGenerator generator) {
        this.generator = generator;
    }


    public void writePool(StructureTemplatePool pool, @Nonnull HashCache cache){
        Path outputFolder = this.generator.getOutputFolder();
        ResourceLocation key = pool.getName();
        Path path = outputFolder.resolve("data/" + key.getNamespace() + "/worldgen/template_pool/" + key.getPath() + ".json");
        StructureTemplatePool.CODEC.encodeStart(JsonOps.INSTANCE, Holder.direct(pool)).result().ifPresent(x -> {
            try {
                DataProvider.save(GSON, cache, x, path);
            } catch (IOException e){
                MKNpc.LOGGER.error("Couldn't write template pool {}", path, e);
            }
        });
    }

    @Override
    public String getName() {
        return "Template Pools";
    }
}