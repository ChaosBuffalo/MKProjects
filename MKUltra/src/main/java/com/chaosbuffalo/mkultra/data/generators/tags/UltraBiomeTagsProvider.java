package com.chaosbuffalo.mkultra.data.generators.tags;

import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.data.UltraTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class UltraBiomeTagsProvider extends BiomeTagsProvider {
    public UltraBiomeTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookup,
                                  @Nullable ExistingFileHelper existingFileHelper) {
        super(packOutput, lookup, MKUltra.MODID, existingFileHelper);
    }

    @Override
    public void addTags(HolderLookup.Provider provider) {
        tag(UltraTags.Biomes.HAS_INTRO_CASTLE)
                .addTag(BiomeTags.IS_OVERWORLD);
        tag(UltraTags.Biomes.HAS_DESERT_TEMPLE_VILLAGE)
                .addTag(BiomeTags.HAS_VILLAGE_DESERT)
                .addTag(BiomeTags.HAS_VILLAGE_SAVANNA)
                .addTag(BiomeTags.IS_BADLANDS)
                .addTag(BiomeTags.IS_MOUNTAIN);
        tag(UltraTags.Biomes.HAS_NECROTIDE_ALTER)
                .addTag(BiomeTags.IS_BADLANDS)
                .addTag(BiomeTags.IS_SAVANNA)
                .addTag(BiomeTags.HAS_VILLAGE_DESERT);
        tag(UltraTags.Biomes.HAS_DEEPSLATE_OBELISK)
                .addTag(BiomeTags.IS_OVERWORLD);
        tag(UltraTags.Biomes.HAS_HYBOREAN_CRYPT)
                .addTag(BiomeTags.IS_MOUNTAIN)
                .addTag(BiomeTags.IS_SAVANNA)
                .addTag(BiomeTags.IS_JUNGLE)
                .addTag(BiomeTags.IS_FOREST)
                .addTag(BiomeTags.IS_BADLANDS)
                .addTag(BiomeTags.HAS_VILLAGE_DESERT);
        tag(UltraTags.Biomes.HAS_DECAYING_CHURCH)
                .addTag(BiomeTags.IS_FOREST)
                .addTag(BiomeTags.IS_JUNGLE)
                .addTag(BiomeTags.HAS_VILLAGE_PLAINS)
                .addTag(BiomeTags.HAS_VILLAGE_DESERT);
        tag(UltraTags.Biomes.HAS_THEMCROMANCER_LAIR)
                .addTag(BiomeTags.HAS_VILLAGE_DESERT)
                .addTag(BiomeTags.HAS_VILLAGE_PLAINS)
                .addTag(BiomeTags.IS_SAVANNA)
                .addTag(BiomeTags.IS_FOREST);
        tag(UltraTags.Biomes.HAS_FIRE_SHRINE);
//                .addTag(BiomeTags.IS_MOUNTAIN)
//                .addTag(BiomeTags.IS_SAVANNA)
//                .addTag(BiomeTags.IS_JUNGLE)
//                .addTag(BiomeTags.IS_FOREST)
//                .addTag(BiomeTags.IS_BADLANDS)
//                .add(Biomes.DESERT);
    }
}
