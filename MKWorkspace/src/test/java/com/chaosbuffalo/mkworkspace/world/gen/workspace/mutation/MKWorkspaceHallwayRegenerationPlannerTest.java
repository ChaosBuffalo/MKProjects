package com.chaosbuffalo.mkworkspace.world.gen.workspace.mutation;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePlannerId;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKPlannedPiece;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.scaffold.MKWorkspaceGridLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MKWorkspaceHallwayRegenerationPlannerTest {
    private final MKWorkspaceHallwayRegenerationPlanner planner = new MKWorkspaceHallwayRegenerationPlanner();

    @Test
    void planTargetsFloorPlanHallwaysOnly() {
        MKWorkspacePieceDefinition room = piece("room_template", "room", "floor_plan_room",
                MKWorkspacePlannerId.of("keep.main.floor_plan.room.main"));
        MKWorkspacePieceDefinition hallway = piece("hall_template", "hall", "floor_plan_linear_run",
                MKWorkspacePlannerId.of("keep.main.floor_plan.hallway.main"));
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of(room, hallway));

        MKWorkspaceHallwayRegenerationPlanner.RegenerationPlan plan = planner.plan(workspace, List.of(
                planned("room", "floor_plan_room", room.plannerId()),
                planned("hall", "floor_plan_linear_run", hallway.plannerId()),
                planned("branch_hall", "floor_plan_linear_run",
                        MKWorkspacePlannerId.of("keep.main.floor_plan.hallway.branch"))
        ));

        assertEquals(List.of(hallway), plan.existingHallwayPieces());
        assertEquals(List.of("hall_template", "branch_hall_template"),
                plan.hallwayPieces().stream().map(MKPlannedPiece::pieceName).toList());
        assertTrue(plan.hasWork());
    }

    @Test
    void mergeReplacesHallwaysAndPreservesRooms() {
        MKWorkspacePieceDefinition room = piece("room_template", "room", "floor_plan_room",
                MKWorkspacePlannerId.of("keep.main.floor_plan.room.main"));
        MKWorkspacePieceDefinition oldHallway = piece("hall_template", "hall", "floor_plan_linear_run",
                MKWorkspacePlannerId.of("keep.main.floor_plan.hallway.main"));
        MKWorkspacePieceDefinition newHallway = piece("hall_template", "hall", "floor_plan_linear_run",
                MKWorkspacePlannerId.of("keep.main.floor_plan.hallway.main.updated"));
        MKWorkspacePieceDefinition addedHallway = piece("branch_hall_template", "branch_hall",
                "floor_plan_linear_run", MKWorkspacePlannerId.of("keep.main.floor_plan.hallway.branch"));
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of(room, oldHallway));

        List<MKWorkspacePieceDefinition> merged = planner.mergeGeneratedHallways(workspace,
                List.of(newHallway, addedHallway));

        assertEquals(List.of(room, newHallway, addedHallway), merged);
    }

    private static MKPlannedPiece planned(String baseName, String towerPieceKind, MKWorkspacePlannerId plannerId) {
        return new MKPlannedPiece(
                "floor.plan",
                baseName,
                5,
                5,
                5,
                List.of(),
                Map.of(MKWorkspaceHallwayRegenerationPlanner.TOWER_PIECE_KIND_TAG, towerPieceKind),
                plannerId
        );
    }

    private static MKWorkspacePieceDefinition piece(String pieceName, String baseName, String towerPieceKind,
                                                   MKWorkspacePlannerId plannerId) {
        return new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                pieceName,
                "floor.plan",
                plannerId,
                0,
                MKWorkspaceDimensions.defaultDimensions(),
                1,
                List.of(),
                BlockPos.ZERO,
                new BoundingBox(0, 0, 0, 1, 1, 1),
                new BoundingBox(0, 0, 0, 1, 1, 1),
                BlockPos.ZERO,
                BlockPos.ZERO,
                List.of(),
                List.of(),
                Map.of(
                        MKWorkspaceHallwayRegenerationPlanner.TOWER_PIECE_KIND_TAG, towerPieceKind,
                        MKWorkspaceGridLayout.TAG_BASE_NAME, baseName
                )
        );
    }
}
