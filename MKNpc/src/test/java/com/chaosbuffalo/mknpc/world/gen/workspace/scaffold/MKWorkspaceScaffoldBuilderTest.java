package com.chaosbuffalo.mknpc.world.gen.workspace.scaffold;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKPlannedPiece;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

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
        assertEquals(27, clearBounds.minZ());
        assertEquals(53, clearBounds.maxX());
        assertEquals(10, clearBounds.maxY());
        assertEquals(53, clearBounds.maxZ());
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
}
