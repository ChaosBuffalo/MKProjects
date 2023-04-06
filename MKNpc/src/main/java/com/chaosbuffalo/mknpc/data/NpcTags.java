package com.chaosbuffalo.mknpc.data;

import com.chaosbuffalo.mknpc.MKNpc;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;

public class NpcTags {

    public static class Biomes {
        public static final TagKey<Biome> HAS_TEST_STRUCTURES = tag("has_test_structures");

        private static TagKey<Biome> tag(String name) {
            return TagKey.create(Registries.BIOME, new ResourceLocation(MKNpc.MODID, name));
        }
    }

    public static class Structures {
        public static final TagKey<Structure> TEST_STRUCTURE = tag("test_structures");

        private static TagKey<Structure> tag(String name) {
            return TagKey.create(Registries.STRUCTURE, new ResourceLocation(MKNpc.MODID, name));
        }
    }
}
