package com.chaosbuffalo.mknpc.data.registries;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.data.MKJigsawBuilder;
import com.chaosbuffalo.mknpc.data.NpcTags;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKDungeonConnectorSettings;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKDungeonLayoutSettings;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKVerticalProgressionMode;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.util.Map;

public class NpcStructures {
    public static final ResourceKey<Structure> TEST_JIGSAW = createKey("test_jigsaw");
    public static final ResourceKey<Structure> TEST_TOWER = createKey("test_tower");

    private static ResourceKey<Structure> createKey(String name) {
        return ResourceKey.create(Registries.STRUCTURE, MKNpc.id(name));
    }

    public static void bootstrap(BootstrapContext<Structure> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<StructureTemplatePool> templates = context.lookup(Registries.TEMPLATE_POOL);

        context.register(TEST_JIGSAW,
                new MKJigsawBuilder(
                        emptySpawnsStructure(biomes.getOrThrow(NpcTags.Biomes.HAS_TEST_STRUCTURES),
                                GenerationStep.Decoration.SURFACE_STRUCTURES, TerrainAdjustment.NONE),
                        templates.getOrThrow(NpcStructurePools.DIGGER_BASE_POOL)).build());

        context.register(TEST_TOWER,
                new MKJigsawBuilder(
                        emptySpawnsStructure(biomes.getOrThrow(NpcTags.Biomes.HAS_TEST_TOWER),
                                GenerationStep.Decoration.SURFACE_STRUCTURES, TerrainAdjustment.BEARD_THIN),
                        templates.getOrThrow(NpcStructurePools.TEST_TOWER_START_POOL))
                        .setMaxDepth(12)
                        .setMaxDistFromCenter(96)
                        .setDungeonLayout(new MKDungeonLayoutSettings(
                                3,
                                3,
                                1,
                                2,
                                0,
                                false,
                                MKVerticalProgressionMode.MIXED,
                                new MKDungeonConnectorSettings(
                                        connector("main_forward"),
                                        connector("main_back"),
                                        connector("branch"),
                                        connector("connect_down"),
                                        connector("connect_up"),
                                        connector("boss_forward"),
                                        connector("boss_back")
                                )
                        ))
                        .build());
    }

    private static ResourceLocation connector(String name) {
        return MKNpc.id(name);
    }

    public static Structure.StructureSettings structure(HolderSet<Biome> biomes, GenerationStep.Decoration step, TerrainAdjustment adjustment) {
        return structure(biomes, Map.of(), step, adjustment);
    }

    public static Structure.StructureSettings emptySpawnsStructure(HolderSet<Biome> biomes, GenerationStep.Decoration step, TerrainAdjustment adjustment) {
        return structure(biomes,
                Map.of(
                        MobCategory.MONSTER, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.PIECE,
                                WeightedRandomList.create())
                ),
                step, adjustment);
    }

    public static Structure.StructureSettings structure(HolderSet<Biome> biomes, Map<MobCategory, StructureSpawnOverride> mobs, GenerationStep.Decoration step, TerrainAdjustment adjustment) {
        return new Structure.StructureSettings(biomes, mobs, step, adjustment);
    }
}
