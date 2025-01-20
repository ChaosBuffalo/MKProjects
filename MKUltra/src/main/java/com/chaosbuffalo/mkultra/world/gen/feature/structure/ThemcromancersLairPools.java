package com.chaosbuffalo.mkultra.world.gen.feature.structure;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKSinglePoolElement;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.data.registries.UltraStructurePools;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class ThemcromancersLairPools {

    public static ResourceKey<StructureTemplatePool> ENTRYWAY_ROAD_POOL =
            UltraStructurePools.createKey("themcromancers_lair/entryway_road");
    public static ResourceKey<StructureTemplatePool> GATE_POOL =
            UltraStructurePools.createKey("themcromancers_lair/gates");
    public static ResourceKey<StructureTemplatePool> COMPOUND_POOL =
            UltraStructurePools.createKey("themcromancers_lair/compound");
    public static ResourceKey<StructureTemplatePool> ANNEX_ROAD_POOL =
            UltraStructurePools.createKey("themcromancers_lair/annex_road");
    public static ResourceKey<StructureTemplatePool> LIGHTS_POOL =
            UltraStructurePools.createKey("themcromancers_lair/lights");
    public static ResourceKey<StructureTemplatePool> ANNEX_POOL =
            UltraStructurePools.createKey("themcromancers_lair/annex");


    private static class Templates {
        private static final ResourceLocation BASE_1 = MKUltra.id("themcromancers_lair/base_1");
        private static final ResourceLocation ENTRY_PATH_1 = MKUltra.id("themcromancers_lair/entry_path_1");
        private static final ResourceLocation GATE_1 = MKUltra.id("themcromancers_lair/gate_1");
        private static final ResourceLocation LIGHT_PILLAR_1 = MKUltra.id("themcromancers_lair/light_pillar_1");
        private static final ResourceLocation PATH_1 = MKUltra.id("themcromancers_lair/pathway_1");
        private static final ResourceLocation TOWER_1 = MKUltra.id("themcromancers_lair/tower_1");

    }

    public static void register(BootstrapContext<StructureTemplatePool> pContext, String pName, StructureTemplatePool pValue) {
        pContext.register(UltraStructurePools.createKey(pName), pValue);
    }

    public static void registerPools(BootstrapContext<StructureTemplatePool> pContext) {
        HolderGetter<StructureTemplatePool> holderGetter = pContext.lookup(Registries.TEMPLATE_POOL);
        Holder<StructureTemplatePool> empty = holderGetter.getOrThrow(Pools.EMPTY);

        pContext.register(ENTRYWAY_ROAD_POOL, new StructureTemplatePool(empty,
                ImmutableList.of(
                        Pair.of(MKSinglePoolElement.forTemplate(Templates.ENTRY_PATH_1, true), 1)
                ),
                StructureTemplatePool.Projection.TERRAIN_MATCHING));
        pContext.register(ANNEX_ROAD_POOL, new StructureTemplatePool(empty,
                ImmutableList.of(
                        Pair.of(MKSinglePoolElement.forTemplate(Templates.PATH_1, true), 1)
                ),
                StructureTemplatePool.Projection.TERRAIN_MATCHING));
        pContext.register(COMPOUND_POOL, new StructureTemplatePool(empty,
                ImmutableList.of(
                        Pair.of(MKSinglePoolElement.forTemplate(Templates.BASE_1, false), 1)
                ),
                StructureTemplatePool.Projection.RIGID));
        pContext.register(LIGHTS_POOL, new StructureTemplatePool(empty,
                ImmutableList.of(
                        Pair.of(MKSinglePoolElement.forTemplate(Templates.LIGHT_PILLAR_1, false), 1)
                ),
                StructureTemplatePool.Projection.RIGID));
        pContext.register(GATE_POOL, new StructureTemplatePool(empty,
                ImmutableList.of(
                        Pair.of(MKSinglePoolElement.forTemplate(Templates.GATE_1, false), 1)
                ),
                StructureTemplatePool.Projection.RIGID));
        pContext.register(ANNEX_POOL, new StructureTemplatePool(empty,
                ImmutableList.of(
                        Pair.of(MKSinglePoolElement.forTemplate(Templates.TOWER_1, false), 1)
                ),
                StructureTemplatePool.Projection.RIGID));
    }
}
