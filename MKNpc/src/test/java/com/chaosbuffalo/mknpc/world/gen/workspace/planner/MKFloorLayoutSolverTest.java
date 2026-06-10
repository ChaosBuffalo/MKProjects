package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorRoomKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorRoomProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorTopologySettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHallwayLeadInMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitPathKind;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MKFloorLayoutSolverTest {
    @Test
    void acceptedMaskReflectsSprawlRejectedBranches() {
        MKFloorLayoutSolver.FloorLayoutResult result = new MKFloorLayoutSolver().solve(
                settings(0.0f),
                9,
                9,
                List.of(new MKWorkspaceFamilyHorizontalExitDefinition(Direction.NORTH,
                        MKWorkspaceHorizontalExitPathKind.MAIN_EXIT,
                        MKWorkspaceFloorRoomProfile.INHERITED_MAIN_OPENING_PROFILE_ID)),
                1,
                1234L
        );

        MKFloorLayoutSolver.LogicalSegment mainRoom = result.segments().stream()
                .filter(segment -> segment.kind() == MKFloorLayoutSolver.SegmentKind.MAIN_ROOM)
                .findFirst()
                .orElseThrow();
        assertEquals("none", mainRoom.acceptedMask());
        assertEquals(2, result.rejectedExits().size());
    }

    @Test
    void acceptedMaskReflectsPlacedBranches() {
        MKFloorLayoutSolver.FloorLayoutResult result = new MKFloorLayoutSolver().solve(
                settings(1.0f),
                9,
                9,
                List.of(new MKWorkspaceFamilyHorizontalExitDefinition(Direction.NORTH,
                        MKWorkspaceHorizontalExitPathKind.MAIN_EXIT,
                        MKWorkspaceFloorRoomProfile.INHERITED_MAIN_OPENING_PROFILE_ID)),
                1,
                1234L
        );

        MKFloorLayoutSolver.LogicalSegment mainRoom = result.segments().stream()
                .filter(segment -> segment.kind() == MKFloorLayoutSolver.SegmentKind.MAIN_ROOM)
                .findFirst()
                .orElseThrow();
        assertEquals("ew", mainRoom.acceptedMask());
    }

    @Test
    void floorTopologySettingsCodecCarriesLockedLayoutSeed() {
        MKWorkspaceFloorTopologySettings settings = settings(0.75f).withLockedLayoutSeed(Optional.of(8675309L));

        JsonElement encoded = MKWorkspaceFloorTopologySettings.CODEC.encodeStart(JsonOps.INSTANCE, settings)
                .getOrThrow();
        MKWorkspaceFloorTopologySettings decoded = MKWorkspaceFloorTopologySettings.CODEC.parse(JsonOps.INSTANCE,
                encoded).getOrThrow();

        assertEquals(Optional.of(8675309L), decoded.lockedLayoutSeed());
        assertEquals(0.75f, decoded.sprawl());
        assertEquals(1, decoded.mainRoomProfiles().size());
    }

    @Test
    void branchPathUsesCapWhenContinuationWouldExceedBounds() {
        MKWorkspaceFloorRoomProfile oversizedBranchRoom = MKWorkspaceFloorRoomProfile
                .defaults(MKWorkspaceFloorRoomKind.BRANCH_ROOM, 9, 70, 7);
        MKWorkspaceFloorTopologySettings settings = new MKWorkspaceFloorTopologySettings(
                "tower.primary",
                "main_floor",
                1,
                1,
                MKWorkspaceFloorTopologySettings.MAX_BRANCH_PIECES_BEFORE_CAP,
                MKWorkspaceHallwayLeadInMode.MANUAL,
                1,
                false,
                false,
                false,
                1.0f,
                Optional.empty(),
                List.of(MKWorkspaceFloorRoomProfile.defaults(MKWorkspaceFloorRoomKind.MAIN_ROOM, 9, 9, 7)),
                List.of(oversizedBranchRoom),
                List.of(MKWorkspaceFloorRoomProfile.defaults(MKWorkspaceFloorRoomKind.BRANCH_CAP, 9, 9, 7)),
                List.of(MKWorkspaceFloorRoomProfile.defaults(MKWorkspaceFloorRoomKind.MAIN_CAP_APPROACH, 9, 9, 7)),
                List.of(MKWorkspaceFloorRoomProfile.defaults(MKWorkspaceFloorRoomKind.MAIN_CAP, 9, 9, 7))
        );

        MKFloorLayoutSolver.FloorLayoutResult result = new MKFloorLayoutSolver().solve(
                settings,
                9,
                9,
                List.of(new MKWorkspaceFamilyHorizontalExitDefinition(Direction.NORTH,
                        MKWorkspaceHorizontalExitPathKind.BRANCH,
                        MKWorkspaceFloorRoomProfile.INHERITED_BRANCH_OPENING_PROFILE_ID)),
                1,
                99L
        );

        assertEquals(0, result.segments().stream()
                .filter(segment -> segment.kind() == MKFloorLayoutSolver.SegmentKind.BRANCH_ROOM)
                .count());
        assertEquals(1, result.segments().stream()
                .filter(segment -> segment.kind() == MKFloorLayoutSolver.SegmentKind.BRANCH_CAP)
                .count());
        assertTrue(result.fitsHardLimit());
    }

    @Test
    void requiredMainPathRejectionMarksResultInvalid() {
        MKWorkspaceFloorRoomProfile oversizedMainRoom = MKWorkspaceFloorRoomProfile
                .defaults(MKWorkspaceFloorRoomKind.MAIN_ROOM, 9, 65, 7);
        MKWorkspaceFloorTopologySettings settings = new MKWorkspaceFloorTopologySettings(
                "tower.primary",
                "main_floor",
                3,
                3,
                0,
                MKWorkspaceHallwayLeadInMode.MANUAL,
                1,
                false,
                false,
                false,
                1.0f,
                Optional.empty(),
                List.of(oversizedMainRoom),
                List.of(MKWorkspaceFloorRoomProfile.defaults(MKWorkspaceFloorRoomKind.BRANCH_ROOM, 9, 9, 7)),
                List.of(MKWorkspaceFloorRoomProfile.defaults(MKWorkspaceFloorRoomKind.BRANCH_CAP, 9, 9, 7)),
                List.of(MKWorkspaceFloorRoomProfile.defaults(MKWorkspaceFloorRoomKind.MAIN_CAP_APPROACH, 9, 9, 7)),
                List.of(MKWorkspaceFloorRoomProfile.defaults(MKWorkspaceFloorRoomKind.MAIN_CAP, 9, 9, 7))
        );

        MKFloorLayoutSolver.FloorLayoutResult result = new MKFloorLayoutSolver().solve(
                settings,
                9,
                9,
                List.of(new MKWorkspaceFamilyHorizontalExitDefinition(Direction.NORTH,
                        MKWorkspaceHorizontalExitPathKind.MAIN_EXIT,
                        MKWorkspaceFloorRoomProfile.INHERITED_MAIN_OPENING_PROFILE_ID)),
                1,
                99L
        );

        assertFalse(result.fitsHardLimit());
        assertTrue(result.hasRequiredRejections());
        assertTrue(result.rejectedExits().stream().anyMatch(rejection ->
                rejection.required() && "horizontal bound".equals(rejection.reason())));
    }

    @Test
    void mainExitDirectionTurnsNextMainPathSegment() {
        MKWorkspaceFloorRoomProfile eastTurningMainRoom = MKWorkspaceFloorRoomProfile
                .defaults(MKWorkspaceFloorRoomKind.MAIN_ROOM, 9, 9, 7)
                .withHorizontalExits(List.of(new MKWorkspaceFamilyHorizontalExitDefinition(
                        Direction.EAST,
                        MKWorkspaceHorizontalExitPathKind.MAIN_EXIT,
                        MKWorkspaceFloorRoomProfile.INHERITED_MAIN_OPENING_PROFILE_ID)));
        MKWorkspaceFloorTopologySettings settings = new MKWorkspaceFloorTopologySettings(
                "tower.primary",
                "main_floor",
                2,
                2,
                0,
                MKWorkspaceHallwayLeadInMode.MANUAL,
                1,
                false,
                false,
                false,
                1.0f,
                Optional.empty(),
                List.of(eastTurningMainRoom),
                List.of(MKWorkspaceFloorRoomProfile.defaults(MKWorkspaceFloorRoomKind.BRANCH_ROOM, 9, 9, 7)),
                List.of(MKWorkspaceFloorRoomProfile.defaults(MKWorkspaceFloorRoomKind.BRANCH_CAP, 9, 9, 7)),
                List.of(MKWorkspaceFloorRoomProfile.defaults(MKWorkspaceFloorRoomKind.MAIN_CAP_APPROACH, 9, 9, 7)),
                List.of(MKWorkspaceFloorRoomProfile.defaults(MKWorkspaceFloorRoomKind.MAIN_CAP, 9, 9, 7))
        );

        List<MKFloorLayoutSolver.LogicalSegment> mainRooms = new MKFloorLayoutSolver().solve(
                        settings,
                        9,
                        9,
                        List.of(new MKWorkspaceFamilyHorizontalExitDefinition(Direction.NORTH,
                                MKWorkspaceHorizontalExitPathKind.MAIN_EXIT,
                                MKWorkspaceFloorRoomProfile.INHERITED_MAIN_OPENING_PROFILE_ID)),
                        1,
                        99L
                ).segments().stream()
                .filter(segment -> segment.kind() == MKFloorLayoutSolver.SegmentKind.MAIN_ROOM)
                .toList();

        assertEquals(2, mainRooms.size());
        assertEquals(Direction.NORTH, mainRooms.get(0).direction());
        assertEquals(Direction.EAST, mainRooms.get(1).direction());
        assertTrue(mainRooms.get(1).rect().centerX() > mainRooms.get(0).rect().centerX());
    }

    private static MKWorkspaceFloorTopologySettings settings(float sprawl) {
        MKWorkspaceFloorRoomProfile mainRoom = MKWorkspaceFloorRoomProfile
                .defaults(MKWorkspaceFloorRoomKind.MAIN_ROOM, 9, 9, 7)
                .withHorizontalExits(List.of(
                        new MKWorkspaceFamilyHorizontalExitDefinition(Direction.NORTH,
                                MKWorkspaceHorizontalExitPathKind.MAIN_EXIT,
                                MKWorkspaceFloorRoomProfile.INHERITED_MAIN_OPENING_PROFILE_ID),
                        new MKWorkspaceFamilyHorizontalExitDefinition(Direction.EAST,
                                MKWorkspaceHorizontalExitPathKind.BRANCH,
                                MKWorkspaceFloorRoomProfile.INHERITED_BRANCH_OPENING_PROFILE_ID),
                        new MKWorkspaceFamilyHorizontalExitDefinition(Direction.WEST,
                                MKWorkspaceHorizontalExitPathKind.BRANCH,
                                MKWorkspaceFloorRoomProfile.INHERITED_BRANCH_OPENING_PROFILE_ID)
                ));
        return new MKWorkspaceFloorTopologySettings(
                "tower.primary",
                "main_floor",
                1,
                1,
                0,
                MKWorkspaceHallwayLeadInMode.MANUAL,
                1,
                false,
                false,
                false,
                sprawl,
                java.util.Optional.empty(),
                List.of(mainRoom),
                List.of(MKWorkspaceFloorRoomProfile.defaults(MKWorkspaceFloorRoomKind.BRANCH_ROOM, 9, 9, 7)),
                List.of(MKWorkspaceFloorRoomProfile.defaults(MKWorkspaceFloorRoomKind.BRANCH_CAP, 9, 9, 7)),
                List.of(MKWorkspaceFloorRoomProfile.defaults(MKWorkspaceFloorRoomKind.MAIN_CAP_APPROACH, 9, 9, 7)),
                List.of(MKWorkspaceFloorRoomProfile.defaults(MKWorkspaceFloorRoomKind.MAIN_CAP, 9, 9, 7))
        );
    }
}
