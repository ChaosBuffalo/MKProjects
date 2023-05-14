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

public class DecayingChurchPools {

    private static final ResourceLocation BASE_1 = new ResourceLocation(MKUltra.MODID, "decaying_church/base_1");
    private static final ResourceLocation HALLWAY_1 = new ResourceLocation(MKUltra.MODID, "decaying_church/hallway_1");
    private static final ResourceLocation HALLWAY_2 = new ResourceLocation(MKUltra.MODID, "decaying_church/hallway_2");
    private static final ResourceLocation LANDING_1 = new ResourceLocation(MKUltra.MODID, "decaying_church/landing_1");
    private static final ResourceLocation ROOM_CELLS = new ResourceLocation(MKUltra.MODID, "decaying_church/room_cells");
    private static final ResourceLocation ROOM_LAVA = new ResourceLocation(MKUltra.MODID, "decaying_church/room_lava");
    private static final ResourceLocation STAIRS_DOWN_1 = new ResourceLocation(MKUltra.MODID, "decaying_church/stairs_down_1");

    public static ResourceKey<StructureTemplatePool> BASE =
            UltraStructurePools.createKey("decaying_church/base");
    public static ResourceKey<StructureTemplatePool> HALLWAYS =
            UltraStructurePools.createKey("decaying_church/hallways");
    public static ResourceKey<StructureTemplatePool> ROOMS =
            UltraStructurePools.createKey("decaying_church/rooms");
    public static ResourceKey<StructureTemplatePool> STAIRS_DOWN =
            UltraStructurePools.createKey("decaying_church/stairs_down");
    public static ResourceKey<StructureTemplatePool> STAIRS_LANDING =
            UltraStructurePools.createKey("decaying_church/stairs_landing");

    public static void registerPools(BootstapContext<StructureTemplatePool> pContext) {
        HolderGetter<StructureTemplatePool> holderGetter = pContext.lookup(Registries.TEMPLATE_POOL);
        Holder<StructureTemplatePool> empty = holderGetter.getOrThrow(Pools.EMPTY);
        pContext.register(BASE, new StructureTemplatePool(empty,
                ImmutableList.of(
                        Pair.of(MKSinglePoolElement.forTemplate(BASE_1, false), 1)
                ),
                StructureTemplatePool.Projection.RIGID));
        pContext.register(HALLWAYS, new StructureTemplatePool(empty,
                ImmutableList.of(
                        Pair.of(MKSinglePoolElement.forTemplate(HALLWAY_1, false), 1),
                        Pair.of(MKSinglePoolElement.forTemplate(HALLWAY_2, false), 1)
                ),
                StructureTemplatePool.Projection.RIGID));

        pContext.register(ROOMS, new StructureTemplatePool(empty,
                ImmutableList.of(
                        Pair.of(MKSinglePoolElement.forTemplate(ROOM_CELLS, false), 1),
                        Pair.of(MKSinglePoolElement.forTemplate(ROOM_LAVA, false), 1)
                ),
                StructureTemplatePool.Projection.RIGID));
        pContext.register(STAIRS_DOWN, new StructureTemplatePool(empty,
                ImmutableList.of(
                        Pair.of(MKSinglePoolElement.forTemplate(STAIRS_DOWN_1, false), 1)
                ),
                StructureTemplatePool.Projection.RIGID));
        pContext.register(STAIRS_LANDING, new StructureTemplatePool(empty,
                ImmutableList.of(
                        Pair.of(MKSinglePoolElement.forTemplate(LANDING_1, false), 1)
                ),
                StructureTemplatePool.Projection.RIGID));
    }

}
