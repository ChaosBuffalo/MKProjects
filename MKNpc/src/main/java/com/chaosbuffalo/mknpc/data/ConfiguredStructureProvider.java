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
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public abstract class ConfiguredStructureProvider implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final DataGenerator generator;

    public ConfiguredStructureProvider(DataGenerator generator) {
        this.generator = generator;
    }


    public void writeFeature(ConfiguredStructureData configuredData, @Nonnull HashCache cache){
        Path outputFolder = this.generator.getOutputFolder();
        ResourceLocation key = configuredData.name;
        Path path = outputFolder.resolve("data/" + key.getNamespace() + "/worldgen/configured_structure_feature/" + key.getPath() + ".json");
        try {
            JsonElement element = configuredData.serialize(JsonOps.INSTANCE);
            DataProvider.save(GSON, cache, element, path);
        } catch (IOException e){
            MKNpc.LOGGER.error("Couldn't write configured structure start {}", path, e);
        }
    }

    @Override
    public String getName() {
        return "Configured Features";
    }

    public static class ConfiguredStructureData implements IDynamicMapSerializer {
        ResourceLocation name;
        ResourceLocation startingPool;
        int depth;
        TagKey<Biome> biomeTagKey;
        StructureFeature<?> feature;
        boolean adaptNoise;
        Map<String, StructureSpawnOverride> spawnOverrides;

        public ConfiguredStructureData(ResourceLocation name, ResourceLocation startingPool, int depth, TagKey<Biome> biomeTagKey, StructureFeature<?> feature) {
            this.name = name;
            this.startingPool = startingPool;
            this.depth = depth;
            this.biomeTagKey = biomeTagKey;
            this.feature = feature;
            this.adaptNoise = false;
            spawnOverrides = new HashMap<>();
        }

        public ConfiguredStructureData withAdaptNoise(boolean val) {
            this.adaptNoise = val;
            return this;
        }

        public ConfiguredStructureData withSpawnOverride(String key, StructureSpawnOverride override) {
            spawnOverrides.put(key, override);
            return this;
        }

        @Override
        public <D> void deserialize(Dynamic<D> dynamic) {
            // we dont actually need this as we're just using this to serialize it
        }

        @Override
        public <D> void writeAdditionalData(DynamicOps<D> dynamicOps, ImmutableMap.Builder<D, D> builder) {
            ResourceLocation featureName = ForgeRegistries.STRUCTURE_FEATURES.getKey(feature);
            builder.put(dynamicOps.createString("type"), dynamicOps.createString(featureName.toString()));
            builder.put(dynamicOps.createString("config"), dynamicOps.createMap(
                    ImmutableMap.of(
                            dynamicOps.createString("start_pool"), dynamicOps.createString(startingPool.toString()),
                            dynamicOps.createString("size"), dynamicOps.createInt(depth)
                    )
            ));
            builder.put(dynamicOps.createString("biomes"),
                    TagKey.hashedCodec(ForgeRegistries.BIOMES.getRegistryKey()).encodeStart(dynamicOps, biomeTagKey).result().get());
            ImmutableMap.Builder<D, D> spawnOverrides = ImmutableMap.builder();
            for (Map.Entry<String, StructureSpawnOverride> override : this.spawnOverrides.entrySet()) {
                spawnOverrides.put(dynamicOps.createString(override.getKey()), StructureSpawnOverride.CODEC.encodeStart(dynamicOps, override.getValue()).result().get());
            }
            builder.put(dynamicOps.createString("spawn_overrides"), dynamicOps.createMap(spawnOverrides.build()));

            builder.put(dynamicOps.createString("adapt_noise"), dynamicOps.createBoolean(adaptNoise));
        }
    }
}
