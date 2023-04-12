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

public class IntroCastlePools {

    public static final ResourceKey<StructureTemplatePool> ISLAND_POOL = UltraStructurePools.createKey("intro_castle.green_knight_island");
    public static final ResourceKey<StructureTemplatePool> INTRO_CASTLE_TOP = UltraStructurePools.createKey("intro_castle.castle_top");
    public static final ResourceKey<StructureTemplatePool> INTRO_CASTLE_BASE = UltraStructurePools.createKey("intro_castle.castle_base");

    private static class Templates {
        private static final ResourceLocation GREEN_KNIGHT_ISLAND = template("intro_castle/green_knight_island");
        private static final ResourceLocation CASTLE_TOP = template("intro_castle/castle_top");
        private static final ResourceLocation CASTLE_BASE = template("intro_castle/castle_base");

        private static ResourceLocation template(String name) {
            return new ResourceLocation(MKUltra.MODID, name);
        }
    }


    public static void registerPools(BootstapContext<StructureTemplatePool> pContext) {
        HolderGetter<StructureTemplatePool> holderGetter = pContext.lookup(Registries.TEMPLATE_POOL);
        Holder<StructureTemplatePool> empty = holderGetter.getOrThrow(Pools.EMPTY);


        pContext.register(ISLAND_POOL, new StructureTemplatePool(empty,
                ImmutableList.of(
                        Pair.of(MKSinglePoolElement.forTemplate(Templates.GREEN_KNIGHT_ISLAND, false), 1)
                ),
                StructureTemplatePool.Projection.RIGID));
        pContext.register(INTRO_CASTLE_TOP, new StructureTemplatePool(empty,
                ImmutableList.of(
                        Pair.of(MKSinglePoolElement.forTemplate(Templates.CASTLE_TOP, false), 1)
                ),
                StructureTemplatePool.Projection.RIGID));
        pContext.register(INTRO_CASTLE_BASE, new StructureTemplatePool(empty,
                ImmutableList.of(
                        Pair.of(MKSinglePoolElement.forTemplate(Templates.CASTLE_BASE, false), 1)
                ),
                StructureTemplatePool.Projection.RIGID));
    }
}
