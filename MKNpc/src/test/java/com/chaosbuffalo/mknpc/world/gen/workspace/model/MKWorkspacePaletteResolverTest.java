package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKPlannedPiece;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKTowerWorkspacePlanner;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MKWorkspacePaletteResolverTest {
    @Test
    void familyPaletteInheritsCategoryAndOverridesOnlySpecifiedRoles() {
        MKWorkspaceMaterialPalette base = palette("smooth_stone", "stone_bricks", "smooth_stone",
                "stone_brick_stairs", "stone_brick_slab", "ladder");
        MKWorkspacePaletteOverride categoryOverride = new MKWorkspacePaletteOverride(
                null,
                id("deepslate_bricks"),
                null,
                null,
                null,
                null
        );
        MKWorkspacePaletteOverride familyOverride = new MKWorkspacePaletteOverride(
                null,
                null,
                null,
                id("deepslate_brick_stairs"),
                null,
                null
        );
        MKTowerWorkspaceCategoryProfile mainProfile = new MKTowerWorkspaceCategoryProfile(
                MKTowerWorkspaceCategory.MAIN, 9, 9, 5, 1, 2, 10, categoryOverride);
        MKTowerWorkspaceFamilyDefinition family = new MKTowerWorkspaceFamilyDefinition(
                "floor_main",
                MKTowerWorkspaceCategory.MAIN,
                MKWorkspacePieceRole.FLOOR_MAIN,
                true,
                9,
                9,
                5,
                MKWorkspaceHorizontalExtrusionMode.FULL_BODY,
                List.of(),
                familyOverride
        );
        MKStructureWorkspace workspace = workspace(base, List.of(mainProfile), List.of(family), List.of());

        MKWorkspaceMaterialPalette resolved = new MKWorkspacePaletteResolver().resolveFamily(workspace, family);

        assertEquals(base.floorBlock(), resolved.floorBlock());
        assertEquals(id("deepslate_bricks"), resolved.wallBlock());
        assertEquals(base.ceilingBlock(), resolved.ceilingBlock());
        assertEquals(id("deepslate_brick_stairs"), resolved.stairBlock());
        assertEquals(base.slabBlock(), resolved.slabBlock());
        assertEquals(base.ladderBlock(), resolved.ladderBlock());
    }

    @Test
    void hallwayFamilyUsesSameResolverWithoutCategory() {
        MKWorkspaceMaterialPalette base = palette("smooth_stone", "stone_bricks", "smooth_stone",
                "stone_brick_stairs", "stone_brick_slab", "ladder");
        MKWorkspacePaletteOverride hallwayOverride = new MKWorkspacePaletteOverride(
                id("polished_deepslate"),
                null,
                null,
                null,
                null,
                id("vine")
        );
        MKHallwayFamilyDefinition hallway = new MKHallwayFamilyDefinition(
                "branch", "branch_opening", 5, 3, 3, 0, false, true, hallwayOverride);
        MKStructureWorkspace workspace = workspace(base, MKTowerWorkspaceCategoryProfile.createDefaults(MKWorkspaceDimensions.defaultDimensions()),
                List.of(), List.of(hallway));

        MKWorkspaceMaterialPalette resolved = new MKWorkspacePaletteResolver().resolveFamily(workspace, hallway);

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
        MKTowerWorkspaceCategoryProfile mainProfile = new MKTowerWorkspaceCategoryProfile(
                MKTowerWorkspaceCategory.MAIN, 9, 9, 5);
        MKTowerWorkspaceFamilyDefinition family = new MKTowerWorkspaceFamilyDefinition(
                "floor_main",
                MKTowerWorkspaceCategory.MAIN,
                MKWorkspacePieceRole.FLOOR_MAIN,
                true,
                9,
                9,
                5,
                MKWorkspaceHorizontalExtrusionMode.FULL_BODY,
                List.of(),
                familyOverride
        );
        MKStructureWorkspace workspace = workspace(base, List.of(mainProfile), List.of(family), List.of());

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
        MKWorkspacePaletteOverride categoryOverride = new MKWorkspacePaletteOverride(
                null,
                id("deepslate_bricks"),
                null,
                null,
                null,
                null
        );
        MKWorkspacePaletteOverride familyOverride = new MKWorkspacePaletteOverride(
                null,
                null,
                null,
                id("spruce_stairs"),
                id("spruce_slab"),
                null
        );
        MKWorkspacePaletteOverride hallwayOverride = new MKWorkspacePaletteOverride(
                id("polished_deepslate"),
                null,
                null,
                null,
                null,
                id("vine")
        );
        MKTowerWorkspaceCategoryProfile mainProfile = new MKTowerWorkspaceCategoryProfile(
                MKTowerWorkspaceCategory.MAIN, 9, 9, 5, 1, 2, 10, categoryOverride);
        MKTowerWorkspaceFamilyDefinition family = new MKTowerWorkspaceFamilyDefinition(
                "floor_main", MKTowerWorkspaceCategory.MAIN, MKWorkspacePieceRole.FLOOR_MAIN,
                true, 9, 9, 5, MKWorkspaceHorizontalExtrusionMode.FULL_BODY, List.of(),
                familyOverride);
        MKHallwayFamilyDefinition hallway = new MKHallwayFamilyDefinition(
                "branch", "branch_opening", 5, 3, 3, 0, false, true, hallwayOverride);
        MKStructureWorkspace roundTripped = MKStructureWorkspace.fromTag(
                workspace(base, List.of(mainProfile), List.of(family), List.of(hallway)).toTag());

        assertEquals(id("oak_stairs"), roundTripped.palette().stairBlock());
        assertEquals(id("oak_slab"), roundTripped.palette().slabBlock());
        assertEquals(id("ladder"), roundTripped.palette().ladderBlock());
        assertTrue(roundTripped.categoryProfile(MKTowerWorkspaceCategory.MAIN).orElseThrow().paletteOverrideOpt().isPresent());
        assertTrue(roundTripped.familyDefinitions().getFirst().paletteOverrideOpt().isPresent());
        assertTrue(roundTripped.hallwayFamilies().getFirst().paletteOverrideOpt().isPresent());
        assertEquals(id("spruce_stairs"),
                roundTripped.familyDefinitions().getFirst().paletteOverrideOpt().orElseThrow().stairBlockOpt().orElseThrow());
        assertEquals(id("vine"),
                roundTripped.hallwayFamilies().getFirst().paletteOverrideOpt().orElseThrow().ladderBlockOpt().orElseThrow());
    }

    private static MKStructureWorkspace workspace(MKWorkspaceMaterialPalette palette,
                                                  List<MKTowerWorkspaceCategoryProfile> categoryProfiles,
                                                  List<MKTowerWorkspaceFamilyDefinition> familyDefinitions,
                                                  List<MKHallwayFamilyDefinition> hallwayFamilies) {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        MKWorkspaceStairAuthoringConfig stairConfig = MKWorkspaceStairAuthoringConfig.defaultConfig();
        return new MKStructureWorkspace(
                UUID.randomUUID(),
                BlockPos.ZERO,
                "mknpc",
                "palette_test",
                MKStructureFamilyType.TOWER,
                dimensions,
                palette,
                stairConfig,
                MKVerticalAccessPlacement.CENTER,
                1,
                2,
                4,
                MKWorkspaceVerticalAccessSpec.defaultSpec(),
                MKTowerWorkspaceFloorSettings.defaultSettings(),
                categoryProfiles,
                familyDefinitions,
                MKHorizontalOpeningProfile.createDefaults(dimensions),
                hallwayFamilies,
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
