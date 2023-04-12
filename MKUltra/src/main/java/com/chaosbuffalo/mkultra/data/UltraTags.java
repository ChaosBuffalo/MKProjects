package com.chaosbuffalo.mkultra.data;

import com.chaosbuffalo.mkultra.MKUltra;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;

public class UltraTags {

    public static class Biomes {
        public static final TagKey<Biome> HAS_INTRO_CASTLE = tag("has_intro_castle");
        public static final TagKey<Biome> HAS_DESERT_TEMPLE_VILLAGE = tag("has_desert_temple_village");
        public static final TagKey<Biome> HAS_NECROTIDE_ALTER = tag("has_necrotide_alter");

        private static TagKey<Biome> tag(String name) {
            return TagKey.create(Registries.BIOME, new ResourceLocation(MKUltra.MODID, name));
        }
    }

    public static class Structures {
        public static final TagKey<Structure> INTRO_CASTLE = tag("intro_castle");
        public static final TagKey<Structure> DESERT_TEMPLE_VILLAGE = tag("desert_temple_village");
        public static final TagKey<Structure> NECROTIDE_ALTER = tag("necrotide_alter");

        private static TagKey<Structure> tag(String name) {
            return TagKey.create(Registries.STRUCTURE, new ResourceLocation(MKUltra.MODID, name));
        }
    }
}
