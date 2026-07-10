package com.chaosbuffalo.mkworkspace.world.gen.workspace.planner;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MKPlannedPieceTest {
    @Test
    void derivesPlannerIdFromRoleAndPieceName() {
        MKPlannedPiece piece = new MKPlannedPiece(
                "keep.main.tower.center.floor.basement_01.floor_plan.room",
                "main_00",
                9,
                9,
                5,
                List.of(),
                Map.of()
        );

        assertEquals("keep.main.tower.center.floor.basement_01.floor_plan.room.main_00",
                piece.plannerId().value());
    }
}
