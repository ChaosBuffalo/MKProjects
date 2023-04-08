package com.chaosbuffalo.mkultra.world.gen.feature.structure;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKSinglePoolElement;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.data.registries.UltraStructurePools;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class NecrotideAlterPools {

    public static ResourceKey<StructureTemplatePool> BASE =
            UltraStructurePools.createKey("necrotide_alter/base");

    private static final ResourceLocation BASE_NAME = new ResourceLocation(MKUltra.MODID, "necrotide_alter/base");
    private static final ResourceLocation TOWER_LEFT = new ResourceLocation(MKUltra.MODID, "necrotide_alter/tower_left");
    private static final ResourceLocation TOWER_RIGHT = new ResourceLocation(MKUltra.MODID, "necrotide_alter/tower_right");

    public static final int GEN_DEPTH = 7;

    public static void register(BootstapContext<StructureTemplatePool> pContext, String pName, StructureTemplatePool pValue) {
        pContext.register(UltraStructurePools.createKey(pName), pValue);
    }

    public static void registerPools(BootstapContext<StructureTemplatePool> pContext) {
        HolderGetter<StructureTemplatePool> holderGetter = pContext.lookup(Registries.TEMPLATE_POOL);
        Holder<StructureTemplatePool> empty = holderGetter.getOrThrow(Pools.EMPTY);

        pContext.register(BASE, new StructureTemplatePool(empty,
                ImmutableList.of(
                        Pair.of(MKSinglePoolElement.forTemplate(BASE_NAME, false), 1)
                ),
                StructureTemplatePool.Projection.TERRAIN_MATCHING));
        register(pContext, "necrotide_alter/towers", new StructureTemplatePool(empty,
                ImmutableList.of(
                        Pair.of(MKSinglePoolElement.forTemplate(TOWER_LEFT, false), 1),
                        Pair.of(MKSinglePoolElement.forTemplate(TOWER_RIGHT, false), 1)
                ),
                StructureTemplatePool.Projection.TERRAIN_MATCHING));
    }

//    public static final StructureTemplatePool BASE = new StructureTemplatePool(
//            new ResourceLocation(MKUltra.MODID, "necrotide_alter/base"),
//            new ResourceLocation("empty"),
//            ImmutableList.of(
//                    Pair.of(MKSingleJigsawPiece.getMKSingleJigsaw(BASE_NAME, false), 1)),
//            StructureTemplatePool.Projection.TERRAIN_MATCHING);
//
//    public static final StructureTemplatePool TOWERS = new StructureTemplatePool(
//            new ResourceLocation(MKUltra.MODID, "necrotide_alter/towers"),
//            new ResourceLocation("empty"),
//            ImmutableList.of(
//                    Pair.of(MKSingleJigsawPiece.getMKSingleJigsaw(TOWER_LEFT, false), 1),
//                    Pair.of(MKSingleJigsawPiece.getMKSingleJigsaw(TOWER_RIGHT, false), 1)
//            ),
//            StructureTemplatePool.Projection.RIGID);

}
