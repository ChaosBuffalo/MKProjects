package com.chaosbuffalo.mknpc.data.providers;

import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public abstract class NpcDefinitionTagsProvider extends IntrinsicHolderTagsProvider<NpcDefinition> {


    public NpcDefinitionTagsProvider(PackOutput p_256164_, ResourceKey<? extends Registry<NpcDefinition>> p_256155_,
                                     CompletableFuture<HolderLookup.Provider> p_256488_, Function<NpcDefinition,
            ResourceKey<NpcDefinition>> p_256168_, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(p_256164_, p_256155_, p_256488_, p_256168_, modId, existingFileHelper);
    }
}
