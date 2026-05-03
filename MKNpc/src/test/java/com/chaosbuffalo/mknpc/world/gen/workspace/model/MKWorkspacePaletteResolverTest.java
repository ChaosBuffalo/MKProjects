package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKPlannedPiece;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKTowerWorkspacePlanner;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MKWorkspacePaletteResolverTest {
    @Test
    void familyPaletteInheritsCategoryAndOverridesOnlySpecifiedRoles() {
        MKWorkspaceMaterialPalette base = palette("smooth_stone", "stone_bricks", "smooth_stone",
                "stone_brick_stairs", "stone_brick_slab", "ladder");
        MKWorkspacePaletteOverride categoryOverride = new MKWorkspacePaletteOverride(
                Optional.empty(),
                Optional.of(id("deepslate_bricks")),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
        MKWorkspacePaletteOverride familyOverride = new MKWorkspacePaletteOverride(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(id("deepslate_brick_stairs")),
                Optional.empty(),
                Optional.empty()
        );
        MKTowerWorkspaceCategoryProfile mainProfile = new MKTowerWorkspaceCategoryProfile(
                MKTowerWorkspaceCategory.MAIN, 9, 9, 5, 1, 2, 10, Optional.of(categoryOverride));
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
                Optional.of(familyOverride)
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
                Optional.of(id("polished_deepslate")),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(id("vine"))
        );
        MKHallwayFamilyDefinition hallway = new MKHallwayFamilyDefinition(
                "branch", "branch_opening", 5, 3, 3, 0, false, true, Optional.of(hallwayOverride));
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
                Optional.empty(),
                Optional.of(id("deepslate_bricks")),
                Optional.empty(),
                Optional.of(id("deepslate_brick_stairs")),
                Optional.of(id("deepslate_brick_slab")),
                Optional.of(id("vine"))
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
                Optional.of(familyOverride)
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
                Optional.empty(),
                Optional.of(id("deepslate_bricks")),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
        MKWorkspacePaletteOverride familyOverride = new MKWorkspacePaletteOverride(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(id("spruce_stairs")),
                Optional.of(id("spruce_slab")),
                Optional.empty()
        );
        MKWorkspacePaletteOverride hallwayOverride = new MKWorkspacePaletteOverride(
                Optional.of(id("polished_deepslate")),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(id("vine"))
        );
        MKTowerWorkspaceCategoryProfile mainProfile = new MKTowerWorkspaceCategoryProfile(
                MKTowerWorkspaceCategory.MAIN, 9, 9, 5, 1, 2, 10, Optional.of(categoryOverride));
        MKTowerWorkspaceFamilyDefinition family = new MKTowerWorkspaceFamilyDefinition(
                "floor_main", MKTowerWorkspaceCategory.MAIN, MKWorkspacePieceRole.FLOOR_MAIN,
                true, 9, 9, 5, MKWorkspaceHorizontalExtrusionMode.FULL_BODY, List.of(),
                Optional.of(familyOverride));
        MKHallwayFamilyDefinition hallway = new MKHallwayFamilyDefinition(
                "branch", "branch_opening", 5, 3, 3, 0, false, true, Optional.of(hallwayOverride));
        MKStructureWorkspace roundTripped = MKStructureWorkspace.fromTag(
                workspace(base, List.of(mainProfile), List.of(family), List.of(hallway)).toTag());

        assertEquals(id("oak_stairs"), roundTripped.palette().stairBlock());
        assertEquals(id("oak_slab"), roundTripped.palette().slabBlock());
        assertEquals(id("ladder"), roundTripped.palette().ladderBlock());
        assertTrue(roundTripped.categoryProfile(MKTowerWorkspaceCategory.MAIN).orElseThrow().paletteOverride().isPresent());
        assertTrue(roundTripped.familyDefinitions().getFirst().paletteOverride().isPresent());
        assertTrue(roundTripped.hallwayFamilies().getFirst().paletteOverride().isPresent());
        assertEquals(id("spruce_stairs"),
                roundTripped.familyDefinitions().getFirst().paletteOverride().orElseThrow().stairBlock().orElseThrow());
        assertEquals(id("vine"),
                roundTripped.hallwayFamilies().getFirst().paletteOverride().orElseThrow().ladderBlock().orElseThrow());
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
