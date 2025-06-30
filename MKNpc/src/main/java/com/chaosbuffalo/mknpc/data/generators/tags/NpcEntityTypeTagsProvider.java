package com.chaosbuffalo.mknpc.data.generators.tags;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.init.MKNpcEntityTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.EntityTypeTags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class NpcEntityTypeTagsProvider extends EntityTypeTagsProvider {

    public NpcEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider,
                                     @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, MKNpc.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(EntityTypeTags.UNDEAD).add(MKNpcEntityTypes.SKELETON_TYPE.get(), MKNpcEntityTypes.ZOMBIE_PIGLIN_TYPE.get());
        tag(EntityTypeTags.ZOMBIES).add(MKNpcEntityTypes.ZOMBIE_PIGLIN_TYPE.get());
        tag(EntityTypeTags.SKELETONS).add(MKNpcEntityTypes.SKELETON_TYPE.get());
    }
}
