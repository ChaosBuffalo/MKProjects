package com.chaosbuffalo.mkultra.data.generators.tags;

import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.data.UltraTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraftforge.common.data.ExistingFileHelper;
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
                .addTag(BiomeTags.HAS_VILLAGE_DESERT);
        tag(UltraTags.Biomes.HAS_NECROTIDE_ALTER)
                .addTag(BiomeTags.HAS_VILLAGE_DESERT);
    }
}
