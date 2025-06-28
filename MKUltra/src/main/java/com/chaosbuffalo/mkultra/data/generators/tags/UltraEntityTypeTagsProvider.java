package com.chaosbuffalo.mkultra.data.generators.tags;

import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.init.MKUEntities;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.EntityTypeTags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class UltraEntityTypeTagsProvider extends EntityTypeTagsProvider {
    public UltraEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider,
                                       @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, MKUltra.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {

        tag(EntityTypeTags.UNDEAD).add(MKUEntities.HUMAN_GHOST_TYPE.get(),
                MKUEntities.HYBOREAN_SKELETON_TYPE.get(), MKUEntities.ZOMBIFIED_PIGLIN_TYPE.get());
        tag(EntityTypeTags.ZOMBIES).add(MKUEntities.ZOMBIFIED_PIGLIN_TYPE.get());
        tag(EntityTypeTags.SKELETONS).add(MKUEntities.HYBOREAN_SKELETON_TYPE.get());
    }
}
