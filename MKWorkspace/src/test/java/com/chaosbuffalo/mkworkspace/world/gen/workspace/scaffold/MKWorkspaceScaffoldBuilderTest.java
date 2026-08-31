package com.chaosbuffalo.mkworkspace.world.gen.workspace.scaffold;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceHorizontalExtrusionMode;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceContentSelectionTags;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKPlannedConnector;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKPlannedPiece;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MKWorkspaceScaffoldBuilderTest {
    @Test
    void workspaceClearBoundsExtendToBuildHeight() {
        MKWorkspaceScaffoldBuilder builder = new MKWorkspaceScaffoldBuilder();

        BoundingBox expanded = builder.extendToWorkspaceClearHeight(
                new BoundingBox(10, 80, 20, 30, 96, 40),
                -64,
                319
        );

        assertEquals(10, expanded.minX());
        assertEquals(-64, expanded.minY());
        assertEquals(20, expanded.minZ());
        assertEquals(30, expanded.maxX());
        assertEquals(319, expanded.maxY());
        assertEquals(40, expanded.maxZ());
    }

    @Test
    void linearRunKindsUseDistinctScaffoldStyles() {
        MKWorkspaceScaffoldBuilder builder = new MKWorkspaceScaffoldBuilder();

        assertEquals(MKWorkspaceScaffoldBuilder.LinearRunScaffoldStyle.SOLID_WALL,
                builder.linearRunScaffoldStyle(linearRun("solid_wall")));
        assertEquals(MKWorkspaceScaffoldBuilder.LinearRunScaffoldStyle.PARAPET,
                builder.linearRunScaffoldStyle(linearRun("parapet")));
        assertEquals(MKWorkspaceScaffoldBuilder.LinearRunScaffoldStyle.OPEN_WALKWAY,
                builder.linearRunScaffoldStyle(linearRun("open_walkway")));
        assertEquals(MKWorkspaceScaffoldBuilder.LinearRunScaffoldStyle.DEFENSIVE_WALL,
                builder.linearRunScaffoldStyle(linearRun("defensive_wall")));
        assertEquals(MKWorkspaceScaffoldBuilder.LinearRunScaffoldStyle.ENCLOSED_CORRIDOR,
                builder.linearRunScaffoldStyle(linearRun("enclosed_corridor")));
    }

    @Test
    void floorPlanLinearRunKindsUseDistinctScaffoldStyles() {
        MKWorkspaceScaffoldBuilder builder = new MKWorkspaceScaffoldBuilder();

        assertEquals(MKWorkspaceScaffoldBuilder.LinearRunScaffoldStyle.SOLID_WALL,
                builder.linearRunScaffoldStyle(floorPlanLinearRun("solid_wall")));
        assertEquals(MKWorkspaceScaffoldBuilder.LinearRunScaffoldStyle.ENCLOSED_CORRIDOR,
                builder.linearRunScaffoldStyle(floorPlanLinearRun("enclosed_corridor")));
    }

    @Test
    void connectorFaceLayersUseFloorWallAndCeilingPaletteBlocks() {
        MKWorkspaceScaffoldBuilder builder = new MKWorkspaceScaffoldBuilder();

        assertEquals(MKWorkspaceScaffoldBuilder.ConnectorFaceLayer.FLOOR,
                builder.connectorFaceLayer(10, 10, 16, 1));
        assertEquals(MKWorkspaceScaffoldBuilder.ConnectorFaceLayer.WALL,
                builder.connectorFaceLayer(13, 10, 16, 1));
        assertEquals(MKWorkspaceScaffoldBuilder.ConnectorFaceLayer.CEILING,
                builder.connectorFaceLayer(16, 10, 16, 1));
    }

    @Test
    void horizontalExtrusionModeUsesConnectorOverrideThenPieceTagsAndLinearRunStyle() {
        MKWorkspaceScaffoldBuilder builder = new MKWorkspaceScaffoldBuilder();
        MKPlannedConnector connector = connector(null);
        MKPlannedConnector floorOnlyConnector = connector(MKWorkspaceHorizontalExtrusionMode.FLOOR_ONLY);

        assertEquals(MKWorkspaceHorizontalExtrusionMode.FLOOR_ONLY,
                builder.getHorizontalExtrusionMode(pieceKind("room"), floorOnlyConnector,
                        MKWorkspaceScaffoldBuilder.LinearRunScaffoldStyle.ENCLOSED_CORRIDOR));
        assertEquals(MKWorkspaceHorizontalExtrusionMode.FULL_FACE,
                builder.getHorizontalExtrusionMode(linearRun("defensive_wall"), connector,
                        MKWorkspaceScaffoldBuilder.LinearRunScaffoldStyle.DEFENSIVE_WALL));
        assertEquals(MKWorkspaceHorizontalExtrusionMode.FULL_FACE,
                builder.getHorizontalExtrusionMode(pieceWithTag("workspace_horizontal_extrusion_mode", "full_face"),
                        connector, MKWorkspaceScaffoldBuilder.LinearRunScaffoldStyle.ENCLOSED_CORRIDOR));
        assertEquals(MKWorkspaceHorizontalExtrusionMode.FULL_BODY,
                builder.getHorizontalExtrusionMode(pieceKind("room"), connector,
                        MKWorkspaceScaffoldBuilder.LinearRunScaffoldStyle.ENCLOSED_CORRIDOR));
    }

    @Test
    void selectedLayoutClearBoundsMergeAcrossVariantRowGaps() {
        MKWorkspaceScaffoldBuilder builder = new MKWorkspaceScaffoldBuilder();
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        MKPlannedPiece templateA = plannedPiece("piece_a", 0);
        MKPlannedPiece templateB = plannedPiece("piece_b", 0);
        MKPlannedPiece variantA = plannedPiece("piece_a", 1);
        MKPlannedPiece variantB = plannedPiece("piece_b", 1);

        BoundingBox clearBounds = builder.layoutClearBoundsForPieces(workspace,
                List.of(templateA, templateB, variantA, variantB),
                List.of(variantA, variantB));

        assertNotNull(clearBounds);
        assertEquals(4, clearBounds.minX());
        assertEquals(-4, clearBounds.minY());
        assertEquals(21, clearBounds.minZ());
        assertEquals(43, clearBounds.maxX());
        assertEquals(10, clearBounds.maxY());
        assertEquals(43, clearBounds.maxZ());
    }

    @Test
    void gridLayoutUsesEachFamilyColumnWidthInsteadOfGlobalMaxWidth() {
        MKWorkspaceGridLayout layout = new MKWorkspaceGridLayout();
        MKPlannedPiece smallA = plannedPiece("small_a", 0, 5, 5, 5);
        MKPlannedPiece wide = plannedPiece("wide", 0, 17, 5, 5);
        MKPlannedPiece smallB = plannedPiece("small_b", 0, 5, 5, 5);

        List<MKWorkspaceGridLayout.Placement> placements = layout.assignPlacements(BlockPos.ZERO,
                List.of(smallA, wide, smallB), 1, 2, 2, 4, 2);

        assertEquals(15, placements.get(0).previewBounds().getXSpan());
        assertEquals(27, placements.get(1).previewBounds().getXSpan());
        assertEquals(15, placements.get(2).previewBounds().getXSpan());
        assertEquals(2, placements.get(1).previewBounds().minX() - placements.get(0).previewBounds().maxX() - 1);
        assertEquals(2, placements.get(2).previewBounds().minX() - placements.get(1).previewBounds().maxX() - 1);
    }

    @Test
    void gridLayoutKeepsFamiliesForSameContentSlotContiguous() {
        MKWorkspaceGridLayout layout = new MKWorkspaceGridLayout();
        MKPlannedPiece platform = slottedPiece("platform_family", "platform_contents");
        MKPlannedPiece pillars = slottedPiece("pillars_family", "pillars");
        MKPlannedPiece gazebo = slottedPiece("gazebo_family", "platform_contents");

        List<MKWorkspaceGridLayout.Placement> placements = layout.assignPlacements(BlockPos.ZERO,
                List.of(platform, pillars, gazebo), 1, 2, 2, 4, 2);

        assertTrue(placements.get(0).previewBounds().minX() < placements.get(2).previewBounds().minX());
        assertTrue(placements.get(2).previewBounds().minX() < placements.get(1).previewBounds().minX());
        assertEquals(2,
                placements.get(2).previewBounds().minX() - placements.get(0).previewBounds().maxX() - 1);
    }

    @Test
    void existingWorkspaceClearBoundsMergeExpandedPiecePreviewBounds() {
        MKWorkspaceScaffoldBuilder builder = new MKWorkspaceScaffoldBuilder();
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO).withPieces(List.of(
                piece("piece_a", new BoundingBox(10, 70, 20, 18, 78, 28)),
                piece("piece_b", new BoundingBox(30, 80, 40, 38, 88, 48))
        ));

        BoundingBox clearBounds = builder.existingWorkspaceClearBounds(workspace);

        assertNotNull(clearBounds);
        assertEquals(6, clearBounds.minX());
        assertEquals(66, clearBounds.minY());
        assertEquals(16, clearBounds.minZ());
        assertEquals(42, clearBounds.maxX());
        assertEquals(92, clearBounds.maxY());
        assertEquals(52, clearBounds.maxZ());
    }

    @Test
    void existingPieceContentClearPositionsUseExactPreviewBoundsAndSidecars() {
        MKWorkspaceScaffoldBuilder builder = new MKWorkspaceScaffoldBuilder();
        MKWorkspacePieceDefinition piece = new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "piece_a_1",
                "test.piece_a_1",
                1,
                MKWorkspaceDimensions.defaultDimensions(),
                1,
                List.of(),
                new BlockPos(12, 64, 12),
                new BoundingBox(12, 64, 12, 14, 66, 14),
                new BoundingBox(10, 63, 10, 16, 67, 16),
                new BlockPos(9, 64, 13),
                new BlockPos(8, 64, 13),
                List.of(new BlockPos(17, 65, 13)),
                List.of(new BlockPos(12, 62, 12)),
                Map.of()
        );

        Set<BlockPos> positions = builder.collectExistingPieceContentPositions(List.of(piece));

        assertTrue(positions.contains(new BlockPos(10, 63, 10)));
        assertTrue(positions.contains(new BlockPos(16, 67, 16)));
        assertTrue(positions.contains(piece.structureBlockPos()));
        assertTrue(positions.contains(piece.signPos()));
        assertTrue(positions.contains(piece.markerPositions().getFirst()));
        assertTrue(positions.contains(piece.generatedStairPositions().getFirst()));
        assertFalse(positions.contains(new BlockPos(9, 63, 10)));
        assertFalse(positions.contains(new BlockPos(10, 62, 10)));
        assertFalse(positions.contains(new BlockPos(10, 63, 9)));
        assertFalse(positions.contains(new BlockPos(17, 67, 16)));
        assertFalse(positions.contains(new BlockPos(16, 68, 16)));
        assertFalse(positions.contains(new BlockPos(16, 67, 17)));
    }

    @Test
    void verticalJigsawsAlignToExteriorShellFaceWithThickVerticalShellMargin() {
        MKWorkspaceScaffoldBuilder builder = new MKWorkspaceScaffoldBuilder();
        MKStructureWorkspace workspace = workspaceWithVerticalShellMargin(3);
        MKWorkspacePieceDefinition sourcePiece = piece("source", new BoundingBox(10, 70, 20, 20, 80, 30));
        MKPlannedPiece plannedPiece = new MKPlannedPiece(
                "test.vertical_access",
                "vertical_access",
                7,
                7,
                5,
                List.of(
                        new MKPlannedConnector(MKConnectorRole.CONNECT_UP, Direction.UP, 3, 3,
                                "minecraft:empty", "minecraft:empty"),
                        new MKPlannedConnector(MKConnectorRole.CONNECT_DOWN, Direction.DOWN, 3, 3,
                                "minecraft:empty", "minecraft:empty")
                ),
                Map.of("tower_piece_kind", "room")
        );

        MKWorkspacePieceDefinition generated = builder.createDerivedLogicalPiece(workspace, plannedPiece, sourcePiece);

        MKWorkspaceConnectorDefinition up = connector(generated, Direction.UP);
        MKWorkspaceConnectorDefinition down = connector(generated, Direction.DOWN);
        assertEquals(10, up.relativePos().getY());
        assertEquals(0, down.relativePos().getY());
    }

    private static MKPlannedPiece linearRun(String kind) {
        return new MKPlannedPiece(
                "test.linear_run." + kind,
                "test_" + kind,
                5,
                5,
                5,
                List.of(),
                Map.of(
                        "tower_piece_kind", "linear_run",
                        "workspace_linear_run_kind", kind
                )
        );
    }

    private static MKPlannedPiece floorPlanLinearRun(String kind) {
        return new MKPlannedPiece(
                "test.floor_plan_linear_run." + kind,
                "test_floor_plan_" + kind,
                5,
                5,
                5,
                List.of(),
                Map.of(
                        "tower_piece_kind", "floor_plan_linear_run",
                        "workspace_linear_run_kind", kind
                )
        );
    }

    private static MKPlannedPiece pieceKind(String pieceKind) {
        return new MKPlannedPiece(
                "test." + pieceKind,
                "test_" + pieceKind,
                5,
                5,
                5,
                List.of(),
                Map.of("tower_piece_kind", pieceKind)
        );
    }

    private static MKPlannedPiece pieceWithTag(String key, String value) {
        return new MKPlannedPiece(
                "test.room",
                "test_room",
                5,
                5,
                5,
                List.of(),
                Map.of(
                        "tower_piece_kind", "room",
                        key, value
                )
        );
    }

    private static MKPlannedConnector connector(MKWorkspaceHorizontalExtrusionMode horizontalExtrusionModeOverride) {
        return new MKPlannedConnector(MKConnectorRole.MAIN_FORWARD, Direction.NORTH, 3, 3,
                0, 0, "minecraft:empty", null, horizontalExtrusionModeOverride);
    }

    private static MKPlannedPiece plannedPiece(String baseName, int variantIndex) {
        return plannedPiece(baseName, variantIndex, 5, 5, 5);
    }

    private static MKPlannedPiece plannedPiece(String baseName, int variantIndex, int width, int length, int height) {
        return new MKPlannedPiece(
                "test." + baseName,
                variantIndex == 0 ? baseName + "_template" : baseName + "_" + variantIndex,
                width,
                length,
                height,
                List.of(),
                Map.of(
                        MKWorkspaceGridLayout.TAG_BASE_NAME, baseName,
                        MKWorkspaceGridLayout.TAG_VARIANT_INDEX, String.valueOf(variantIndex)
                )
        );
    }

    private static MKPlannedPiece slottedPiece(String familyId, String slotId) {
        return new MKPlannedPiece(
                "test.insert",
                familyId + "_template",
                5,
                5,
                5,
                List.of(),
                Map.of(
                        MKWorkspaceGridLayout.TAG_BASE_NAME, familyId,
                        MKWorkspaceGridLayout.TAG_VARIANT_INDEX, "0",
                        MKWorkspaceContentSelectionTags.TOPOLOGY_SLOT_ID, slotId,
                        MKWorkspaceContentSelectionTags.FAMILY_ID, familyId
                )
        );
    }

    private static MKWorkspacePieceDefinition piece(String pieceName, BoundingBox previewBounds) {
        return new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                pieceName,
                "test." + pieceName,
                0,
                MKWorkspaceDimensions.defaultDimensions(),
                1,
                List.of(),
                new BlockPos(previewBounds.minX(), previewBounds.minY(), previewBounds.minZ()),
                previewBounds,
                previewBounds,
                BlockPos.ZERO,
                BlockPos.ZERO,
                List.of(),
                List.of(),
                Map.of()
        );
    }

    private static MKStructureWorkspace workspaceWithVerticalShellMargin(int verticalShellMargin) {
        MKStructureWorkspace draft = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        return new MKStructureWorkspace(
                draft.id(),
                draft.anchor(),
                draft.namespace(),
                draft.structureName(),
                draft.topologyProfile(),
                draft.dimensions(),
                draft.palette(),
                draft.stairConfig(),
                draft.verticalAccessPlacement(),
                draft.shellMargin(),
                verticalShellMargin,
                draft.exteriorAirMargin(),
                draft.previewMargin(),
                draft.verticalAccessSpec(),
                draft.familyDefinitions(),
                draft.openingProfiles(),
                draft.linearRunFamilies(),
                draft.insertFamilies(),
                draft.createdAt(),
                draft.updatedAt(),
                draft.pieces(),
                draft.layerStates()
        );
    }

    private static MKWorkspaceConnectorDefinition connector(MKWorkspacePieceDefinition piece, Direction facing) {
        return piece.connectors().stream()
                .filter(connector -> connector.facing() == facing)
                .findFirst()
                .orElseThrow();
    }
}
