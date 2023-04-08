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

public class DesertTempleVillagePools {

    public static final ResourceKey<StructureTemplatePool> DESERT_TEMPLE_VILLAGE_BASE =
            UltraStructurePools.createKey("desert_temple_village");


    public static final int GEN_DEPTH = 6;

    private static class Templates {
        private static final ResourceLocation DESERT_TEMPLE_VILLAGE_WELL = new ResourceLocation(MKUltra.MODID, "desert_temple_village/desert_well");
        private static final ResourceLocation DESERT_TEMPLE_STREET_1 = new ResourceLocation(MKUltra.MODID, "desert_temple_village/desert_temple_road_1");
        private static final ResourceLocation DESERT_TEMPLE_SMALL = new ResourceLocation(MKUltra.MODID, "desert_temple_village/cleric_temple_small");

    }

    public static void register(BootstapContext<StructureTemplatePool> pContext, String pName, StructureTemplatePool pValue) {
        pContext.register(UltraStructurePools.createKey(pName), pValue);
    }

    public static void registerPools(BootstapContext<StructureTemplatePool> pContext) {
        HolderGetter<StructureTemplatePool> holderGetter = pContext.lookup(Registries.TEMPLATE_POOL);
        Holder<StructureTemplatePool> empty = holderGetter.getOrThrow(Pools.EMPTY);

        pContext.register(DESERT_TEMPLE_VILLAGE_BASE, new StructureTemplatePool(empty,
                ImmutableList.of(
                        Pair.of(MKSinglePoolElement.forTemplate(Templates.DESERT_TEMPLE_VILLAGE_WELL, true), 1)
                ),
                StructureTemplatePool.Projection.TERRAIN_MATCHING));
        register(pContext, "desert_temple_streets", new StructureTemplatePool(empty,
                ImmutableList.of(
                        Pair.of(MKSinglePoolElement.forTemplate(Templates.DESERT_TEMPLE_STREET_1, true), 1)
                ),
                StructureTemplatePool.Projection.TERRAIN_MATCHING));
        register(pContext, "desert_temples", new StructureTemplatePool(empty,
                ImmutableList.of(
                        Pair.of(MKSinglePoolElement.forTemplate(Templates.DESERT_TEMPLE_SMALL, false), 1)
                ),
                StructureTemplatePool.Projection.RIGID));
    }

//        public static final StructureTemplatePool DESERT_TEMPLE_VILLAGE_BASE =
//            new StructureTemplatePool(new ResourceLocation(MKUltra.MODID, "desert_temple_village"),
//                    new ResourceLocation("empty"), ImmutableList.of(
//                    Pair.of(MKSingleJigsawPiece.getMKSingleJigsaw(DESERT_TEMPLE_VILLAGE_WELL, true), 1)), StructureTemplatePool.Projection.TERRAIN_MATCHING);

//    public static final StructureTemplatePool DESERT_TEMPLE_STREETS = new StructureTemplatePool(new ResourceLocation(MKUltra.MODID, "desert_temple_streets"), new ResourceLocation("empty"),
//            ImmutableList.of(
//                    Pair.of(MKSingleJigsawPiece.getMKSingleJigsaw(DESERT_TEMPLE_STREET_1, true), 1)
//            ),
//            StructureTemplatePool.Projection.TERRAIN_MATCHING);

//    public static final StructureTemplatePool DESERT_TEMPLES = new StructureTemplatePool(new ResourceLocation(MKUltra.MODID, "desert_temples"), new ResourceLocation("empty"),
//            ImmutableList.of(
//                    Pair.of(MKSingleJigsawPiece.getMKSingleJigsaw(DESERT_TEMPLE_SMALL, false), 1)
//            ),
//            StructureTemplatePool.Projection.RIGID);


}
