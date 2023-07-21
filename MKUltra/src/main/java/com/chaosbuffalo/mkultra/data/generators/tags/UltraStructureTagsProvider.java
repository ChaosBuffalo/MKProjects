package com.chaosbuffalo.mkultra.data.generators.tags;

import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.data.UltraTags;
import com.chaosbuffalo.mkultra.data.registries.UltraStructures;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.StructureTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class UltraStructureTagsProvider extends StructureTagsProvider {
    public UltraStructureTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookup,
                                      @Nullable ExistingFileHelper existingFileHelper) {
        super(packOutput, lookup, MKUltra.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        tag(UltraTags.Structures.INTRO_CASTLE)
                .add(UltraStructures.INTRO_CASTLE);
        tag(UltraTags.Structures.DESERT_TEMPLE_VILLAGE)
                .add(UltraStructures.DESERT_TEMPLE_VILLAGE);
        tag(UltraTags.Structures.NECROTIDE_ALTER)
                .add(UltraStructures.NECROTIDE_ALTER);
        tag(UltraTags.Structures.DEEPSLATE_OBELISK)
                .add(UltraStructures.DEEPSLATE_OBELISK);
        tag(UltraTags.Structures.HYBOREAN_CRYPT)
                .add(UltraStructures.HYBOREAN_CRYPT);
        tag(UltraTags.Structures.DECAYING_CHURCH)
                .add(UltraStructures.DECAYING_CHURCH);
    }
}
