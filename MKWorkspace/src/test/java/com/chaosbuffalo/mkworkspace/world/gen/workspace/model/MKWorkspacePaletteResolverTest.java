package com.chaosbuffalo.mkworkspace.world.gen.workspace.model;

import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKFloorRoomKind;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKFloorTopologySettings;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKHorizontalExitPathKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.*;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKPlannedPiece;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKFloorTopologyPlanner;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKWorkspaceVerticalStackPlanner;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKTowerWorkspacePlanner;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKWalledKeepWorkspacePlanner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MKWorkspacePaletteResolverTest {
    @Test
    void topologyGroupPaletteWalksDotHierarchy() {
        MKWorkspaceMaterialPalette base = palette("smooth_stone", "stone_bricks", "smooth_stone",
                "stone_brick_stairs", "stone_brick_slab", "ladder");
        MKWorkspaceTopologyProfile topologyProfile = MKWalledKeepWorkspacePlanner.defaultTopologyProfile(false)
                .withPlannerScopePaletteOverride("keep", Optional.of(new MKWorkspacePaletteOverride(
                        null, id("deepslate_bricks"), null, null, null, null)))
                .withPlannerScopePaletteOverride("keep.center", Optional.of(new MKWorkspacePaletteOverride(
                        id("red_sandstone"), null, null, null, null, null)))
                .withPlannerScopePaletteOverride("keep.center.main_floor", Optional.of(new MKWorkspacePaletteOverride(
                        null, null, id("bamboo_planks"), null, null, null)));
        MKStructureWorkspace workspace = workspace(topologyProfile, base, List.of(), List.of());

        MKWorkspaceMaterialPalette resolved = new MKWorkspacePaletteResolver()
                .resolvePlannerScope(workspace, "keep.center.main_floor");

        assertEquals(id("red_sandstone"), resolved.floorBlock());
        assertEquals(id("deepslate_bricks"), resolved.wallBlock());
        assertEquals(id("bamboo_planks"), resolved.ceilingBlock());
    }

    @Test
    void floorTopologyPaletteInheritsStackTopologyGroupPalette() {
        MKWorkspaceMaterialPalette base = palette("smooth_stone", "stone_bricks", "smooth_stone",
                "stone_brick_stairs", "stone_brick_slab", "ladder");
        MKWorkspaceTopologyProfile topologyProfile = MKTowerWorkspacePlanner.defaultTopologyProfile()
                .withPlannerScopePaletteOverride("tower", Optional.of(new MKWorkspacePaletteOverride(
                        null, id("deepslate_bricks"), null, null, null, null)))
                .withPlannerScopePaletteOverride("tower.primary", Optional.of(new MKWorkspacePaletteOverride(
                        id("red_sandstone"), null, null, null, null, null)))
                .withPlannerScopePaletteOverride("tower.primary.main_floor", Optional.of(new MKWorkspacePaletteOverride(
                        null, null, id("bamboo_planks"), null, null, null)));
        MKStructureWorkspace workspace = workspace(topologyProfile, base, List.of(), List.of());

        MKWorkspaceMaterialPalette resolved = new MKWorkspacePaletteResolver()
                .resolveFloorTopology(workspace, "tower.primary", "main_floor");

        assertEquals(id("red_sandstone"), resolved.floorBlock());
        assertEquals(id("deepslate_bricks"), resolved.wallBlock());
        assertEquals(id("bamboo_planks"), resolved.ceilingBlock());
    }

    @Test
    void familyPaletteAppliesFamilyOverrideOnlyForSpecifiedRoles() {
        MKWorkspaceMaterialPalette base = palette("smooth_stone", "stone_bricks", "smooth_stone",
                "stone_brick_stairs", "stone_brick_slab", "ladder");
        MKWorkspacePaletteOverride familyOverride = new MKWorkspacePaletteOverride(
                null,
                null,
                null,
                id("deepslate_brick_stairs"),
                null,
                null
        );
        MKWorkspaceRoomFamilyDefinition family = MKWorkspaceRoomFamilyDefinition.forTopologySlot(
                "floor_main",
                MKWorkspaceTopologySlotMetadata.fromTopologyRole("custom.main", "floor", "room", false),
                "",
                true,
                9,
                9,
                5,
                MKWorkspaceHorizontalExtrusionMode.FULL_BODY,
                List.of(),
                0,
                0,
                null,
                familyOverride
        );
        MKStructureWorkspace workspace = workspace(base, List.of(family), List.of());

        MKWorkspaceMaterialPalette resolved = new MKWorkspacePaletteResolver().resolveFamily(workspace, family);

        assertEquals(base.floorBlock(), resolved.floorBlock());
        assertEquals(base.wallBlock(), resolved.wallBlock());
        assertEquals(base.ceilingBlock(), resolved.ceilingBlock());
        assertEquals(id("deepslate_brick_stairs"), resolved.stairBlock());
        assertEquals(base.slabBlock(), resolved.slabBlock());
        assertEquals(base.ladderBlock(), resolved.ladderBlock());
    }

    @Test
    void stackBackedFamilyPaletteUsesStackDefaults() {
        MKWorkspaceMaterialPalette base = palette("smooth_stone", "stone_bricks", "smooth_stone",
                "stone_brick_stairs", "stone_brick_slab", "ladder");
        MKWorkspacePaletteOverride stackOverride = new MKWorkspacePaletteOverride(
                null,
                id("deepslate_bricks"),
                null,
                null,
                null,
                null
        );
        MKWorkspaceRoomFamilyDefinition family = MKWorkspaceRoomFamilyDefinition.forVerticalStackSlot(
                "floor_main",
                MKWorkspaceVerticalStackSlot.MAIN_FLOOR,
                "tower.primary",
                true,
                0,
                0,
                0,
                MKWorkspaceHorizontalExtrusionMode.FULL_BODY,
                List.of(),
                0,
                0,
                null,
                null
        );
        MKWorkspaceTopologyProfile topologyProfile = MKTowerWorkspacePlanner.defaultTopologyProfile()
                .withVerticalStackSettings(MKTowerWorkspacePlanner.defaultTopologyProfile()
                        .verticalStackSettingsOrDefault("tower.primary")
                        .withPaletteOverride(java.util.Optional.of(stackOverride)));
        MKStructureWorkspace workspace = workspace(topologyProfile, base, List.of(family), List.of());

        MKWorkspaceMaterialPalette resolved = new MKWorkspacePaletteResolver().resolveFamily(workspace, family);

        assertEquals(id("deepslate_bricks"), resolved.wallBlock());
    }

    @Test
    void floorTopologyPaletteOverridesStackPaletteForRootRoomsAndFloorPlanPieces() {
        MKWorkspaceMaterialPalette base = palette("smooth_stone", "stone_bricks", "smooth_stone",
                "stone_brick_stairs", "stone_brick_slab", "ladder");
        MKWorkspacePaletteOverride stackOverride = new MKWorkspacePaletteOverride(
                null,
                id("deepslate_bricks"),
                null,
                null,
                null,
                null
        );
        MKWorkspacePaletteOverride floorOverride = new MKWorkspacePaletteOverride(
                id("red_sandstone"),
                null,
                null,
                null,
                null,
                null
        );
        MKWorkspacePaletteOverride roomOverride = new MKWorkspacePaletteOverride(
                null,
                null,
                id("bamboo_planks"),
                null,
                null,
                null
        );
        MKWorkspaceVerticalStackSettings stackSettings = MKTowerWorkspacePlanner.defaultTopologyProfile()
                .verticalStackSettingsOrDefault("tower.primary")
                .withPaletteOverride(java.util.Optional.of(stackOverride));
        MKFloorTopologySettings floorSettings = MKFloorTopologySettings
                .defaults(stackSettings, MKWorkspaceVerticalStackSlot.MAIN_FLOOR.suffix())
                .withPaletteOverride(java.util.Optional.of(floorOverride));
        floorSettings = floorSettings.withRoomProfile(MKFloorRoomKind.MAIN_ROOM, 0,
                floorSettings.mainRoomProfiles().getFirst().withPaletteOverride(java.util.Optional.of(roomOverride)));
        MKWorkspaceTopologyProfile topologyProfile = MKTowerWorkspacePlanner.defaultTopologyProfile()
                .withVerticalStackSettings(stackSettings)
                .withFloorTopologySettings(floorSettings);
        MKWorkspaceRoomFamilyDefinition family = MKWorkspaceRoomFamilyDefinition.forVerticalStackSlot(
                "floor_main",
                MKWorkspaceVerticalStackSlot.MAIN_FLOOR,
                "tower.primary",
                true,
                0,
                0,
                0,
                MKWorkspaceHorizontalExtrusionMode.FULL_BODY,
                List.of(new MKFamilyHorizontalExitDefinition(
                        Direction.NORTH,
                        MKHorizontalExitPathKind.MAIN_EXIT,
                        "main_opening",
                        MKWorkspaceHorizontalExitConnectionMode.LINEAR_RUN
                )),
                0,
                0,
                null,
                null
        );
        MKStructureWorkspace workspace = workspace(topologyProfile, base, List.of(family), List.of());

        MKPlannedPiece rootRoom = new MKWorkspaceVerticalStackPlanner(MKTowerWorkspacePlanner.PRIMARY_STACK_ID).createRoomPieces(workspace, List.of(family)).getFirst();
        List<MKPlannedPiece> floorPieces = new MKFloorTopologyPlanner().createFloorTopologyPieces(workspace,
                List.of(family));
        MKPlannedPiece floorRoom = floorPieces.stream()
                .filter(piece -> "floor_plan_room".equals(piece.tags().get("tower_piece_kind")))
                .filter(piece -> "main_room".equals(piece.tags().get("workspace_floor_room_kind")))
                .findFirst()
                .orElseThrow();
        MKPlannedPiece hallway = floorPieces.stream()
                .filter(piece -> "floor_plan_linear_run".equals(piece.tags().get("tower_piece_kind")))
                .findFirst()
                .orElseThrow();

        assertEquals(id("red_sandstone").toString(), rootRoom.tags().get(MKWorkspacePaletteTags.FLOOR_BLOCK_TAG));
        assertEquals(id("deepslate_bricks").toString(), rootRoom.tags().get(MKWorkspacePaletteTags.WALL_BLOCK_TAG));
        assertEquals(id("red_sandstone").toString(), floorRoom.tags().get(MKWorkspacePaletteTags.FLOOR_BLOCK_TAG));
        assertEquals(id("deepslate_bricks").toString(), floorRoom.tags().get(MKWorkspacePaletteTags.WALL_BLOCK_TAG));
        assertEquals(id("bamboo_planks").toString(), floorRoom.tags().get(MKWorkspacePaletteTags.CEILING_BLOCK_TAG));
        assertEquals(id("red_sandstone").toString(), hallway.tags().get(MKWorkspacePaletteTags.FLOOR_BLOCK_TAG));
        assertEquals(id("deepslate_bricks").toString(), hallway.tags().get(MKWorkspacePaletteTags.WALL_BLOCK_TAG));
    }

    @Test
    void linearRunFamilyUsesSameResolverWithoutTopologySlotMetadata() {
        MKWorkspaceMaterialPalette base = palette("smooth_stone", "stone_bricks", "smooth_stone",
                "stone_brick_stairs", "stone_brick_slab", "ladder");
        MKWorkspacePaletteOverride linearRunOverride = new MKWorkspacePaletteOverride(
                id("polished_deepslate"),
                null,
                null,
                null,
                null,
                id("vine")
        );
        MKWorkspaceLinearRunFamilyDefinition linearRun = new MKWorkspaceLinearRunFamilyDefinition(
                "branch", MKWorkspaceLinearRunKind.ENCLOSED_CORRIDOR, "branch_opening", 5, 3, 3,
                0, false, true, MKWorkspaceLinearRunProjection.RIGID,
                List.of(MKWorkspaceLinearRunPieceShape.STRAIGHT), linearRunOverride);
        MKStructureWorkspace workspace = workspace(base, List.of(), List.of(linearRun));

        MKWorkspaceMaterialPalette resolved = new MKWorkspacePaletteResolver().resolveFamily(workspace, linearRun);

        assertEquals(id("polished_deepslate"), resolved.floorBlock());
        assertEquals(base.wallBlock(), resolved.wallBlock());
        assertEquals(id("vine"), resolved.ladderBlock());
    }

    @Test
    void plannerWritesResolvedPaletteTagsForRoomFamilies() {
        MKWorkspaceMaterialPalette base = palette("smooth_stone", "stone_bricks", "smooth_stone",
                "stone_brick_stairs", "stone_brick_slab", "ladder");
        MKWorkspacePaletteOverride familyOverride = new MKWorkspacePaletteOverride(
                null,
                id("deepslate_bricks"),
                null,
                id("deepslate_brick_stairs"),
                id("deepslate_brick_slab"),
                id("vine")
        );
        MKWorkspaceRoomFamilyDefinition family = MKWorkspaceRoomFamilyDefinition.forTopologySlot(
                "floor_main",
                MKWorkspaceTopologySlotMetadata.fromTopologySlotId("tower.primary.main_floor"),
                "tower.primary",
                true,
                9,
                9,
                5,
                MKWorkspaceHorizontalExtrusionMode.FULL_BODY,
                List.of(),
                0,
                0,
                familyOverride
        );
        MKStructureWorkspace workspace = workspace(base, List.of(family), List.of());

        MKPlannedPiece planned = new MKTowerWorkspacePlanner().createCanonicalPieces(workspace).getFirst();

        assertEquals(id("deepslate_bricks").toString(), planned.tags().get(MKWorkspacePaletteTags.WALL_BLOCK_TAG));
        assertEquals(id("deepslate_brick_stairs").toString(), planned.tags().get(MKWorkspacePaletteTags.STAIR_BLOCK_TAG));
        assertEquals(id("deepslate_brick_slab").toString(), planned.tags().get(MKWorkspacePaletteTags.SLAB_BLOCK_TAG));
        assertEquals(id("vine").toString(), planned.tags().get(MKWorkspacePaletteTags.LADDER_BLOCK_TAG));
    }

    @Test
    void workspaceCodecPreservesPaletteRolesAndScopedOverrides() {
        MKWorkspaceMaterialPalette base = palette("smooth_stone", "stone_bricks", "smooth_stone",
                "oak_stairs", "oak_slab", "ladder");
        MKWorkspacePaletteOverride familyOverride = new MKWorkspacePaletteOverride(
                null,
                null,
                null,
                id("spruce_stairs"),
                id("spruce_slab"),
                null
        );
        MKWorkspacePaletteOverride linearRunOverride = new MKWorkspacePaletteOverride(
                id("polished_deepslate"),
                null,
                null,
                null,
                null,
                id("vine")
        );
        MKWorkspaceRoomFamilyDefinition family = MKWorkspaceRoomFamilyDefinition.forTopologySlot(
                "floor_main", MKWorkspaceTopologySlotMetadata.fromTopologySlotId("tower.primary.main_floor"),
                "tower.primary", true, 9, 9, 5, MKWorkspaceHorizontalExtrusionMode.FULL_BODY, List.of(),
                0, 0,
                familyOverride);
        MKWorkspaceLinearRunFamilyDefinition linearRun = new MKWorkspaceLinearRunFamilyDefinition(
                "branch", MKWorkspaceLinearRunKind.ENCLOSED_CORRIDOR, "branch_opening", 5, 3, 3,
                0, false, true, MKWorkspaceLinearRunProjection.RIGID,
                List.of(MKWorkspaceLinearRunPieceShape.STRAIGHT), linearRunOverride);
        MKStructureWorkspace roundTripped = MKStructureWorkspace.fromTag(
                workspace(base, List.of(family), List.of(linearRun)).toTag());

        assertEquals(id("oak_stairs"), roundTripped.palette().stairBlock());
        assertEquals(id("oak_slab"), roundTripped.palette().slabBlock());
        assertEquals(id("ladder"), roundTripped.palette().ladderBlock());
        assertTrue(roundTripped.familyDefinitions().getFirst().paletteOverrideOpt().isPresent());
        assertTrue(roundTripped.linearRunFamilies().getFirst().paletteOverrideOpt().isPresent());
        assertEquals(id("spruce_stairs"),
                roundTripped.familyDefinitions().getFirst().paletteOverrideOpt().orElseThrow().stairBlockOpt().orElseThrow());
        assertEquals(id("vine"),
                roundTripped.linearRunFamilies().getFirst().paletteOverrideOpt().orElseThrow().ladderBlockOpt().orElseThrow());
    }

    @Test
    void workspaceCodecPreservesplannerScopePaletteOverrides() {
        MKWorkspaceMaterialPalette base = palette("smooth_stone", "stone_bricks", "smooth_stone",
                "oak_stairs", "oak_slab", "ladder");
        MKWorkspaceTopologyProfile topologyProfile = MKTowerWorkspacePlanner.defaultTopologyProfile()
                .withPlannerScopePaletteOverride("tower.primary.main_floor", Optional.of(new MKWorkspacePaletteOverride(
                        id("red_sandstone"), null, null, null, null, null)));

        MKStructureWorkspace roundTripped = MKStructureWorkspace.fromTag(
                workspace(topologyProfile, base, List.of(), List.of()).toTag());

        assertEquals(id("red_sandstone"), roundTripped.topologyProfile()
                .plannerScopePaletteOverride("tower.primary.main_floor")
                .orElseThrow()
                .floorBlockOpt()
                .orElseThrow());
    }

    @Test
    void paletteSwapSafetyRejectsSharedSourceBlockWithDivergentTargets() {
        MKWorkspaceMaterialPalette source = palette("smooth_stone", "stone_bricks", "smooth_stone",
                "stone_brick_stairs", "stone_brick_slab", "ladder");
        MKWorkspaceMaterialPalette target = palette("smooth_stone", "stone_bricks", "deepslate_bricks",
                "stone_brick_stairs", "stone_brick_slab", "ladder");

        assertFalse(MKWorkspacePaletteSwapSafety.canRepresentAsBlockReplacement(source, target));
    }

    @Test
    void paletteSwapSafetyAllowsSharedSourceBlockWhenTargetIsSharedToo() {
        MKWorkspaceMaterialPalette source = palette("smooth_stone", "stone_bricks", "smooth_stone",
                "stone_brick_stairs", "stone_brick_slab", "ladder");
        MKWorkspaceMaterialPalette target = palette("deepslate_bricks", "stone_bricks", "deepslate_bricks",
                "stone_brick_stairs", "stone_brick_slab", "ladder");

        assertTrue(MKWorkspacePaletteSwapSafety.canRepresentAsBlockReplacement(source, target));
    }

    private static MKStructureWorkspace workspace(MKWorkspaceMaterialPalette palette,
                                                  List<MKWorkspaceRoomFamilyDefinition> familyDefinitions,
                                                  List<MKWorkspaceLinearRunFamilyDefinition> linearRunFamilies) {
        return workspace(MKTowerWorkspacePlanner.defaultTopologyProfile(), palette, familyDefinitions,
                linearRunFamilies);
    }

    private static MKStructureWorkspace workspace(MKWorkspaceTopologyProfile topologyProfile,
                                                  MKWorkspaceMaterialPalette palette,
                                                  List<MKWorkspaceRoomFamilyDefinition> familyDefinitions,
                                                  List<MKWorkspaceLinearRunFamilyDefinition> linearRunFamilies) {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        MKWorkspaceStairAuthoringConfig stairConfig = MKWorkspaceStairAuthoringConfig.defaultConfig();
        return new MKStructureWorkspace(
                UUID.randomUUID(),
                BlockPos.ZERO,
                "mknpc",
                "palette_test",
                topologyProfile,
                dimensions,
                palette,
                stairConfig,
                MKVerticalAccessPlacement.CENTER,
                1,
                2,
                4,
                MKWorkspaceVerticalAccessSpec.defaultSpec(),
                familyDefinitions,
                MKHorizontalOpeningProfile.createDefaults(dimensions),
                linearRunFamilies,
                0,
                0,
                List.of()
        );
    }

    private static MKWorkspaceMaterialPalette palette(String floor, String wall, String ceiling, String stair,
                                                      String slab, String ladder) {
        return new MKWorkspaceMaterialPalette(id(floor), id(wall), id(ceiling), id(stair), id(slab), id(ladder));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("minecraft", path);
    }
}
