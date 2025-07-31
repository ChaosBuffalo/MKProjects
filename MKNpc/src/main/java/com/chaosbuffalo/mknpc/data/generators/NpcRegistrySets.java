package com.chaosbuffalo.mknpc.data.generators;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.data.MKNpcDefinitions;
import com.chaosbuffalo.mknpc.data.registries.NpcStructurePools;
import com.chaosbuffalo.mknpc.data.registries.NpcStructureSets;
import com.chaosbuffalo.mknpc.data.registries.NpcStructures;
import com.chaosbuffalo.mknpc.npc.NpcRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class NpcRegistrySets extends DatapackBuiltinEntriesProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.STRUCTURE, NpcStructures::bootstrap)
            .add(Registries.STRUCTURE_SET, NpcStructureSets::bootstrap)
            .add(Registries.TEMPLATE_POOL, NpcStructurePools::bootstrap)
            .add(NpcRegistries.NPC_DEFINITIONS, MKNpcDefinitions::bootstrap);


    public NpcRegistrySets(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Collections.singleton(MKNpc.MODID));
    }
}
