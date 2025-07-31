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

public class FireShrinePools {
    public static ResourceKey<StructureTemplatePool> PILLARS =
            UltraStructurePools.createKey("fire_shrine/pillars");
    public static ResourceKey<StructureTemplatePool> CORNERS_WEST =
            UltraStructurePools.createKey("fire_shrine/corners_west");
    public static ResourceKey<StructureTemplatePool> CORNERS_EAST =
            UltraStructurePools.createKey("fire_shrine/corners_east");
    public static ResourceKey<StructureTemplatePool> PLATFORM_CONTENTS =
            UltraStructurePools.createKey("fire_shrine/platform_contents");
    public static ResourceKey<StructureTemplatePool> PLATFORMS =
            UltraStructurePools.createKey("fire_shrine/platforms");
    public static ResourceKey<StructureTemplatePool> CENTER =
            UltraStructurePools.createKey("fire_shrine/centers");
    public static ResourceKey<StructureTemplatePool> TOWERS =
            UltraStructurePools.createKey("fire_shrine/towers");

    private static class Templates {
        private static final ResourceLocation CENTER_1 = MKUltra.id("fire_shrine/center_1");
        private static final ResourceLocation CORNER_WEST = MKUltra.id("fire_shrine/corner_west");
        private static final ResourceLocation CORNER_EAST = MKUltra.id("fire_shrine/corner_east");
        private static final ResourceLocation GAZEBO = MKUltra.id("fire_shrine/gazebo");
        private static final ResourceLocation LAVA_FOUNTAIN = MKUltra.id("fire_shrine/lava_fountain");
        private static final ResourceLocation PILLAR_1 = MKUltra.id("fire_shrine/pillar_1");
        private static final ResourceLocation PLATFORM_1 = MKUltra.id("fire_shrine/platform_1");
        private static final ResourceLocation TOWER_1 = MKUltra.id("fire_shrine/tower_1");
    }

    public static void registerPools(BootstrapContext<StructureTemplatePool> pContext) {
        HolderGetter<StructureTemplatePool> holderGetter = pContext.lookup(Registries.TEMPLATE_POOL);
        Holder<StructureTemplatePool> empty = holderGetter.getOrThrow(Pools.EMPTY);

        pContext.register(PILLARS, new StructureTemplatePool(empty,
                ImmutableList.of(
                        Pair.of(MKSinglePoolElement.forTemplate(Templates.PILLAR_1, false), 1)
                ),
                StructureTemplatePool.Projection.RIGID));
        pContext.register(TOWERS, new StructureTemplatePool(empty,
                ImmutableList.of(
                        Pair.of(MKSinglePoolElement.forTemplate(Templates.TOWER_1, false), 1)
                ),
                StructureTemplatePool.Projection.RIGID));
        pContext.register(CENTER, new StructureTemplatePool(empty,
                ImmutableList.of(
                        Pair.of(MKSinglePoolElement.forTemplate(Templates.CENTER_1, false), 1)
                ),
                StructureTemplatePool.Projection.RIGID));
        pContext.register(CORNERS_WEST, new StructureTemplatePool(empty,
                ImmutableList.of(
                        Pair.of(MKSinglePoolElement.forTemplate(Templates.CORNER_WEST, true), 1)
                ),
                StructureTemplatePool.Projection.TERRAIN_MATCHING));
        pContext.register(CORNERS_EAST, new StructureTemplatePool(empty,
                ImmutableList.of(
                        Pair.of(MKSinglePoolElement.forTemplate(Templates.CORNER_EAST, true), 1)
                ),
                StructureTemplatePool.Projection.TERRAIN_MATCHING));
        pContext.register(PLATFORM_CONTENTS, new StructureTemplatePool(empty,
                ImmutableList.of(
                        Pair.of(MKSinglePoolElement.forTemplate(Templates.GAZEBO, false), 1),
                        Pair.of(MKSinglePoolElement.forTemplate(Templates.LAVA_FOUNTAIN, false), 1)
                ),
                StructureTemplatePool.Projection.RIGID));
        pContext.register(PLATFORMS, new StructureTemplatePool(empty,
                ImmutableList.of(
                        Pair.of(MKSinglePoolElement.forTemplate(Templates.PLATFORM_1, false), 1)
                ),
                StructureTemplatePool.Projection.RIGID));

    }
}