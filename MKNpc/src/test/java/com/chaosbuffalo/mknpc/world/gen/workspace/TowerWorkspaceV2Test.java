package com.chaosbuffalo.mknpc.world.gen.workspace;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKHallwayFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKHorizontalOpeningProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureFamilyType;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceCategory;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceCategoryProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKVerticalAccessPlacement;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitPathKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceRole;
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
                        new MKHorizontalOpeningProfile("boss_main", 3, 3, true, false),
                        new MKHorizontalOpeningProfile("boss_branch", 3, 3, false, true)
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
                List.of(
                        new MKTowerWorkspaceCategoryProfile(MKTowerWorkspaceCategory.ENTRY, 9, 9, dimensions.entranceHeight(), 3),
                        new MKTowerWorkspaceCategoryProfile(MKTowerWorkspaceCategory.MAIN, 9, 9, dimensions.roomHeight(), 3),
                        new MKTowerWorkspaceCategoryProfile(MKTowerWorkspaceCategory.BASEMENT, 9, 9, dimensions.basementHeight(), 3),
                        new MKTowerWorkspaceCategoryProfile(MKTowerWorkspaceCategory.BOSS, 9, 9, dimensions.roomHeight(), 3),
                        new MKTowerWorkspaceCategoryProfile(MKTowerWorkspaceCategory.BASEMENT_CAP, 9, 9, dimensions.basementHeight(), 3)
                ),
                List.of(
                        new MKTowerWorkspaceFamilyDefinition("entry", MKTowerWorkspaceCategory.ENTRY, MKWorkspacePieceRole.ENTRY, true,
                                9, 9, dimensions.entranceHeight(),
                                List.of(new MKWorkspaceFamilyHorizontalExitDefinition(net.minecraft.core.Direction.SOUTH,
                                        MKWorkspaceHorizontalExitPathKind.MAIN, "entry_main"))),
                        new MKTowerWorkspaceFamilyDefinition("floor_main", MKTowerWorkspaceCategory.MAIN, MKWorkspacePieceRole.FLOOR_MAIN, true,
                                9, 9, dimensions.roomHeight(),
                                List.of(new MKWorkspaceFamilyHorizontalExitDefinition(net.minecraft.core.Direction.NORTH,
                                        MKWorkspaceHorizontalExitPathKind.BRANCH, "main_branch"))),
                        new MKTowerWorkspaceFamilyDefinition("boss_approach", MKTowerWorkspaceCategory.BOSS, MKWorkspacePieceRole.BOSS_APPROACH, true,
                                9, 9, dimensions.roomHeight(),
                                List.of()),
                        new MKTowerWorkspaceFamilyDefinition("boss_cap", MKTowerWorkspaceCategory.BOSS, MKWorkspacePieceRole.BOSS_CAP, true,
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
}
