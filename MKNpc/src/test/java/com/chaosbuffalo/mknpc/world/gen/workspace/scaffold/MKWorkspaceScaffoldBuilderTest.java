package com.chaosbuffalo.mknpc.world.gen.workspace.scaffold;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExtrusionMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKPlannedConnector;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKPlannedPiece;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        assertEquals(19, clearBounds.minZ());
        assertEquals(41, clearBounds.maxX());
        assertEquals(10, clearBounds.maxY());
        assertEquals(41, clearBounds.maxZ());
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
        return new MKPlannedPiece(
                "test." + baseName,
                variantIndex == 0 ? baseName + "_template" : baseName + "_" + variantIndex,
                5,
                5,
                5,
                List.of(),
                Map.of(
                        MKWorkspaceGridLayout.TAG_BASE_NAME, baseName,
                        MKWorkspaceGridLayout.TAG_VARIANT_INDEX, String.valueOf(variantIndex)
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
}
