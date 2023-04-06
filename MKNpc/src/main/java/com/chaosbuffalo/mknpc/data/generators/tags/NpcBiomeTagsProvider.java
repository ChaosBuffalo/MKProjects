package com.chaosbuffalo.mknpc.data.generators.tags;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.data.NpcTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class NpcBiomeTagsProvider extends BiomeTagsProvider {
    public NpcBiomeTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookup,
                                @Nullable ExistingFileHelper existingFileHelper) {
        super(packOutput, lookup, MKNpc.MODID, existingFileHelper);
    }

    @Override
    public void addTags(HolderLookup.Provider provider) {
        tag(NpcTags.Biomes.HAS_TEST_STRUCTURES)
                .addTag(BiomeTags.IS_FOREST);
    }
}
