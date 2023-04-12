package com.chaosbuffalo.mkultra.data.generators;

import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.data.registries.UltraStructurePools;
import com.chaosbuffalo.mkultra.data.registries.UltraStructureSets;
import com.chaosbuffalo.mkultra.data.registries.UltraStructures;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class MKURegistrySets extends DatapackBuiltinEntriesProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.STRUCTURE, UltraStructures::bootstrap)
            .add(Registries.STRUCTURE_SET, UltraStructureSets::bootstrap)
            .add(Registries.TEMPLATE_POOL, UltraStructurePools::bootstrap);

    public MKURegistrySets(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Collections.singleton(MKUltra.MODID));
    }
}
