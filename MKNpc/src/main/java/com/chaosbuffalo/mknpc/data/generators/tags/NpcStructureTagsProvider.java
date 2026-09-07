package com.chaosbuffalo.mknpc.data.generators.tags;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.data.NpcTags;
import com.chaosbuffalo.mknpc.data.registries.NpcStructures;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.StructureTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class NpcStructureTagsProvider extends StructureTagsProvider {
    public NpcStructureTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookup,
                                    @Nullable ExistingFileHelper existingFileHelper) {
        super(packOutput, lookup, MKNpc.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        tag(NpcTags.Structures.TEST_STRUCTURE)
                .add(NpcStructures.TEST_JIGSAW);
        tag(NpcTags.Structures.TEST_TOWER)
                .add(NpcStructures.TEST_TOWER);
        tag(NpcTags.Structures.TEST_KEEP)
                .add(NpcStructures.TEST_KEEP);
    }
}
