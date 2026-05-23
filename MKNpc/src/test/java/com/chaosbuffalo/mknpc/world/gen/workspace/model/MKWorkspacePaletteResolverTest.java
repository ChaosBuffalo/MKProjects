package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKPlannedPiece;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKTowerWorkspacePlanner;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MKWorkspacePaletteResolverTest {
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
        MKTowerWorkspaceFamilyDefinition family = MKTowerWorkspaceFamilyDefinition.forTopologySlot(
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
        MKTowerWorkspaceFamilyDefinition family = MKTowerWorkspaceFamilyDefinition.forTowerStackSlot(
                "floor_main",
                MKTowerWorkspaceStackSlot.MAIN_FLOOR,
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
        MKWorkspaceTopologyProfile topologyProfile = MKWorkspaceTopologyProfile.tower()
                .withTowerStackSettings(MKWorkspaceTopologyProfile.tower()
                        .towerStackSettingsOrDefault("tower.primary")
                        .withPaletteOverride(java.util.Optional.of(stackOverride)));
        MKStructureWorkspace workspace = workspace(topologyProfile, base, List.of(family), List.of());

        MKWorkspaceMaterialPalette resolved = new MKWorkspacePaletteResolver().resolveFamily(workspace, family);

        assertEquals(id("deepslate_bricks"), resolved.wallBlock());
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
        MKTowerWorkspaceFamilyDefinition family = MKTowerWorkspaceFamilyDefinition.forTopologySlot(
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
        MKTowerWorkspaceFamilyDefinition family = MKTowerWorkspaceFamilyDefinition.forTopologySlot(
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
                                                  List<MKTowerWorkspaceFamilyDefinition> familyDefinitions,
                                                  List<MKWorkspaceLinearRunFamilyDefinition> linearRunFamilies) {
        return workspace(MKWorkspaceTopologyProfile.tower(), palette, familyDefinitions,
                linearRunFamilies);
    }

    private static MKStructureWorkspace workspace(MKWorkspaceTopologyProfile topologyProfile,
                                                  MKWorkspaceMaterialPalette palette,
                                                  List<MKTowerWorkspaceFamilyDefinition> familyDefinitions,
                                                  List<MKWorkspaceLinearRunFamilyDefinition> linearRunFamilies) {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        MKWorkspaceStairAuthoringConfig stairConfig = MKWorkspaceStairAuthoringConfig.defaultConfig();
        return new MKStructureWorkspace(
                UUID.randomUUID(),
                BlockPos.ZERO,
                "mknpc",
                "palette_test",
                MKStructureFamilyType.TOWER,
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
