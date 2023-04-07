package com.chaosbuffalo.mknpc.data.registries;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKSinglePoolElement;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class NpcStructurePools {
    public static final ResourceKey<StructureTemplatePool> DIGGER_CAMP_POOL = createKey("digger/diggercamp");
    public static final ResourceKey<StructureTemplatePool> DIGGER_BASE_POOL = createKey("digger/diggerbase");
    public static final ResourceKey<StructureTemplatePool> DIGGER_ROAD_POOL = createKey("digger/diggerroad");

    private static final ResourceLocation DIGGER_TENT_DBL_1 = new ResourceLocation(MKNpc.MODID, "digger/diggertentdbl1");
    private static final ResourceLocation DIGGER_TENT_SGL_1 = new ResourceLocation(MKNpc.MODID, "digger/diggertentsgl1");
    private static final ResourceLocation DIGGER_BIG_1 = new ResourceLocation(MKNpc.MODID, "digger/diggerbig1");
    private static final ResourceLocation DIGGER_ROAD_1 = new ResourceLocation(MKNpc.MODID, "digger/diggerrd1");

    public static ResourceKey<StructureTemplatePool> createKey(String pName) {
        return ResourceKey.create(Registries.TEMPLATE_POOL, new ResourceLocation(MKNpc.MODID, pName));
    }

    public static void register(BootstapContext<StructureTemplatePool> pContext, String pName, StructureTemplatePool pValue) {
        pContext.register(createKey(pName), pValue);
    }

    public static void bootstrap(BootstapContext<StructureTemplatePool> pContext) {
        HolderGetter<StructureTemplatePool> holderGetter = pContext.lookup(Registries.TEMPLATE_POOL);
        Holder<StructureTemplatePool> empty = holderGetter.getOrThrow(Pools.EMPTY);
        pContext.register(DIGGER_CAMP_POOL, new StructureTemplatePool(empty,
                ImmutableList.of(
                        Pair.of(MKSinglePoolElement.getMKSingleJigsaw(DIGGER_TENT_DBL_1, false), 1),
                        Pair.of(MKSinglePoolElement.getMKSingleJigsaw(DIGGER_TENT_SGL_1, false), 1),
                        Pair.of(StructurePoolElement.empty(), 2)
                ),
                StructureTemplatePool.Projection.RIGID));

        pContext.register(DIGGER_BASE_POOL, new StructureTemplatePool(empty,
                ImmutableList.of(
                        Pair.of(MKSinglePoolElement.getMKSingleJigsaw(DIGGER_BIG_1, false), 1)
                ),
                StructureTemplatePool.Projection.RIGID));

        pContext.register(DIGGER_ROAD_POOL, new StructureTemplatePool(empty,
                ImmutableList.of(
                        Pair.of(MKSinglePoolElement.getMKSingleJigsaw(DIGGER_ROAD_1, false), 1)
                ),
                StructureTemplatePool.Projection.RIGID));
    }
}
