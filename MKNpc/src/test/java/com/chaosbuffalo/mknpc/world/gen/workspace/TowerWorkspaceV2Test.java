package com.chaosbuffalo.mknpc.world.gen.workspace;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawPieceRole;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKDungeonTopologyGroupRule;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKDungeonConnectorSettings;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKDungeonLayoutController;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKDungeonLayoutSettings;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKDungeonPieceState;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawPieceMetadata;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKVerticalProgressionMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.export.MKWorkspaceExportManifest;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKHorizontalOpeningProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceStackSlot;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerStackBudget;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKVerticalAccessPlacement;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationPolicy;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitConnectionMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExtrusionMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitPathKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunPieceShape;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunProjection;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteOverride;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKResolvedVerticalAccessProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRuntimePieceInfo;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairRiseType;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTemplateReuseTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyPathSettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologySlotMetadata;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTowerStackFloorCounts;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTowerStackSettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessSpec;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVoidMarginTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKPlannedConnector;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKPlannedPiece;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKTowerStackDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKTowerStackPlanner;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKTowerWorkspacePlanner;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKWalledKeepWorkspacePlanner;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TowerWorkspaceV2Test {

    @Test
    void defaultStairAuthoringUsesMixedRiseStrategy() {
        assertEquals(MKWorkspaceStairRiseType.MIXED, MKWorkspaceStairAuthoringConfig.defaultConfig().riseType());
    }

    @Test
    void reusableTowerStackPlannerMatchesTowerRoomPieces() {
        MKStructureWorkspace workspace = baseWorkspace(
                List.of(
                        new MKHorizontalOpeningProfile("entry_main", 3, 3, true, false),
                        new MKHorizontalOpeningProfile("main_branch", 3, 3, false, true)
                ),
                List.of()
        );

        List<String> stackPieceNames = new MKTowerStackPlanner()
                .createRoomPieces(workspace, workspace.familyDefinitions()).stream()
                .map(MKPlannedPiece::pieceName)
                .toList();
        List<String> towerRoomPieceNames = new MKTowerWorkspacePlanner().createCanonicalPieces(workspace).stream()
                .filter(piece -> !"linear_run".equals(piece.tags().get("tower_piece_kind")))
                .map(MKPlannedPiece::pieceName)
                .toList();

        assertEquals(towerRoomPieceNames, stackPieceNames);
    }

    @Test
    void reusableTowerStackPlannerSupportsScopedConnectorPools() {
        MKStructureWorkspace workspace = baseWorkspace(
                List.of(
                        new MKHorizontalOpeningProfile("entry_main", 3, 3, true, false),
                        new MKHorizontalOpeningProfile("main_branch", 3, 3, false, true)
                ),
                List.of()
        );
        MKWorkspaceTowerStackSettings stackSettings = workspace.topologyProfile()
                .towerStackSettingsOrDefault("tower.primary");
        MKTowerStackDefinition stackDefinition = MKTowerStackDefinition.scoped(
                "keep.center", true, new MKWorkspaceTowerStackSettings(
                        "keep.center",
                        stackSettings.mainFloors(),
                        stackSettings.basementFloors(),
                        stackSettings.height(),
                        stackSettings.width(),
                        stackSettings.length(),
                        stackSettings.shaftSize(),
                        stackSettings.verticalAccessPlacement(),
                        stackSettings.stairConfig(),
                        stackSettings.topCapApproachEnabled(),
                        stackSettings.basementCapApproachEnabled(),
                        stackSettings.foundationPolicy(),
                        stackSettings.paletteOverride()));

        List<MKPlannedPiece> pieces = new MKTowerStackPlanner().createRoomPieces(
                workspace, stackDefinition, workspace.familyDefinitions());

        MKPlannedPiece entry = pieces.stream()
                .filter(piece -> piece.pieceName().equals("entry"))
                .findFirst()
                .orElseThrow();
        MKPlannedPiece floor = pieces.stream()
                .filter(piece -> piece.pieceName().equals("floor_main"))
                .findFirst()
                .orElseThrow();
        MKPlannedPiece topCapApproach = pieces.stream()
                .filter(piece -> piece.pieceName().equals("top_cap_approach"))
                .findFirst()
                .orElseThrow();

        assertEquals("keep.center", entry.tags().get("workspace_tower_stack_id"));
        assertTrue(entry.connectors().stream().anyMatch(connector ->
                connector.role() == MKConnectorRole.CONNECT_UP &&
                        "tower_stacks/keep/center/connect_up".equals(connector.targetPoolName())));
        assertTrue(floor.connectors().stream().anyMatch(connector ->
                connector.role() == MKConnectorRole.CONNECT_DOWN &&
                        "tower_stacks/keep/center/connect_up".equals(connector.incomingPoolName())));
        assertTrue(topCapApproach.connectors().stream().anyMatch(connector ->
                connector.role() == MKConnectorRole.TOP_CAP_FORWARD &&
                        "tower_stacks/keep/center/top_cap".equals(connector.targetPoolName())));
    }

    @Test
    void towerPlannerUsesPrimaryTowerStackSettingsWhenPresent() {
        MKWorkspaceTopologyProfile topologyProfile = MKWorkspaceTopologyProfile.tower()
                .withTowerStackSettings(new MKWorkspaceTowerStackSettings(
                        "tower.primary", 2, 0, 9, 11, 13,
                        3, MKVerticalAccessPlacement.CENTER, MKWorkspaceStairAuthoringConfig.defaultConfig(),
                        false, false));
        MKStructureWorkspace workspace = withTopologyAndLinearRuns(
                baseWorkspace(
                        List.of(
                                new MKHorizontalOpeningProfile("entry_main", 3, 3, true, false),
                                new MKHorizontalOpeningProfile("main_branch", 3, 3, false, true)
                        ),
                        List.of()
                ),
                topologyProfile,
                List.of(),
                List.of()
        );

        List<MKPlannedPiece> pieces = new MKTowerWorkspacePlanner().createCanonicalPieces(workspace);
        MKPlannedPiece entry = pieces.stream()
                .filter(piece -> piece.pieceName().equals("entry"))
                .findFirst()
                .orElseThrow();

        assertEquals("tower.primary", entry.tags().get("workspace_tower_stack_id"));
        assertEquals("2", entry.tags().get("workspace_tower_stack_main_floors"));
        assertEquals("0", entry.tags().get("workspace_tower_stack_basement_floors"));
        assertEquals("tower.primary.entry", entry.tags().get("workspace_topology_slot_id"));
        assertEquals(11, entry.interiorWidth());
        assertEquals(13, entry.interiorLength());
        assertEquals(9, entry.interiorHeight());
        assertFalse(pieces.stream().anyMatch(piece -> piece.pieceName().equals("top_cap_approach")));
        assertFalse(pieces.stream().anyMatch(piece -> piece.pieceName().equals("basement_cap_approach")));
    }

    @Test
    void mixedRiseStrategyResolvesHeightRejectedByStairOnly() {
        ResourceLocation stairBlock = ResourceLocation.parse("minecraft:stone_brick_stairs");
        ResourceLocation slabBlock = ResourceLocation.parse("minecraft:stone_brick_slab");
        ResourceLocation ladderBlock = ResourceLocation.parse("minecraft:ladder");
        MKWorkspaceStairAuthoringConfig stairOnly = new MKWorkspaceStairAuthoringConfig(
                MKWorkspaceStairMode.RUN_PROFILE,
                MKWorkspaceStairRiseType.STAIR,
                1,
                stairBlock,
                slabBlock,
                ladderBlock
        );
        MKWorkspaceStairAuthoringConfig mixed = new MKWorkspaceStairAuthoringConfig(
                MKWorkspaceStairMode.RUN_PROFILE,
                MKWorkspaceStairRiseType.MIXED,
                1,
                stairBlock,
                slabBlock,
                ladderBlock
        );

        assertTrue(MKResolvedVerticalAccessProfile.resolve(stairOnly, 5, 5, 6).isEmpty());
        assertTrue(MKResolvedVerticalAccessProfile.resolve(mixed, 5, 5, 6).isPresent());
    }

    @Test
    void resolvedRunProfileRequiresTwoBlockSameColumnPassClearance() {
        ResourceLocation stairBlock = ResourceLocation.parse("minecraft:stone_brick_stairs");
        ResourceLocation slabBlock = ResourceLocation.parse("minecraft:stone_brick_slab");
        ResourceLocation ladderBlock = ResourceLocation.parse("minecraft:ladder");
        MKWorkspaceStairAuthoringConfig slabOnly = new MKWorkspaceStairAuthoringConfig(
                MKWorkspaceStairMode.RUN_PROFILE,
                MKWorkspaceStairRiseType.SLAB,
                2,
                stairBlock,
                slabBlock,
                ladderBlock
        );
        MKWorkspaceStairAuthoringConfig mixed = new MKWorkspaceStairAuthoringConfig(
                MKWorkspaceStairMode.RUN_PROFILE,
                MKWorkspaceStairRiseType.MIXED,
                2,
                stairBlock,
                slabBlock,
                ladderBlock
        );

        assertTrue(MKResolvedVerticalAccessProfile.resolve(slabOnly, 3, 3, 5).isEmpty());
        assertTrue(MKResolvedVerticalAccessProfile.resolve(slabOnly, 5, 5, 5).isPresent());
        MKResolvedVerticalAccessProfile profile = MKResolvedVerticalAccessProfile.resolve(mixed, 5, 5, 5)
                .orElseThrow();
        assertTrue(profile.hasRequiredPassClearance());
    }

    @Test
    void allowedBandHeightsIncludeEveryResolvableHeightBelowFortyEight() {
        ResourceLocation stairBlock = ResourceLocation.parse("minecraft:stone_brick_stairs");
        ResourceLocation slabBlock = ResourceLocation.parse("minecraft:stone_brick_slab");
        ResourceLocation ladderBlock = ResourceLocation.parse("minecraft:ladder");
        MKWorkspaceStairAuthoringConfig ladder = new MKWorkspaceStairAuthoringConfig(
                MKWorkspaceStairMode.LADDER,
                MKWorkspaceStairRiseType.MIXED,
                1,
                stairBlock,
                slabBlock,
                ladderBlock
        );

        List<Integer> allowedHeights = MKWorkspaceDimensions.getAllowedBandHeights(ladder, 3, 5, 3, 6);

        assertTrue(allowedHeights.size() > 6);
        assertEquals(3, allowedHeights.getFirst());
        assertEquals(MKWorkspaceDimensions.MAX_BAND_HEIGHT_EXCLUSIVE - 1, allowedHeights.getLast());
        assertTrue(allowedHeights.stream().allMatch(height -> height < MKWorkspaceDimensions.MAX_BAND_HEIGHT_EXCLUSIVE));
    }

    @Test
    void snappingBandHeightFallsBackWhenNoBandHeightsAreResolvable() {
        ResourceLocation stairBlock = ResourceLocation.parse("minecraft:stone_brick_stairs");
        ResourceLocation slabBlock = ResourceLocation.parse("minecraft:stone_brick_slab");
        ResourceLocation ladderBlock = ResourceLocation.parse("minecraft:ladder");
        MKWorkspaceStairAuthoringConfig overwideStairs = new MKWorkspaceStairAuthoringConfig(
                MKWorkspaceStairMode.RUN_PROFILE,
                MKWorkspaceStairRiseType.MIXED,
                4,
                stairBlock,
                slabBlock,
                ladderBlock
        );

        assertTrue(MKWorkspaceDimensions.getAllowedBandHeights(overwideStairs, 3, 5, 3,
                MKWorkspaceDimensions.MAX_BAND_HEIGHT_EXCLUSIVE).isEmpty());
        assertEquals(5, MKWorkspaceDimensions.snapToNearestAllowedBandHeight(overwideStairs, 3, 5, 5, 3,
                MKWorkspaceDimensions.MAX_BAND_HEIGHT_EXCLUSIVE));
    }

    @Test
    void slabRiseStrategyAllowsThreeByThreeShaftWithReusableBandHeights() {
        ResourceLocation stairBlock = ResourceLocation.parse("minecraft:stone_brick_stairs");
        ResourceLocation slabBlock = ResourceLocation.parse("minecraft:stone_brick_slab");
        ResourceLocation ladderBlock = ResourceLocation.parse("minecraft:ladder");
        MKWorkspaceStairAuthoringConfig slabOnly = new MKWorkspaceStairAuthoringConfig(
                MKWorkspaceStairMode.RUN_PROFILE,
                MKWorkspaceStairRiseType.SLAB,
                1,
                stairBlock,
                slabBlock,
                ladderBlock
        );

        assertFalse(MKWorkspaceDimensions.getAllowedBandHeights(slabOnly, 3, 5, 3,
                MKWorkspaceDimensions.MAX_BAND_HEIGHT_EXCLUSIVE).isEmpty());
        assertFalse(MKWorkspaceDimensions.getAllowedBandHeights(slabOnly, 5, 5, 3,
                MKWorkspaceDimensions.MAX_BAND_HEIGHT_EXCLUSIVE).isEmpty());
        assertEquals(3, MKWorkspaceDimensions.snapToNearestUsableShaftSize(slabOnly, 9, 9, 3, 3));
    }

    @Test
    void plannerCreatesSeparateMainAndBranchLinearRunPools() {
        MKStructureWorkspace workspace = baseWorkspace(List.of(
                        new MKHorizontalOpeningProfile("entry_main", 3, 3, true, false),
                        new MKHorizontalOpeningProfile("entry_branch", 3, 3, false, true),
                        new MKHorizontalOpeningProfile("main_main", 3, 3, true, false),
                        new MKHorizontalOpeningProfile("main_branch", 3, 3, false, true),
                        new MKHorizontalOpeningProfile("basement_main", 3, 3, true, false),
                        new MKHorizontalOpeningProfile("basement_branch", 3, 3, false, true),
                        new MKHorizontalOpeningProfile("top_cap_main", 3, 3, true, false),
                        new MKHorizontalOpeningProfile("top_cap_branch", 3, 3, false, true)
                ),
                List.of(
                        new MKWorkspaceLinearRunFamilyDefinition("surface", MKWorkspaceLinearRunKind.ENCLOSED_CORRIDOR,
                                "entry_main", 5, 3, 3, 0, true, false, MKWorkspaceLinearRunProjection.RIGID,
                                List.of(MKWorkspaceLinearRunPieceShape.STRAIGHT), workspacePalette().floorBlock(),
                                workspacePalette().wallBlock(), workspacePalette().ceilingBlock()),
                        new MKWorkspaceLinearRunFamilyDefinition("branch", MKWorkspaceLinearRunKind.ENCLOSED_CORRIDOR,
                                "main_branch", 5, 3, 3, 0, false, true, MKWorkspaceLinearRunProjection.RIGID,
                                List.of(MKWorkspaceLinearRunPieceShape.STRAIGHT), workspacePalette().floorBlock(),
                                workspacePalette().wallBlock(), workspacePalette().ceilingBlock())
                ));

        List<MKPlannedPiece> pieces = new MKTowerWorkspacePlanner().createCanonicalPieces(workspace);

        MKPlannedPiece entry = pieces.stream().filter(piece -> piece.pieceName().equals("entry")).findFirst().orElseThrow();
        assertTrue(entry.connectors().stream().anyMatch(connector ->
                connector.role() == MKConnectorRole.MAIN_BACK &&
                        "linear_runs/main/entry_main".equals(connector.targetPoolName())));
        assertTrue(entry.connectors().stream().noneMatch(connector ->
                connector.role() == MKConnectorRole.BRANCH &&
                        "linear_runs/branch/entry_main".equals(connector.targetPoolName())));

        MKPlannedPiece mainLinearRun = pieces.stream().filter(piece -> piece.pieceName().equals("linear_run_surface_main")).findFirst().orElseThrow();
        assertTrue(mainLinearRun.connectors().stream().allMatch(connector -> connector.incomingPoolName().equals("linear_runs/main/entry_main")));

        MKPlannedPiece branchLinearRun = pieces.stream().filter(piece -> piece.pieceName().equals("linear_run_branch_branch")).findFirst().orElseThrow();
        assertTrue(branchLinearRun.connectors().stream().allMatch(connector -> connector.incomingPoolName().equals("linear_runs/branch/main_branch")));
    }

    @Test
    void walledKeepTopologyProfileRoundTripsThroughWorkspaceTags() {
        MKWorkspaceStairAuthoringConfig centerStairs = new MKWorkspaceStairAuthoringConfig(
                MKWorkspaceStairMode.LADDER,
                MKWorkspaceStairRiseType.MIXED,
                2,
                ResourceLocation.parse("minecraft:oak_stairs"),
                ResourceLocation.parse("minecraft:oak_slab"),
                ResourceLocation.parse("minecraft:vine")
        );
        MKWorkspaceTopologyProfile topologyProfile = MKWorkspaceTopologyProfile.walledKeep(true, false, true, false)
                .withTowerStackSettings(new MKWorkspaceTowerStackSettings("keep.center", 2, 1, 9,
                        19, 21, 5, MKVerticalAccessPlacement.EAST, centerStairs, true, false));
        MKStructureWorkspace workspace = withTopologyAndLinearRuns(
                baseWorkspace(List.of(new MKHorizontalOpeningProfile("entry_main", 3, 3, true, false)), List.of()),
                topologyProfile,
                List.of(),
                List.of()
        );

        MKStructureWorkspace decoded = MKStructureWorkspace.fromTag(workspace.toTag());

        assertEquals(MKWorkspaceTopologyProfile.WALLED_KEEP_PROFILE_TYPE, decoded.topologyProfile().profileType());
        assertTrue(decoded.topologyProfile().uniqueNorthWestCornerTower());
        assertFalse(decoded.topologyProfile().uniqueNorthEastCornerTower());
        assertTrue(decoded.topologyProfile().uniqueSouthEastCornerTower());
        assertFalse(decoded.topologyProfile().uniqueSouthWestCornerTower());
        assertFalse(decoded.topologyProfile().uniqueCornerTowers());
        MKWorkspaceTowerStackSettings centerSettings = decoded.topologyProfile()
                .towerStackSettings("keep.center")
                .orElseThrow();
        assertEquals(19, centerSettings.width());
        assertEquals(21, centerSettings.length());
        assertEquals(9, centerSettings.height());
        assertEquals(5, centerSettings.shaftSize());
        assertEquals(MKVerticalAccessPlacement.EAST, centerSettings.verticalAccessPlacement());
        assertEquals(MKWorkspaceStairMode.LADDER, centerSettings.stairConfig().mode());
        assertEquals(2, centerSettings.stairConfig().stairWidth());
        assertEquals(ResourceLocation.parse("minecraft:vine"), centerSettings.stairConfig().ladderBlock());
    }

    @Test
    void exportManifestRestoresTopologyProfileOnImport() {
        MKWorkspaceStairAuthoringConfig centerStairs = new MKWorkspaceStairAuthoringConfig(
                MKWorkspaceStairMode.LADDER,
                MKWorkspaceStairRiseType.MIXED,
                2,
                ResourceLocation.parse("minecraft:oak_stairs"),
                ResourceLocation.parse("minecraft:oak_slab"),
                ResourceLocation.parse("minecraft:vine")
        );
        MKWorkspaceTopologyProfile topologyProfile = MKWorkspaceTopologyProfile.walledKeep(false, true, false, true)
                .withTowerStackSettings(new MKWorkspaceTowerStackSettings("keep.center", 3, 2, 11,
                        23, 25, 5, MKVerticalAccessPlacement.WEST, centerStairs, false, true))
                .withPathSettings(new MKWorkspaceTopologyPathSettings(
                        "main", 2, 4, 3));
        MKStructureWorkspace workspace = withTopologyAndLinearRuns(
                baseWorkspace(List.of(new MKHorizontalOpeningProfile("entry_main", 3, 3, true, false)), List.of()),
                topologyProfile,
                List.of(),
                List.of()
        );

        MKWorkspaceExportManifest manifest = MKWorkspaceExportManifest.snapshotFromWorkspace(workspace, 4, "test");
        MKStructureWorkspace imported = new MKStructureWorkspaceImportService().workspaceFromManifest(
                UUID.randomUUID(), new BlockPos(7, 80, 7), 123L, manifest);

        assertEquals(MKWorkspaceTopologyProfile.WALLED_KEEP_PROFILE_TYPE, manifest.settings().topologyProfile().profileType());
        assertEquals(MKWorkspaceTopologyProfile.WALLED_KEEP_PROFILE_TYPE, imported.topologyProfile().profileType());
        assertFalse(imported.topologyProfile().uniqueNorthWestCornerTower());
        assertTrue(imported.topologyProfile().uniqueNorthEastCornerTower());
        assertFalse(imported.topologyProfile().uniqueSouthEastCornerTower());
        assertTrue(imported.topologyProfile().uniqueSouthWestCornerTower());
        MKWorkspaceTowerStackSettings centerSettings = imported.topologyProfile()
                .towerStackSettings("keep.center")
                .orElseThrow();
        assertEquals(23, centerSettings.width());
        assertEquals(25, centerSettings.length());
        assertEquals(11, centerSettings.height());
        assertEquals(5, centerSettings.shaftSize());
        assertEquals(MKVerticalAccessPlacement.WEST, centerSettings.verticalAccessPlacement());
        assertEquals(MKWorkspaceStairMode.LADDER, centerSettings.stairConfig().mode());
        assertEquals(3, imported.topologyPathSettings("main").maxBranchPiecesBeforeCap());
    }

    @Test
    void exportTopologySettingsCarryStackAndPathData() {
        MKWorkspaceTopologyProfile topologyProfile = MKWorkspaceTopologyProfile.tower()
                .withTowerStackSettings(new MKWorkspaceTowerStackSettings("tower.primary", 2, 1, 9,
                        15, 17, 3, MKVerticalAccessPlacement.CENTER,
                        MKWorkspaceStairAuthoringConfig.defaultConfig(), true, false))
                .withPathSettings(new MKWorkspaceTopologyPathSettings(
                        "main", 2, 5, 4));
        MKStructureWorkspace workspace = withTopologyAndLinearRuns(
                baseWorkspace(List.of(new MKHorizontalOpeningProfile("entry_main", 3, 3, true, false)), List.of()),
                topologyProfile,
                List.of(),
                List.of()
        );

        MKWorkspaceExportManifest manifest = MKWorkspaceExportManifest.snapshotFromWorkspace(workspace, 4, "test");
        MKWorkspaceTowerStackSettings exportedStack = manifest.settings().topologyProfile().towerStackSettings("tower.primary")
                .orElseThrow();
        MKWorkspaceTopologyPathSettings exportedPath =
                manifest.settings().topologyProfile().pathSettingsOrDefault("main");

        assertEquals(15, exportedStack.width());
        assertEquals(17, exportedStack.length());
        assertEquals(9, exportedStack.height());
        assertEquals(2, exportedPath.minMainPathPieces());
        assertEquals(5, exportedPath.maxMainPathPieces());
        assertEquals(4, exportedPath.maxBranchPiecesBeforeCap());
    }

    @Test
    void workspaceDimensionsSerializeShaftWidthWithoutObsoleteField() {
        CompoundTag tag = MKWorkspaceDimensions.defaultDimensions().toTag();

        assertTrue(tag.contains("shaftWidth"));
        assertFalse(tag.contains("hall" + "wayWidth"));
    }

    @Test
    void exportDimensionsUseShaftWidthAndLinearRunConnectionMode() {
        MKStructureWorkspace workspace = baseWorkspace(
                List.of(new MKHorizontalOpeningProfile("entry_main", 3, 3, true, false)),
                List.of()
        );
        MKWorkspaceExportManifest manifest = MKWorkspaceExportManifest.snapshotFromWorkspace(workspace, 4, "test");
        JsonObject json = MKWorkspaceExportManifest.CODEC.encodeStart(JsonOps.INSTANCE, manifest)
                .getOrThrow()
                .getAsJsonObject();

        assertEquals(workspace.dimensions().shaftWidth(), manifest.settings().dimensions().shaftWidth());
        assertEquals("linear_run", MKWorkspaceHorizontalExitConnectionMode.LINEAR_RUN.getSerializedName());
        assertTrue(json.has("template_groups"));
        assertFalse(json.has("categories"));
        assertTrue(json.getAsJsonObject("runtime_hints").has("template_groups"));
        assertFalse(json.getAsJsonObject("runtime_hints").has("categories"));
    }

    @Test
    void pathBudgetCodecsUseTopologyGroupTerminology() {
        MKDungeonLayoutSettings settings = new MKDungeonLayoutSettings(
                1,
                3,
                1,
                4,
                2,
                false,
                MKVerticalProgressionMode.MIXED,
                true,
                false,
                List.of(new MKDungeonTopologyGroupRule("main", 1, 2, true, null)),
                connectorSettings()
        );
        JsonObject settingsJson = MKDungeonLayoutSettings.CODEC.encodeStart(JsonOps.INSTANCE, settings)
                .getOrThrow()
                .getAsJsonObject();
        JsonObject ruleJson = settingsJson.getAsJsonArray("topology_group_rules").get(0).getAsJsonObject();

        assertTrue(settingsJson.has("topology_group_rules"));
        assertFalse(settingsJson.has("category_rules"));
        assertEquals("main", ruleJson.get("topology_group").getAsString());
        assertFalse(ruleJson.has("category"));

        MKJigsawPieceMetadata metadata = new MKJigsawPieceMetadata(MKJigsawPieceRole.ROOM, 0, 0,
                true, false, false, false, "main", false);
        JsonObject metadataJson = MKJigsawPieceMetadata.CODEC.encodeStart(JsonOps.INSTANCE, metadata)
                .getOrThrow()
                .getAsJsonObject();
        assertEquals("main", metadataJson.get("topology_group").getAsString());
        assertFalse(metadataJson.has("category"));
    }

    @Test
    void resolvedFamilyMetadataComesFromTopologySlot() {
        MKTowerWorkspaceFamilyDefinition mismatchedFamily = topologyFamily(
                "custom_top",
                "tower.primary.top_cap",
                "tower.primary",
                true,
                0,
                0,
                0,
                MKWorkspaceHorizontalExtrusionMode.TUNNEL_ONLY,
                List.of(MKWorkspaceFamilyHorizontalExitDefinition.verticalAccess(Direction.DOWN)),
                0,
                0,
                null,
                null
        );
        MKStructureWorkspace workspace = withTopologyAndLinearRuns(
                baseWorkspace(List.of(new MKHorizontalOpeningProfile("entry_main", 3, 3, true, false)), List.of()),
                MKWorkspaceTopologyProfile.tower(),
                List.of(mismatchedFamily),
                List.of()
        );

        assertEquals("top_cap",
                workspace.resolveFamilySettings(mismatchedFamily).slotMetadata().topologyGroupId());
        assertEquals(MKJigsawPieceRole.TOP_CAP,
                workspace.resolveFamilySettings(mismatchedFamily).slotMetadata().jigsawPieceRole());
        assertEquals("tower.primary.top_cap",
                new MKTowerStackPlanner().createPieceForFamily(workspace, mismatchedFamily).roleId());

        MKWorkspaceExportManifest.ExportFamilyDefinition exported =
                MKWorkspaceExportManifest.ExportFamilyDefinition.from(mismatchedFamily);
        assertEquals("top_cap", exported.slotMetadata().topologyGroupId());
        assertEquals(MKJigsawPieceRole.TOP_CAP, exported.slotMetadata().jigsawPieceRole());

        CompoundTag tag = mismatchedFamily.toTag();
        assertFalse(tag.contains("category"));
        assertFalse(tag.contains("pieceRole"));
        MKTowerWorkspaceFamilyDefinition decoded = MKTowerWorkspaceFamilyDefinition.fromTag(tag);
        assertEquals("top_cap", decoded.slotMetadata().topologyGroupId());
        assertEquals(MKJigsawPieceRole.TOP_CAP, decoded.slotMetadata().jigsawPieceRole());
    }

    @Test
    void importNormalizesStackFamilyMetadataFromTopologySlot() {
        MKStructureWorkspace workspace = withTopologyAndLinearRuns(
                baseWorkspace(List.of(new MKHorizontalOpeningProfile("entry_main", 3, 3, true, false)), List.of()),
                MKWorkspaceTopologyProfile.tower(),
                List.of(),
                List.of()
        );
        MKWorkspaceExportManifest manifest = MKWorkspaceExportManifest.snapshotFromWorkspace(workspace, 4, "test");
        MKWorkspaceExportManifest.ExportWorkspaceSettings settings = manifest.settings();
        MKWorkspaceExportManifest.ExportFamilyDefinition staleFamily =
                new MKWorkspaceExportManifest.ExportFamilyDefinition(
                        "imported_stale_top",
                        MKWorkspaceTopologySlotMetadata.fromTopologySlotId("tower.primary.top_cap"),
                        "tower.primary",
                        true,
                        0,
                        0,
                        0,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION,
                        List.of(),
                        0,
                        0,
                        null,
                        null);
        MKWorkspaceExportManifest.ExportWorkspaceSettings staleSettings =
                new MKWorkspaceExportManifest.ExportWorkspaceSettings(
                        settings.anchor(),
                        settings.shellMargin(),
                        settings.exteriorAirMargin(),
                        settings.previewMargin(),
                        settings.verticalAccessPlacement(),
                        settings.dimensions(),
                        settings.palette(),
                        settings.stairConfig(),
                        settings.verticalAccessSpec(),
                        settings.topologyProfile(),
                        List.of(staleFamily),
                        settings.openingProfiles(),
                        settings.linearRunFamilies());
        MKWorkspaceExportManifest staleManifest = new MKWorkspaceExportManifest(
                manifest.schemaVersion(),
                manifest.workspaceId(),
                manifest.namespace(),
                manifest.structureName(),
                manifest.exportedAt(),
                manifest.createdAt(),
                manifest.updatedAt(),
                staleSettings,
                manifest.runtimeHints(),
                manifest.templateGroups(),
                manifest.pieces());

        MKStructureWorkspace imported = new MKStructureWorkspaceImportService().workspaceFromManifest(
                UUID.randomUUID(), new BlockPos(7, 80, 7), 123L, staleManifest);
        MKTowerWorkspaceFamilyDefinition importedFamily = imported.familyDefinitions().stream()
                .filter(family -> family.baseName().equals("imported_stale_top"))
                .findFirst()
                .orElseThrow();

        assertEquals("top_cap", importedFamily.slotMetadata().topologyGroupId());
        assertEquals(MKJigsawPieceRole.TOP_CAP, importedFamily.slotMetadata().jigsawPieceRole());
        assertEquals("tower.primary.top_cap",
                new MKTowerStackPlanner().createPieceForFamily(imported, importedFamily).roleId());
    }

    @Test
    void walledKeepPlannerCreatesExplicitRoomAndLinearRunPieces() {
        MKWorkspaceFoundationPolicy wallFoundation = MKWorkspaceFoundationPolicy.maskedExtendBottomBlocks(List.of(
                ResourceLocation.parse("minecraft:stone_bricks"),
                ResourceLocation.parse("minecraft:cobblestone")
        ));
        MKStructureWorkspace workspace = withTopologyAndLinearRuns(
                baseWorkspace(List.of(new MKHorizontalOpeningProfile("wall_opening", 3, 3, true, true)), List.of()),
                MKWorkspaceTopologyProfile.walledKeep(false),
                List.of(topologyFamily(
                        "keep_center_entry",
                        "keep.center.entry",
                        "keep.center",
                        true,
                        9,
                        9,
                        MKWorkspaceDimensions.defaultDimensions().entranceHeight(),
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION,
                        List.of(),
                        0,
                        0,
                        MKWorkspaceFoundationPolicy.none(),
                        null
                )),
                List.of(new MKWorkspaceLinearRunFamilyDefinition(
                        "keep_wall_north",
                        "keep.perimeter.north",
                        MKWorkspaceLinearRunKind.SOLID_WALL,
                        "wall_opening",
                        11,
                        3,
                        5,
                        0,
                        true,
                        true,
                        MKWorkspaceLinearRunProjection.RIGID,
                        List.of(MKWorkspaceLinearRunPieceShape.STRAIGHT),
                        wallFoundation,
                        null
                ))
        );

        List<MKPlannedPiece> pieces = new MKWalledKeepWorkspacePlanner().createCanonicalPieces(workspace);
        MKPlannedPiece centerEntry = pieces.stream()
                .filter(piece -> piece.pieceName().equals("keep_center_entry"))
                .findFirst()
                .orElseThrow();
        MKPlannedPiece northWall = pieces.stream()
                .filter(piece -> piece.pieceName().equals("keep_wall_north_north_west_0"))
                .findFirst()
                .orElseThrow();

        assertEquals("keep.center.entry", centerEntry.tags().get("workspace_topology_slot_id"));
        assertEquals("keep.center", centerEntry.tags().get("workspace_vertical_access_group_id"));
        assertTrue(centerEntry.connectors().stream().anyMatch(connector ->
                connector.role() == MKConnectorRole.CONNECT_UP &&
                        "tower_stacks/keep/center/connect_up".equals(connector.targetPoolName())));
        assertTrue(centerEntry.connectors().stream().anyMatch(connector ->
                connector.role() == MKConnectorRole.CONNECT_DOWN &&
                        "tower_stacks/keep/center/connect_down_entry".equals(connector.targetPoolName())));

        assertEquals("linear_run", northWall.tags().get("tower_piece_kind"));
        assertEquals("keep.perimeter.north_west.0", northWall.tags().get("workspace_topology_slot_id"));
        assertEquals("keep.perimeter.north", northWall.tags().get("workspace_perimeter_source_slot_id"));
        assertEquals("north_west", northWall.tags().get("workspace_perimeter_chain_id"));
        assertEquals("solid_wall", northWall.tags().get("workspace_linear_run_kind"));
        assertEquals(MKWorkspaceFoundationMode.MASKED_EXTEND_BOTTOM_BLOCKS.getSerializedName(),
                northWall.tags().get(MKWorkspaceFoundationPolicy.MODE_TAG));
        assertTrue(northWall.connectors().stream().anyMatch(connector ->
                connector.facing() == Direction.WEST &&
                        "keep_slots/keep/perimeter/north_west/0".equals(connector.incomingPoolName())));
        assertTrue(northWall.connectors().stream().anyMatch(connector ->
                connector.facing() == Direction.EAST &&
                        "keep_slots/keep/perimeter/north_west/1".equals(connector.targetPoolName())));
    }

    @Test
    void walledKeepDefaultsGenerateKeepSpecificPieces() {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        MKStructureWorkspace workspace = withTopologyAndLinearRuns(
                baseWorkspace(MKHorizontalOpeningProfile.createDefaults(dimensions), List.of()),
                MKWorkspaceTopologyProfile.walledKeep(false),
                MKTowerWorkspaceFamilyDefinition.createWalledKeepDefaults(dimensions),
                MKWorkspaceLinearRunFamilyDefinition.createWalledKeepDefaults(dimensions, workspacePalette())
        );

        assertEquals(List.of(), workspace.validate());
        List<MKPlannedPiece> pieces = new MKWalledKeepWorkspacePlanner().createCanonicalPieces(workspace);

        MKPlannedPiece centerEntry = pieces.stream()
                .filter(piece -> piece.pieceName().equals("keep_center_entry"))
                .findFirst()
                .orElseThrow();
        assertEquals(17, centerEntry.interiorWidth());
        assertEquals(17, centerEntry.interiorLength());
        assertEquals(7, centerEntry.interiorHeight());
        assertEquals("keep.center.entry", centerEntry.tags().get("workspace_topology_slot_id"));
        MKPlannedPiece sharedCorner = pieces.stream()
                .filter(piece -> piece.pieceName().equals("keep_corner_north_west_entry"))
                .findFirst()
                .orElseThrow();
        assertEquals("keep.corner.north_west.entry", sharedCorner.tags().get("workspace_topology_slot_id"));
        assertEquals("true", sharedCorner.tags().get(MKWorkspaceRuntimePieceInfo.ALLOW_ON_BRANCH_PATH_TAG));
        assertEquals("full_face", sharedCorner.tags().get("workspace_connector_stitch"));
        assertTrue(sharedCorner.connectors().stream().anyMatch(connector ->
                connector.role() == MKConnectorRole.CONNECT_UP));
        assertTrue(sharedCorner.connectors().stream().anyMatch(connector ->
                connector.role() == MKConnectorRole.CONNECT_DOWN));
        MKPlannedPiece cornerTopCap = pieces.stream()
                .filter(piece -> piece.pieceName().equals("keep_corner_north_west_top_cap"))
                .findFirst()
                .orElseThrow();
        assertEquals("true", cornerTopCap.tags().get(MKWorkspaceRuntimePieceInfo.ALLOW_ON_BRANCH_PATH_TAG));
        assertTrue(pieces.stream().anyMatch(piece -> piece.pieceName().equals("keep_corner_north_west_top_cap")));
        assertTrue(pieces.stream().anyMatch(piece -> piece.pieceName().equals("keep_corner_north_west_basement_cap")));
        MKPlannedPiece northWall = pieces.stream()
                .filter(piece -> piece.pieceName().equals("keep_wall_segment_north_west_0"))
                .findFirst()
                .orElseThrow();
        MKPlannedPiece gatehouse = pieces.stream()
                .filter(piece -> piece.pieceName().equals("keep_gate_main"))
                .findFirst()
                .orElseThrow();
        assertEquals(northWall.interiorWidth(), gatehouse.interiorWidth());
        assertTrue(gatehouse.connectors().stream().anyMatch(connector ->
                connector.role() == MKConnectorRole.MAIN_BACK &&
                        connector.facing() == Direction.SOUTH &&
                        !connector.placesJigsaw()));
        assertTrue(gatehouse.connectors().stream().anyMatch(connector ->
                connector.role() == MKConnectorRole.BRANCH &&
                        connector.facing() == Direction.WEST &&
                        "keep_slots/keep/perimeter/south_west/0".equals(connector.targetPoolName())));
        assertTrue(gatehouse.connectors().stream().anyMatch(connector ->
                connector.role() == MKConnectorRole.BRANCH &&
                        connector.facing() == Direction.EAST &&
                        "keep_slots/keep/perimeter/south_east/0".equals(connector.targetPoolName())));
        assertEquals(
                pieces.stream().filter(piece -> "south_west".equals(piece.tags().get("workspace_perimeter_chain_id"))).count(),
                pieces.stream().filter(piece -> "south_east".equals(piece.tags().get("workspace_perimeter_chain_id"))).count());
        long northWestCount = pieces.stream()
                .filter(piece -> "north_west".equals(piece.tags().get("workspace_perimeter_chain_id")))
                .count();
        long northEastCount = pieces.stream()
                .filter(piece -> "north_east".equals(piece.tags().get("workspace_perimeter_chain_id")))
                .count();
        assertEquals(1, northWestCount - northEastCount);
        MKPlannedPiece northWestTerminal = pieces.stream()
                .filter(piece -> "north_west".equals(piece.tags().get("workspace_perimeter_chain_id")))
                .filter(piece -> piece.tags().get("workspace_perimeter_segment_index")
                        .equals(Integer.toString(Integer.parseInt(piece.tags().get("workspace_perimeter_segment_count")) - 1)))
                .findFirst()
                .orElseThrow();
        MKPlannedPiece northEastTerminal = pieces.stream()
                .filter(piece -> "north_east".equals(piece.tags().get("workspace_perimeter_chain_id")))
                .filter(piece -> piece.tags().get("workspace_perimeter_segment_index")
                        .equals(Integer.toString(Integer.parseInt(piece.tags().get("workspace_perimeter_segment_count")) - 1)))
                .findFirst()
                .orElseThrow();
        assertTrue(northWestTerminal.connectors().stream().anyMatch(connector ->
                connector.role() == MKConnectorRole.BRANCH &&
                        connector.facing() == Direction.EAST &&
                        !connector.placesJigsaw()));
        assertTrue(northEastTerminal.connectors().stream().anyMatch(connector ->
                connector.role() == MKConnectorRole.BRANCH &&
                        connector.facing() == Direction.WEST &&
                        !connector.placesJigsaw()));
        assertEquals(7, northWall.interiorHeight());
        assertEquals("defensive_wall", northWall.tags().get("workspace_linear_run_kind"));
        assertFalse(pieces.stream().anyMatch(piece -> "parapet".equals(piece.tags().get("workspace_linear_run_kind"))));
        assertTrue(pieces.stream().anyMatch(piece -> piece.pieceName().equals("keep_walkway_south") &&
                "open_walkway".equals(piece.tags().get("workspace_linear_run_kind"))));
    }

    @Test
    void walledKeepPlannerMarksRotatedTemplateReuseForWallsAndSharedCorners() {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        MKStructureWorkspace workspace = withTopologyAndLinearRuns(
                baseWorkspace(MKHorizontalOpeningProfile.createDefaults(dimensions), List.of()),
                MKWorkspaceTopologyProfile.walledKeep(false),
                MKTowerWorkspaceFamilyDefinition.createWalledKeepDefaults(dimensions),
                MKWorkspaceLinearRunFamilyDefinition.createWalledKeepDefaults(dimensions, workspacePalette())
        );

        List<MKPlannedPiece> pieces = new MKWalledKeepWorkspacePlanner().createCanonicalPieces(workspace);
        MKPlannedPiece wallSource = pieces.stream()
                .filter(piece -> piece.pieceName().equals("keep_wall_segment_south_west_0"))
                .findFirst()
                .orElseThrow();
        MKPlannedPiece northWall = pieces.stream()
                .filter(piece -> piece.pieceName().equals("keep_wall_segment_north_west_0"))
                .findFirst()
                .orElseThrow();
        MKPlannedPiece cornerSource = pieces.stream()
                .filter(piece -> piece.pieceName().equals("keep_corner_north_west_entry"))
                .findFirst()
                .orElseThrow();
        MKPlannedPiece southEastCorner = pieces.stream()
                .filter(piece -> piece.pieceName().equals("keep_corner_south_east_entry"))
                .findFirst()
                .orElseThrow();

        assertEquals("true", wallSource.tags().get(MKWorkspaceTemplateReuseTags.AUTHORING_PIECE_TAG));
        assertEquals("keep_wall_segment_south_west_0",
                wallSource.tags().get(MKWorkspaceTemplateReuseTags.SOURCE_ID_TAG));
        assertEquals(MKWorkspaceTemplateReuseTags.ROTATION_NONE,
                wallSource.tags().get(MKWorkspaceTemplateReuseTags.ROTATION_TAG));
        assertEquals("false", northWall.tags().get(MKWorkspaceTemplateReuseTags.AUTHORING_PIECE_TAG));
        assertEquals("keep_wall_segment_south_west_0",
                northWall.tags().get(MKWorkspaceTemplateReuseTags.SOURCE_ID_TAG));
        assertEquals(MKWorkspaceTemplateReuseTags.ROTATION_CLOCKWISE_180,
                northWall.tags().get(MKWorkspaceTemplateReuseTags.ROTATION_TAG));
        assertEquals(MKWorkspaceTemplateReuseTags.REUSE_MODE_ROTATE_EXPORT,
                northWall.tags().get(MKWorkspaceTemplateReuseTags.REUSE_MODE_TAG));

        assertEquals("true", cornerSource.tags().get(MKWorkspaceTemplateReuseTags.AUTHORING_PIECE_TAG));
        assertEquals("keep_corner_north_west_entry",
                cornerSource.tags().get(MKWorkspaceTemplateReuseTags.SOURCE_ID_TAG));
        assertEquals("false", southEastCorner.tags().get(MKWorkspaceTemplateReuseTags.AUTHORING_PIECE_TAG));
        assertEquals("keep_corner_north_west_entry",
                southEastCorner.tags().get(MKWorkspaceTemplateReuseTags.SOURCE_ID_TAG));
        assertEquals(MKWorkspaceTemplateReuseTags.ROTATION_CLOCKWISE_180,
                southEastCorner.tags().get(MKWorkspaceTemplateReuseTags.ROTATION_TAG));
    }

    @Test
    void walledKeepSharedCornerPlannerNormalizesHorizontalDimensionsToSquare() {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        MKWorkspaceTopologyProfile topologyProfile = MKWorkspaceTopologyProfile.walledKeep(false)
                .withTowerStackSettings(new MKWorkspaceTowerStackSettings("keep.corner.shared", 1, 1, 7,
                        9, 13, true, false));
        MKStructureWorkspace workspace = withTopologyAndLinearRuns(
                baseWorkspace(MKHorizontalOpeningProfile.createDefaults(dimensions), List.of()),
                topologyProfile,
                MKTowerWorkspaceFamilyDefinition.createWalledKeepDefaults(dimensions),
                MKWorkspaceLinearRunFamilyDefinition.createWalledKeepDefaults(dimensions, workspacePalette())
        );

        List<MKPlannedPiece> pieces = new MKWalledKeepWorkspacePlanner().createCanonicalPieces(workspace);
        List<MKPlannedPiece> sharedCornerEntries = pieces.stream()
                .filter(piece -> piece.pieceName().startsWith("keep_corner_"))
                .filter(piece -> piece.tags().getOrDefault("workspace_topology_slot_id", "").endsWith(".entry"))
                .toList();

        assertEquals(4, sharedCornerEntries.size());
        for (MKPlannedPiece cornerEntry : sharedCornerEntries) {
            assertEquals(13, cornerEntry.interiorWidth());
            assertEquals(13, cornerEntry.interiorLength());
        }
    }

    @Test
    void walledKeepValidationAllowsIndependentTowerStackHeights() {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        MKWorkspaceTopologyProfile topologyProfile = MKWorkspaceTopologyProfile.walledKeep(false)
                .withTowerStackSettings(new MKWorkspaceTowerStackSettings("keep.center", 1, 1, 9))
                .withTowerStackSettings(new MKWorkspaceTowerStackSettings("keep.corner.shared", 1, 1, 7));
        List<MKTowerWorkspaceFamilyDefinition> families = MKTowerWorkspaceFamilyDefinition
                .createWalledKeepDefaults(dimensions).stream()
                .map(family -> family.topologySlotId().startsWith("keep.center.") ?
                        copyFamilyWithHeight(family, 9) : family)
                .toList();
        MKStructureWorkspace workspace = withTopologyAndLinearRuns(
                baseWorkspace(MKHorizontalOpeningProfile.createDefaults(dimensions), List.of()),
                topologyProfile,
                families,
                MKWorkspaceLinearRunFamilyDefinition.createWalledKeepDefaults(dimensions, workspacePalette())
        );

        assertEquals(List.of(), workspace.validate());
    }

    @Test
    void stackFloorValidationIgnoresGlobalDefaultHeightsWhenStackSettingsExist() {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        MKWorkspaceTopologyProfile topologyProfile = MKWorkspaceTopologyProfile.tower()
                .withTowerStackSettings(new MKWorkspaceTowerStackSettings("tower.primary", 1, 1, 7));
        List<MKTowerWorkspaceFamilyDefinition> families = MKTowerWorkspaceFamilyDefinition
                .createDefaults(dimensions).stream()
                .map(family -> copyFamilyWithHeight(family, 7))
                .toList();
        MKStructureWorkspace workspace = withTopologyAndLinearRuns(
                baseWorkspace(MKHorizontalOpeningProfile.createDefaults(dimensions), List.of()),
                topologyProfile,
                families,
                MKWorkspaceLinearRunFamilyDefinition.createDefaults(dimensions, workspacePalette())
        );

        assertEquals(List.of(), workspace.validate());
    }

    @Test
    void familyExitValidationUsesResolvedStackDimensions() {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        MKWorkspaceTopologyProfile topologyProfile = MKWorkspaceTopologyProfile.tower()
                .withTowerStackSettings(new MKWorkspaceTowerStackSettings(
                        "tower.primary", 1, 1, 7, 15, 15,
                        3, MKVerticalAccessPlacement.CENTER, MKWorkspaceStairAuthoringConfig.defaultConfig(),
                        true, true));
        List<MKTowerWorkspaceFamilyDefinition> families = MKTowerWorkspaceFamilyDefinition
                .createDefaults(dimensions).stream()
                .map(family -> family.baseName().equals("floor_main") ?
                        MKTowerWorkspaceFamilyDefinition.forTopologySlot(
                                family.baseName(),
                                family.slotMetadata(),
                                family.verticalAccessGroupId(),
                                family.supportsVerticalAccess(),
                                family.roomWidth(),
                                family.roomLength(),
                                7,
                                family.horizontalExtrusionMode(),
                                List.of(new MKWorkspaceFamilyHorizontalExitDefinition(
                                        Direction.NORTH,
                                        MKWorkspaceHorizontalExitPathKind.BRANCH,
                                        "main_branch",
                                        MKWorkspaceHorizontalExitConnectionMode.DIRECT_ROOM,
                                        5,
                                        0)),
                                family.topVoidMargin(),
                                family.bottomVoidMargin(),
                                family.foundationPolicyOverride(),
                                family.paletteOverride()) :
                        copyFamilyWithHeight(family, 7))
                .toList();
        MKStructureWorkspace workspace = withTopologyAndLinearRuns(
                baseWorkspace(
                        List.of(
                                new MKHorizontalOpeningProfile("main_opening", 3, 3, true, false),
                                new MKHorizontalOpeningProfile("entry_main", 3, 3, true, false),
                                new MKHorizontalOpeningProfile("main_branch", 3, 3, false, true)
                        ),
                        List.of(new MKWorkspaceLinearRunFamilyDefinition(
                                "branch", MKWorkspaceLinearRunKind.ENCLOSED_CORRIDOR,
                                "main_branch", 5, 3, 3, 0, false, true,
                                MKWorkspaceLinearRunProjection.RIGID,
                                List.of(MKWorkspaceLinearRunPieceShape.STRAIGHT),
                                workspacePalette().floorBlock(),
                                workspacePalette().wallBlock(),
                                workspacePalette().ceilingBlock()))
                ),
                topologyProfile,
                families,
                List.of()
        );

        assertEquals(List.of(), workspace.validate());
    }

    @Test
    void walledKeepPlannerTagsTowerStacksAndLinearRunVoidMargins() {
        MKWorkspaceTopologyProfile topologyProfile = MKWorkspaceTopologyProfile.walledKeep(false)
                .withTowerStackSettings(new MKWorkspaceTowerStackSettings("keep.center", 3, 2, 7));
        MKStructureWorkspace workspace = withTopologyAndLinearRuns(
                baseWorkspace(List.of(new MKHorizontalOpeningProfile("wall_opening", 3, 3, true, true)), List.of()),
                topologyProfile,
                List.of(topologyFamily(
                        "keep_center_entry",
                        "keep.center.entry",
                        "keep.center",
                        true,
                        9,
                        9,
                        7,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION,
                        List.of(),
                        0,
                        0,
                        MKWorkspaceFoundationPolicy.none(),
                        null
                )),
                List.of(new MKWorkspaceLinearRunFamilyDefinition(
                        "keep_wall_north",
                        "keep.perimeter.north",
                        MKWorkspaceLinearRunKind.SOLID_WALL,
                        "wall_opening",
                        11,
                        3,
                        7,
                        0,
                        true,
                        true,
                        MKWorkspaceLinearRunProjection.RIGID,
                        List.of(MKWorkspaceLinearRunPieceShape.STRAIGHT),
                        2,
                        MKWorkspaceFoundationPolicy.none(),
                        null
                ))
        );

        List<MKPlannedPiece> pieces = new MKWalledKeepWorkspacePlanner().createCanonicalPieces(workspace);
        MKPlannedPiece centerEntry = pieces.stream()
                .filter(piece -> piece.pieceName().equals("keep_center_entry"))
                .findFirst()
                .orElseThrow();
        MKPlannedPiece northWall = pieces.stream()
                .filter(piece -> piece.pieceName().equals("keep_wall_north_north_west_0"))
                .findFirst()
                .orElseThrow();

        assertEquals("keep.center", centerEntry.tags().get("workspace_tower_stack_id"));
        assertEquals("3", centerEntry.tags().get("workspace_tower_stack_main_floors"));
        assertEquals("2", centerEntry.tags().get("workspace_tower_stack_basement_floors"));
        assertEquals("2", northWall.tags().get(MKWorkspaceVoidMarginTags.TOP_VOID_MARGIN_TAG));
    }

    @Test
    void walledKeepStackSettingsControlCapApproachPiecesPerStack() {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        MKWorkspaceStairAuthoringConfig centerStairs = new MKWorkspaceStairAuthoringConfig(
                MKWorkspaceStairMode.LADDER,
                MKWorkspaceStairRiseType.MIXED,
                2,
                ResourceLocation.parse("minecraft:stone_brick_stairs"),
                ResourceLocation.parse("minecraft:stone_brick_slab"),
                ResourceLocation.parse("minecraft:ladder")
        );
        MKWorkspaceTopologyProfile topologyProfile = MKWorkspaceTopologyProfile.walledKeep(false)
                .withTowerStackSettings(new MKWorkspaceTowerStackSettings("keep.center", 1, 1, 7,
                        17, 17, 5, MKVerticalAccessPlacement.EAST, centerStairs, false, true));
        MKStructureWorkspace workspace = withTopologyAndLinearRuns(
                baseWorkspace(MKHorizontalOpeningProfile.createDefaults(dimensions), List.of()),
                topologyProfile,
                MKTowerWorkspaceFamilyDefinition.createWalledKeepDefaults(dimensions),
                MKWorkspaceLinearRunFamilyDefinition.createWalledKeepDefaults(dimensions, workspacePalette())
        );

        List<MKPlannedPiece> pieces = new MKWalledKeepWorkspacePlanner().createCanonicalPieces(workspace);

        MKPlannedPiece centerEntry = pieces.stream()
                .filter(piece -> piece.pieceName().equals("keep_center_entry"))
                .findFirst()
                .orElseThrow();
        assertEquals("east", centerEntry.tags().get(MKWorkspaceVerticalAccessTags.PLACEMENT_TAG));
        assertEquals("ladder", centerEntry.tags().get("workspace_vertical_access_stair_mode"));
        assertEquals("2", centerEntry.tags().get("workspace_vertical_access_stair_width"));
        assertEquals(17, centerEntry.interiorWidth());
        assertEquals(17, centerEntry.interiorLength());
        assertEquals(MKWorkspaceStairMode.LADDER,
                workspace.stairConfigForPiece(pieceToDefinitionWithConnectors(workspace, centerEntry)).mode());
        assertTrue(centerEntry.connectors().stream()
                .filter(connector -> connector.role() == MKConnectorRole.CONNECT_UP)
                .anyMatch(connector -> connector.openingWidth() == 5 && connector.openingHeight() == 5));
        assertFalse(pieces.stream().anyMatch(piece -> piece.pieceName().equals("keep_center_top_cap_approach")));
        assertTrue(pieces.stream().anyMatch(piece -> piece.pieceName().equals("keep_center_basement_cap_approach")));
        assertTrue(pieces.stream().anyMatch(piece -> piece.pieceName().equals("keep_corner_north_west_top_cap_approach")));
        assertFalse(pieces.stream().anyMatch(piece ->
                piece.pieceName().equals("keep_corner_north_west_basement_cap_approach")));
    }

    @Test
    void walledKeepRuntimePoolsUseSlotGraphAndSharedCorners() {
        MKStructureWorkspace workspace = withTopologyAndLinearRuns(
                baseWorkspace(List.of(new MKHorizontalOpeningProfile("wall_opening", 3, 3, true, true)), List.of()),
                MKWorkspaceTopologyProfile.walledKeep(false),
                List.of(
                        topologyFamily(
                                "keep_center_entry",
                                "keep.center.entry",
                                "keep.center",
                                false,
                                9,
                                9,
                                MKWorkspaceDimensions.defaultDimensions().entranceHeight(),
                                MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION,
                                List.of(),
                                0,
                                0,
                                MKWorkspaceFoundationPolicy.none(),
                                null
                        ),
                        topologyFamily(
                                "keep_gate_main",
                                MKWorkspaceTopologySlotMetadata.explicit("keep.gate.main", "entry", "room", false),
                                "keep.gate",
                                false,
                                7,
                                7,
                                5,
                                MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION,
                                List.of(),
                                0,
                                0,
                                MKWorkspaceFoundationPolicy.none(),
                                null
                        ),
                        topologyFamily(
                                "keep_corner_shared",
                                "keep.corner.shared",
                                "keep.corner",
                                false,
                                7,
                                7,
                                7,
                                MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION,
                                List.of(),
                                0,
                                0,
                                MKWorkspaceFoundationPolicy.none(),
                                null
                        )
                ),
                List.of(
                        new MKWorkspaceLinearRunFamilyDefinition(
                                "keep_walkway_south",
                                "keep.walkway.south",
                                MKWorkspaceLinearRunKind.OPEN_WALKWAY,
                                "wall_opening",
                                9,
                                3,
                                3,
                                0,
                                true,
                                true,
                                MKWorkspaceLinearRunProjection.RIGID,
                                List.of(MKWorkspaceLinearRunPieceShape.STRAIGHT),
                                MKWorkspaceFoundationPolicy.none(),
                                null
                        ),
                        new MKWorkspaceLinearRunFamilyDefinition(
                                "keep_wall_south",
                                "keep.perimeter.south",
                                MKWorkspaceLinearRunKind.SOLID_WALL,
                                "wall_opening",
                                13,
                                3,
                                5,
                                0,
                                true,
                                true,
                                MKWorkspaceLinearRunProjection.RIGID,
                                List.of(MKWorkspaceLinearRunPieceShape.STRAIGHT),
                                MKWorkspaceFoundationPolicy.none(),
                                null
                        )
                )
        );

        List<MKWorkspacePieceDefinition> exportedPieces = new MKWalledKeepWorkspacePlanner().createCanonicalPieces(workspace).stream()
                .map(piece -> pieceToDefinitionWithConnectors(workspace, piece))
                .toList();
        MKWorkspaceExportManifest manifest = MKWorkspaceExportManifest.fromWorkspace(workspace.withPieces(exportedPieces), 1, "now");

        assertEquals("keep_center_entry", manifest.runtimeHints().startBaseName());
        assertRuntimePoolContains(workspace, manifest, "keep_slots/keep/walkway/south", "keep_walkway_south");
        assertRuntimePoolContains(workspace, manifest, "keep_slots/keep/gate/main", "keep_gate_main");
        assertRuntimePoolContains(workspace, manifest, "keep_slots/keep/perimeter/south_west/0",
                "keep_wall_south_south_west_0");
        assertRuntimePoolContains(workspace, manifest, "keep_slots/keep/perimeter/south_east/0",
                "keep_wall_south_south_east_0");
        assertRuntimePoolContains(workspace, manifest, "keep_slots/keep/corner/north_west", "keep_corner_shared");
        assertRuntimePoolContains(workspace, manifest, "keep_slots/keep/corner/north_east", "keep_corner_shared");
        assertRuntimePoolContains(workspace, manifest, "keep_slots/keep/corner/south_east", "keep_corner_shared");
        assertRuntimePoolContains(workspace, manifest, "keep_slots/keep/corner/south_west", "keep_corner_shared");
        MKWorkspaceExportManifest.ExportRuntimeTemplateGroup cornerGroup = manifest.runtimeHints().templateGroups().stream()
                .filter(group -> group.baseName().equals("keep_corner_shared"))
                .findFirst()
                .orElseThrow();
        assertTrue(cornerGroup.pieceMetadata().allowOnBranchPath());
    }

    @Test
    void walledKeepImportMigrationAllowsCornerStackPiecesOnBranches() {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        MKStructureWorkspace keepWorkspace = withTopologyAndLinearRuns(
                baseWorkspace(MKHorizontalOpeningProfile.createDefaults(dimensions), List.of()),
                MKWorkspaceTopologyProfile.walledKeep(false),
                MKTowerWorkspaceFamilyDefinition.createWalledKeepDefaults(dimensions),
                MKWorkspaceLinearRunFamilyDefinition.createWalledKeepDefaults(dimensions, workspacePalette())
        );
        Map<String, String> staleCornerTags = Map.of(
                "workspace_topology_slot_id", "keep.corner.south_west.entry",
                MKWorkspaceRuntimePieceInfo.ALLOW_ON_BRANCH_PATH_TAG, "false"
        );

        Map<String, String> migrated = MKStructureWorkspaceImportService.migrateImportedRuntimeTags(
                keepWorkspace, staleCornerTags);
        Map<String, String> towerMigrated = MKStructureWorkspaceImportService.migrateImportedRuntimeTags(
                baseWorkspace(MKHorizontalOpeningProfile.createDefaults(dimensions), List.of()), staleCornerTags);

        assertEquals("true", migrated.get(MKWorkspaceRuntimePieceInfo.ALLOW_ON_BRANCH_PATH_TAG));
        assertEquals("false", towerMigrated.get(MKWorkspaceRuntimePieceInfo.ALLOW_ON_BRANCH_PATH_TAG));
    }

    @Test
    void perCornerUniqueModeUsesSharedOnlyForSharedCorners() {
        MKStructureWorkspace workspace = withTopologyAndLinearRuns(
                baseWorkspace(List.of(new MKHorizontalOpeningProfile("wall_opening", 3, 3, true, true)), List.of()),
                MKWorkspaceTopologyProfile.walledKeep(false, false, true, false),
                List.of(
                        topologyFamily(
                                "keep_center_entry",
                                "keep.center.entry",
                                "keep.center",
                                false,
                                9,
                                9,
                                MKWorkspaceDimensions.defaultDimensions().entranceHeight(),
                                MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION,
                                List.of(),
                                0,
                                0,
                                MKWorkspaceFoundationPolicy.none(),
                                null
                        ),
                        topologyFamily(
                                "keep_corner_shared",
                                "keep.corner.shared",
                                "keep.corner",
                                false,
                                7,
                                7,
                                7,
                                MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION,
                                List.of(),
                                0,
                                0,
                                MKWorkspaceFoundationPolicy.none(),
                                null
                        ),
                        topologyFamily(
                                "keep_corner_south_east",
                                "keep.corner.south_east",
                                "keep.corner.south_east",
                                false,
                                9,
                                9,
                                8,
                                MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION,
                                List.of(),
                                0,
                                0,
                                MKWorkspaceFoundationPolicy.none(),
                                null
                        )
                ),
                List.of()
        );

        List<MKWorkspacePieceDefinition> exportedPieces = new MKWalledKeepWorkspacePlanner().createCanonicalPieces(workspace).stream()
                .map(piece -> pieceToDefinitionWithConnectors(workspace, piece))
                .toList();
        MKWorkspaceExportManifest manifest = MKWorkspaceExportManifest.fromWorkspace(workspace.withPieces(exportedPieces), 1, "now");

        assertRuntimePoolContains(workspace, manifest, "keep_slots/keep/corner/north_west", "keep_corner_shared");
        assertRuntimePoolContains(workspace, manifest, "keep_slots/keep/corner/north_east", "keep_corner_shared");
        assertRuntimePoolContains(workspace, manifest, "keep_slots/keep/corner/south_east", "keep_corner_south_east");
        assertRuntimePoolContains(workspace, manifest, "keep_slots/keep/corner/south_west", "keep_corner_shared");
        assertRuntimePoolDoesNotContain(workspace, manifest, "keep_slots/keep/corner/south_east", "keep_corner_shared");
    }

    @Test
    void allUniqueCornersDoNotGenerateSharedCornerTemplate() {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        MKStructureWorkspace workspace = withTopologyAndLinearRuns(
                baseWorkspace(MKHorizontalOpeningProfile.createDefaults(dimensions), List.of()),
                MKWorkspaceTopologyProfile.walledKeep(true),
                List.of(
                        topologyFamily(
                                "keep_corner_shared",
                                "keep.corner.shared",
                                "keep.corner.shared",
                                true,
                                7,
                                7,
                                7,
                                MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION,
                                List.of(),
                                0,
                                0,
                                MKWorkspaceFoundationPolicy.none(),
                                null
                        ),
                        topologyFamily(
                                "keep_corner_north_west",
                                "keep.corner.north_west",
                                "keep.corner.north_west",
                                true,
                                9,
                                9,
                                9,
                                MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION,
                                List.of(),
                                0,
                                0,
                                MKWorkspaceFoundationPolicy.none(),
                                null
                        )
                ),
                List.of()
        );

        List<MKPlannedPiece> pieces = new MKWalledKeepWorkspacePlanner().createCanonicalPieces(workspace);

        assertFalse(pieces.stream().anyMatch(piece -> piece.pieceName().equals("keep_corner_shared")));
        assertTrue(pieces.stream().anyMatch(piece -> piece.pieceName().equals("keep_corner_north_west") &&
                piece.interiorHeight() == 9));
    }

    @Test
    void runtimeMetadataUsesFoundationPolicyFromOwningFamilies() {
        MKWorkspaceFoundationPolicy roomFoundation = MKWorkspaceFoundationPolicy.uniformBlock(
                ResourceLocation.parse("minecraft:stone_bricks"));
        MKWorkspaceFoundationPolicy runFoundation = MKWorkspaceFoundationPolicy.maskedExtendBottomBlocks(List.of(
                ResourceLocation.parse("minecraft:stone_bricks")));
        MKStructureWorkspace workspace = withTopologyAndLinearRuns(
                baseWorkspace(List.of(new MKHorizontalOpeningProfile("wall_opening", 3, 3, true, true)), List.of()),
                MKWorkspaceTopologyProfile.walledKeep(false),
                List.of(topologyFamily(
                        "keep_center_entry",
                        "keep.center.entry",
                        "keep.center",
                        true,
                        9,
                        9,
                        MKWorkspaceDimensions.defaultDimensions().entranceHeight(),
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION,
                        List.of(),
                        0,
                        0,
                        roomFoundation,
                        null
                )),
                List.of(new MKWorkspaceLinearRunFamilyDefinition(
                        "keep_wall_north",
                        "keep.perimeter.north",
                        MKWorkspaceLinearRunKind.SOLID_WALL,
                        "wall_opening",
                        11,
                        3,
                        5,
                        0,
                        true,
                        true,
                        MKWorkspaceLinearRunProjection.RIGID,
                        List.of(MKWorkspaceLinearRunPieceShape.STRAIGHT),
                        runFoundation,
                        null
                ))
        );
        List<MKWorkspacePieceDefinition> exportedPieces = new MKWalledKeepWorkspacePlanner().createCanonicalPieces(workspace).stream()
                .map(piece -> pieceToDefinitionWithConnectors(workspace, piece))
                .toList();
        MKWorkspaceExportManifest manifest = MKWorkspaceExportManifest.fromWorkspace(workspace.withPieces(exportedPieces), 1, "now");

        MKWorkspaceExportManifest.ExportRuntimeTemplateGroup roomGroup = manifest.runtimeHints().templateGroups().stream()
                .filter(group -> group.baseName().equals("keep_center_entry"))
                .findFirst()
                .orElseThrow();
        MKWorkspaceExportManifest.ExportRuntimeTemplateGroup runGroup = manifest.runtimeHints().templateGroups().stream()
                .filter(group -> group.baseName().equals("keep_wall_north_north_west_0"))
                .findFirst()
                .orElseThrow();

        assertEquals(MKWorkspaceFoundationMode.UNIFORM_STATE, roomGroup.pieceMetadata().foundationPolicy().mode());
        assertEquals(ResourceLocation.parse("minecraft:stone_bricks"),
                roomGroup.pieceMetadata().foundationPolicy().foundationBlock());
        assertEquals(MKWorkspaceFoundationMode.MASKED_EXTEND_BOTTOM_BLOCKS, runGroup.pieceMetadata().foundationPolicy().mode());
        assertEquals(List.of(ResourceLocation.parse("minecraft:stone_bricks")),
                runGroup.pieceMetadata().foundationPolicy().maskBlocks());
    }

    @Test
    void stackFoundationPolicyFlowsToResolvedRoomPieces() {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        MKWorkspaceFoundationPolicy stackFoundation = MKWorkspaceFoundationPolicy.uniformBlock(
                ResourceLocation.parse("minecraft:stone_bricks"));
        MKWorkspaceTopologyProfile topologyProfile = MKWorkspaceTopologyProfile.walledKeep(false)
                .withTowerStackSettings(MKWorkspaceTowerStackSettings.defaults("keep.center", 7)
                        .withFoundationPolicy(stackFoundation));
        MKStructureWorkspace workspace = withTopologyAndLinearRuns(
                baseWorkspace(MKHorizontalOpeningProfile.createDefaults(dimensions), List.of()),
                topologyProfile,
                MKTowerWorkspaceFamilyDefinition.createWalledKeepDefaults(dimensions),
                MKWorkspaceLinearRunFamilyDefinition.createWalledKeepDefaults(dimensions, workspacePalette())
        );

        MKPlannedPiece centerEntry = new MKWalledKeepWorkspacePlanner().createCanonicalPieces(workspace).stream()
                .filter(piece -> piece.pieceName().equals("keep_center_entry"))
                .findFirst()
                .orElseThrow();

        assertEquals(MKWorkspaceFoundationMode.UNIFORM_STATE.getSerializedName(),
                centerEntry.tags().get(MKWorkspaceFoundationPolicy.MODE_TAG));

        MKStructureWorkspace exportedWorkspace = workspace.withPieces(List.of(
                pieceToDefinitionWithConnectors(workspace, centerEntry)));
        MKWorkspaceExportManifest manifest = MKWorkspaceExportManifest.fromWorkspace(exportedWorkspace, 1, "now");
        MKWorkspaceExportManifest.ExportRuntimeTemplateGroup centerEntryGroup = manifest.runtimeHints().templateGroups().stream()
                .filter(group -> group.baseName().equals("keep_center_entry"))
                .findFirst()
                .orElseThrow();
        assertEquals(MKWorkspaceFoundationMode.UNIFORM_STATE,
                centerEntryGroup.pieceMetadata().foundationPolicy().mode());
    }

    @Test
    void familyFoundationOverrideBeatsStackFoundationDefault() {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        MKWorkspaceFoundationPolicy stackFoundation = MKWorkspaceFoundationPolicy.uniformBlock(
                ResourceLocation.parse("minecraft:stone_bricks"));
        MKWorkspaceFoundationPolicy familyFoundation = MKWorkspaceFoundationPolicy.maskedExtendBottomBlocks(List.of(
                ResourceLocation.parse("minecraft:oak_planks")));
        MKWorkspaceTopologyProfile topologyProfile = MKWorkspaceTopologyProfile.walledKeep(false)
                .withTowerStackSettings(MKWorkspaceTowerStackSettings.defaults("keep.center", 7)
                        .withFoundationPolicy(stackFoundation));
        List<MKTowerWorkspaceFamilyDefinition> families = MKTowerWorkspaceFamilyDefinition.createWalledKeepDefaults(dimensions)
                .stream()
                .map(family -> family.baseName().equals("keep_center_entry") ?
                        copyFamilyWithFoundation(family, familyFoundation) : family)
                .toList();
        MKStructureWorkspace workspace = withTopologyAndLinearRuns(
                baseWorkspace(MKHorizontalOpeningProfile.createDefaults(dimensions), List.of()),
                topologyProfile,
                families,
                MKWorkspaceLinearRunFamilyDefinition.createWalledKeepDefaults(dimensions, workspacePalette())
        );

        MKPlannedPiece centerEntry = new MKWalledKeepWorkspacePlanner().createCanonicalPieces(workspace).stream()
                .filter(piece -> piece.pieceName().equals("keep_center_entry"))
                .findFirst()
                .orElseThrow();

        assertEquals(MKWorkspaceFoundationMode.MASKED_EXTEND_BOTTOM_BLOCKS.getSerializedName(),
                centerEntry.tags().get(MKWorkspaceFoundationPolicy.MODE_TAG));
    }

    @Test
    void explicitFamilyFoundationNoneSuppressesStackFoundationDefault() {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        MKWorkspaceFoundationPolicy stackFoundation = MKWorkspaceFoundationPolicy.uniformBlock(
                ResourceLocation.parse("minecraft:stone_bricks"));
        MKWorkspaceTopologyProfile topologyProfile = MKWorkspaceTopologyProfile.walledKeep(false)
                .withTowerStackSettings(MKWorkspaceTowerStackSettings.defaults("keep.center", 7)
                        .withFoundationPolicy(stackFoundation));
        List<MKTowerWorkspaceFamilyDefinition> families = MKTowerWorkspaceFamilyDefinition.createWalledKeepDefaults(dimensions)
                .stream()
                .map(family -> family.baseName().equals("keep_center_entry") ?
                        copyFamilyWithFoundation(family, MKWorkspaceFoundationPolicy.none()) : family)
                .toList();
        MKStructureWorkspace workspace = withTopologyAndLinearRuns(
                baseWorkspace(MKHorizontalOpeningProfile.createDefaults(dimensions), List.of()),
                topologyProfile,
                families,
                MKWorkspaceLinearRunFamilyDefinition.createWalledKeepDefaults(dimensions, workspacePalette())
        );

        MKPlannedPiece centerEntry = new MKWalledKeepWorkspacePlanner().createCanonicalPieces(workspace).stream()
                .filter(piece -> piece.pieceName().equals("keep_center_entry"))
                .findFirst()
                .orElseThrow();

        assertFalse(centerEntry.tags().containsKey(MKWorkspaceFoundationPolicy.MODE_TAG));
    }

    @Test
    void stackPaletteDefaultsFlowToResolvedRoomPieces() {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        ResourceLocation stackWallBlock = ResourceLocation.parse("minecraft:polished_blackstone_bricks");
        MKWorkspaceTopologyProfile topologyProfile = MKWorkspaceTopologyProfile.walledKeep(false)
                .withTowerStackSettings(MKWorkspaceTowerStackSettings.defaults("keep.center", 7)
                        .withPaletteOverride(Optional.of(MKWorkspacePaletteOverride.EMPTY.withWallBlock(stackWallBlock))));
        MKStructureWorkspace workspace = withTopologyAndLinearRuns(
                baseWorkspace(MKHorizontalOpeningProfile.createDefaults(dimensions), List.of()),
                topologyProfile,
                MKTowerWorkspaceFamilyDefinition.createWalledKeepDefaults(dimensions),
                MKWorkspaceLinearRunFamilyDefinition.createWalledKeepDefaults(dimensions, workspacePalette())
        );

        MKPlannedPiece centerEntry = new MKWalledKeepWorkspacePlanner().createCanonicalPieces(workspace).stream()
                .filter(piece -> piece.pieceName().equals("keep_center_entry"))
                .findFirst()
                .orElseThrow();

        assertEquals(stackWallBlock.toString(), centerEntry.tags().get(MKWorkspacePaletteTags.WALL_BLOCK_TAG));
    }

    @Test
    void exportTopologyPathDepthUsesTopologyPathSettings() {
        MKWorkspaceTopologyProfile topologyProfile = MKWorkspaceTopologyProfile.tower()
                .withPathSettings(new MKWorkspaceTopologyPathSettings("main", 3, 5, 7));
        MKStructureWorkspace workspace = withTopologyAndLinearRuns(
                baseWorkspace(MKHorizontalOpeningProfile.createDefaults(MKWorkspaceDimensions.defaultDimensions()),
                        List.of()),
                topologyProfile,
                List.of(),
                List.of()
        );

        MKWorkspaceExportManifest manifest = MKWorkspaceExportManifest.snapshotFromWorkspace(workspace, 1, "now");
        MKWorkspaceTopologyPathSettings mainPath =
                manifest.settings().topologyProfile().pathSettingsOrDefault("main");

        assertEquals(3, mainPath.minMainPathPieces());
        assertEquals(5, mainPath.maxMainPathPieces());
        assertEquals(7, mainPath.maxBranchPiecesBeforeCap());
    }

    @Test
    void walledKeepPlannerUsesAssignedPerimeterRunSlotsOnly() {
        MKStructureWorkspace workspace = withTopologyAndLinearRuns(
                baseWorkspace(List.of(new MKHorizontalOpeningProfile("wall_opening", 3, 3, true, true)), List.of()),
                MKWorkspaceTopologyProfile.walledKeep(false),
                List.of(topologyFamily(
                        "keep_center_entry",
                        "keep.center.entry",
                        "keep.center",
                        true,
                        9,
                        9,
                        7,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION,
                        List.of(),
                        0,
                        0,
                        MKWorkspaceFoundationPolicy.none(),
                        null
                )),
                List.of(
                        new MKWorkspaceLinearRunFamilyDefinition(
                                "keep_wall_north",
                                "keep.wall.north",
                                MKWorkspaceLinearRunKind.SOLID_WALL,
                                "wall_opening",
                                11,
                                3,
                                7,
                                0,
                                false,
                                true,
                                MKWorkspaceLinearRunProjection.RIGID,
                                List.of(MKWorkspaceLinearRunPieceShape.STRAIGHT),
                                MKWorkspaceFoundationPolicy.none(),
                                null
                        ),
                        new MKWorkspaceLinearRunFamilyDefinition(
                                "keep_parapet_north",
                                "keep.parapet.north",
                                MKWorkspaceLinearRunKind.PARAPET,
                                "wall_opening",
                                11,
                                3,
                                7,
                                0,
                                false,
                                true,
                                MKWorkspaceLinearRunProjection.RIGID,
                                List.of(MKWorkspaceLinearRunPieceShape.STRAIGHT),
                                MKWorkspaceFoundationPolicy.none(),
                                null
                        )
                )
        );

        List<MKPlannedPiece> pieces = new MKWalledKeepWorkspacePlanner().createCanonicalPieces(workspace);

        assertFalse(pieces.stream().anyMatch(piece -> piece.pieceName().equals("keep_wall_north")));
        assertFalse(pieces.stream().anyMatch(piece -> piece.pieceName().equals("keep_parapet_north")));
    }

    @Test
    void plannerMapsMainEntryAndMainExitToDistinctConnectorRoles() {
        MKStructureWorkspace workspace = baseWorkspace(
                List.of(
                        new MKHorizontalOpeningProfile("entry_main", 3, 3, true, false),
                        new MKHorizontalOpeningProfile("main_branch", 3, 3, false, true)
                ),
                List.of()
        );
        MKTowerWorkspaceFamilyDefinition entryFamily = workspace.familyDefinitions().stream()
                .filter(family -> family.baseName().equals("entry"))
                .findFirst()
                .orElseThrow();
        MKTowerWorkspaceFamilyDefinition updatedEntry = MKTowerWorkspaceFamilyDefinition.forTopologySlot(
                entryFamily.baseName(),
                entryFamily.slotMetadata(),
                entryFamily.verticalAccessGroupId(),
                entryFamily.supportsVerticalAccess(),
                entryFamily.roomWidth(),
                entryFamily.roomLength(),
                entryFamily.roomHeight(),
                entryFamily.horizontalExtrusionMode(),
                List.of(
                        new MKWorkspaceFamilyHorizontalExitDefinition(net.minecraft.core.Direction.NORTH,
                                MKWorkspaceHorizontalExitPathKind.MAIN_ENTRY, "entry_main"),
                        new MKWorkspaceFamilyHorizontalExitDefinition(net.minecraft.core.Direction.SOUTH,
                                MKWorkspaceHorizontalExitPathKind.MAIN_EXIT, "entry_main")
                ),
                entryFamily.topVoidMargin(),
                entryFamily.bottomVoidMargin(),
                entryFamily.paletteOverride()
        );
        workspace = new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                workspace.topologyProfile(),
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                workspace.familyDefinitions().stream()
                        .map(family -> family.baseName().equals("entry") ? updatedEntry : family)
                        .toList(),
                workspace.openingProfiles(),
                workspace.linearRunFamilies(),
                workspace.createdAt(),
                workspace.updatedAt(),
                workspace.pieces()
        );

        MKPlannedPiece entry = new MKTowerWorkspacePlanner().createCanonicalPieces(workspace).stream()
                .filter(piece -> piece.pieceName().equals("entry"))
                .findFirst()
                .orElseThrow();

        assertTrue(entry.connectors().stream().anyMatch(connector ->
                connector.role() == MKConnectorRole.MAIN_FORWARD &&
                        connector.facing() == net.minecraft.core.Direction.NORTH));
        assertTrue(entry.connectors().stream().anyMatch(connector ->
                        connector.role() == MKConnectorRole.MAIN_BACK &&
                        connector.facing() == net.minecraft.core.Direction.SOUTH));
    }

    @Test
    void familyDefinitionCodecRoundTripPreservesVoidMargins() {
        MKTowerWorkspaceFamilyDefinition family = topologyFamily(
                "side_room",
                "tower.primary.top_cap",
                "tower.primary",
                false,
                9,
                9,
                7,
                MKWorkspaceHorizontalExtrusionMode.FULL_BODY,
                List.of(),
                4,
                0,
                null,
                null
        );

        MKTowerWorkspaceFamilyDefinition decoded = MKTowerWorkspaceFamilyDefinition.fromTag(family.toTag());

        assertEquals(4, decoded.topVoidMargin());
        assertEquals(0, decoded.bottomVoidMargin());
        assertEquals(7, decoded.roomHeight());
    }

    @Test
    void plannerTagsOnlyNonShaftPiecesWithVoidMargins() {
        MKStructureWorkspace workspace = baseWorkspace(
                List.of(
                        new MKHorizontalOpeningProfile("entry_main", 3, 3, true, false),
                        new MKHorizontalOpeningProfile("main_branch", 3, 3, false, true)
                ),
                List.of()
        );
        List<MKTowerWorkspaceFamilyDefinition> families = new java.util.ArrayList<>(workspace.familyDefinitions());
        families.add(topologyFamily("main_side_room", "tower.primary.main_floor", "tower.primary",
                false, 9, 9, workspace.dimensions().roomHeight(),
                MKWorkspaceHorizontalExtrusionMode.FULL_BODY, List.of(), 2, 1, null, null));
        workspace = new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                workspace.topologyProfile(),
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                families,
                workspace.openingProfiles(),
                workspace.linearRunFamilies(),
                workspace.createdAt(),
                workspace.updatedAt(),
                workspace.pieces()
        );

        List<MKPlannedPiece> pieces = new MKTowerWorkspacePlanner().createCanonicalPieces(workspace);

        MKPlannedPiece topCap = pieces.stream().filter(piece -> piece.pieceName().equals("top_cap"))
                .findFirst().orElseThrow();
        MKPlannedPiece basementCap = pieces.stream().filter(piece -> piece.pieceName().equals("basement_cap"))
                .findFirst().orElseThrow();
        MKPlannedPiece floor = pieces.stream().filter(piece -> piece.pieceName().equals("floor_main"))
                .findFirst().orElseThrow();
        MKPlannedPiece sideRoom = pieces.stream().filter(piece -> piece.pieceName().equals("main_side_room"))
                .findFirst().orElseThrow();
        assertFalse(topCap.tags().containsKey(MKWorkspaceVoidMarginTags.TOP_VOID_MARGIN_TAG));
        assertFalse(basementCap.tags().containsKey(MKWorkspaceVoidMarginTags.BOTTOM_VOID_MARGIN_TAG));
        assertFalse(floor.tags().containsKey(MKWorkspaceVoidMarginTags.TOP_VOID_MARGIN_TAG));
        assertFalse(floor.tags().containsKey(MKWorkspaceVoidMarginTags.BOTTOM_VOID_MARGIN_TAG));
        assertEquals("2", sideRoom.tags().get(MKWorkspaceVoidMarginTags.TOP_VOID_MARGIN_TAG));
        assertEquals("1", sideRoom.tags().get(MKWorkspaceVoidMarginTags.BOTTOM_VOID_MARGIN_TAG));
    }

    @Test
    void familyDefinitionCodecRoundTripPreservesSingleVerticalExit() {
        MKTowerWorkspaceFamilyDefinition family = topologyFamily(
                "top_only",
                "tower.primary.main_floor",
                "tower.primary",
                false,
                9,
                9,
                MKWorkspaceDimensions.defaultDimensions().roomHeight(),
                MKWorkspaceHorizontalExtrusionMode.TUNNEL_ONLY,
                List.of(MKWorkspaceFamilyHorizontalExitDefinition.verticalAccess(net.minecraft.core.Direction.UP)),
                0,
                0,
                null,
                null
        );

        MKTowerWorkspaceFamilyDefinition decoded = MKTowerWorkspaceFamilyDefinition.fromTag(family.toTag());

        assertTrue(decoded.supportsVerticalAccess());
        assertTrue(decoded.hasVerticalAccess(net.minecraft.core.Direction.UP));
        assertFalse(decoded.hasVerticalAccess(net.minecraft.core.Direction.DOWN));
    }

    @Test
    void plannerUsesDeclaredTopAndBottomVerticalExits() {
        MKStructureWorkspace workspace = baseWorkspace(
                List.of(
                        new MKHorizontalOpeningProfile("entry_main", 3, 3, true, false),
                        new MKHorizontalOpeningProfile("main_branch", 3, 3, false, true)
                ),
                List.of()
        );
        MKTowerWorkspaceFamilyDefinition topOnlyMain = topologyFamily(
                "floor_main",
                "tower.primary.main_floor",
                "tower.primary",
                false,
                9,
                9,
                workspace.dimensions().roomHeight(),
                MKWorkspaceHorizontalExtrusionMode.TUNNEL_ONLY,
                List.of(
                        new MKWorkspaceFamilyHorizontalExitDefinition(net.minecraft.core.Direction.NORTH,
                                MKWorkspaceHorizontalExitPathKind.BRANCH, "main_branch"),
                        MKWorkspaceFamilyHorizontalExitDefinition.verticalAccess(net.minecraft.core.Direction.UP)
                ),
                0,
                0,
                null,
                null
        );
        workspace = withFamilyDefinitions(workspace, workspace.familyDefinitions().stream()
                .map(family -> family.baseName().equals("floor_main") ? topOnlyMain : family)
                .toList());

        MKPlannedPiece floor = new MKTowerWorkspacePlanner().createCanonicalPieces(workspace).stream()
                .filter(piece -> piece.pieceName().equals("floor_main"))
                .findFirst()
                .orElseThrow();

        assertTrue(floor.connectors().stream().anyMatch(connector ->
                connector.facing() == net.minecraft.core.Direction.UP &&
                        connector.role() == MKConnectorRole.CONNECT_UP));
        assertFalse(floor.connectors().stream().anyMatch(connector ->
                connector.facing() == net.minecraft.core.Direction.DOWN));
        assertEquals("up", floor.tags().get(MKWorkspaceVerticalAccessTags.DIRECTION_TAG));
    }

    @Test
    void defaultTowerMainEntranceIsOpeningOnly() {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        MKTowerWorkspaceFamilyDefinition entryFamily = MKTowerWorkspaceFamilyDefinition.createDefaults(dimensions).stream()
                .filter(family -> family.topologySlotId().endsWith(".entry"))
                .findFirst()
                .orElseThrow();
        MKWorkspaceFamilyHorizontalExitDefinition entrance = entryFamily.horizontalExits().getFirst();

        assertEquals(MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION, entryFamily.horizontalExtrusionMode());
        assertEquals(MKWorkspaceHorizontalExitPathKind.MAIN_ENTRY, entrance.pathKind());
        assertEquals(MKWorkspaceHorizontalExitConnectionMode.NO_CONNECTION, entrance.connectionMode());

        MKStructureWorkspace workspace = new MKStructureWorkspace(
                UUID.randomUUID(),
                BlockPos.ZERO,
                "mkdev",
                "default_entrance",
                dimensions,
                workspacePalette(),
                MKWorkspaceStairAuthoringConfig.defaultConfig(),
                MKVerticalAccessPlacement.CENTER,
                1,
                2,
                4,
                MKWorkspaceVerticalAccessSpec.defaultSpec(),
                MKTowerWorkspaceFamilyDefinition.createDefaults(dimensions),
                MKHorizontalOpeningProfile.createDefaults(dimensions),
                MKWorkspaceLinearRunFamilyDefinition.createDefaults(dimensions, workspacePalette()),
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                List.of()
        );

        MKPlannedConnector plannedEntrance = new MKTowerWorkspacePlanner().createCanonicalPieces(workspace).stream()
                .filter(piece -> piece.pieceName().equals("entry"))
                .findFirst()
                .orElseThrow()
                .connectors()
                .stream()
                .filter(connector -> connector.role() == MKConnectorRole.MAIN_FORWARD)
                .findFirst()
                .orElseThrow();

        assertFalse(plannedEntrance.placesJigsaw());
        assertEquals(net.minecraft.core.Direction.SOUTH, plannedEntrance.facing());
    }

    @Test
    void mainEndingEntryPlansIncomingEndingPoolAndExportsMetadata() {
        MKStructureWorkspace workspace = baseWorkspace(
                List.of(
                        new MKHorizontalOpeningProfile("entry_main", 3, 3, true, false),
                        new MKHorizontalOpeningProfile("main_branch", 3, 3, false, true)
                ),
                List.of()
        );
        MKTowerWorkspaceFamilyDefinition endingFamily = topologyFamily(
                "main_end",
                "tower.primary.main_floor",
                "tower.primary",
                false,
                9,
                9,
                5,
                MKWorkspaceHorizontalExtrusionMode.TUNNEL_ONLY,
                List.of(new MKWorkspaceFamilyHorizontalExitDefinition(net.minecraft.core.Direction.NORTH,
                        MKWorkspaceHorizontalExitPathKind.MAIN_ENDING_ENTRY, "entry_main")),
                0,
                0,
                null,
                null
        );
        workspace = new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                List.of(endingFamily),
                workspace.openingProfiles(),
                workspace.linearRunFamilies(),
                workspace.createdAt(),
                workspace.updatedAt(),
                List.of()
        );

        MKPlannedPiece plannedEnding = new MKTowerWorkspacePlanner().createCanonicalPieces(workspace).stream()
                .filter(piece -> piece.pieceName().equals("main_end"))
                .findFirst()
                .orElseThrow();
        MKPlannedConnector endingConnector = plannedEnding.connectors().stream()
                .filter(connector -> connector.role() == MKConnectorRole.MAIN_FORWARD)
                .findFirst()
                .orElseThrow();

        assertEquals("main_endings/main", endingConnector.incomingPoolName());
        assertEquals("minecraft:empty", endingConnector.targetPoolName());
        assertEquals("true", plannedEnding.tags().get(MKWorkspaceRuntimePieceInfo.MAIN_PATH_ENDING_TAG));
        assertEquals("main", plannedEnding.tags().get(MKWorkspaceRuntimePieceInfo.TOPOLOGY_GROUP_TAG));
    }

    @Test
    void branchCapEntryPlansIncomingCapPoolAndExportsMetadata() {
        MKStructureWorkspace workspace = baseWorkspace(
                List.of(
                        new MKHorizontalOpeningProfile("entry_main", 3, 3, true, false),
                        new MKHorizontalOpeningProfile("main_branch", 3, 3, false, true)
                ),
                List.of()
        );
        MKTowerWorkspaceFamilyDefinition branchCapFamily = topologyFamily(
                "branch_cap",
                "tower.primary.main_floor",
                "tower.primary",
                false,
                9,
                9,
                5,
                MKWorkspaceHorizontalExtrusionMode.TUNNEL_ONLY,
                List.of(new MKWorkspaceFamilyHorizontalExitDefinition(net.minecraft.core.Direction.NORTH,
                        MKWorkspaceHorizontalExitPathKind.BRANCH_CAP_ENTRY, "main_branch")),
                0,
                0,
                null,
                null
        );
        workspace = new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                java.util.stream.Stream.concat(workspace.familyDefinitions().stream(), java.util.stream.Stream.of(branchCapFamily))
                        .toList(),
                workspace.openingProfiles(),
                workspace.linearRunFamilies(),
                workspace.createdAt(),
                workspace.updatedAt(),
                List.of()
        );

        List<MKPlannedPiece> pieces = new MKTowerWorkspacePlanner().createCanonicalPieces(workspace);
        MKPlannedPiece plannedCap = pieces.stream()
                .filter(piece -> piece.pieceName().equals("branch_cap"))
                .findFirst()
                .orElseThrow();
        MKPlannedConnector capConnector = plannedCap.connectors().stream()
                .filter(connector -> connector.role() == MKConnectorRole.BRANCH)
                .findFirst()
                .orElseThrow();

        assertEquals("branch_caps/main_branch", capConnector.incomingPoolName());
        assertEquals("minecraft:empty", capConnector.targetPoolName());
        assertEquals("true", plannedCap.tags().get(MKWorkspaceRuntimePieceInfo.BRANCH_CAP_TAG));
        assertEquals("true", plannedCap.tags().get(MKWorkspaceRuntimePieceInfo.TERMINAL_TAG));
        assertEquals("false", plannedCap.tags().get(MKWorkspaceRuntimePieceInfo.ALLOW_ON_MAIN_PATH_TAG));
        assertEquals("true", plannedCap.tags().get(MKWorkspaceRuntimePieceInfo.ALLOW_ON_BRANCH_PATH_TAG));

        MKStructureWorkspace branchCapWorkspace = workspace;
        MKStructureWorkspace exportedWorkspace = branchCapWorkspace.withPieces(pieces.stream()
                .map(piece -> pieceToDefinitionWithConnectors(branchCapWorkspace, piece))
                .toList());
        MKWorkspaceExportManifest manifest = MKWorkspaceExportManifest.fromWorkspace(exportedWorkspace, 4, "test");
        MKWorkspaceExportManifest.ExportRuntimeTemplateGroup templateGroup = manifest.runtimeHints().templateGroups().stream()
                .filter(runtimeGroup -> runtimeGroup.baseName().equals("branch_cap"))
                .findFirst()
                .orElseThrow();
        MKWorkspaceExportManifest.ExportRuntimePool capPool = manifest.runtimeHints().pools().stream()
                .filter(pool -> pool.poolId().equals(ResourceLocation.parse("mkdev:planner_test/branch_caps/main_branch")))
                .findFirst()
                .orElseThrow();

        assertTrue(templateGroup.pieceMetadata().branchCap());
        assertEquals(List.of("branch_cap"), capPool.childBaseNames());
    }

    @Test
    void workspaceRejectsMainEndingEntryWithMainExit() {
        MKStructureWorkspace workspace = baseWorkspace(
                List.of(
                        new MKHorizontalOpeningProfile("entry_main", 3, 3, true, false),
                        new MKHorizontalOpeningProfile("main_branch", 3, 3, false, true)
                ),
                List.of()
        );
        MKTowerWorkspaceFamilyDefinition invalidEnding = topologyFamily(
                "invalid_end",
                "tower.primary.main_floor",
                "tower.primary",
                false,
                9,
                9,
                5,
                MKWorkspaceHorizontalExtrusionMode.TUNNEL_ONLY,
                List.of(
                        new MKWorkspaceFamilyHorizontalExitDefinition(net.minecraft.core.Direction.NORTH,
                                MKWorkspaceHorizontalExitPathKind.MAIN_ENDING_ENTRY, "entry_main"),
                        new MKWorkspaceFamilyHorizontalExitDefinition(net.minecraft.core.Direction.SOUTH,
                                MKWorkspaceHorizontalExitPathKind.MAIN_EXIT, "entry_main")
                ),
                0,
                0,
                null,
                null
        );
        workspace = new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                List.of(invalidEnding),
                workspace.openingProfiles(),
                workspace.linearRunFamilies(),
                workspace.createdAt(),
                workspace.updatedAt(),
                List.of()
        );

        assertTrue(workspace.validate().stream()
                .anyMatch(error -> error.contains("cannot define a main ending entry and a main exit")));
    }

    @Test
    void workspaceRejectsInvalidBranchCapEntryShapes() {
        MKStructureWorkspace workspace = baseWorkspace(
                List.of(
                        new MKHorizontalOpeningProfile("entry_main", 3, 3, true, false),
                        new MKHorizontalOpeningProfile("main_branch", 3, 3, false, true)
                ),
                List.of()
        );
        MKTowerWorkspaceFamilyDefinition invalidCap = topologyFamily(
                "invalid_cap",
                "tower.primary.main_floor",
                "tower.primary",
                false,
                9,
                9,
                5,
                MKWorkspaceHorizontalExtrusionMode.TUNNEL_ONLY,
                List.of(
                        new MKWorkspaceFamilyHorizontalExitDefinition(net.minecraft.core.Direction.NORTH,
                                MKWorkspaceHorizontalExitPathKind.BRANCH_CAP_ENTRY, "main_branch",
                                MKWorkspaceHorizontalExitConnectionMode.NO_CONNECTION),
                        new MKWorkspaceFamilyHorizontalExitDefinition(net.minecraft.core.Direction.SOUTH,
                                MKWorkspaceHorizontalExitPathKind.MAIN_EXIT, "entry_main",
                                MKWorkspaceHorizontalExitConnectionMode.DIRECT_ROOM)
                ),
                0,
                0,
                null,
                null
        );
        workspace = new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                List.of(invalidCap),
                workspace.openingProfiles(),
                workspace.linearRunFamilies(),
                workspace.createdAt(),
                workspace.updatedAt(),
                List.of()
        );

        List<String> errors = workspace.validate();
        assertTrue(errors.stream().anyMatch(error -> error.contains("branch_cap_entry must place a connector")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("cannot define a branch cap entry and a main path exit")));
    }

    @Test
    void layoutControllerSwitchesFromContinuationToEndingAtTopologyGroupTarget() {
        ResourceLocation endingPool = ResourceLocation.fromNamespaceAndPath("mknpc", "test/main_endings/main");
        MKDungeonLayoutController controller = new MKDungeonLayoutController(new MKDungeonLayoutSettings(
                1,
                3,
                1,
                4,
                0,
                true,
                MKVerticalProgressionMode.MIXED,
                true,
                false,
                List.of(new MKDungeonTopologyGroupRule("main", 2, 2, true, endingPool)),
                connectorSettings()
        ));
        MKDungeonPieceState beforeTarget = new MKDungeonPieceState(0, 0, 1, 0, true, 3,
                "main", 1, 2);
        MKDungeonPieceState atTarget = new MKDungeonPieceState(0, 0, 2, 0, true, 3,
                "main", 2, 2);
        MKJigsawPieceMetadata continuation = new MKJigsawPieceMetadata(MKJigsawPieceRole.ROOM, 0, 0,
                true, false, false, false, "main", false);
        MKJigsawPieceMetadata ending = new MKJigsawPieceMetadata(MKJigsawPieceRole.ROOM, 0, 0,
                true, false, false, false, "main", true);
        var mainConnector = new com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorInfo(
                ResourceLocation.fromNamespaceAndPath("mknpc", "main_back"),
                ResourceLocation.fromNamespaceAndPath("mknpc", "main_forward"),
                net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.TEMPLATE_POOL,
                        ResourceLocation.fromNamespaceAndPath("mknpc", "test/main")),
                MKConnectorRole.MAIN_BACK
        );

        assertTrue(controller.getRejectionReason(beforeTarget, mainConnector, continuation).isEmpty());
        assertTrue(controller.getRejectionReason(beforeTarget, mainConnector, ending).isPresent());
        assertTrue(controller.getRejectionReason(atTarget, mainConnector, continuation).isPresent());
        assertTrue(controller.getRejectionReason(atTarget, mainConnector, ending).isEmpty());
        assertEquals(java.util.Optional.of(endingPool), controller.endingPoolForState(atTarget, mainConnector));
    }

    @Test
    void layoutControllerRequiresBranchCapAtTopologyGroupBranchLimitWhenCapsAvailable() {
        MKDungeonLayoutController controller = new MKDungeonLayoutController(new MKDungeonLayoutSettings(
                1,
                3,
                1,
                4,
                5,
                true,
                MKVerticalProgressionMode.MIXED,
                true,
                false,
                List.of(new MKDungeonTopologyGroupRule("main", 1, 2, 2, true, null)),
                connectorSettings()
        ));
        MKDungeonPieceState atBranchLimit = new MKDungeonPieceState(0, 0, 2, 2, false, 3,
                "main", 1, 2);
        MKJigsawPieceMetadata continuation = new MKJigsawPieceMetadata(MKJigsawPieceRole.ROOM, 0, 0,
                false, true, false, false, "main", false, false);
        MKJigsawPieceMetadata branchCap = new MKJigsawPieceMetadata(MKJigsawPieceRole.ROOM, 0, 0,
                false, true, true, false, "main", false, true);
        var branchConnector = new com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorInfo(
                ResourceLocation.fromNamespaceAndPath("mknpc", "branch"),
                ResourceLocation.fromNamespaceAndPath("mknpc", "branch"),
                net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.TEMPLATE_POOL,
                        ResourceLocation.fromNamespaceAndPath("mknpc", "test/rooms/branch/main_branch")),
                MKConnectorRole.BRANCH
        );

        assertTrue(controller.getRejectionReason(atBranchLimit, branchConnector, continuation, false).isEmpty());
        assertEquals(java.util.Optional.of("branch_cap_required"),
                controller.getRejectionReason(atBranchLimit, branchConnector, continuation, true));
        assertTrue(controller.getRejectionReason(atBranchLimit, branchConnector, branchCap, true).isEmpty());
    }

    @Test
    void topologyPathBranchCapLimitExportsToManifest() {
        MKStructureWorkspace workspace = baseWorkspace(
                List.of(
                        new MKHorizontalOpeningProfile("entry_main", 3, 3, true, false),
                        new MKHorizontalOpeningProfile("main_branch", 3, 3, false, true)
                ),
                List.of()
        );
        workspace = withTopologyAndLinearRuns(
                        workspace,
                        workspace.topologyProfile().withPathSettings(
                        workspace.topologyPathSettings("main")
                                .withMaxBranchPiecesBeforeCap(3)),
                List.of(),
                List.of()
        );

        MKStructureWorkspace exportSource = workspace;
        List<MKPlannedPiece> pieces = new MKTowerWorkspacePlanner().createCanonicalPieces(exportSource);
        MKStructureWorkspace exportWorkspace = exportSource.withPieces(pieces.stream()
                .map(piece -> pieceToDefinitionWithConnectors(exportSource, piece))
                .toList());
        MKWorkspaceExportManifest manifest = MKWorkspaceExportManifest.fromWorkspace(exportWorkspace, 4, "test");
        MKWorkspaceTopologyPathSettings mainPath =
                manifest.settings().topologyProfile().pathSettingsOrDefault("main");

        assertEquals(3, mainPath.maxBranchPiecesBeforeCap());
    }

    @Test
    void plannerTranslatesAuthoredExitOffsetsToConnectorOffsets() {
        MKStructureWorkspace workspace = baseWorkspace(
                List.of(
                        new MKHorizontalOpeningProfile("entry_main", 3, 3, true, false),
                        new MKHorizontalOpeningProfile("main_branch", 3, 3, false, true)
                ),
                List.of()
        );
        MKTowerWorkspaceFamilyDefinition updatedMain = topologyFamily(
                "floor_main",
                "tower.primary.main_floor",
                "tower.primary",
                true,
                9,
                9,
                workspace.dimensions().roomHeight(),
                MKWorkspaceHorizontalExtrusionMode.TUNNEL_ONLY,
                List.of(
                        new MKWorkspaceFamilyHorizontalExitDefinition(net.minecraft.core.Direction.NORTH,
                                MKWorkspaceHorizontalExitPathKind.BRANCH, "main_branch",
                                MKWorkspaceHorizontalExitConnectionMode.LINEAR_RUN, 2, 1),
                        new MKWorkspaceFamilyHorizontalExitDefinition(net.minecraft.core.Direction.SOUTH,
                                MKWorkspaceHorizontalExitPathKind.BRANCH, "main_branch",
                                MKWorkspaceHorizontalExitConnectionMode.LINEAR_RUN, 2, 1),
                        new MKWorkspaceFamilyHorizontalExitDefinition(net.minecraft.core.Direction.EAST,
                                MKWorkspaceHorizontalExitPathKind.BRANCH, "main_branch",
                                MKWorkspaceHorizontalExitConnectionMode.LINEAR_RUN, 2, 1),
                        new MKWorkspaceFamilyHorizontalExitDefinition(net.minecraft.core.Direction.WEST,
                                MKWorkspaceHorizontalExitPathKind.BRANCH, "main_branch",
                                MKWorkspaceHorizontalExitConnectionMode.LINEAR_RUN, 2, 1)
                ),
                0,
                0,
                null,
                null
        );
        workspace = new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                workspace.familyDefinitions().stream()
                        .map(family -> family.baseName().equals("floor_main") ? updatedMain : family)
                        .toList(),
                workspace.openingProfiles(),
                workspace.linearRunFamilies(),
                workspace.createdAt(),
                workspace.updatedAt(),
                workspace.pieces()
        );

        MKPlannedPiece mainPiece = new MKTowerWorkspacePlanner().createCanonicalPieces(workspace).stream()
                .filter(piece -> piece.pieceName().equals("floor_main"))
                .findFirst()
                .orElseThrow();

        assertEquals(2, branchConnector(mainPiece, net.minecraft.core.Direction.NORTH).lateralOffset());
        assertEquals(-2, branchConnector(mainPiece, net.minecraft.core.Direction.SOUTH).lateralOffset());
        assertEquals(2, branchConnector(mainPiece, net.minecraft.core.Direction.EAST).lateralOffset());
        assertEquals(-2, branchConnector(mainPiece, net.minecraft.core.Direction.WEST).lateralOffset());
        assertTrue(mainPiece.connectors().stream()
                .filter(connector -> connector.role() == MKConnectorRole.BRANCH)
                .allMatch(connector -> connector.verticalOffset() == 1));
    }

    @Test
    void towerStackFloorCountsDefaultCapApproachFlags() {
        assertTrue(MKWorkspaceTowerStackFloorCounts.DEFAULT_TOP_CAP_APPROACH_ENABLED);
        assertFalse(MKWorkspaceTowerStackFloorCounts.DEFAULT_BASEMENT_CAP_APPROACH_ENABLED);
    }

    @Test
    void floorCountsReflectOptionalCapApproachPieces() {
        MKTowerStackBudget budget = MKTowerStackBudget.fromDimensions(MKWorkspaceDimensions.defaultDimensions());

        List<Integer> mainWithApproach = MKWorkspaceTowerStackFloorCounts.allowedMainFloorCounts(budget,
                1, true, false);
        List<Integer> mainWithoutApproach = MKWorkspaceTowerStackFloorCounts.allowedMainFloorCounts(budget,
                1, false, false);
        List<Integer> basementWithoutApproach = MKWorkspaceTowerStackFloorCounts.allowedBasementFloorCounts(budget,
                1, true, false);
        List<Integer> basementWithApproach = MKWorkspaceTowerStackFloorCounts.allowedBasementFloorCounts(budget,
                1, true, true);

        assertTrue(mainWithoutApproach.getLast() >= mainWithApproach.getLast());
        assertTrue(basementWithApproach.getLast() <= basementWithoutApproach.getLast());
    }

    @Test
    void defaultWorkspaceAuthoringDataValidates() {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        MKWorkspaceMaterialPalette palette = workspacePalette();
        MKStructureWorkspace workspace = new MKStructureWorkspace(
                UUID.randomUUID(),
                BlockPos.ZERO,
                "mkdev",
                "default_validation",
                dimensions,
                palette,
                MKWorkspaceStairAuthoringConfig.defaultConfig(),
                MKVerticalAccessPlacement.CENTER,
                1,
                2,
                4,
                MKWorkspaceVerticalAccessSpec.defaultSpec(),
                MKTowerWorkspaceFamilyDefinition.createDefaults(dimensions),
                MKHorizontalOpeningProfile.createDefaults(dimensions),
                MKWorkspaceLinearRunFamilyDefinition.createDefaults(dimensions, palette),
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                List.of()
        );

        assertEquals(List.of(), workspace.validate());
    }

    @Test
    void plannerConnectsTopCapDirectlyWhenApproachDisabled() {
        MKStructureWorkspace workspace = withTowerStackFloorCounts(baseWorkspace(
                        List.of(
                                new MKHorizontalOpeningProfile("entry_main", 3, 3, true, false),
                                new MKHorizontalOpeningProfile("main_branch", 3, 3, false, true)
                        ),
                        List.of()),
                1, 1, false, false);

        List<MKPlannedPiece> pieces = new MKTowerWorkspacePlanner().createCanonicalPieces(workspace);
        MKPlannedPiece topCap = pieces.stream()
                .filter(piece -> piece.pieceName().equals("top_cap"))
                .findFirst()
                .orElseThrow();

        assertTrue(pieces.stream().noneMatch(piece -> piece.pieceName().equals("top_cap_approach")));
        assertTrue(topCap.connectors().stream().anyMatch(connector ->
                connector.role() == MKConnectorRole.CONNECT_DOWN &&
                        "connect_up".equals(connector.incomingPoolName())));
        assertEquals(MKJigsawPieceRole.TOP_CAP.getSerializedName(),
                topCap.tags().get(MKWorkspaceRuntimePieceInfo.ROLE_TAG));
        assertEquals("1", topCap.tags().get(MKWorkspaceRuntimePieceInfo.PROGRESSION_DELTA_TAG));
        assertEquals("1", topCap.tags().get(MKWorkspaceRuntimePieceInfo.VERTICAL_LEVEL_DELTA_TAG));
    }

    @Test
    void plannerConnectsBasementCapThroughApproachWhenEnabled() {
        MKStructureWorkspace workspace = withTowerStackFloorCounts(baseWorkspace(
                        List.of(
                                new MKHorizontalOpeningProfile("entry_main", 3, 3, true, false),
                                new MKHorizontalOpeningProfile("main_branch", 3, 3, false, true)
                        ),
                        List.of()),
                1, 1, true, true);

        List<MKPlannedPiece> pieces = new MKTowerWorkspacePlanner().createCanonicalPieces(workspace);
        MKPlannedPiece approach = pieces.stream()
                .filter(piece -> piece.pieceName().equals("basement_cap_approach"))
                .findFirst()
                .orElseThrow();
        MKPlannedPiece basementCap = pieces.stream()
                .filter(piece -> piece.pieceName().equals("basement_cap"))
                .findFirst()
                .orElseThrow();

        assertTrue(approach.connectors().stream().anyMatch(connector ->
                connector.role() == MKConnectorRole.CONNECT_UP &&
                        "connect_down".equals(connector.incomingPoolName())));
        assertTrue(approach.connectors().stream().anyMatch(connector ->
                connector.role() == MKConnectorRole.TOP_CAP_FORWARD &&
                        "bottom_cap".equals(connector.targetPoolName())));
        assertEquals(MKJigsawPieceRole.BASEMENT_CAP_APPROACH.getSerializedName(),
                approach.tags().get(MKWorkspaceRuntimePieceInfo.ROLE_TAG));
        assertTrue(basementCap.connectors().stream().anyMatch(connector ->
                connector.role() == MKConnectorRole.TOP_CAP_BACK &&
                        "bottom_cap".equals(connector.incomingPoolName())));
        assertEquals("0", basementCap.tags().get(MKWorkspaceRuntimePieceInfo.PROGRESSION_DELTA_TAG));
        assertEquals("0", basementCap.tags().get(MKWorkspaceRuntimePieceInfo.VERTICAL_LEVEL_DELTA_TAG));
    }

    @Test
    void plannerAndExportSupportDirectRoomExitPools() {
        MKStructureWorkspace workspace = baseWorkspace(
                List.of(
                        new MKHorizontalOpeningProfile("entry_main", 3, 3, true, false),
                        new MKHorizontalOpeningProfile("main_branch", 3, 3, false, true)
                ),
                List.of()
        );
        List<MKTowerWorkspaceFamilyDefinition> updatedFamilies = workspace.familyDefinitions().stream()
                .map(family -> {
                    if (family.baseName().equals("entry")) {
                        return MKTowerWorkspaceFamilyDefinition.forTopologySlot(
                                family.baseName(),
                                family.slotMetadata(),
                                family.verticalAccessGroupId(),
                                family.supportsVerticalAccess(),
                                family.roomWidth(),
                                family.roomLength(),
                                family.roomHeight(),
                                family.horizontalExtrusionMode(),
                                List.of(new MKWorkspaceFamilyHorizontalExitDefinition(
                                        net.minecraft.core.Direction.SOUTH,
                                        MKWorkspaceHorizontalExitPathKind.MAIN_EXIT,
                                        "entry_main",
                                        MKWorkspaceHorizontalExitConnectionMode.DIRECT_ROOM
                                )),
                                0,
                                0,
                                family.paletteOverride()
                        );
                    }
                    if (family.baseName().equals("floor_main")) {
                        return MKTowerWorkspaceFamilyDefinition.forTopologySlot(
                                family.baseName(),
                                family.slotMetadata(),
                                family.verticalAccessGroupId(),
                                family.supportsVerticalAccess(),
                                family.roomWidth(),
                                family.roomLength(),
                                family.roomHeight(),
                                family.horizontalExtrusionMode(),
                                List.of(new MKWorkspaceFamilyHorizontalExitDefinition(
                                        net.minecraft.core.Direction.NORTH,
                                        MKWorkspaceHorizontalExitPathKind.MAIN_ENTRY,
                                        "entry_main",
                                        MKWorkspaceHorizontalExitConnectionMode.DIRECT_ROOM
                                )),
                                0,
                                0,
                                family.paletteOverride()
                        );
                    }
                    return family;
                })
                .toList();
        workspace = new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                updatedFamilies,
                workspace.openingProfiles(),
                workspace.linearRunFamilies(),
                workspace.createdAt(),
                workspace.updatedAt(),
                workspace.pieces()
        );

        List<MKPlannedPiece> pieces = new MKTowerWorkspacePlanner().createCanonicalPieces(workspace);
        MKPlannedPiece entry = pieces.stream().filter(piece -> piece.pieceName().equals("entry")).findFirst().orElseThrow();
        MKPlannedPiece floorMain = pieces.stream().filter(piece -> piece.pieceName().equals("floor_main")).findFirst().orElseThrow();

        assertTrue(entry.connectors().stream().anyMatch(connector ->
                connector.role() == MKConnectorRole.MAIN_BACK &&
                        "rooms/main_forward/entry_main".equals(connector.targetPoolName())));
        assertTrue(floorMain.connectors().stream().anyMatch(connector ->
                connector.role() == MKConnectorRole.MAIN_FORWARD &&
                        "rooms/main_forward/entry_main".equals(connector.incomingPoolName())));

        MKStructureWorkspace directWorkspace = workspace;
        List<MKWorkspacePieceDefinition> exportedPieces = pieces.stream()
                .map(piece -> pieceToDefinitionWithConnectors(directWorkspace, piece))
                .toList();
        MKStructureWorkspace exportedWorkspace = directWorkspace.withPieces(exportedPieces);
        MKWorkspaceExportManifest manifest = MKWorkspaceExportManifest.fromWorkspace(exportedWorkspace, 4, "test");
        MKWorkspaceExportManifest.ExportRuntimePool directPool = manifest.runtimeHints().pools().stream()
                .filter(pool -> pool.poolId().equals(ResourceLocation.parse("mkdev:planner_test/rooms/main_forward/entry_main")))
                .findFirst()
                .orElseThrow();

        assertTrue(directPool.childBaseNames().contains("floor_main"));
    }

    @Test
    void branchOnlyFamiliesExportBranchOnlyRuntimeTags() {
        MKStructureWorkspace workspace = baseWorkspace(
                List.of(
                        new MKHorizontalOpeningProfile("entry_main", 3, 3, true, false),
                        new MKHorizontalOpeningProfile("main_branch", 3, 3, false, true)
                ),
                List.of()
        );
        MKTowerWorkspaceFamilyDefinition updatedMain = topologyFamily(
                "floor_main",
                "tower.primary.main_floor",
                "tower.primary",
                true,
                9,
                9,
                workspace.dimensions().roomHeight(),
                MKWorkspaceHorizontalExtrusionMode.TUNNEL_ONLY,
                List.of(new MKWorkspaceFamilyHorizontalExitDefinition(net.minecraft.core.Direction.NORTH,
                        MKWorkspaceHorizontalExitPathKind.BRANCH, "main_branch")),
                0,
                0,
                null,
                null
        );
        workspace = new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                workspace.familyDefinitions().stream()
                        .map(family -> family.baseName().equals("floor_main") ? updatedMain : family)
                        .toList(),
                workspace.openingProfiles(),
                workspace.linearRunFamilies(),
                workspace.createdAt(),
                workspace.updatedAt(),
                workspace.pieces()
        );

        MKPlannedPiece mainPiece = new MKTowerWorkspacePlanner().createCanonicalPieces(workspace).stream()
                .filter(piece -> piece.pieceName().equals("floor_main"))
                .findFirst()
                .orElseThrow();
        MKWorkspaceRuntimePieceInfo runtimeInfo = MKWorkspaceRuntimePieceInfo.fromTags(mainPiece.tags()).orElseThrow();

        assertTrue(!runtimeInfo.allowOnMainPath());
        assertTrue(runtimeInfo.allowOnBranchPath());
    }

    @Test
    void exportRuntimePoolsExcludeMainPathRoomsFromBranchPools() {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        MKStructureWorkspace workspace = new MKStructureWorkspace(
                UUID.randomUUID(),
                BlockPos.ZERO,
                "mkdev",
                "pool_filter_test",
                dimensions,
                workspacePalette(),
                MKWorkspaceStairAuthoringConfig.defaultConfig(),
                MKVerticalAccessPlacement.CENTER,
                1,
                2,
                4,
                MKWorkspaceVerticalAccessSpec.defaultSpec(),
                MKTowerWorkspaceFamilyDefinition.createDefaults(),
                MKHorizontalOpeningProfile.createDefaults(dimensions),
                List.of(),
                1L,
                2L,
                List.of(
                        pieceWithRuntimeAndIncomingPool("main_room", "main_room",
                                new MKWorkspaceRuntimePieceInfo(true,
                                        com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawPieceRole.ROOM,
                                        0, 0, true, false, false, false),
                                ResourceLocation.parse("mkdev:pool_filter_test/linear_runs/branch/branch_opening")),
                        pieceWithRuntimeAndIncomingPool("branch_room", "branch_room",
                                new MKWorkspaceRuntimePieceInfo(false,
                                        com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawPieceRole.ROOM,
                                        0, 0, false, true, false, false),
                                ResourceLocation.parse("mkdev:pool_filter_test/linear_runs/branch/branch_opening"))
                )
        );

        MKWorkspaceExportManifest manifest = MKWorkspaceExportManifest.fromWorkspace(workspace, 4, "test");
        MKWorkspaceExportManifest.ExportRuntimePool branchPool = manifest.runtimeHints().pools().stream()
                .filter(pool -> pool.poolId().equals(ResourceLocation.parse("mkdev:pool_filter_test/linear_runs/branch/branch_opening")))
                .findFirst()
                .orElseThrow();

        assertEquals(List.of("branch_room"), branchPool.childBaseNames());
    }

    @Test
    void validationRejectsLinearRunPathMismatch() {
        MKWorkspaceVerticalAccessSpec verticalAccessSpec = MKWorkspaceVerticalAccessSpec.defaultSpec();
        int bandCap = verticalAccessSpec.getBandCapForReusableHeight(
                MKWorkspaceDimensions.defaultDimensions().roomHeight());

        MKStructureWorkspace workspace = new MKStructureWorkspace(
                UUID.randomUUID(),
                BlockPos.ZERO,
                "mkdev",
                "validation_test",
                MKWorkspaceDimensions.defaultDimensions(),
                workspacePalette(),
                MKWorkspaceStairAuthoringConfig.defaultConfig(),
                MKVerticalAccessPlacement.CENTER,
                1,
                2,
                4,
                verticalAccessSpec,
                MKTowerWorkspaceFamilyDefinition.createDefaults(),
                List.of(new MKHorizontalOpeningProfile("branch_only", 3, 3, false, true)),
                List.of(new MKWorkspaceLinearRunFamilyDefinition("bad_linear_run",
                        MKWorkspaceLinearRunKind.ENCLOSED_CORRIDOR, "branch_only", 5, 3, bandCap, 1,
                        true, false, MKWorkspaceLinearRunProjection.RIGID,
                        List.of(MKWorkspaceLinearRunPieceShape.STRAIGHT), workspacePalette().floorBlock(),
                        workspacePalette().wallBlock(), workspacePalette().ceilingBlock())),
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                List.of()
        );

        List<String> errors = workspace.validate();
        assertTrue(errors.stream().anyMatch(error -> error.contains("branch-only")));
    }

    @Test
    void validationRequiresReferencedFamilyExitOpeningProfiles() {
        MKStructureWorkspace workspace = baseWorkspace(
                List.of(new MKHorizontalOpeningProfile("entry_main", 3, 3, true, false)),
                List.of()
        );

        List<String> errors = workspace.validate();
        assertTrue(errors.stream().anyMatch(error -> error.contains("floor_main")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("main_branch")));
    }

    @Test
    void stackBackedDefaultFamiliesInheritTopologyGeometry() {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        MKWorkspaceTopologyProfile topologyProfile = MKWorkspaceTopologyProfile.tower()
                .withTowerStackSettings(new MKWorkspaceTowerStackSettings("tower.primary", 2, 1, 9,
                        15, 17, 3, MKVerticalAccessPlacement.CENTER,
                        MKWorkspaceStairAuthoringConfig.defaultConfig(), true, false));
        MKStructureWorkspace workspace = withTopologyAndLinearRuns(
                baseWorkspace(MKHorizontalOpeningProfile.createDefaults(dimensions), List.of()),
                topologyProfile,
                MKTowerWorkspaceFamilyDefinition.createDefaults(dimensions),
                List.of()
        );

        MKTowerWorkspaceFamilyDefinition mainFloor = workspace.familyDefinitions().stream()
                .filter(family -> family.baseName().equals("floor_main"))
                .findFirst()
                .orElseThrow();

        assertTrue(mainFloor.roomWidthOverrideOpt().isEmpty());
        assertTrue(mainFloor.roomLengthOverrideOpt().isEmpty());
        assertTrue(mainFloor.roomHeightOverrideOpt().isEmpty());
        assertEquals(15, workspace.resolveFamilySettings(mainFloor).roomWidth());
        assertEquals(17, workspace.resolveFamilySettings(mainFloor).roomLength());
        assertEquals(9, workspace.resolveFamilySettings(mainFloor).roomHeight());
        assertEquals(List.of(), workspace.validate());
    }

    @Test
    void stackBackedFamilyGeometryOverridesResolvePerDimension() {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        MKWorkspaceTopologyProfile topologyProfile = MKWorkspaceTopologyProfile.tower()
                .withTowerStackSettings(new MKWorkspaceTowerStackSettings("tower.primary", 2, 1, 9,
                        15, 17, 3, MKVerticalAccessPlacement.CENTER,
                        MKWorkspaceStairAuthoringConfig.defaultConfig(), true, false));
        List<MKTowerWorkspaceFamilyDefinition> families = MKTowerWorkspaceFamilyDefinition.createDefaults(dimensions)
                .stream()
                .map(family -> family.baseName().equals("floor_main") ?
                        copyFamilyWithGeometry(family, 11, 0, 0) : family)
                .toList();
        MKStructureWorkspace workspace = withTopologyAndLinearRuns(
                baseWorkspace(MKHorizontalOpeningProfile.createDefaults(dimensions), List.of()),
                topologyProfile,
                families,
                List.of()
        );

        MKTowerWorkspaceFamilyDefinition mainFloor = workspace.familyDefinitions().stream()
                .filter(family -> family.baseName().equals("floor_main"))
                .findFirst()
                .orElseThrow();

        assertEquals(11, workspace.resolveFamilySettings(mainFloor).roomWidth());
        assertEquals(17, workspace.resolveFamilySettings(mainFloor).roomLength());
        assertEquals(9, workspace.resolveFamilySettings(mainFloor).roomHeight());
        assertEquals(List.of(), workspace.validate());
    }

    @Test
    void stackBackedValidationUsesTopologyStackSettings() {
        MKStructureWorkspace workspace = baseWorkspace(
                List.of(
                        new MKHorizontalOpeningProfile("entry_main", 3, 3, true, false),
                        new MKHorizontalOpeningProfile("main_branch", 3, 3, false, true)
                ),
                List.of(
                        new MKWorkspaceLinearRunFamilyDefinition("entry_run",
                                MKWorkspaceLinearRunKind.ENCLOSED_CORRIDOR, "entry_main", 5, 3, 3, 0,
                                true, false, MKWorkspaceLinearRunProjection.RIGID,
                                List.of(MKWorkspaceLinearRunPieceShape.STRAIGHT), workspacePalette().floorBlock(),
                                workspacePalette().wallBlock(), workspacePalette().ceilingBlock()),
                        new MKWorkspaceLinearRunFamilyDefinition("branch_run",
                                MKWorkspaceLinearRunKind.ENCLOSED_CORRIDOR, "main_branch", 5, 3, 3, 0,
                                false, true, MKWorkspaceLinearRunProjection.RIGID,
                                List.of(MKWorkspaceLinearRunPieceShape.STRAIGHT), workspacePalette().floorBlock(),
                                workspacePalette().wallBlock(), workspacePalette().ceilingBlock())
                )
        );
        workspace = new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                workspace.topologyProfile(),
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                workspace.familyDefinitions(),
                workspace.openingProfiles(),
                workspace.linearRunFamilies(),
                workspace.createdAt(),
                workspace.updatedAt(),
                workspace.pieces()
        );

        assertEquals(List.of(), workspace.validate());
    }

    @Test
    void validationAllowsTopologyBandsWithIndependentlyResolvedCanonicalProfiles() {
        MKStructureWorkspace workspace = baseWorkspace(
                List.of(
                        new MKHorizontalOpeningProfile("entry_main", 3, 3, true, false),
                        new MKHorizontalOpeningProfile("main_branch", 3, 3, false, true)
                ),
                List.of()
        );
        workspace = new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                workspace.topologyProfile(),
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                workspace.familyDefinitions(),
                workspace.openingProfiles(),
                workspace.linearRunFamilies(),
                workspace.createdAt(),
                workspace.updatedAt(),
                workspace.pieces()
        );

        List<String> errors = workspace.validate();
        assertFalse(errors.stream().anyMatch(error -> error.contains("top_cap full height must be one of")));
    }

    @Test
    void validationRejectsFloorCountsThatExceedTowerBudget() {
        MKStructureWorkspace workspace = baseWorkspace(
                List.of(
                        new MKHorizontalOpeningProfile("entry_main", 3, 3, true, false),
                        new MKHorizontalOpeningProfile("main_branch", 3, 3, false, true)
                ),
                List.of()
        );

        MKWorkspaceTopologyProfile topologyProfile = workspace.topologyProfile()
                .withTowerStackSettings(workspace.topologyProfile()
                        .towerStackSettings("tower.primary")
                        .orElseThrow()
                        .withMainFloors(11)
                        .withBasementFloors(11));
        workspace = new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                topologyProfile,
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                workspace.familyDefinitions(),
                workspace.openingProfiles(),
                workspace.linearRunFamilies(),
                workspace.createdAt(),
                workspace.updatedAt(),
                workspace.pieces()
        );

        List<String> errors = workspace.validate();
        assertTrue(errors.stream().anyMatch(error -> error.contains("main floor count must be one of")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("basement floor count must be one of")));
    }

    @Test
    void workspaceCodecRoundTripPreservesNestedWorkspaceData() {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        MKStructureWorkspace workspace = new MKStructureWorkspace(
                UUID.randomUUID(),
                new BlockPos(32, 80, 32),
                "mkdev",
                "codec_round_trip",
                dimensions,
                workspacePalette(),
                MKWorkspaceStairAuthoringConfig.defaultConfig(),
                MKVerticalAccessPlacement.CENTER,
                1,
                2,
                4,
                MKWorkspaceVerticalAccessSpec.defaultSpec(),
                MKTowerWorkspaceFamilyDefinition.createDefaults(),
                MKHorizontalOpeningProfile.createDefaults(dimensions),
                List.of(new MKWorkspaceLinearRunFamilyDefinition("surface", MKWorkspaceLinearRunKind.ENCLOSED_CORRIDOR,
                        "entry_main", 5, 3, 3, 0, true, false, MKWorkspaceLinearRunProjection.RIGID,
                        List.of(MKWorkspaceLinearRunPieceShape.STRAIGHT), workspacePalette().floorBlock(),
                        workspacePalette().wallBlock(), workspacePalette().ceilingBlock())),
                100L,
                200L,
                List.of(new MKWorkspacePieceDefinition(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "entry_template",
                        "tower.primary.entry",
                        2,
                        dimensions,
                        1,
                        List.of(new MKWorkspaceConnectorDefinition(
                                MKConnectorRole.MAIN_FORWARD,
                                net.minecraft.core.Direction.NORTH,
                                new BlockPos(0, 1, -4),
                                3,
                                3,
                                1,
                                2,
                                ResourceLocation.parse("mkdev:entry"),
                                ResourceLocation.parse("mkdev:linear_run"),
                                ResourceLocation.parse("mkdev:pool/target"),
                                ResourceLocation.parse("mkdev:pool/incoming")
                        )),
                        new BlockPos(40, 80, 40),
                        new BoundingBox(40, 80, 40, 48, 87, 48),
                        new BoundingBox(38, 78, 38, 50, 89, 50),
                        new BlockPos(41, 80, 41),
                        new BlockPos(42, 80, 42),
                        List.of(new BlockPos(43, 81, 43)),
                        List.of(new BlockPos(44, 82, 44)),
                        Map.of("workspace_base_name", "entry", "workspace_piece_kind", "template")
                ))
        );

        CompoundTag tag = workspace.toTag();
        MKStructureWorkspace decoded = MKStructureWorkspace.fromTag(tag);

        assertEquals(Tag.TAG_INT_ARRAY, tag.get("id").getId());
        assertEquals(workspace.id(), decoded.id());
        assertEquals(workspace.anchor(), decoded.anchor());
        assertEquals(workspace.verticalAccessSpec().shaftSize(), decoded.verticalAccessSpec().shaftSize());
        assertEquals(workspace.topologyProfile().towerStackSettings("tower.primary").orElseThrow().mainFloors(),
                decoded.topologyProfile().towerStackSettings("tower.primary").orElseThrow().mainFloors());
        assertEquals(workspace.topologyProfile().towerStackSettings("tower.primary").orElseThrow().basementFloors(),
                decoded.topologyProfile().towerStackSettings("tower.primary").orElseThrow().basementFloors());
        assertEquals(workspace.familyDefinitions().get(0).horizontalExtrusionMode(),
                decoded.familyDefinitions().get(0).horizontalExtrusionMode());
        assertEquals(workspace.familyDefinitions().get(0).horizontalExits(),
                decoded.familyDefinitions().get(0).horizontalExits());
        assertEquals(workspace.pieces().get(0).pieceId(), decoded.pieces().get(0).pieceId());
        assertEquals(workspace.pieces().get(0).connectors().get(0).incomingPool(),
                decoded.pieces().get(0).connectors().get(0).incomingPool());
        assertEquals(workspace.pieces().get(0).tags(), decoded.pieces().get(0).tags());
    }

    @Test
    void backupSnapshotManifestAllowsTemplateOnlyWorkspaces() {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        MKStructureWorkspace workspace = new MKStructureWorkspace(
                UUID.randomUUID(),
                new BlockPos(32, 80, 32),
                "mkdev",
                "template_only_backup",
                dimensions,
                workspacePalette(),
                MKWorkspaceStairAuthoringConfig.defaultConfig(),
                MKVerticalAccessPlacement.CENTER,
                1,
                2,
                4,
                MKWorkspaceVerticalAccessSpec.defaultSpec(),
                MKTowerWorkspaceFamilyDefinition.createDefaults(),
                MKHorizontalOpeningProfile.createDefaults(dimensions),
                List.of(),
                100L,
                200L,
                List.of(new MKWorkspacePieceDefinition(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "entry_template",
                        "tower.primary.entry",
                        0,
                        dimensions,
                        1,
                        List.of(),
                        new BlockPos(40, 80, 40),
                        new BoundingBox(40, 80, 40, 48, 87, 48),
                        new BoundingBox(38, 78, 38, 50, 89, 50),
                        new BlockPos(41, 80, 41),
                        new BlockPos(42, 80, 42),
                        List.of(),
                        List.of(),
                        Map.of("workspace_base_name", "entry", "workspace_piece_kind", "template")
                ))
        );

        MKWorkspaceExportManifest manifest = MKWorkspaceExportManifest.snapshotFromWorkspace(workspace, 1, "now");

        assertEquals("", manifest.runtimeHints().startBaseName());
        assertEquals(1, manifest.pieces().size());
        assertEquals("entry_template", manifest.pieces().getFirst().pieceName());
    }

    @Test
    void exportManifestAllowsTemplateOnlyWorkspacesUntilRuntimeStructureValidation() {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        MKStructureWorkspace workspace = new MKStructureWorkspace(
                UUID.randomUUID(),
                new BlockPos(32, 80, 32),
                "mkdev",
                "template_only_export",
                dimensions,
                workspacePalette(),
                MKWorkspaceStairAuthoringConfig.defaultConfig(),
                MKVerticalAccessPlacement.CENTER,
                1,
                2,
                4,
                MKWorkspaceVerticalAccessSpec.defaultSpec(),
                MKTowerWorkspaceFamilyDefinition.createDefaults(),
                MKHorizontalOpeningProfile.createDefaults(dimensions),
                List.of(),
                100L,
                200L,
                List.of(new MKWorkspacePieceDefinition(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "entry_template",
                        "tower.primary.entry",
                        0,
                        dimensions,
                        1,
                        List.of(),
                        new BlockPos(40, 80, 40),
                        new BoundingBox(40, 80, 40, 48, 87, 48),
                        new BoundingBox(38, 78, 38, 50, 89, 50),
                        new BlockPos(41, 80, 41),
                        new BlockPos(42, 80, 42),
                        List.of(),
                        List.of(),
                        Map.of("workspace_base_name", "entry", "workspace_piece_kind", "template")
                ))
        );

        MKWorkspaceExportManifest manifest = MKWorkspaceExportManifest.fromWorkspace(workspace, 1, "now");
        List<String> validationErrors = manifest.validateRuntimeStructureExport();

        assertEquals("", manifest.runtimeHints().startBaseName());
        assertTrue(validationErrors.stream().anyMatch(error -> error.contains("runtime structure pieces")));
        assertTrue(validationErrors.stream().anyMatch(error -> error.contains("did not define a runtime start piece")));
    }

    private static MKStructureWorkspace baseWorkspace(List<MKHorizontalOpeningProfile> openingProfiles,
                                                      List<MKWorkspaceLinearRunFamilyDefinition> linearRunFamilies) {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        MKWorkspaceVerticalAccessSpec verticalAccessSpec = MKWorkspaceVerticalAccessSpec.defaultSpec();
        return new MKStructureWorkspace(
                UUID.randomUUID(),
                BlockPos.ZERO,
                "mkdev",
                "planner_test",
                dimensions,
                workspacePalette(),
                MKWorkspaceStairAuthoringConfig.defaultConfig(),
                MKVerticalAccessPlacement.CENTER,
                1,
                2,
                4,
                verticalAccessSpec,
                List.of(
                        MKTowerWorkspaceFamilyDefinition.forTowerStackSlot("entry", MKTowerWorkspaceStackSlot.ENTRY,
                                "tower.primary", true, 0, 0, 0, MKWorkspaceHorizontalExtrusionMode.TUNNEL_ONLY,
                                List.of(new MKWorkspaceFamilyHorizontalExitDefinition(net.minecraft.core.Direction.SOUTH,
                                        MKWorkspaceHorizontalExitPathKind.MAIN_EXIT, "entry_main")),
                                0, 0, null, null),
                        MKTowerWorkspaceFamilyDefinition.forTowerStackSlot("floor_main", MKTowerWorkspaceStackSlot.MAIN_FLOOR,
                                "tower.primary", true, 0, 0, 0, MKWorkspaceHorizontalExtrusionMode.TUNNEL_ONLY,
                                List.of(new MKWorkspaceFamilyHorizontalExitDefinition(net.minecraft.core.Direction.NORTH,
                                        MKWorkspaceHorizontalExitPathKind.BRANCH, "main_branch")),
                                0, 0, null, null),
                        MKTowerWorkspaceFamilyDefinition.forTowerStackSlot("top_cap_approach",
                                MKTowerWorkspaceStackSlot.TOP_CAP_APPROACH, "tower.primary", true, 0, 0, 0,
                                MKWorkspaceHorizontalExtrusionMode.TUNNEL_ONLY, List.of(), 0, 0, null, null),
                        MKTowerWorkspaceFamilyDefinition.forTowerStackSlot("top_cap", MKTowerWorkspaceStackSlot.TOP_CAP,
                                "tower.primary", true, 0, 0, 0, MKWorkspaceHorizontalExtrusionMode.TUNNEL_ONLY,
                                List.of(), 0, 0, null, null),
                        MKTowerWorkspaceFamilyDefinition.forTowerStackSlot("basement_entry",
                                MKTowerWorkspaceStackSlot.BASEMENT_ENTRY, "tower.primary", true, 0, 0, 0,
                                MKWorkspaceHorizontalExtrusionMode.TUNNEL_ONLY, List.of(), 0, 0, null, null),
                        MKTowerWorkspaceFamilyDefinition.forTowerStackSlot("basement_main",
                                MKTowerWorkspaceStackSlot.BASEMENT_FLOOR, "tower.primary", true, 0, 0, 0,
                                MKWorkspaceHorizontalExtrusionMode.TUNNEL_ONLY, List.of(), 0, 0, null, null),
                        MKTowerWorkspaceFamilyDefinition.forTowerStackSlot("basement_cap_approach",
                                MKTowerWorkspaceStackSlot.BASEMENT_CAP_APPROACH, "tower.primary", true, 0, 0, 0,
                                MKWorkspaceHorizontalExtrusionMode.TUNNEL_ONLY, List.of(), 0, 0, null, null),
                        MKTowerWorkspaceFamilyDefinition.forTowerStackSlot("basement_cap",
                                MKTowerWorkspaceStackSlot.BASEMENT_CAP, "tower.primary", true, 0, 0, 0,
                                MKWorkspaceHorizontalExtrusionMode.TUNNEL_ONLY, List.of(), 0, 0, null, null)
                ),
                openingProfiles,
                linearRunFamilies,
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                List.of()
        );
    }

    private static MKStructureWorkspace withTopologyAndLinearRuns(MKStructureWorkspace workspace,
                                                                  MKWorkspaceTopologyProfile topologyProfile,
                                                                  List<MKTowerWorkspaceFamilyDefinition> familyDefinitions,
                                                                  List<MKWorkspaceLinearRunFamilyDefinition> linearRunFamilies) {
        return new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                topologyProfile,
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                familyDefinitions.isEmpty() ? workspace.familyDefinitions() : familyDefinitions,
                workspace.openingProfiles(),
                linearRunFamilies,
                workspace.createdAt(),
                workspace.updatedAt(),
                workspace.pieces()
        );
    }

    private static MKTowerWorkspaceFamilyDefinition topologyFamily(String baseName,
                                                                   String topologySlotId,
                                                                   String verticalAccessGroupId,
                                                                   boolean supportsVerticalAccess,
                                                                   int roomWidth,
                                                                   int roomLength,
                                                                   int roomHeight,
                                                                   MKWorkspaceHorizontalExtrusionMode horizontalExtrusionMode,
                                                                   List<MKWorkspaceFamilyHorizontalExitDefinition> horizontalExits,
                                                                   int topVoidMargin,
                                                                   int bottomVoidMargin,
                                                                   MKWorkspaceFoundationPolicy foundationPolicy,
                                                                   MKWorkspacePaletteOverride paletteOverride) {
        return topologyFamily(baseName, MKWorkspaceTopologySlotMetadata.fromTopologySlotId(topologySlotId),
                verticalAccessGroupId, supportsVerticalAccess, roomWidth, roomLength, roomHeight,
                horizontalExtrusionMode, horizontalExits, topVoidMargin, bottomVoidMargin, foundationPolicy,
                paletteOverride);
    }

    private static MKTowerWorkspaceFamilyDefinition topologyFamily(String baseName,
                                                                   MKWorkspaceTopologySlotMetadata slotMetadata,
                                                                   String verticalAccessGroupId,
                                                                   boolean supportsVerticalAccess,
                                                                   int roomWidth,
                                                                   int roomLength,
                                                                   int roomHeight,
                                                                   MKWorkspaceHorizontalExtrusionMode horizontalExtrusionMode,
                                                                   List<MKWorkspaceFamilyHorizontalExitDefinition> horizontalExits,
                                                                   int topVoidMargin,
                                                                   int bottomVoidMargin,
                                                                   MKWorkspaceFoundationPolicy foundationPolicy,
                                                                   MKWorkspacePaletteOverride paletteOverride) {
        return MKTowerWorkspaceFamilyDefinition.forTopologySlot(baseName, slotMetadata, verticalAccessGroupId,
                supportsVerticalAccess, roomWidth, roomLength, roomHeight, horizontalExtrusionMode, horizontalExits,
                topVoidMargin, bottomVoidMargin, foundationPolicy, paletteOverride);
    }

    private static MKStructureWorkspace withTowerStackFloorCounts(MKStructureWorkspace workspace,
                                                                  int mainFloors,
                                                                  int basementFloors,
                                                                  boolean topCapApproachEnabled,
                                                                  boolean basementCapApproachEnabled) {
        MKWorkspaceTopologyProfile topologyProfile = workspace.topologyProfile()
                .towerStackSettings("tower.primary")
                .map(settings -> workspace.topologyProfile().withTowerStackSettings(settings
                        .withMainFloors(mainFloors)
                        .withBasementFloors(basementFloors)
                        .withTopCapApproachEnabled(topCapApproachEnabled)
                        .withBasementCapApproachEnabled(basementCapApproachEnabled)))
                .orElse(workspace.topologyProfile());
        return new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                topologyProfile,
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                workspace.familyDefinitions(),
                workspace.openingProfiles(),
                workspace.linearRunFamilies(),
                workspace.createdAt(),
                workspace.updatedAt(),
                workspace.pieces()
        );
    }

    private static MKStructureWorkspace withFamilyDefinitions(MKStructureWorkspace workspace,
                                                              List<MKTowerWorkspaceFamilyDefinition> familyDefinitions) {
        return new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                familyDefinitions,
                workspace.openingProfiles(),
                workspace.linearRunFamilies(),
                workspace.createdAt(),
                workspace.updatedAt(),
                workspace.pieces()
        );
    }

    private static MKWorkspaceMaterialPalette workspacePalette() {
        return MKWorkspaceMaterialPalette.defaultPalette();
    }

    private static MKDungeonConnectorSettings connectorSettings() {
        return new MKDungeonConnectorSettings(
                ResourceLocation.fromNamespaceAndPath("mknpc", "main_forward"),
                ResourceLocation.fromNamespaceAndPath("mknpc", "main_back"),
                ResourceLocation.fromNamespaceAndPath("mknpc", "branch"),
                ResourceLocation.fromNamespaceAndPath("mknpc", "connect_down"),
                ResourceLocation.fromNamespaceAndPath("mknpc", "connect_up"),
                ResourceLocation.fromNamespaceAndPath("mknpc", "top_cap_forward"),
                ResourceLocation.fromNamespaceAndPath("mknpc", "top_cap_back")
        );
    }

    private static MKPlannedConnector branchConnector(MKPlannedPiece piece, net.minecraft.core.Direction direction) {
        return piece.connectors().stream()
                .filter(connector -> connector.role() == MKConnectorRole.BRANCH && connector.facing() == direction)
                .findFirst()
                .orElseThrow();
    }

    private static MKWorkspacePieceDefinition pieceWithRuntimeAndIncomingPool(String pieceName, String baseName,
                                                                              MKWorkspaceRuntimePieceInfo runtimeInfo,
                                                                              ResourceLocation incomingPool) {
        java.util.Map<String, String> tags = new java.util.LinkedHashMap<>();
        tags.put("workspace_base_name", baseName);
        tags.put("workspace_piece_kind", "instance");
        runtimeInfo.applyToTags(tags);
        return new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                pieceName,
                "tower.primary.main_floor",
                0,
                MKWorkspaceDimensions.defaultDimensions(),
                1,
                List.of(new MKWorkspaceConnectorDefinition(
                        MKConnectorRole.MAIN_BACK,
                        net.minecraft.core.Direction.NORTH,
                        BlockPos.ZERO,
                        3,
                        3,
                        0,
                        0,
                        ResourceLocation.parse("mkdev:main_back"),
                        ResourceLocation.parse("mkdev:main_forward"),
                        ResourceLocation.parse("minecraft:empty"),
                        incomingPool
                )),
                BlockPos.ZERO,
                new BoundingBox(0, 0, 0, 1, 1, 1),
                new BoundingBox(0, 0, 0, 1, 1, 1),
                BlockPos.ZERO,
                BlockPos.ZERO,
                List.of(),
                List.of(),
                tags
        );
    }

    private static MKWorkspacePieceDefinition pieceToDefinitionWithConnectors(MKStructureWorkspace workspace,
                                                                              MKPlannedPiece plannedPiece) {
        List<MKWorkspaceConnectorDefinition> connectors = plannedPiece.connectors().stream()
                .map(connector -> new MKWorkspaceConnectorDefinition(
                        connector.role(),
                        connector.facing(),
                        BlockPos.ZERO,
                        connector.openingWidth(),
                        connector.openingHeight(),
                        connector.lateralOffset(),
                        connector.verticalOffset(),
                        ResourceLocation.fromNamespaceAndPath(workspace.namespace(), connector.role().getSerializedName()),
                        ResourceLocation.fromNamespaceAndPath(workspace.namespace(), "target"),
                        parseWorkspacePool(workspace, connector.targetPoolName()),
                        parseWorkspacePool(workspace, connector.incomingPoolName())
                ))
                .toList();
        return new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                workspace.id(),
                plannedPiece.pieceName(),
                plannedPiece.roleId(),
                0,
                MKWorkspaceDimensions.defaultDimensions(),
                1,
                connectors,
                BlockPos.ZERO,
                new BoundingBox(0, 0, 0, 1, 1, 1),
                new BoundingBox(0, 0, 0, 1, 1, 1),
                BlockPos.ZERO,
                BlockPos.ZERO,
                List.of(),
                List.of(),
                plannedPiece.tags()
        );
    }

    private static ResourceLocation parseWorkspacePool(MKStructureWorkspace workspace, String poolName) {
        if (poolName == null || poolName.isBlank()) {
            return ResourceLocation.parse("minecraft:empty");
        }
        if (poolName.contains(":")) {
            return ResourceLocation.parse(poolName);
        }
        return ResourceLocation.parse(workspace.namespace() + ":" + workspace.structureName() + "/" + poolName);
    }

    private static MKTowerWorkspaceFamilyDefinition copyFamilyWithHeight(MKTowerWorkspaceFamilyDefinition family,
                                                                         int height) {
        return MKTowerWorkspaceFamilyDefinition.forTopologySlot(
                family.baseName(),
                family.slotMetadata(),
                family.verticalAccessGroupId(),
                family.supportsVerticalAccess(),
                family.roomWidth(),
                family.roomLength(),
                height,
                family.horizontalExtrusionMode(),
                family.horizontalExits(),
                family.topVoidMargin(),
                family.bottomVoidMargin(),
                family.foundationPolicyOverride(),
                family.paletteOverride()
        );
    }

    private static MKTowerWorkspaceFamilyDefinition copyFamilyWithGeometry(MKTowerWorkspaceFamilyDefinition family,
                                                                           int width, int length, int height) {
        return MKTowerWorkspaceFamilyDefinition.forTopologySlot(
                family.baseName(),
                family.slotMetadata(),
                family.verticalAccessGroupId(),
                family.supportsVerticalAccess(),
                width,
                length,
                height,
                family.horizontalExtrusionMode(),
                family.horizontalExits(),
                family.topVoidMargin(),
                family.bottomVoidMargin(),
                family.foundationPolicyOverride(),
                family.paletteOverride()
        );
    }

    private static MKTowerWorkspaceFamilyDefinition copyFamilyWithFoundation(MKTowerWorkspaceFamilyDefinition family,
                                                                             MKWorkspaceFoundationPolicy foundationPolicy) {
        return MKTowerWorkspaceFamilyDefinition.forTopologySlot(
                family.baseName(),
                family.slotMetadata(),
                family.verticalAccessGroupId(),
                family.supportsVerticalAccess(),
                family.roomWidth(),
                family.roomLength(),
                family.roomHeight(),
                family.horizontalExtrusionMode(),
                family.horizontalExits(),
                family.topVoidMargin(),
                family.bottomVoidMargin(),
                foundationPolicy,
                family.paletteOverride()
        );
    }

    private static void assertRuntimePoolContains(MKStructureWorkspace workspace,
                                                  MKWorkspaceExportManifest manifest,
                                                  String poolBaseName,
                                                  String childBaseName) {
        ResourceLocation poolId = ResourceLocation.parse(workspace.namespace() + ":" +
                workspace.structureName() + "/" + poolBaseName);
        MKWorkspaceExportManifest.ExportRuntimePool pool = manifest.runtimeHints().pools().stream()
                .filter(candidate -> candidate.poolId().equals(poolId))
                .findFirst()
                .orElseThrow();
        assertTrue(pool.childBaseNames().contains(childBaseName),
                "Expected " + poolId + " to contain " + childBaseName + " but found " + pool.childBaseNames());
    }

    private static void assertRuntimePoolDoesNotContain(MKStructureWorkspace workspace,
                                                        MKWorkspaceExportManifest manifest,
                                                        String poolBaseName,
                                                        String childBaseName) {
        ResourceLocation poolId = ResourceLocation.parse(workspace.namespace() + ":" +
                workspace.structureName() + "/" + poolBaseName);
        MKWorkspaceExportManifest.ExportRuntimePool pool = manifest.runtimeHints().pools().stream()
                .filter(candidate -> candidate.poolId().equals(poolId))
                .findFirst()
                .orElseThrow();
        assertFalse(pool.childBaseNames().contains(childBaseName),
                "Expected " + poolId + " not to contain " + childBaseName + " but found " + pool.childBaseNames());
    }
}

