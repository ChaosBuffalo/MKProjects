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
        public static final TagKey<Biome> HAS_TEST_TOWER = tag("has_test_tower");

        private static TagKey<Biome> tag(String name) {
            return TagKey.create(Registries.BIOME, MKNpc.id(name));
        }
    }

    public static class Structures {
        public static final TagKey<Structure> TEST_STRUCTURE = tag("test_structures");
        public static final TagKey<Structure> TEST_TOWER = tag("test_tower");

        private static TagKey<Structure> tag(String name) {
            return TagKey.create(Registries.STRUCTURE, MKNpc.id(name));
        }
    }
}
