package com.chaosbuffalo.mkworkspace.world.gen.workspace;

import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKPlannedPiece;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.scaffold.MKWorkspaceGridLayout;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.scaffold.MKWorkspaceScaffoldBuilder;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MKStructureWorkspaceServiceVariantLayoutTest {
    @Test
    void variantLayoutPreservesTaggedCanonicalBaseColumn() {
        MKStructureWorkspaceService service = new MKStructureWorkspaceService();
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        MKPlannedPiece canonical = plannedPiece("hub_spoke_corner_north_west", "corner_source", 0);
        MKPlannedPiece variant = plannedPiece("corner_source_1", "corner_source", 1);

        List<MKPlannedPiece> layoutPieces = service.physicalVariantLayoutPieces(
                workspace,
                List.of(canonical),
                Map.of("corner_source", canonical),
                List.of(variant)
        );

        List<MKWorkspaceGridLayout.Placement> placements = new MKWorkspaceGridLayout().assignPlacements(
                workspace.anchor(),
                layoutPieces,
                workspace.shellMargin(),
                workspace.verticalShellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                MKWorkspaceScaffoldBuilder.GRID_COLUMNS,
                MKWorkspaceScaffoldBuilder.CELL_PADDING
        );

        assertEquals("corner_source", layoutPieces.get(0).tags().get(MKWorkspaceGridLayout.TAG_BASE_NAME));
        assertEquals(placements.get(0).previewBounds().minX(), placements.get(1).previewBounds().minX());
    }

    @Test
    void variantPlacementUsesExistingPhysicalColumnWhenPlannerOrderDiffers() {
        MKStructureWorkspaceService service = new MKStructureWorkspaceService();
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO).withPieces(List.of(
                workspacePiece("hub_spoke_corner_north_west", "corner_source", 0, 8, 8),
                workspacePiece("hub_spoke_center", "center", 0, 40, 8),
                workspacePiece("hub_spoke_center_1", "center", 1, 40, 80)
        ));
        MKPlannedPiece center = plannedPiece("hub_spoke_center", "center", 0);
        MKPlannedPiece corner = plannedPiece("hub_spoke_corner_north_west", "corner_source", 0);
        MKPlannedPiece cornerVariant = plannedPiece("hub_spoke_corner_north_west_1", "corner_source", 1);

        List<MKPlannedPiece> layoutPieces = service.physicalVariantLayoutPieces(
                workspace,
                List.of(center, corner),
                Map.of("center", center, "corner_source", corner),
                List.of(cornerVariant)
        );
        MKWorkspaceGridLayout.Placement placement = service.alignedVariantPlacement(workspace, "corner_source", 1,
                cornerVariant, layoutPieces);

        assertEquals("corner_source", layoutPieces.get(0).tags().get(MKWorkspaceGridLayout.TAG_BASE_NAME));
        assertEquals(8, placement.previewBounds().minX());
        assertEquals(80, placement.previewBounds().minZ());
    }

    private static MKPlannedPiece plannedPiece(String pieceName, String baseName, int variantIndex) {
        return new MKPlannedPiece(
                "hub_spoke.corner.shared",
                pieceName,
                7,
                7,
                3,
                List.of(),
                Map.of(
                        MKWorkspaceGridLayout.TAG_BASE_NAME, baseName,
                        MKWorkspaceGridLayout.TAG_VARIANT_INDEX, Integer.toString(variantIndex)
                )
        );
    }

    private static MKWorkspacePieceDefinition workspacePiece(String pieceName, String baseName, int variantIndex,
                                                            int previewX, int previewZ) {
        BoundingBox previewBounds = new BoundingBox(previewX, 0, previewZ, previewX + 10, 4, previewZ + 10);
        BoundingBox exportBounds = new BoundingBox(previewX + 2, 0, previewZ + 2, previewX + 8, 4, previewZ + 8);
        return new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                pieceName,
                "hub_spoke.corner.shared",
                variantIndex,
                new MKWorkspaceDimensions(7, 7, 3, 3, 3, 3, 3, 3),
                List.of(),
                new BlockPos(exportBounds.minX(), exportBounds.minY(), exportBounds.minZ()),
                exportBounds,
                previewBounds,
                new BlockPos(exportBounds.minX(), exportBounds.minY(), exportBounds.minZ()),
                new BlockPos(previewX - 1, 0, previewZ),
                List.of(),
                List.of(),
                Map.of(
                        MKWorkspaceGridLayout.TAG_BASE_NAME, baseName,
                        MKWorkspaceGridLayout.TAG_VARIANT_INDEX, Integer.toString(variantIndex)
                )
        );
    }
}
