package com.chaosbuffalo.mknpc.world.gen.workspace;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKDungeonCategoryRule;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKDungeonConnectorSettings;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKDungeonLayoutController;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKDungeonLayoutSettings;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKDungeonPieceState;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawPieceRole;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawPieceMetadata;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKVerticalProgressionMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.export.MKWorkspaceExportManifest;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKHorizontalOpeningProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureFamilyType;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceCategory;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceCategoryProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFloorSettings;
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
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKResolvedVerticalAccessProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRuntimePieceInfo;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairRiseType;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessSpec;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKPlannedConnector;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKPlannedPiece;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKTowerWorkspacePlanner;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKWalledKeepWorkspacePlanner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
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
        MKStructureWorkspace workspace = withTopologyAndLinearRuns(
                baseWorkspace(List.of(new MKHorizontalOpeningProfile("entry_main", 3, 3, true, false)), List.of()),
                MKWorkspaceTopologyProfile.walledKeep(true),
                List.of(),
                List.of()
        );

        MKStructureWorkspace decoded = MKStructureWorkspace.fromTag(workspace.toTag());

        assertEquals(MKWorkspaceTopologyProfile.WALLED_KEEP_PROFILE_TYPE, decoded.topologyProfile().profileType());
        assertTrue(decoded.topologyProfile().uniqueCornerTowers());
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
                List.of(new MKTowerWorkspaceFamilyDefinition(
                        "keep_center_entry",
                        MKTowerWorkspaceCategory.ENTRY,
                        MKWorkspacePieceRole.ENTRY,
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
                .filter(piece -> piece.pieceName().equals("keep_wall_north"))
                .findFirst()
                .orElseThrow();

        assertEquals("keep.center.entry", centerEntry.tags().get("workspace_topology_slot_id"));
        assertEquals("keep.center", centerEntry.tags().get("workspace_vertical_access_group_id"));
        assertTrue(centerEntry.connectors().stream().anyMatch(connector ->
                connector.role() == MKConnectorRole.CONNECT_UP &&
                        "vertical_access/keep.center/up".equals(connector.targetPoolName()) &&
                        "vertical_access/keep.center/down".equals(connector.incomingPoolName())));
        assertTrue(centerEntry.connectors().stream().anyMatch(connector ->
                connector.role() == MKConnectorRole.CONNECT_DOWN &&
                        "vertical_access/keep.center/down".equals(connector.targetPoolName()) &&
                        "vertical_access/keep.center/up".equals(connector.incomingPoolName())));

        assertEquals(MKWorkspacePieceRole.HALLWAY, northWall.role());
        assertEquals("keep.perimeter.north", northWall.tags().get("workspace_topology_slot_id"));
        assertEquals("solid_wall", northWall.tags().get("workspace_linear_run_kind"));
        assertEquals(MKWorkspaceFoundationMode.MASKED_EXTEND_BOTTOM_BLOCKS.getSerializedName(),
                northWall.tags().get(MKWorkspaceFoundationPolicy.MODE_TAG));
        assertTrue(northWall.connectors().stream().anyMatch(connector ->
                connector.facing() == Direction.WEST &&
                        "keep_slots/keep/perimeter/north".equals(connector.incomingPoolName())));
        assertTrue(northWall.connectors().stream().anyMatch(connector ->
                connector.facing() == Direction.EAST &&
                        "keep_slots/keep/perimeter/north".equals(connector.incomingPoolName())));
    }

    @Test
    void walledKeepDefaultsGenerateKeepSpecificPieces() {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        MKStructureWorkspace workspace = withTopologyAndLinearRuns(
                withCategoryProfiles(baseWorkspace(MKHorizontalOpeningProfile.createDefaults(dimensions), List.of()),
                        MKTowerWorkspaceCategoryProfile.createWalledKeepDefaults(dimensions)),
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
        assertTrue(pieces.stream().anyMatch(piece -> piece.pieceName().equals("keep_corner_shared") &&
                "keep.corner.shared".equals(piece.tags().get("workspace_topology_slot_id"))));
        MKPlannedPiece northWall = pieces.stream()
                .filter(piece -> piece.pieceName().equals("keep_wall_north"))
                .findFirst()
                .orElseThrow();
        assertEquals(7, northWall.interiorHeight());
        assertEquals("solid_wall", northWall.tags().get("workspace_linear_run_kind"));
        assertFalse(pieces.stream().anyMatch(piece -> "parapet".equals(piece.tags().get("workspace_linear_run_kind"))));
        assertTrue(pieces.stream().anyMatch(piece -> piece.pieceName().equals("keep_walkway_south") &&
                "open_walkway".equals(piece.tags().get("workspace_linear_run_kind"))));
    }

    @Test
    void walledKeepRuntimePoolsUseSlotGraphAndSharedCorners() {
        MKStructureWorkspace workspace = withTopologyAndLinearRuns(
                baseWorkspace(List.of(new MKHorizontalOpeningProfile("wall_opening", 3, 3, true, true)), List.of()),
                MKWorkspaceTopologyProfile.walledKeep(false),
                List.of(
                        new MKTowerWorkspaceFamilyDefinition(
                                "keep_center_entry",
                                MKTowerWorkspaceCategory.ENTRY,
                                MKWorkspacePieceRole.ENTRY,
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
                        new MKTowerWorkspaceFamilyDefinition(
                                "keep_gate_main",
                                MKTowerWorkspaceCategory.ENTRY,
                                MKWorkspacePieceRole.ENTRY,
                                "keep.gate.main",
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
                        new MKTowerWorkspaceFamilyDefinition(
                                "keep_corner_shared",
                                MKTowerWorkspaceCategory.MAIN,
                                MKWorkspacePieceRole.FLOOR_MAIN,
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
        assertRuntimePoolContains(workspace, manifest, "keep_slots/keep/perimeter/south", "keep_wall_south");
        assertRuntimePoolContains(workspace, manifest, "keep_slots/keep/corner/north_west", "keep_corner_shared");
        assertRuntimePoolContains(workspace, manifest, "keep_slots/keep/corner/north_east", "keep_corner_shared");
        assertRuntimePoolContains(workspace, manifest, "keep_slots/keep/corner/south_east", "keep_corner_shared");
        assertRuntimePoolContains(workspace, manifest, "keep_slots/keep/corner/south_west", "keep_corner_shared");
    }

    @Test
    void uniqueCornerOverrideReplacesSharedCornerForThatSlot() {
        MKStructureWorkspace workspace = withTopologyAndLinearRuns(
                baseWorkspace(List.of(new MKHorizontalOpeningProfile("wall_opening", 3, 3, true, true)), List.of()),
                MKWorkspaceTopologyProfile.walledKeep(true),
                List.of(
                        new MKTowerWorkspaceFamilyDefinition(
                                "keep_center_entry",
                                MKTowerWorkspaceCategory.ENTRY,
                                MKWorkspacePieceRole.ENTRY,
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
                        new MKTowerWorkspaceFamilyDefinition(
                                "keep_corner_shared",
                                MKTowerWorkspaceCategory.MAIN,
                                MKWorkspacePieceRole.FLOOR_MAIN,
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
                        new MKTowerWorkspaceFamilyDefinition(
                                "keep_corner_south_east",
                                MKTowerWorkspaceCategory.MAIN,
                                MKWorkspacePieceRole.FLOOR_MAIN,
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
        assertRuntimePoolContains(workspace, manifest, "keep_slots/keep/corner/south_east", "keep_corner_south_east");
        assertRuntimePoolDoesNotContain(workspace, manifest, "keep_slots/keep/corner/south_east", "keep_corner_shared");
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
                List.of(new MKTowerWorkspaceFamilyDefinition(
                        "keep_center_entry",
                        MKTowerWorkspaceCategory.ENTRY,
                        MKWorkspacePieceRole.ENTRY,
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

        MKWorkspaceExportManifest.ExportRuntimeCategory roomCategory = manifest.runtimeHints().categories().stream()
                .filter(category -> category.baseName().equals("keep_center_entry"))
                .findFirst()
                .orElseThrow();
        MKWorkspaceExportManifest.ExportRuntimeCategory runCategory = manifest.runtimeHints().categories().stream()
                .filter(category -> category.baseName().equals("keep_wall_north"))
                .findFirst()
                .orElseThrow();

        assertEquals(MKWorkspaceFoundationMode.UNIFORM_STATE, roomCategory.pieceMetadata().foundationPolicy().mode());
        assertEquals(ResourceLocation.parse("minecraft:stone_bricks"),
                roomCategory.pieceMetadata().foundationPolicy().foundationBlock());
        assertEquals(MKWorkspaceFoundationMode.MASKED_EXTEND_BOTTOM_BLOCKS, runCategory.pieceMetadata().foundationPolicy().mode());
        assertEquals(List.of(ResourceLocation.parse("minecraft:stone_bricks")),
                runCategory.pieceMetadata().foundationPolicy().maskBlocks());
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
        MKTowerWorkspaceFamilyDefinition updatedEntry = new MKTowerWorkspaceFamilyDefinition(
                entryFamily.baseName(),
                entryFamily.category(),
                entryFamily.pieceRole(),
                entryFamily.supportsVerticalAccess(),
                entryFamily.roomWidth(),
                entryFamily.roomLength(),
                entryFamily.roomHeight(),
                List.of(
                        new MKWorkspaceFamilyHorizontalExitDefinition(net.minecraft.core.Direction.NORTH,
                                MKWorkspaceHorizontalExitPathKind.MAIN_ENTRY, "entry_main"),
                        new MKWorkspaceFamilyHorizontalExitDefinition(net.minecraft.core.Direction.SOUTH,
                                MKWorkspaceHorizontalExitPathKind.MAIN_EXIT, "entry_main")
                )
        );
        workspace = new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                workspace.familyType(),
                workspace.topologyProfile(),
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                workspace.floorSettings(),
                workspace.categoryProfiles(),
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
        MKTowerWorkspaceFamilyDefinition family = new MKTowerWorkspaceFamilyDefinition(
                "side_room",
                MKTowerWorkspaceCategory.TOP_CAP,
                MKWorkspacePieceRole.TOP_CAP,
                false,
                9,
                9,
                7,
                MKWorkspaceHorizontalExtrusionMode.FULL_BODY,
                List.of(),
                4,
                0,
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
        families.add(new MKTowerWorkspaceFamilyDefinition("main_side_room", MKTowerWorkspaceCategory.MAIN,
                MKWorkspacePieceRole.FLOOR_MAIN, false, 9, 9, workspace.dimensions().roomHeight(),
                MKWorkspaceHorizontalExtrusionMode.FULL_BODY, List.of(), 2, 1, null));
        workspace = new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                workspace.familyType(),
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                workspace.floorSettings(),
                workspace.categoryProfiles(),
                families,
                workspace.openingProfiles(),
                workspace.linearRunFamilies(),
                workspace.createdAt(),
                workspace.updatedAt(),
                workspace.pieces()
        );

        List<MKPlannedPiece> pieces = new MKTowerWorkspacePlanner().createCanonicalPieces(workspace);

        MKPlannedPiece topCap = pieces.stream().filter(piece -> piece.role() == MKWorkspacePieceRole.TOP_CAP)
                .findFirst().orElseThrow();
        MKPlannedPiece basementCap = pieces.stream().filter(piece -> piece.role() == MKWorkspacePieceRole.BASEMENT_CAP)
                .findFirst().orElseThrow();
        MKPlannedPiece floor = pieces.stream().filter(piece -> piece.role() == MKWorkspacePieceRole.FLOOR_MAIN)
                .filter(piece -> piece.pieceName().equals("floor_main"))
                .findFirst().orElseThrow();
        MKPlannedPiece sideRoom = pieces.stream().filter(piece -> piece.pieceName().equals("main_side_room"))
                .findFirst().orElseThrow();
        assertFalse(topCap.tags().containsKey(MKTowerWorkspaceCategoryProfile.TOP_VOID_MARGIN_TAG));
        assertFalse(basementCap.tags().containsKey(MKTowerWorkspaceCategoryProfile.BOTTOM_VOID_MARGIN_TAG));
        assertFalse(floor.tags().containsKey(MKTowerWorkspaceCategoryProfile.TOP_VOID_MARGIN_TAG));
        assertFalse(floor.tags().containsKey(MKTowerWorkspaceCategoryProfile.BOTTOM_VOID_MARGIN_TAG));
        assertEquals("2", sideRoom.tags().get(MKTowerWorkspaceCategoryProfile.TOP_VOID_MARGIN_TAG));
        assertEquals("1", sideRoom.tags().get(MKTowerWorkspaceCategoryProfile.BOTTOM_VOID_MARGIN_TAG));
    }

    @Test
    void familyDefinitionCodecRoundTripPreservesSingleVerticalExit() {
        MKTowerWorkspaceFamilyDefinition family = new MKTowerWorkspaceFamilyDefinition(
                "top_only",
                MKTowerWorkspaceCategory.MAIN,
                MKWorkspacePieceRole.FLOOR_MAIN,
                false,
                9,
                9,
                MKWorkspaceDimensions.defaultDimensions().roomHeight(),
                List.of(MKWorkspaceFamilyHorizontalExitDefinition.verticalAccess(net.minecraft.core.Direction.UP))
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
        MKTowerWorkspaceFamilyDefinition topOnlyMain = new MKTowerWorkspaceFamilyDefinition(
                "floor_main",
                MKTowerWorkspaceCategory.MAIN,
                MKWorkspacePieceRole.FLOOR_MAIN,
                false,
                9,
                9,
                workspace.dimensions().roomHeight(),
                MKWorkspaceHorizontalExtrusionMode.TUNNEL_ONLY,
                List.of(
                        new MKWorkspaceFamilyHorizontalExitDefinition(net.minecraft.core.Direction.NORTH,
                                MKWorkspaceHorizontalExitPathKind.BRANCH, "main_branch"),
                        MKWorkspaceFamilyHorizontalExitDefinition.verticalAccess(net.minecraft.core.Direction.UP)
                )
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
                .filter(family -> family.pieceRole() == MKWorkspacePieceRole.ENTRY)
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
                MKStructureFamilyType.TOWER,
                dimensions,
                workspacePalette(),
                MKWorkspaceStairAuthoringConfig.defaultConfig(),
                MKVerticalAccessPlacement.CENTER,
                1,
                2,
                4,
                MKWorkspaceVerticalAccessSpec.defaultSpec(),
                MKTowerWorkspaceFloorSettings.defaultSettings(),
                MKTowerWorkspaceCategoryProfile.createDefaults(dimensions),
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
        MKTowerWorkspaceFamilyDefinition endingFamily = new MKTowerWorkspaceFamilyDefinition(
                "main_end",
                MKTowerWorkspaceCategory.MAIN,
                MKWorkspacePieceRole.FLOOR_MAIN,
                false,
                9,
                9,
                5,
                List.of(new MKWorkspaceFamilyHorizontalExitDefinition(net.minecraft.core.Direction.NORTH,
                        MKWorkspaceHorizontalExitPathKind.MAIN_ENDING_ENTRY, "entry_main"))
        );
        workspace = new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                workspace.familyType(),
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                workspace.floorSettings(),
                workspace.categoryProfiles(),
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
        assertEquals("main", plannedEnding.tags().get(MKWorkspaceRuntimePieceInfo.CATEGORY_TAG));
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
        MKTowerWorkspaceFamilyDefinition branchCapFamily = new MKTowerWorkspaceFamilyDefinition(
                "branch_cap",
                MKTowerWorkspaceCategory.MAIN,
                MKWorkspacePieceRole.FLOOR_MAIN,
                false,
                9,
                9,
                5,
                List.of(new MKWorkspaceFamilyHorizontalExitDefinition(net.minecraft.core.Direction.NORTH,
                        MKWorkspaceHorizontalExitPathKind.BRANCH_CAP_ENTRY, "main_branch"))
        );
        workspace = new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                workspace.familyType(),
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                workspace.floorSettings(),
                workspace.categoryProfiles(),
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
        MKWorkspaceExportManifest.ExportRuntimeCategory category = manifest.runtimeHints().categories().stream()
                .filter(runtimeCategory -> runtimeCategory.baseName().equals("branch_cap"))
                .findFirst()
                .orElseThrow();
        MKWorkspaceExportManifest.ExportRuntimePool capPool = manifest.runtimeHints().pools().stream()
                .filter(pool -> pool.poolId().equals(ResourceLocation.parse("mkdev:planner_test/branch_caps/main_branch")))
                .findFirst()
                .orElseThrow();

        assertTrue(category.pieceMetadata().branchCap());
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
        MKTowerWorkspaceFamilyDefinition invalidEnding = new MKTowerWorkspaceFamilyDefinition(
                "invalid_end",
                MKTowerWorkspaceCategory.MAIN,
                MKWorkspacePieceRole.FLOOR_MAIN,
                false,
                9,
                9,
                5,
                List.of(
                        new MKWorkspaceFamilyHorizontalExitDefinition(net.minecraft.core.Direction.NORTH,
                                MKWorkspaceHorizontalExitPathKind.MAIN_ENDING_ENTRY, "entry_main"),
                        new MKWorkspaceFamilyHorizontalExitDefinition(net.minecraft.core.Direction.SOUTH,
                                MKWorkspaceHorizontalExitPathKind.MAIN_EXIT, "entry_main")
                )
        );
        workspace = new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                workspace.familyType(),
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                workspace.floorSettings(),
                workspace.categoryProfiles(),
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
        MKTowerWorkspaceFamilyDefinition invalidCap = new MKTowerWorkspaceFamilyDefinition(
                "invalid_cap",
                MKTowerWorkspaceCategory.MAIN,
                MKWorkspacePieceRole.FLOOR_MAIN,
                false,
                9,
                9,
                5,
                List.of(
                        new MKWorkspaceFamilyHorizontalExitDefinition(net.minecraft.core.Direction.NORTH,
                                MKWorkspaceHorizontalExitPathKind.BRANCH_CAP_ENTRY, "main_branch",
                                MKWorkspaceHorizontalExitConnectionMode.NO_CONNECTION),
                        new MKWorkspaceFamilyHorizontalExitDefinition(net.minecraft.core.Direction.SOUTH,
                                MKWorkspaceHorizontalExitPathKind.MAIN_EXIT, "entry_main",
                                MKWorkspaceHorizontalExitConnectionMode.DIRECT_ROOM)
                )
        );
        workspace = new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                workspace.familyType(),
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                workspace.floorSettings(),
                workspace.categoryProfiles(),
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
    void layoutControllerSwitchesFromContinuationToEndingAtCategoryTarget() {
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
                List.of(new MKDungeonCategoryRule("main", 2, 2, true, endingPool)),
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
    void layoutControllerRequiresBranchCapAtCategoryBranchLimitWhenCapsAvailable() {
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
                List.of(new MKDungeonCategoryRule("main", 1, 2, 2, true, null)),
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
    void categoryProfileBranchCapLimitExportsToManifest() {
        MKStructureWorkspace workspace = baseWorkspace(
                List.of(
                        new MKHorizontalOpeningProfile("entry_main", 3, 3, true, false),
                        new MKHorizontalOpeningProfile("main_branch", 3, 3, false, true)
                ),
                List.of()
        );
        List<MKTowerWorkspaceCategoryProfile> categoryProfiles = workspace.categoryProfiles().stream()
                .map(profile -> profile.category() == MKTowerWorkspaceCategory.MAIN ?
                        new MKTowerWorkspaceCategoryProfile(
                                profile.category(),
                                profile.roomWidth(),
                                profile.roomLength(),
                                profile.fullHeight(),
                                profile.minMainPathPieces(),
                                profile.maxMainPathPieces(),
                                3) :
                        profile)
                .toList();
        workspace = new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                workspace.familyType(),
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                workspace.floorSettings(),
                categoryProfiles,
                workspace.familyDefinitions(),
                workspace.openingProfiles(),
                workspace.linearRunFamilies(),
                workspace.createdAt(),
                workspace.updatedAt(),
                workspace.pieces()
        );

        MKStructureWorkspace exportSource = workspace;
        List<MKPlannedPiece> pieces = new MKTowerWorkspacePlanner().createCanonicalPieces(exportSource);
        MKStructureWorkspace exportWorkspace = exportSource.withPieces(pieces.stream()
                .map(piece -> pieceToDefinitionWithConnectors(exportSource, piece))
                .toList());
        MKWorkspaceExportManifest manifest = MKWorkspaceExportManifest.fromWorkspace(exportWorkspace, 4, "test");
        MKWorkspaceExportManifest.ExportCategoryProfile mainProfile = manifest.settings().categoryProfiles().stream()
                .filter(profile -> profile.category() == MKTowerWorkspaceCategory.MAIN)
                .findFirst()
                .orElseThrow();

        assertEquals(3, mainProfile.maxBranchPiecesBeforeCap());
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
        MKTowerWorkspaceFamilyDefinition updatedMain = new MKTowerWorkspaceFamilyDefinition(
                "floor_main",
                MKTowerWorkspaceCategory.MAIN,
                MKWorkspacePieceRole.FLOOR_MAIN,
                true,
                9,
                9,
                workspace.dimensions().roomHeight(),
                List.of(
                        new MKWorkspaceFamilyHorizontalExitDefinition(net.minecraft.core.Direction.NORTH,
                                MKWorkspaceHorizontalExitPathKind.BRANCH, "main_branch",
                                MKWorkspaceHorizontalExitConnectionMode.HALLWAY, 2, 1),
                        new MKWorkspaceFamilyHorizontalExitDefinition(net.minecraft.core.Direction.SOUTH,
                                MKWorkspaceHorizontalExitPathKind.BRANCH, "main_branch",
                                MKWorkspaceHorizontalExitConnectionMode.HALLWAY, 2, 1),
                        new MKWorkspaceFamilyHorizontalExitDefinition(net.minecraft.core.Direction.EAST,
                                MKWorkspaceHorizontalExitPathKind.BRANCH, "main_branch",
                                MKWorkspaceHorizontalExitConnectionMode.HALLWAY, 2, 1),
                        new MKWorkspaceFamilyHorizontalExitDefinition(net.minecraft.core.Direction.WEST,
                                MKWorkspaceHorizontalExitPathKind.BRANCH, "main_branch",
                                MKWorkspaceHorizontalExitConnectionMode.HALLWAY, 2, 1)
                )
        );
        workspace = new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                workspace.familyType(),
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                workspace.floorSettings(),
                workspace.categoryProfiles(),
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
    void floorSettingsDefaultCapApproachFlags() {
        MKTowerWorkspaceFloorSettings settings = MKTowerWorkspaceFloorSettings.defaultSettings();

        assertTrue(settings.topCapApproachEnabled());
        assertFalse(settings.basementCapApproachEnabled());
    }

    @Test
    void floorCountsReflectOptionalCapApproachPieces() {
        List<MKTowerWorkspaceCategoryProfile> categoryProfiles = MKTowerWorkspaceCategoryProfile.createDefaults(
                MKWorkspaceDimensions.defaultDimensions());

        List<Integer> mainWithApproach = MKTowerWorkspaceFloorSettings.allowedMainFloorCounts(categoryProfiles,
                1, true, false);
        List<Integer> mainWithoutApproach = MKTowerWorkspaceFloorSettings.allowedMainFloorCounts(categoryProfiles,
                1, false, false);
        List<Integer> basementWithoutApproach = MKTowerWorkspaceFloorSettings.allowedBasementFloorCounts(categoryProfiles,
                1, true, false);
        List<Integer> basementWithApproach = MKTowerWorkspaceFloorSettings.allowedBasementFloorCounts(categoryProfiles,
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
                MKStructureFamilyType.TOWER,
                dimensions,
                palette,
                MKWorkspaceStairAuthoringConfig.defaultConfig(),
                MKVerticalAccessPlacement.CENTER,
                1,
                2,
                4,
                MKWorkspaceVerticalAccessSpec.defaultSpec(),
                MKTowerWorkspaceFloorSettings.defaultSettings(),
                MKTowerWorkspaceCategoryProfile.createDefaults(dimensions),
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
        MKStructureWorkspace workspace = withFloorSettings(baseWorkspace(
                        List.of(
                                new MKHorizontalOpeningProfile("entry_main", 3, 3, true, false),
                                new MKHorizontalOpeningProfile("main_branch", 3, 3, false, true)
                        ),
                        List.of()),
                new MKTowerWorkspaceFloorSettings(1, 1, false, false));

        List<MKPlannedPiece> pieces = new MKTowerWorkspacePlanner().createCanonicalPieces(workspace);
        MKPlannedPiece topCap = pieces.stream()
                .filter(piece -> piece.pieceName().equals("top_cap"))
                .findFirst()
                .orElseThrow();

        assertTrue(pieces.stream().noneMatch(piece -> piece.role() == MKWorkspacePieceRole.TOP_CAP_APPROACH));
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
        MKStructureWorkspace workspace = withFloorSettings(baseWorkspace(
                        List.of(
                                new MKHorizontalOpeningProfile("entry_main", 3, 3, true, false),
                                new MKHorizontalOpeningProfile("main_branch", 3, 3, false, true)
                        ),
                        List.of()),
                new MKTowerWorkspaceFloorSettings(1, 1, true, true));

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
                        return new MKTowerWorkspaceFamilyDefinition(
                                family.baseName(),
                                family.category(),
                                family.pieceRole(),
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
                                ))
                        );
                    }
                    if (family.baseName().equals("floor_main")) {
                        return new MKTowerWorkspaceFamilyDefinition(
                                family.baseName(),
                                family.category(),
                                family.pieceRole(),
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
                                ))
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
                workspace.familyType(),
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                workspace.floorSettings(),
                workspace.categoryProfiles(),
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
        MKTowerWorkspaceFamilyDefinition updatedMain = new MKTowerWorkspaceFamilyDefinition(
                "floor_main",
                MKTowerWorkspaceCategory.MAIN,
                MKWorkspacePieceRole.FLOOR_MAIN,
                true,
                9,
                9,
                workspace.dimensions().roomHeight(),
                List.of(new MKWorkspaceFamilyHorizontalExitDefinition(net.minecraft.core.Direction.NORTH,
                        MKWorkspaceHorizontalExitPathKind.BRANCH, "main_branch"))
        );
        workspace = new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                workspace.familyType(),
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                workspace.floorSettings(),
                workspace.categoryProfiles(),
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
                MKStructureFamilyType.TOWER,
                dimensions,
                workspacePalette(),
                MKWorkspaceStairAuthoringConfig.defaultConfig(),
                MKVerticalAccessPlacement.CENTER,
                1,
                2,
                4,
                MKWorkspaceVerticalAccessSpec.defaultSpec(),
                MKTowerWorkspaceFloorSettings.defaultSettings(),
                MKTowerWorkspaceCategoryProfile.createDefaults(dimensions),
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
    void validationRejectsLinearRunPathMismatchAndBandOverflow() {
        MKWorkspaceVerticalAccessSpec verticalAccessSpec = MKWorkspaceVerticalAccessSpec.defaultSpec();
        MKTowerWorkspaceCategoryProfile mainProfile = MKTowerWorkspaceCategoryProfile.createDefaults(
                MKWorkspaceDimensions.defaultDimensions()).stream()
                .filter(profile -> profile.category() == MKTowerWorkspaceCategory.MAIN)
                .findFirst()
                .orElseThrow();
        int bandCap = verticalAccessSpec.getBandCapForReusableHeight(mainProfile.fullHeight());

        MKStructureWorkspace workspace = new MKStructureWorkspace(
                UUID.randomUUID(),
                BlockPos.ZERO,
                "mkdev",
                "validation_test",
                MKStructureFamilyType.TOWER,
                MKWorkspaceDimensions.defaultDimensions(),
                workspacePalette(),
                MKWorkspaceStairAuthoringConfig.defaultConfig(),
                MKVerticalAccessPlacement.CENTER,
                1,
                2,
                4,
                verticalAccessSpec,
                MKTowerWorkspaceFloorSettings.defaultSettings(),
                MKTowerWorkspaceCategoryProfile.createDefaults(MKWorkspaceDimensions.defaultDimensions()),
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
        assertTrue(errors.stream().anyMatch(error -> error.contains("exceeds main vertical band cap")));
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
    void validationAllowsCategoryBandsWithIndependentlyResolvedCanonicalProfiles() {
        MKStructureWorkspace workspace = baseWorkspace(
                List.of(
                        new MKHorizontalOpeningProfile("entry_main", 3, 3, true, false),
                        new MKHorizontalOpeningProfile("main_branch", 3, 3, false, true)
                ),
                List.of()
        );
        List<MKTowerWorkspaceCategoryProfile> categoryProfiles = workspace.categoryProfiles().stream()
                .map(profile -> profile.category() == MKTowerWorkspaceCategory.TOP_CAP
                        ? new MKTowerWorkspaceCategoryProfile(
                        profile.category(),
                        profile.roomWidth(),
                        profile.roomLength(),
                        profile.fullHeight() + 1)
                        : profile)
                .toList();

        workspace = new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                workspace.familyType(),
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                workspace.floorSettings(),
                categoryProfiles,
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

        workspace = new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                workspace.familyType(),
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                new MKTowerWorkspaceFloorSettings(11, 11),
                workspace.categoryProfiles(),
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
                MKStructureFamilyType.TOWER,
                dimensions,
                workspacePalette(),
                MKWorkspaceStairAuthoringConfig.defaultConfig(),
                MKVerticalAccessPlacement.CENTER,
                1,
                2,
                4,
                MKWorkspaceVerticalAccessSpec.defaultSpec(),
                MKTowerWorkspaceFloorSettings.defaultSettings(),
                MKTowerWorkspaceCategoryProfile.createDefaults(dimensions),
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
                        MKWorkspacePieceRole.ENTRY,
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
        assertEquals(workspace.floorSettings().mainFloors(), decoded.floorSettings().mainFloors());
        assertEquals(workspace.floorSettings().basementFloors(), decoded.floorSettings().basementFloors());
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
                MKStructureFamilyType.TOWER,
                dimensions,
                workspacePalette(),
                MKWorkspaceStairAuthoringConfig.defaultConfig(),
                MKVerticalAccessPlacement.CENTER,
                1,
                2,
                4,
                MKWorkspaceVerticalAccessSpec.defaultSpec(),
                MKTowerWorkspaceFloorSettings.defaultSettings(),
                MKTowerWorkspaceCategoryProfile.createDefaults(dimensions),
                MKTowerWorkspaceFamilyDefinition.createDefaults(),
                MKHorizontalOpeningProfile.createDefaults(dimensions),
                List.of(),
                100L,
                200L,
                List.of(new MKWorkspacePieceDefinition(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "entry_template",
                        MKWorkspacePieceRole.ENTRY,
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
                MKStructureFamilyType.TOWER,
                dimensions,
                workspacePalette(),
                MKWorkspaceStairAuthoringConfig.defaultConfig(),
                MKVerticalAccessPlacement.CENTER,
                1,
                2,
                4,
                MKWorkspaceVerticalAccessSpec.defaultSpec(),
                MKTowerWorkspaceFloorSettings.defaultSettings(),
                MKTowerWorkspaceCategoryProfile.createDefaults(dimensions),
                MKTowerWorkspaceFamilyDefinition.createDefaults(),
                MKHorizontalOpeningProfile.createDefaults(dimensions),
                List.of(),
                100L,
                200L,
                List.of(new MKWorkspacePieceDefinition(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "entry_template",
                        MKWorkspacePieceRole.ENTRY,
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
                MKStructureFamilyType.TOWER,
                dimensions,
                workspacePalette(),
                MKWorkspaceStairAuthoringConfig.defaultConfig(),
                MKVerticalAccessPlacement.CENTER,
                1,
                2,
                4,
                verticalAccessSpec,
                MKTowerWorkspaceFloorSettings.defaultSettings(),
                List.of(
                        new MKTowerWorkspaceCategoryProfile(MKTowerWorkspaceCategory.ENTRY, 9, 9, dimensions.entranceHeight()),
                        new MKTowerWorkspaceCategoryProfile(MKTowerWorkspaceCategory.MAIN, 9, 9, dimensions.roomHeight()),
                        new MKTowerWorkspaceCategoryProfile(MKTowerWorkspaceCategory.BASEMENT, 9, 9, dimensions.basementHeight()),
                        new MKTowerWorkspaceCategoryProfile(MKTowerWorkspaceCategory.TOP_CAP, 9, 9, dimensions.roomHeight()),
                        new MKTowerWorkspaceCategoryProfile(MKTowerWorkspaceCategory.BASEMENT_CAP, 9, 9, dimensions.basementHeight())
                ),
                List.of(
                        new MKTowerWorkspaceFamilyDefinition("entry", MKTowerWorkspaceCategory.ENTRY, MKWorkspacePieceRole.ENTRY, true,
                                9, 9, dimensions.entranceHeight(),
                                List.of(new MKWorkspaceFamilyHorizontalExitDefinition(net.minecraft.core.Direction.SOUTH,
                                        MKWorkspaceHorizontalExitPathKind.MAIN_EXIT, "entry_main"))),
                        new MKTowerWorkspaceFamilyDefinition("floor_main", MKTowerWorkspaceCategory.MAIN, MKWorkspacePieceRole.FLOOR_MAIN, true,
                                9, 9, dimensions.roomHeight(),
                                List.of(new MKWorkspaceFamilyHorizontalExitDefinition(net.minecraft.core.Direction.NORTH,
                                        MKWorkspaceHorizontalExitPathKind.BRANCH, "main_branch"))),
                        new MKTowerWorkspaceFamilyDefinition("top_cap_approach", MKTowerWorkspaceCategory.TOP_CAP, MKWorkspacePieceRole.TOP_CAP_APPROACH, true,
                                9, 9, dimensions.roomHeight(),
                                List.of()),
                        new MKTowerWorkspaceFamilyDefinition("top_cap", MKTowerWorkspaceCategory.TOP_CAP, MKWorkspacePieceRole.TOP_CAP, true,
                                9, 9, dimensions.roomHeight(),
                                List.of()),
                        new MKTowerWorkspaceFamilyDefinition("basement_entry", MKTowerWorkspaceCategory.BASEMENT, MKWorkspacePieceRole.BASEMENT_ENTRY, true,
                                9, 9, dimensions.basementHeight(),
                                List.of()),
                        new MKTowerWorkspaceFamilyDefinition("basement_main", MKTowerWorkspaceCategory.BASEMENT, MKWorkspacePieceRole.BASEMENT_MAIN, true,
                                9, 9, dimensions.basementHeight(),
                                List.of()),
                        new MKTowerWorkspaceFamilyDefinition("basement_cap_approach", MKTowerWorkspaceCategory.BASEMENT_CAP, MKWorkspacePieceRole.BASEMENT_CAP_APPROACH, true,
                                9, 9, dimensions.basementHeight(),
                                List.of()),
                        new MKTowerWorkspaceFamilyDefinition("basement_cap", MKTowerWorkspaceCategory.BASEMENT_CAP, MKWorkspacePieceRole.BASEMENT_CAP, true,
                                9, 9, dimensions.basementHeight(),
                                List.of())
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
                workspace.familyType(),
                topologyProfile,
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                workspace.floorSettings(),
                workspace.categoryProfiles(),
                familyDefinitions.isEmpty() ? workspace.familyDefinitions() : familyDefinitions,
                workspace.openingProfiles(),
                linearRunFamilies,
                workspace.createdAt(),
                workspace.updatedAt(),
                workspace.pieces()
        );
    }

    private static MKStructureWorkspace withCategoryProfiles(MKStructureWorkspace workspace,
                                                             List<MKTowerWorkspaceCategoryProfile> categoryProfiles) {
        return new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                workspace.familyType(),
                workspace.topologyProfile(),
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                workspace.floorSettings(),
                categoryProfiles,
                workspace.familyDefinitions(),
                workspace.openingProfiles(),
                workspace.linearRunFamilies(),
                workspace.createdAt(),
                workspace.updatedAt(),
                workspace.pieces()
        );
    }

    private static MKStructureWorkspace withFloorSettings(MKStructureWorkspace workspace,
                                                          MKTowerWorkspaceFloorSettings floorSettings) {
        return new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                workspace.familyType(),
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                floorSettings,
                workspace.categoryProfiles(),
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
                workspace.familyType(),
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                workspace.floorSettings(),
                workspace.categoryProfiles(),
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
                MKWorkspacePieceRole.FLOOR_MAIN,
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
                plannedPiece.role(),
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

