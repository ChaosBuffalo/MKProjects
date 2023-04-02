package com.chaosbuffalo.mknpc.data;

import com.chaosbuffalo.mkcore.serialization.IDynamicMapSerializer;
import com.chaosbuffalo.mknpc.MKNpc;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class StructureSetProvider implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final DataGenerator generator;

    public StructureSetProvider(DataGenerator generator) {
        this.generator = generator;
    }


    public void writeSet(StructureSetData set, @Nonnull HashCache cache){
        Path outputFolder = this.generator.getOutputFolder();
        ResourceLocation key = set.name;
        Path path = outputFolder.resolve("data/" + key.getNamespace() + "/worldgen/structure_set/" + key.getPath() + ".json");
        try {
            JsonElement element = set.serialize(JsonOps.INSTANCE);
            DataProvider.save(GSON, cache, element, path);
        } catch (IOException e){
            MKNpc.LOGGER.error("Couldn't write structure set {}", path, e);
        }
    }

    @Override
    public String getName() {
        return "Structure Sets";
    }

    public static class StructureSetEntry implements IDynamicMapSerializer {

        ResourceLocation structureName;
        int weight;


        public StructureSetEntry(ResourceLocation structureName, int weight) {
            this.structureName = structureName;
            this.weight = weight;
        }


        @Override
        public <D> void deserialize(Dynamic<D> dynamic) {

        }

        @Override
        public <D> void writeAdditionalData(DynamicOps<D> dynamicOps, ImmutableMap.Builder<D, D> builder) {
            builder.put(dynamicOps.createString("structure"), dynamicOps.createString(structureName.toString()));
            builder.put(dynamicOps.createString("weight"), dynamicOps.createInt(weight));
        }
    }



    public static class StructureSetData implements IDynamicMapSerializer {
        ResourceLocation name;
        List<StructureSetEntry> structureEntries;
        StructurePlacement placement;

        public StructureSetData(ResourceLocation name, StructurePlacement placement) {
            this.name = name;
            this.placement = placement;
            structureEntries = new ArrayList<>();
        }

        public StructureSetData withStructure(ResourceLocation configuredStructureName, int weight) {
            structureEntries.add(new StructureSetEntry(configuredStructureName, weight));
            return this;
        }


        @Override
        public <D> void deserialize(Dynamic<D> dynamic) {
            // we dont actually need this as we're just using this to serialize it
        }

        @Override
        public <D> void writeAdditionalData(DynamicOps<D> dynamicOps, ImmutableMap.Builder<D, D> builder) {
            builder.put(dynamicOps.createString("structures"), dynamicOps.createList(structureEntries.stream().map(x -> x.serialize(dynamicOps))));
            builder.put(dynamicOps.createString("placement"), StructurePlacement.CODEC.encodeStart(dynamicOps, placement).result().get());
        }
    }
}