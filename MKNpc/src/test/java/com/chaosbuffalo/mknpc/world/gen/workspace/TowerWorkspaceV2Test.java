package com.chaosbuffalo.mknpc.world.gen.workspace;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.export.MKWorkspaceExportManifest;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKHallwayFamilyDefinition;
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
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitConnectionMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExtrusionMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitPathKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRuntimePieceInfo;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessSpec;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKPlannedPiece;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKTowerWorkspacePlanner;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TowerWorkspaceV2Test {

    @Test
    void plannerCreatesSeparateMainAndBranchHallwayPools() {
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
                        new MKHallwayFamilyDefinition("surface", "entry_main", 5, 3, 3, 0,
                                true, false, workspacePalette().floorBlock(), workspacePalette().wallBlock(),
                                workspacePalette().ceilingBlock()),
                        new MKHallwayFamilyDefinition("branch", "main_branch", 5, 3, 3, 0,
                                false, true, workspacePalette().floorBlock(), workspacePalette().wallBlock(),
                                workspacePalette().ceilingBlock())
                ));

        List<MKPlannedPiece> pieces = new MKTowerWorkspacePlanner().createCanonicalPieces(workspace);

        MKPlannedPiece entry = pieces.stream().filter(piece -> piece.pieceName().equals("entry")).findFirst().orElseThrow();
        assertTrue(entry.connectors().stream().anyMatch(connector ->
                connector.role() == MKConnectorRole.MAIN_BACK &&
                        "hallways/main/entry_main".equals(connector.targetPoolName())));
        assertTrue(entry.connectors().stream().noneMatch(connector ->
                connector.role() == MKConnectorRole.BRANCH &&
                        "hallways/branch/entry_main".equals(connector.targetPoolName())));

        MKPlannedPiece mainHallway = pieces.stream().filter(piece -> piece.pieceName().equals("hallway_surface_main")).findFirst().orElseThrow();
        assertTrue(mainHallway.connectors().stream().allMatch(connector -> connector.incomingPoolName().equals("hallways/main/entry_main")));

        MKPlannedPiece branchHallway = pieces.stream().filter(piece -> piece.pieceName().equals("hallway_branch_branch")).findFirst().orElseThrow();
        assertTrue(branchHallway.connectors().stream().allMatch(connector -> connector.incomingPoolName().equals("hallways/branch/main_branch")));
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
                workspace.hallwayFamilies(),
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
    void familyExtrusionDefaultsToTunnelOnlyButLegacyDataLoadsAsFullBody() {
        MKTowerWorkspaceFamilyDefinition defaultFamily = MKTowerWorkspaceFamilyDefinition.createDefaults().getFirst();
        assertEquals(MKWorkspaceHorizontalExtrusionMode.TUNNEL_ONLY, defaultFamily.horizontalExtrusionMode());

        CompoundTag legacyTag = defaultFamily.toTag();
        legacyTag.remove("horizontalExtrusionMode");
        MKTowerWorkspaceFamilyDefinition decodedLegacy = MKTowerWorkspaceFamilyDefinition.fromTag(legacyTag);

        assertEquals(MKWorkspaceHorizontalExtrusionMode.FULL_BODY, decodedLegacy.horizontalExtrusionMode());
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
                workspace.hallwayFamilies(),
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
                workspace.hallwayFamilies(),
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
                                ResourceLocation.parse("mkdev:pool_filter_test/hallways/branch/branch_opening")),
                        pieceWithRuntimeAndIncomingPool("branch_room", "branch_room",
                                new MKWorkspaceRuntimePieceInfo(false,
                                        com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawPieceRole.ROOM,
                                        0, 0, false, true, false, false),
                                ResourceLocation.parse("mkdev:pool_filter_test/hallways/branch/branch_opening"))
                )
        );

        MKWorkspaceExportManifest manifest = MKWorkspaceExportManifest.fromWorkspace(workspace, 4, "test");
        MKWorkspaceExportManifest.ExportRuntimePool branchPool = manifest.runtimeHints().pools().stream()
                .filter(pool -> pool.poolId().equals(ResourceLocation.parse("mkdev:pool_filter_test/hallways/branch/branch_opening")))
                .findFirst()
                .orElseThrow();

        assertEquals(List.of("branch_room"), branchPool.childBaseNames());
    }

    @Test
    void validationRejectsHallwayPathMismatchAndBandOverflow() {
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
                List.of(new MKHallwayFamilyDefinition("bad_hallway", "branch_only", 5, 3, bandCap, 1,
                        true, false, workspacePalette().floorBlock(), workspacePalette().wallBlock(),
                        workspacePalette().ceilingBlock())),
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
    void validationRequiresAllCategoryBandsToStayInSharedPhaseFamily() {
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
                workspace.hallwayFamilies(),
                workspace.createdAt(),
                workspace.updatedAt(),
                workspace.pieces()
        );

        List<String> errors = workspace.validate();
        assertTrue(errors.stream().anyMatch(error -> error.contains("top_cap full height must be one of")));
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
                workspace.hallwayFamilies(),
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
                List.of(new MKHallwayFamilyDefinition("surface", "entry_main", 5, 3, 3, 0,
                        true, false, workspacePalette().floorBlock(), workspacePalette().wallBlock(),
                        workspacePalette().ceilingBlock())),
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
                                ResourceLocation.parse("mkdev:hallway"),
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

    private static MKStructureWorkspace baseWorkspace(List<MKHorizontalOpeningProfile> openingProfiles,
                                                      List<MKHallwayFamilyDefinition> hallwayFamilies) {
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
                        new MKTowerWorkspaceFamilyDefinition("basement_cap", MKTowerWorkspaceCategory.BASEMENT_CAP, MKWorkspacePieceRole.BASEMENT_CAP, true,
                                9, 9, dimensions.basementHeight(),
                                List.of())
                ),
                openingProfiles,
                hallwayFamilies,
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                List.of()
        );
    }

    private static MKWorkspaceMaterialPalette workspacePalette() {
        return MKWorkspaceMaterialPalette.defaultPalette();
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
}

