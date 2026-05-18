package com.chaosbuffalo.mknpc.world.gen.workspace.scaffold;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKPlannedPiece;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MKWorkspaceScaffoldBuilderTest {
    @Test
    void workspaceClearBoundsExtendToBuildHeight() {
        MKWorkspaceScaffoldBuilder builder = new MKWorkspaceScaffoldBuilder();

        BoundingBox expanded = builder.extendToWorkspaceClearHeight(
                new BoundingBox(10, -80, 20, 30, 12, 40),
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
        assertEquals(MKWorkspaceScaffoldBuilder.LinearRunScaffoldStyle.ENCLOSED_CORRIDOR,
                builder.linearRunScaffoldStyle(linearRun("enclosed_corridor")));
    }

    private static MKPlannedPiece linearRun(String kind) {
        return new MKPlannedPiece(
                MKWorkspacePieceRole.HALLWAY,
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
}
