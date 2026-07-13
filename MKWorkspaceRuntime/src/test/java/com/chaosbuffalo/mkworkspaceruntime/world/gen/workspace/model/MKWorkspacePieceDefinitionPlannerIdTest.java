package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MKWorkspacePieceDefinitionPlannerIdTest {
    @Test
    void explicitPlannerIdIsStoredInTagsAndRoundTrips() {
        MKWorkspacePlannerId plannerId = MKWorkspacePlannerId.of(
                "keep.main.tower.center.floor.basement_01.floor_plan.room.main_00");
        MKWorkspacePieceDefinition piece = piece(plannerId);

        MKWorkspacePieceDefinition decoded = MKWorkspacePieceDefinition.fromTag(piece.toTag());

        assertEquals(plannerId, decoded.plannerId());
        assertEquals(plannerId.value(), decoded.tags().get(MKWorkspacePieceDefinition.TAG_PLANNER_ID));
    }

    @Test
    void missingPlannerIdFallsBackToRoleAndPieceName() {
        MKWorkspacePieceDefinition piece = new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "main_00",
                "keep.main.floor_plan.room",
                1,
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
                Map.of()
        );

        assertEquals("keep.main.floor_plan.room.main_00", piece.plannerId().value());
        assertEquals(piece.plannerId().value(), piece.tags().get(MKWorkspacePieceDefinition.TAG_PLANNER_ID));
    }

    @Test
    void verticalShellMarginRoundTrips() {
        MKWorkspacePlannerId plannerId = MKWorkspacePlannerId.of("keep.main.room");
        MKWorkspacePieceDefinition piece = new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "main_00",
                "keep.main",
                plannerId,
                1,
                MKWorkspaceDimensions.defaultDimensions(),
                2,
                3,
                List.of(),
                BlockPos.ZERO,
                new BoundingBox(0, 0, 0, 1, 1, 1),
                new BoundingBox(0, 0, 0, 1, 1, 1),
                BlockPos.ZERO,
                BlockPos.ZERO,
                List.of(),
                List.of(),
                Map.of()
        );

        MKWorkspacePieceDefinition decoded = MKWorkspacePieceDefinition.fromTag(piece.toTag());

        assertEquals(2, decoded.shellMargin());
        assertEquals(3, decoded.verticalShellMargin());
    }

    @Test
    void legacyConstructorDefaultsVerticalShellMarginToOne() {
        MKWorkspacePieceDefinition decoded = MKWorkspacePieceDefinition.fromTag(piece(MKWorkspacePlannerId.of("keep.main")).toTag());

        assertEquals(1, decoded.verticalShellMargin());
    }

    public static MKWorkspacePieceDefinition piece(MKWorkspacePlannerId plannerId) {
        return new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "main_00",
                "keep.main.floor_plan.room",
                plannerId,
                1,
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
                Map.of()
        );
    }
}
