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
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKFloorRoomProfile;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKFloorTopologySettings;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKHallwayLeadInMode;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKHorizontalExitPathKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKFloorTopologyPlanner;
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
                        .setMaxDepth(20)
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
        MKWorkspaceExportManifest manifest = manifestOpt.get();
        for (MKFloorTopologySettings settings : manifest.settings().topologyProfile().floorTopologySettings()) {
            MKFloorTopologySettings physicalSettings = physicalFloorTopologySettings(manifest, settings);
            String topologyGroupId = floorTopologyGroupId(settings.stackId(), settings.floorRole());
            String endingPool = settings.mainCapApproachEnabled() ?
                    MKFloorTopologyPlanner.mainCapApproachPoolName(topologyGroupId) :
                    MKFloorTopologyPlanner.mainCapPoolName(topologyGroupId);
            int horizontalPadding = horizontalPadding(manifest);
            int rootWidth = manifest.settings().topologyProfile()
                    .verticalStackSettingsOrDefault(settings.stackId()).width() + horizontalPadding;
            int rootLength = manifest.settings().topologyProfile()
                    .verticalStackSettingsOrDefault(settings.stackId()).length() + horizontalPadding;
            rules.add(new MKDungeonTopologyGroupRule(
                    topologyGroupId,
                    physicalSettings.minMainPathPieces(),
                    physicalSettings.maxMainPathPieces(),
                    physicalSettings.maxBranchPiecesBeforeCap(),
                    physicalSettings.sprawl(),
                    physicalSettings.linksEnabled(),
                    physicalSettings.linkDensity(),
                    physicalSettings.maxLinksPerFloor(),
                    physicalSettings.maxLinksPerRoom(),
                    physicalSettings.maxLinkLength(),
                    physicalSettings.lockedLayoutSeed(),
                    Optional.of(physicalSettings),
                    rootWidth,
                    rootLength,
                    true,
                    ResourceLocation.fromNamespaceAndPath(manifest.namespace(), manifest.structureName() + "/" + endingPool)
            ));
        }
        return List.copyOf(rules);
    }

    private static MKFloorTopologySettings physicalFloorTopologySettings(
            MKWorkspaceExportManifest manifest,
            MKFloorTopologySettings settings) {
        int horizontalPadding = horizontalPadding(manifest);
        HallwayFootprint mainHallway = hallwayFootprint(manifest, settings, true, horizontalPadding);
        HallwayFootprint branchHallway = hallwayFootprint(manifest, settings, false, horizontalPadding);
        return settings.withRoomProfiles(
                        physicalRoomProfiles(manifest, settings, settings.mainRoomProfiles(), horizontalPadding),
                        physicalRoomProfiles(manifest, settings, settings.branchRoomProfiles(), horizontalPadding),
                        physicalRoomProfiles(manifest, settings, settings.branchCapProfiles(), horizontalPadding),
                        physicalRoomProfiles(manifest, settings, settings.mainCapApproachProfiles(), horizontalPadding),
                        physicalRoomProfiles(manifest, settings, settings.mainCapProfiles(), horizontalPadding))
                .withLayoutHallwayFootprints(mainHallway.length(), mainHallway.width(),
                        branchHallway.length(), branchHallway.width());
    }

    private static int horizontalPadding(MKWorkspaceExportManifest manifest) {
        return 2 * (manifest.settings().shellMargin() + manifest.settings().exteriorAirMargin());
    }

    private static HallwayFootprint hallwayFootprint(MKWorkspaceExportManifest manifest,
                                                     MKFloorTopologySettings settings,
                                                     boolean mainPath,
                                                     int horizontalPadding) {
        Optional<HallwayFootprint> exportedFootprint = exportedHallwayFootprint(manifest, settings, mainPath);
        if (exportedFootprint.isPresent()) {
            return exportedFootprint.orElseThrow();
        }
        String openingProfileId = floorOpeningProfileId(manifest, settings, mainPath)
                .orElseGet(() -> firstOpeningProfileId(manifest, mainPath).orElse(""));
        Optional<MKWorkspaceExportManifest.ExportLinearRunFamily> linearRun = manifest.settings().linearRunFamilies()
                .stream()
                .filter(candidate -> isFloorTopologyLinearRun(candidate, mainPath))
                .filter(candidate -> openingProfileId.isBlank() ||
                        candidate.openingProfileId().equals(openingProfileId))
                .findFirst();
        if (linearRun.isPresent()) {
            MKWorkspaceExportManifest.ExportLinearRunFamily run = linearRun.orElseThrow();
            return new HallwayFootprint(
                    run.length() + horizontalPadding,
                    Math.max(1, run.interiorWidth() + horizontalPadding)
            );
        }
        int leadIn = settings.hallwayLeadInMode() == MKHallwayLeadInMode.MANUAL ?
                Math.max(1, settings.manualHallwayLeadInPieces()) :
                Math.max(1, Math.ceilDiv(Math.max(
                        manifest.settings().topologyProfile().verticalStackSettingsOrDefault(settings.stackId()).width(),
                        manifest.settings().topologyProfile().verticalStackSettingsOrDefault(settings.stackId()).length()), 8));
        int openingWidth = openingProfile(manifest, openingProfileId)
                .map(MKWorkspaceExportManifest.ExportOpeningProfile::openingWidth)
                .orElse(3);
        return new HallwayFootprint(leadIn + horizontalPadding, openingWidth + horizontalPadding);
    }

    private static Optional<HallwayFootprint> exportedHallwayFootprint(MKWorkspaceExportManifest manifest,
                                                                       MKFloorTopologySettings settings,
                                                                       boolean mainPath) {
        String pathKind = mainPath ? "main" : "branch";
        return manifest.pieces().stream()
                .filter(piece -> "floor_plan_linear_run".equals(piece.tags().get("tower_piece_kind")))
                .filter(piece -> settings.stackId().equals(piece.tags().get("workspace_floor_topology_stack_id")))
                .filter(piece -> settings.floorRole().equals(piece.tags().get("workspace_floor_topology_floor_role")))
                .filter(piece -> pathKind.equals(piece.tags().get("workspace_linear_run_path_kind")))
                .filter(NpcStructures::baseTemplatePiece)
                .map(piece -> new HallwayFootprint(
                        piece.placement().exportBounds().sizeX(),
                        piece.placement().exportBounds().sizeZ()))
                .findFirst();
    }

    private static List<MKFloorRoomProfile> physicalRoomProfiles(
            MKWorkspaceExportManifest manifest,
            MKFloorTopologySettings settings,
            List<MKFloorRoomProfile> profiles,
            int horizontalPadding) {
        return profiles.stream()
                .map(profile -> physicalRoomProfile(manifest, settings, profile, horizontalPadding))
                .toList();
    }

    private static MKFloorRoomProfile physicalRoomProfile(MKWorkspaceExportManifest manifest,
                                                                   MKFloorTopologySettings settings,
                                                                   MKFloorRoomProfile profile,
                                                                   int horizontalPadding) {
        return exportedRoomFootprint(manifest, settings, profile)
                .map(footprint -> profile.withWidth(footprint.width()).withLength(footprint.length()))
                .orElseGet(() -> profile.withWidth(profile.width() + horizontalPadding)
                        .withLength(profile.length() + horizontalPadding));
    }

    private static Optional<RoomFootprint> exportedRoomFootprint(MKWorkspaceExportManifest manifest,
                                                                 MKFloorTopologySettings settings,
                                                                 MKFloorRoomProfile profile) {
        return manifest.pieces().stream()
                .filter(piece -> "floor_plan_room".equals(piece.tags().get("tower_piece_kind")))
                .filter(piece -> settings.stackId().equals(piece.tags().get("workspace_floor_topology_stack_id")))
                .filter(piece -> settings.floorRole().equals(piece.tags().get("workspace_floor_topology_floor_role")))
                .filter(piece -> profile.id().equals(piece.tags().get("workspace_floor_room_profile_id")))
                .filter(piece -> profile.kind().getSerializedName()
                        .equals(piece.tags().get("workspace_floor_room_kind")))
                .filter(NpcStructures::baseTemplatePiece)
                .map(piece -> new RoomFootprint(
                        piece.placement().exportBounds().sizeX(),
                        piece.placement().exportBounds().sizeZ()))
                .findFirst();
    }

    private static boolean baseTemplatePiece(MKWorkspaceExportManifest.ExportPiece piece) {
        return "template".equals(piece.workspacePieceKind()) && piece.pieceName().endsWith("_template");
    }

    private static boolean isFloorTopologyLinearRun(MKWorkspaceExportManifest.ExportLinearRunFamily linearRun,
                                                    boolean mainPath) {
        if (mainPath && !linearRun.allowOnMainPath()) {
            return false;
        }
        if (!mainPath && !linearRun.allowOnBranchPath()) {
            return false;
        }
        return !linearRun.topologySlotId().startsWith("keep.");
    }

    private static Optional<String> floorOpeningProfileId(MKWorkspaceExportManifest manifest,
                                                          MKFloorTopologySettings settings,
                                                          boolean mainPath) {
        String topologySlotId = settings.stackId() + "." + settings.floorRole();
        MKHorizontalExitPathKind pathKind = mainPath ?
                MKHorizontalExitPathKind.MAIN_EXIT :
                MKHorizontalExitPathKind.BRANCH;
        return manifest.settings().familyDefinitions().stream()
                .filter(family -> family.topologySlotId().equals(topologySlotId))
                .flatMap(family -> family.horizontalExits().stream())
                .filter(exit -> exit.pathKind() == pathKind)
                .map(MKWorkspaceExportManifest.ExportFamilyHorizontalExit::openingProfileId)
                .filter(id -> !id.isBlank())
                .findFirst();
    }

    private static Optional<String> firstOpeningProfileId(MKWorkspaceExportManifest manifest, boolean mainPath) {
        return manifest.settings().openingProfiles().stream()
                .filter(profile -> mainPath ? profile.allowOnMainPath() : profile.allowOnBranchPath())
                .map(MKWorkspaceExportManifest.ExportOpeningProfile::profileId)
                .findFirst();
    }

    private static Optional<MKWorkspaceExportManifest.ExportOpeningProfile> openingProfile(
            MKWorkspaceExportManifest manifest,
            String profileId) {
        return manifest.settings().openingProfiles().stream()
                .filter(profile -> profile.profileId().equals(profileId))
                .findFirst();
    }

    private record HallwayFootprint(int length, int width) {
    }

    private record RoomFootprint(int width, int length) {
    }

    private static int maxFloorBranchDepth(List<MKDungeonTopologyGroupRule> rules, int fallback) {
        return Math.max(fallback, rules.stream()
                .mapToInt(MKDungeonTopologyGroupRule::maxBranchPiecesBeforeCap)
                .max()
                .orElse(0));
    }

    private static String floorTopologyGroupId(String stackId, String floorRole) {
        return stackId + "." + floorRole;
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
