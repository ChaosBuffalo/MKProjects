package com.chaosbuffalo.mkultra.data.generators;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkfaction.faction.MKFactionRegistry;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.data.registries.UltraStructurePools;
import com.chaosbuffalo.mkultra.data.registries.UltraStructureSets;
import com.chaosbuffalo.mkultra.data.registries.UltraStructures;
import com.chaosbuffalo.mkultra.init.MKUFactions;
import com.chaosbuffalo.mkultra.init.MKULootTiers;
import com.chaosbuffalo.mkultra.init.MKUTalentDisplayNodes;
import com.chaosbuffalo.mkultra.init.MKUTalentTrees;
import com.chaosbuffalo.mkweapons.MKWeaponsRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class MKURegistrySets extends DatapackBuiltinEntriesProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.STRUCTURE, UltraStructures::bootstrap)
            .add(Registries.STRUCTURE_SET, UltraStructureSets::bootstrap)
            .add(Registries.TEMPLATE_POOL, UltraStructurePools::bootstrap)
            .add(MKFactionRegistry.FACTION_REGISTRY_KEY, MKUFactions::bootstrap)
            .add(MKCoreRegistry.TALENT_NODE_DISPLAY_REGISTRY_KEY, MKUTalentDisplayNodes::bootstrap)
            .add(MKCoreRegistry.TALENT_TREE_REGISTRY_KEY, MKUTalentTrees::bootstrap)
            .add(MKWeaponsRegistry.LOOT_TIER_REGISTRY_KEY, MKULootTiers::bootstrap);

    public MKURegistrySets(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Collections.singleton(MKUltra.MODID));
    }
}
