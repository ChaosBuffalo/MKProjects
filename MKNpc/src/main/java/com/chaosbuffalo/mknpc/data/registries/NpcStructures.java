package com.chaosbuffalo.mknpc.data.registries;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.data.MKJigsawBuilder;
import com.chaosbuffalo.mknpc.data.NpcTags;
import com.chaosbuffalo.mknpc.data.providers.MKWorkspaceExportManifestLoader;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKDungeonConnectorSettings;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKDungeonLayoutSettings;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKDungeonTopologyGroupRule;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKVerticalProgressionMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.export.MKWorkspaceExportManifest;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorTopologySettings;
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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class NpcStructures {
    private static final Path MODULE_ROOT =
            MKWorkspaceExportManifestLoader.resolveModuleRoot(MKNpc.MODULE_DIRECTORY_NAME);
    public static final ResourceKey<Structure> TEST_JIGSAW = createKey("test_jigsaw");
    public static final ResourceKey<Structure> TEST_TOWER = createKey("test_tower");
    public static final ResourceKey<Structure> TEST_KEEP = createKey("test_keep");

    private static ResourceKey<Structure> createKey(String name) {
        return ResourceKey.create(Registries.STRUCTURE, MKNpc.id(name));
    }

    public static void bootstrap(BootstrapContext<Structure> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<StructureTemplatePool> templates = context.lookup(Registries.TEMPLATE_POOL);
        Optional<MKWorkspaceExportManifest> testTowerManifest = workspaceManifest("test_tower");
        Optional<MKWorkspaceExportManifest> testKeepManifest = workspaceManifest("test_keep");
        List<MKDungeonTopologyGroupRule> testTowerFloorRules = floorTopologyRules(testTowerManifest);
        List<MKDungeonTopologyGroupRule> testKeepFloorRules = floorTopologyRules(testKeepManifest);

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
                                maxFloorBranchDepth(testTowerFloorRules, 0),
                                false,
                                MKVerticalProgressionMode.MIXED,
                                true,
                                false,
                                testTowerFloorRules,
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

        context.register(TEST_KEEP,
                new MKJigsawBuilder(
                        emptySpawnsStructure(biomes.getOrThrow(NpcTags.Biomes.HAS_TEST_KEEP),
                                GenerationStep.Decoration.SURFACE_STRUCTURES, TerrainAdjustment.BEARD_THIN),
                        templates.getOrThrow(NpcStructurePools.TEST_KEEP_START_POOL))
                        .setMaxDepth(18)
                        .setMaxDistFromCenter(116)
                        .setDungeonLayout(new MKDungeonLayoutSettings(
                                1,
                                1,
                                1,
                                64,
                                64,
                                true,
                                MKVerticalProgressionMode.MIXED,
                                true,
                                false,
                                testKeepFloorRules,
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

    private static Optional<MKWorkspaceExportManifest> workspaceManifest(String structureName) {
        return MKWorkspaceExportManifestLoader.loadFromModSource(MODULE_ROOT, MKNpc.id(structureName))
                .map(MKWorkspaceExportManifestLoader.LoadedManifest::manifest);
    }

    private static List<MKDungeonTopologyGroupRule> floorTopologyRules(Optional<MKWorkspaceExportManifest> manifestOpt) {
        if (manifestOpt.isEmpty()) {
            return List.of();
        }
        ArrayList<MKDungeonTopologyGroupRule> rules = new ArrayList<>();
        for (MKWorkspaceFloorTopologySettings settings : manifestOpt.get().settings().topologyProfile().floorTopologySettings()) {
            rules.add(new MKDungeonTopologyGroupRule(
                    floorTopologyGroupId(settings.stackId(), settings.floorRole()),
                    settings.minMainPathPieces(),
                    settings.maxMainPathPieces(),
                    settings.maxBranchPiecesBeforeCap(),
                    true,
                    null
            ));
        }
        return List.copyOf(rules);
    }

    private static int maxFloorBranchDepth(List<MKDungeonTopologyGroupRule> rules, int fallback) {
        return Math.max(fallback, rules.stream()
                .mapToInt(MKDungeonTopologyGroupRule::maxBranchPiecesBeforeCap)
                .max()
                .orElse(0));
    }

    private static String floorTopologyGroupId(String stackId, String floorRole) {
        return "floor/" + safeId(stackId) + "/" + safeId(floorRole);
    }

    private static String safeId(String value) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            builder.append(Character.isLetterOrDigit(c) ? c : '_');
        }
        return builder.toString();
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
