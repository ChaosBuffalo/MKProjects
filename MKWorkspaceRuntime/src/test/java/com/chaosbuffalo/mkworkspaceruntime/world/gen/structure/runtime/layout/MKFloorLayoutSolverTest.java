package com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKFloorRoomKind;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKFloorRoomProfile;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKFloorTopologySettings;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKHallwayLeadInMode;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKHorizontalExitPathKind;
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
                List.of(new MKFamilyHorizontalExitDefinition(Direction.NORTH,
                        MKHorizontalExitPathKind.MAIN_EXIT,
                        MKFloorRoomProfile.INHERITED_MAIN_OPENING_PROFILE_ID)),
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
                List.of(new MKFamilyHorizontalExitDefinition(Direction.NORTH,
                        MKHorizontalExitPathKind.MAIN_EXIT,
                        MKFloorRoomProfile.INHERITED_MAIN_OPENING_PROFILE_ID)),
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
        MKFloorTopologySettings settings = settings(0.75f)
                .withLinksEnabled(true)
                .withLinkDensity(0.5f)
                .withMaxLinksPerFloor(4)
                .withMaxLinksPerRoom(2)
                .withMaxLinkLength(48)
                .withLockedLayoutSeed(Optional.of(8675309L));

        JsonElement encoded = MKFloorTopologySettings.CODEC.encodeStart(JsonOps.INSTANCE, settings)
                .getOrThrow();
        MKFloorTopologySettings decoded = MKFloorTopologySettings.CODEC.parse(JsonOps.INSTANCE,
                encoded).getOrThrow();

        assertEquals(Optional.of(8675309L), decoded.lockedLayoutSeed());
        assertEquals(0.75f, decoded.sprawl());
        assertTrue(decoded.linksEnabled());
        assertEquals(0.5f, decoded.linkDensity());
        assertEquals(4, decoded.maxLinksPerFloor());
        assertEquals(2, decoded.maxLinksPerRoom());
        assertEquals(48, decoded.maxLinkLength());
        assertEquals(1, decoded.mainRoomProfiles().size());
    }

    @Test
    void branchPathUsesCapWhenContinuationWouldExceedBounds() {
        MKFloorRoomProfile oversizedBranchRoom = MKFloorRoomProfile
                .defaults(MKFloorRoomKind.BRANCH_ROOM, 9, 70, 7);
        MKFloorTopologySettings settings = new MKFloorTopologySettings(
                "tower.primary",
                "main_floor",
                1,
                1,
                MKFloorTopologySettings.MAX_BRANCH_PIECES_BEFORE_CAP,
                MKHallwayLeadInMode.MANUAL,
                1,
                false,
                false,
                false,
                1.0f,
                Optional.empty(),
                List.of(MKFloorRoomProfile.defaults(MKFloorRoomKind.MAIN_ROOM, 9, 9, 7)),
                List.of(oversizedBranchRoom),
                List.of(MKFloorRoomProfile.defaults(MKFloorRoomKind.BRANCH_CAP, 9, 9, 7)),
                List.of(MKFloorRoomProfile.defaults(MKFloorRoomKind.MAIN_CAP_APPROACH, 9, 9, 7)),
                List.of(MKFloorRoomProfile.defaults(MKFloorRoomKind.MAIN_CAP, 9, 9, 7))
        );

        MKFloorLayoutSolver.FloorLayoutResult result = new MKFloorLayoutSolver().solve(
                settings,
                9,
                9,
                List.of(new MKFamilyHorizontalExitDefinition(Direction.NORTH,
                        MKHorizontalExitPathKind.BRANCH,
                        MKFloorRoomProfile.INHERITED_BRANCH_OPENING_PROFILE_ID)),
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
        MKFloorRoomProfile oversizedMainRoom = MKFloorRoomProfile
                .defaults(MKFloorRoomKind.MAIN_ROOM, 9, 65, 7);
        MKFloorTopologySettings settings = new MKFloorTopologySettings(
                "tower.primary",
                "main_floor",
                3,
                3,
                0,
                MKHallwayLeadInMode.MANUAL,
                1,
                false,
                false,
                false,
                1.0f,
                Optional.empty(),
                List.of(oversizedMainRoom),
                List.of(MKFloorRoomProfile.defaults(MKFloorRoomKind.BRANCH_ROOM, 9, 9, 7)),
                List.of(MKFloorRoomProfile.defaults(MKFloorRoomKind.BRANCH_CAP, 9, 9, 7)),
                List.of(MKFloorRoomProfile.defaults(MKFloorRoomKind.MAIN_CAP_APPROACH, 9, 9, 7)),
                List.of(MKFloorRoomProfile.defaults(MKFloorRoomKind.MAIN_CAP, 9, 9, 7))
        );

        MKFloorLayoutSolver.FloorLayoutResult result = new MKFloorLayoutSolver().solve(
                settings,
                9,
                9,
                List.of(new MKFamilyHorizontalExitDefinition(Direction.NORTH,
                        MKHorizontalExitPathKind.MAIN_EXIT,
                        MKFloorRoomProfile.INHERITED_MAIN_OPENING_PROFILE_ID)),
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
        MKFloorRoomProfile eastTurningMainRoom = MKFloorRoomProfile
                .defaults(MKFloorRoomKind.MAIN_ROOM, 9, 9, 7)
                .withHorizontalExits(List.of(new MKFamilyHorizontalExitDefinition(
                        Direction.EAST,
                        MKHorizontalExitPathKind.MAIN_EXIT,
                        MKFloorRoomProfile.INHERITED_MAIN_OPENING_PROFILE_ID)));
        MKFloorTopologySettings settings = new MKFloorTopologySettings(
                "tower.primary",
                "main_floor",
                2,
                2,
                0,
                MKHallwayLeadInMode.MANUAL,
                1,
                false,
                false,
                false,
                1.0f,
                Optional.empty(),
                List.of(eastTurningMainRoom),
                List.of(MKFloorRoomProfile.defaults(MKFloorRoomKind.BRANCH_ROOM, 9, 9, 7)),
                List.of(MKFloorRoomProfile.defaults(MKFloorRoomKind.BRANCH_CAP, 9, 9, 7)),
                List.of(MKFloorRoomProfile.defaults(MKFloorRoomKind.MAIN_CAP_APPROACH, 9, 9, 7)),
                List.of(MKFloorRoomProfile.defaults(MKFloorRoomKind.MAIN_CAP, 9, 9, 7))
        );

        List<MKFloorLayoutSolver.LogicalSegment> mainRooms = new MKFloorLayoutSolver().solve(
                        settings,
                        9,
                        9,
                        List.of(new MKFamilyHorizontalExitDefinition(Direction.NORTH,
                                MKHorizontalExitPathKind.MAIN_EXIT,
                                MKFloorRoomProfile.INHERITED_MAIN_OPENING_PROFILE_ID)),
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

    @Test
    void randomizedMainExitResolvesEnabledDirectionsIntoEffectiveProfile() {
        MKFloorRoomProfile profile = MKFloorRoomProfile
                .defaults(MKFloorRoomKind.MAIN_ROOM, 9, 9, 7)
                .withHorizontalExits(List.of(
                        new MKFamilyHorizontalExitDefinition(Direction.NORTH,
                                MKHorizontalExitPathKind.MAIN_EXIT,
                                MKFloorRoomProfile.INHERITED_MAIN_OPENING_PROFILE_ID),
                        new MKFamilyHorizontalExitDefinition(Direction.EAST,
                                MKHorizontalExitPathKind.BRANCH,
                                MKFloorRoomProfile.INHERITED_BRANCH_OPENING_PROFILE_ID),
                        new MKFamilyHorizontalExitDefinition(Direction.WEST,
                                MKHorizontalExitPathKind.BRANCH,
                                MKFloorRoomProfile.INHERITED_BRANCH_OPENING_PROFILE_ID)
                ))
                .withRandomizeMainExit(true);

        MKFloorRoomProfile resolved = profile.withResolvedRandomMainExit(Direction.EAST);

        assertEquals(List.of(Direction.NORTH, Direction.EAST, Direction.WEST),
                profile.randomizedMainExitCandidates());
        assertEquals(Optional.of(Direction.EAST), resolved.mainExitDirection());
        assertFalse(resolved.randomizeMainExit());
        assertTrue(resolved.horizontalExits().stream().anyMatch(exit ->
                exit.direction() == Direction.NORTH &&
                        exit.pathKind() == MKHorizontalExitPathKind.BRANCH));
        assertTrue(resolved.horizontalExits().stream().anyMatch(exit ->
                exit.direction() == Direction.WEST &&
                        exit.pathKind() == MKHorizontalExitPathKind.BRANCH));
    }

    @Test
    void branchCapDefaultsEnableAllLinkCandidates() {
        MKFloorRoomProfile branchCap = MKFloorRoomProfile
                .defaults(MKFloorRoomKind.BRANCH_CAP, 9, 9, 7);

        assertEquals(List.of(Direction.NORTH, Direction.EAST, Direction.WEST),
                branchCap.horizontalExits().stream()
                        .filter(exit -> exit.pathKind() == MKHorizontalExitPathKind.LINK_CANDIDATE)
                        .map(MKFamilyHorizontalExitDefinition::direction)
                        .toList());
    }

    @Test
    void maxLinksPerRoomClampsToAvailableHorizontalLinkExits() {
        MKFloorTopologySettings settings = settings(0.5f).withMaxLinksPerRoom(16);

        assertEquals(3, MKFloorTopologySettings.MAX_LINKS_PER_ROOM);
        assertEquals(3, MKFloorTopologySettings.DEFAULT_MAX_LINKS_PER_ROOM);
        assertEquals(3, settings.maxLinksPerRoom());
    }

    @Test
    void rootBranchCannotReserveSpaceBeforeRequiredMainPath() {
        MKFloorTopologySettings settings = new MKFloorTopologySettings(
                "tower.primary",
                "main_floor",
                1,
                1,
                0,
                MKHallwayLeadInMode.MANUAL,
                1,
                false,
                false,
                false,
                1.0f,
                Optional.empty(),
                List.of(MKFloorRoomProfile.defaults(MKFloorRoomKind.MAIN_ROOM, 9, 9, 7)),
                List.of(MKFloorRoomProfile.defaults(MKFloorRoomKind.BRANCH_ROOM, 9, 9, 7)),
                List.of(MKFloorRoomProfile.defaults(MKFloorRoomKind.BRANCH_CAP, 9, 9, 7)),
                List.of(MKFloorRoomProfile.defaults(MKFloorRoomKind.MAIN_CAP_APPROACH, 9, 9, 7)),
                List.of(MKFloorRoomProfile.defaults(MKFloorRoomKind.MAIN_CAP, 9, 9, 7))
        );

        MKFloorLayoutSolver.FloorLayoutResult result = new MKFloorLayoutSolver().solve(
                settings,
                9,
                9,
                List.of(
                        new MKFamilyHorizontalExitDefinition(Direction.NORTH,
                                MKHorizontalExitPathKind.BRANCH,
                                MKFloorRoomProfile.INHERITED_BRANCH_OPENING_PROFILE_ID),
                        new MKFamilyHorizontalExitDefinition(Direction.NORTH,
                                MKHorizontalExitPathKind.MAIN_EXIT,
                                MKFloorRoomProfile.INHERITED_MAIN_OPENING_PROFILE_ID)
                ),
                1,
                99L
        );

        assertTrue(result.fitsHardLimit());
        assertFalse(result.hasRequiredRejections());
        assertEquals(1, result.segments().stream()
                .filter(segment -> segment.kind() == MKFloorLayoutSolver.SegmentKind.MAIN_ROOM)
                .count());
        assertEquals(1, result.segments().stream()
                .filter(segment -> segment.kind() == MKFloorLayoutSolver.SegmentKind.MAIN_CAP)
                .count());
        assertTrue(result.rejectedExits().stream().anyMatch(rejection ->
                !rejection.required() && rejection.sourceKind() == MKFloorRoomKind.BRANCH_CAP));
    }

    @Test
    void mainPathTriesAlternateProfilesBeforeFailing() {
        MKFloorRoomProfile oversizedMainRoom = new MKFloorRoomProfile(
                "oversized",
                "Oversized",
                MKFloorRoomKind.MAIN_ROOM,
                9,
                255,
                7,
                1000,
                Optional.empty()
        );
        MKFloorRoomProfile fittingMainRoom = new MKFloorRoomProfile(
                "fitting",
                "Fitting",
                MKFloorRoomKind.MAIN_ROOM,
                9,
                9,
                7,
                1,
                Optional.empty()
        );
        MKFloorTopologySettings settings = new MKFloorTopologySettings(
                "tower.primary",
                "main_floor",
                1,
                1,
                0,
                MKHallwayLeadInMode.MANUAL,
                1,
                false,
                false,
                false,
                1.0f,
                Optional.empty(),
                List.of(oversizedMainRoom, fittingMainRoom),
                List.of(MKFloorRoomProfile.defaults(MKFloorRoomKind.BRANCH_ROOM, 9, 9, 7)),
                List.of(MKFloorRoomProfile.defaults(MKFloorRoomKind.BRANCH_CAP, 9, 9, 7)),
                List.of(MKFloorRoomProfile.defaults(MKFloorRoomKind.MAIN_CAP_APPROACH, 9, 9, 7)),
                List.of(MKFloorRoomProfile.defaults(MKFloorRoomKind.MAIN_CAP, 9, 9, 7))
        );

        MKFloorLayoutSolver.FloorLayoutResult result = new MKFloorLayoutSolver().solve(
                settings,
                9,
                9,
                List.of(new MKFamilyHorizontalExitDefinition(Direction.NORTH,
                        MKHorizontalExitPathKind.MAIN_EXIT,
                        MKFloorRoomProfile.INHERITED_MAIN_OPENING_PROFILE_ID)),
                1,
                99L
        );

        MKFloorLayoutSolver.LogicalSegment mainRoom = result.segments().stream()
                .filter(segment -> segment.kind() == MKFloorLayoutSolver.SegmentKind.MAIN_ROOM)
                .findFirst()
                .orElseThrow();
        assertTrue(result.fitsHardLimit());
        assertFalse(result.hasRequiredRejections());
        assertTrue(mainRoom.tooltip().contains("Fitting"));
    }

    @Test
    void linkCandidatesCanCreateDoglegLoopAfterTopologySolve() {
        MKFloorTopologySettings settings = linkSettings().withLinksEnabled(true).withMaxLinkLength(128);

        MKFloorLayoutSolver.FloorLayoutResult result = new MKFloorLayoutSolver().solve(
                settings,
                9,
                9,
                List.of(
                        new MKFamilyHorizontalExitDefinition(Direction.NORTH,
                                MKHorizontalExitPathKind.MAIN_EXIT,
                                MKFloorRoomProfile.INHERITED_MAIN_OPENING_PROFILE_ID),
                        new MKFamilyHorizontalExitDefinition(Direction.EAST,
                                MKHorizontalExitPathKind.BRANCH,
                                MKFloorRoomProfile.INHERITED_BRANCH_OPENING_PROFILE_ID)
                ),
                1,
                99L
        );

        assertEquals(1, result.acceptedLinks().size());
        assertEquals(2, result.acceptedLinks().getFirst().route().size());
        assertEquals(2, result.segments().stream()
                .filter(segment -> segment.kind() == MKFloorLayoutSolver.SegmentKind.LINK_HALL)
                .count());
        assertTrue(result.fitsHardLimit());
    }

    @Test
    void linkCandidatesRespectMaxLinkLength() {
        MKFloorTopologySettings settings = linkSettings().withLinksEnabled(true).withMaxLinkLength(8);

        MKFloorLayoutSolver.FloorLayoutResult result = new MKFloorLayoutSolver().solve(
                settings,
                9,
                9,
                List.of(
                        new MKFamilyHorizontalExitDefinition(Direction.NORTH,
                                MKHorizontalExitPathKind.MAIN_EXIT,
                                MKFloorRoomProfile.INHERITED_MAIN_OPENING_PROFILE_ID),
                        new MKFamilyHorizontalExitDefinition(Direction.EAST,
                                MKHorizontalExitPathKind.BRANCH,
                                MKFloorRoomProfile.INHERITED_BRANCH_OPENING_PROFILE_ID)
                ),
                1,
                99L
        );

        assertTrue(result.acceptedLinks().isEmpty());
        assertFalse(result.segments().stream()
                .anyMatch(segment -> segment.kind() == MKFloorLayoutSolver.SegmentKind.LINK_HALL));
        assertTrue(result.fitsHardLimit());
    }

    private static MKFloorTopologySettings settings(float sprawl) {
        MKFloorRoomProfile mainRoom = MKFloorRoomProfile
                .defaults(MKFloorRoomKind.MAIN_ROOM, 9, 9, 7)
                .withHorizontalExits(List.of(
                        new MKFamilyHorizontalExitDefinition(Direction.NORTH,
                                MKHorizontalExitPathKind.MAIN_EXIT,
                                MKFloorRoomProfile.INHERITED_MAIN_OPENING_PROFILE_ID),
                        new MKFamilyHorizontalExitDefinition(Direction.EAST,
                                MKHorizontalExitPathKind.BRANCH,
                                MKFloorRoomProfile.INHERITED_BRANCH_OPENING_PROFILE_ID),
                        new MKFamilyHorizontalExitDefinition(Direction.WEST,
                                MKHorizontalExitPathKind.BRANCH,
                                MKFloorRoomProfile.INHERITED_BRANCH_OPENING_PROFILE_ID)
                ));
        return new MKFloorTopologySettings(
                "tower.primary",
                "main_floor",
                1,
                1,
                0,
                MKHallwayLeadInMode.MANUAL,
                1,
                false,
                false,
                false,
                sprawl,
                java.util.Optional.empty(),
                List.of(mainRoom),
                List.of(MKFloorRoomProfile.defaults(MKFloorRoomKind.BRANCH_ROOM, 9, 9, 7)),
                List.of(MKFloorRoomProfile.defaults(MKFloorRoomKind.BRANCH_CAP, 9, 9, 7)),
                List.of(MKFloorRoomProfile.defaults(MKFloorRoomKind.MAIN_CAP_APPROACH, 9, 9, 7)),
                List.of(MKFloorRoomProfile.defaults(MKFloorRoomKind.MAIN_CAP, 9, 9, 7))
        );
    }

    private static MKFloorTopologySettings linkSettings() {
        MKFloorRoomProfile mainCap = MKFloorRoomProfile
                .defaults(MKFloorRoomKind.MAIN_CAP, 9, 9, 7)
                .withHorizontalExits(List.of(new MKFamilyHorizontalExitDefinition(
                        Direction.EAST,
                        MKHorizontalExitPathKind.LINK_CANDIDATE,
                        MKFloorRoomProfile.INHERITED_LINK_OPENING_PROFILE_ID)));
        MKFloorRoomProfile branchCap = MKFloorRoomProfile
                .defaults(MKFloorRoomKind.BRANCH_CAP, 9, 9, 7)
                .withHorizontalExits(List.of(new MKFamilyHorizontalExitDefinition(
                        Direction.WEST,
                        MKHorizontalExitPathKind.LINK_CANDIDATE,
                        MKFloorRoomProfile.INHERITED_LINK_OPENING_PROFILE_ID)));
        return new MKFloorTopologySettings(
                "tower.primary",
                "main_floor",
                1,
                1,
                0,
                MKHallwayLeadInMode.MANUAL,
                1,
                true,
                true,
                false,
                1.0f,
                Optional.empty(),
                List.of(MKFloorRoomProfile.defaults(MKFloorRoomKind.MAIN_ROOM, 9, 9, 7)),
                List.of(MKFloorRoomProfile.defaults(MKFloorRoomKind.BRANCH_ROOM, 9, 9, 7)),
                List.of(branchCap),
                List.of(MKFloorRoomProfile.defaults(MKFloorRoomKind.MAIN_CAP_APPROACH, 9, 9, 7)),
                List.of(mainCap)
        );
    }
}
