package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorRoomKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorRoomProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorTopologySettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitPathKind;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MKFloorLayoutSolver {
    private static final int STRUCTURE_RADIUS_LIMIT = 128;

    public FloorLayoutResult solve(MKWorkspaceFloorTopologySettings settings, int rootWidth, int rootLength,
                                   List<MKWorkspaceFamilyHorizontalExitDefinition> rootExits,
                                   int effectiveHallwayLeadInPieces, long seed) {
        Random random = new Random(seed);
        ArrayList<LogicalSegment> segments = new ArrayList<>();
        ArrayList<RejectedExit> rejected = new ArrayList<>();
        LogicalRect root = new LogicalRect(0.0f, 0.0f, Math.max(3, rootWidth), Math.max(3, rootLength));
        segments.add(new LogicalSegment(root, SegmentKind.ROOT, null, "Root",
                "Floor Root\n" + rootWidth + " x " + rootLength, false, ""));

        for (MKWorkspaceFamilyHorizontalExitDefinition exit : horizontalRootExits(rootExits)) {
            if (exit.pathKind() == MKWorkspaceHorizontalExitPathKind.MAIN_EXIT) {
                addPath(segments, rejected, settings, root, exit.direction(), true, 0,
                        effectiveHallwayLeadInPieces, random, true);
            }
        }
        for (MKWorkspaceFamilyHorizontalExitDefinition exit : horizontalRootExits(rootExits)) {
            if (exit.pathKind() == MKWorkspaceHorizontalExitPathKind.BRANCH) {
                addPath(segments, rejected, settings, root, exit.direction(), false, 0,
                        effectiveHallwayLeadInPieces, random, true);
            }
        }
        return new FloorLayoutResult(List.copyOf(segments), List.copyOf(rejected),
                extents(segments), fitsHardLimit(segments) && rejected.stream().noneMatch(RejectedExit::required));
    }

    private void addPath(ArrayList<LogicalSegment> segments, ArrayList<RejectedExit> rejected,
                         MKWorkspaceFloorTopologySettings settings, LogicalRect start, Direction direction,
                         boolean main, int branchDepth, int leadIn, Random random, boolean explicitRootExit) {
        if (!main) {
            addBranchPath(segments, rejected, settings, start, direction, branchDepth, leadIn, random,
                    explicitRootExit);
            return;
        }
        boolean hallwaysEnabled = main ? settings.mainHallwaysEnabled() : settings.branchHallwaysEnabled();
        int hallwayLength = Math.max(1, leadIn);
        LogicalRect cursor = start;
        Direction currentDirection = direction;
        ArrayList<PendingBranchSource> pendingBranches = new ArrayList<>();
        List<RoomStep> roomSteps = roomSteps(settings, main, random);
        for (int i = 0; i < roomSteps.size(); i++) {
            RoomStep step = roomSteps.get(i);
            ArrayList<LogicalSegment> candidate = new ArrayList<>();
            LogicalRect candidateCursor = cursor;
            if (hallwaysEnabled) {
                candidateCursor = appendHallway(candidate, settings, candidateCursor, currentDirection, hallwayLength, main,
                        i == 0);
            }
            candidateCursor = appendRoom(candidate, candidateCursor, currentDirection, step);
            if (!candidateFits(segments, candidate)) {
                rejected.add(new RejectedExit(currentDirection, step.profile().kind(),
                        rejectionReason(segments, candidate), true));
                return;
            }
            segments.addAll(candidate);
            cursor = candidateCursor;
            int roomSegmentIndex = segments.size() - 1;
            if (step.allowBranches()) {
                pendingBranches.add(new PendingBranchSource(roomSegmentIndex, cursor, currentDirection,
                        step.profile(), branchDepth, false));
            }
            if (step.profile().mainExitDirection().isPresent()) {
                currentDirection = rotateRoomExit(step.profile().mainExitDirection().orElseThrow(), currentDirection);
            }
        }
        for (PendingBranchSource source : pendingBranches) {
            String acceptedMask = addBranchesFromRoom(segments, rejected, settings, source.room(),
                    source.pathDirection(), source.profile(), source.branchDepth(), leadIn, random,
                    source.explicitRootExit());
            segments.set(source.segmentIndex(), segments.get(source.segmentIndex()).withAcceptedMask(acceptedMask));
        }
    }

    private List<MKWorkspaceFamilyHorizontalExitDefinition> horizontalRootExits(
            List<MKWorkspaceFamilyHorizontalExitDefinition> rootExits) {
        return rootExits.stream()
                .filter(exit -> exit.direction().getAxis().isHorizontal())
                .toList();
    }

    private void addBranchPath(ArrayList<LogicalSegment> segments, ArrayList<RejectedExit> rejected,
                               MKWorkspaceFloorTopologySettings settings, LogicalRect start, Direction direction,
                               int branchDepth, int leadIn, Random random, boolean explicitRootExit) {
        boolean hallwaysEnabled = settings.branchHallwaysEnabled();
        int hallwayLength = Math.max(1, leadIn);
        LogicalRect cursor = start;
        int desiredRoomCount = sampledCount(0, settings.maxBranchPiecesBeforeCap(), settings.sprawl(), random);
        for (int placedRooms = 0; placedRooms <= desiredRoomCount; placedRooms++) {
            if (placedRooms >= desiredRoomCount) {
                tryAppendBranchCap(segments, rejected, settings, cursor, direction, hallwayLength, hallwaysEnabled,
                        placedRooms == 0, random);
                return;
            }

            RoomStep branchRoom = new RoomStep(weightedProfile(settings.branchRoomProfiles(), random),
                    "B" + (placedRooms + 1), SegmentKind.BRANCH_ROOM,
                    "branch room " + (placedRooms + 1) + " of sampled " + desiredRoomCount, true);
            StepCandidate candidate = stepCandidate(settings, cursor, direction, hallwayLength, false,
                    hallwaysEnabled, placedRooms == 0, branchRoom);
            boolean canContinue = candidateFits(segments, candidate.segments()) &&
                    branchCanTerminateLater(combined(segments, candidate.segments()), settings, candidate.cursor(),
                            direction, hallwayLength, hallwaysEnabled, placedRooms + 1, desiredRoomCount);
            if (!canContinue) {
                tryAppendBranchCap(segments, rejected, settings, cursor, direction, hallwayLength, hallwaysEnabled,
                        placedRooms == 0, random);
                return;
            }

            segments.addAll(candidate.segments());
            cursor = candidate.cursor();
            int roomSegmentIndex = segments.size() - 1;
            String acceptedMask = addBranchesFromRoom(segments, rejected, settings, cursor, direction,
                    branchRoom.profile(), branchDepth, leadIn, random, explicitRootExit);
            segments.set(roomSegmentIndex, segments.get(roomSegmentIndex).withAcceptedMask(acceptedMask));
        }
    }

    private boolean branchCanTerminateLater(List<LogicalSegment> existing, MKWorkspaceFloorTopologySettings settings,
                                            LogicalRect cursor, Direction direction, int hallwayLength,
                                            boolean hallwaysEnabled, int placedRooms, int desiredRoomCount) {
        RoomStep nextStep = placedRooms < desiredRoomCount ?
                new RoomStep(settings.branchRoomProfiles().getFirst(), "B" + (placedRooms + 1),
                        SegmentKind.BRANCH_ROOM, "branch continuation fit probe", true) :
                new RoomStep(settings.branchCapProfiles().getFirst(), "Branch Cap",
                        SegmentKind.BRANCH_CAP, "terminal branch cap fit probe", false);
        StepCandidate nextCandidate = stepCandidate(settings, cursor, direction, hallwayLength, false,
                hallwaysEnabled, false, nextStep);
        return candidateFits(existing, nextCandidate.segments());
    }

    private void tryAppendBranchCap(ArrayList<LogicalSegment> segments, ArrayList<RejectedExit> rejected,
                                    MKWorkspaceFloorTopologySettings settings, LogicalRect cursor,
                                    Direction direction, int hallwayLength, boolean hallwaysEnabled,
                                    boolean leadIn, Random random) {
        RoomStep cap = branchCapStep(settings, random);
        StepCandidate candidate = stepCandidate(settings, cursor, direction, hallwayLength, false, hallwaysEnabled,
                leadIn, cap);
        if (!candidateFits(segments, candidate.segments())) {
            rejected.add(new RejectedExit(direction, MKWorkspaceFloorRoomKind.BRANCH_CAP,
                    rejectionReason(segments, candidate.segments())));
            return;
        }
        segments.addAll(candidate.segments());
    }

    private RoomStep branchCapStep(MKWorkspaceFloorTopologySettings settings, Random random) {
        return new RoomStep(weightedProfile(settings.branchCapProfiles(), random), "Branch Cap",
                SegmentKind.BRANCH_CAP, "terminal branch cap", false);
    }

    private StepCandidate stepCandidate(MKWorkspaceFloorTopologySettings settings, LogicalRect cursor,
                                        Direction direction, int hallwayLength, boolean main,
                                        boolean hallwaysEnabled, boolean leadIn, RoomStep step) {
        ArrayList<LogicalSegment> candidate = new ArrayList<>();
        LogicalRect candidateCursor = cursor;
        if (hallwaysEnabled) {
            candidateCursor = appendHallway(candidate, settings, candidateCursor, direction, hallwayLength, main,
                    leadIn);
        }
        candidateCursor = appendRoom(candidate, candidateCursor, direction, step);
        return new StepCandidate(List.copyOf(candidate), candidateCursor);
    }

    private List<RoomStep> roomSteps(MKWorkspaceFloorTopologySettings settings, boolean main, Random random) {
        ArrayList<RoomStep> steps = new ArrayList<>();
        if (main) {
            int roomCount = sampledCount(settings.minMainPathPieces(), settings.maxMainPathPieces(),
                    settings.sprawl(), random);
            List<MKWorkspaceFloorRoomProfile> mainProfiles = settings.mainRoomProfiles();
            for (int i = 0; i < roomCount; i++) {
                steps.add(new RoomStep(weightedProfile(mainProfiles, random), "M" + (i + 1), SegmentKind.MAIN_ROOM,
                        "main room " + (i + 1) + " of sampled " + roomCount, true));
            }
            if (settings.mainCapApproachEnabled()) {
                steps.add(new RoomStep(weightedProfile(settings.mainCapApproachProfiles(), random), "Approach",
                        SegmentKind.MAIN_CAP, "main cap approach", false));
            }
            steps.add(new RoomStep(weightedProfile(settings.mainCapProfiles(), random), "Main Cap",
                    SegmentKind.MAIN_CAP, "terminal main cap", false));
        } else {
            int roomCount = sampledCount(0, settings.maxBranchPiecesBeforeCap(), settings.sprawl(), random);
            List<MKWorkspaceFloorRoomProfile> branchProfiles = settings.branchRoomProfiles();
            for (int i = 0; i < roomCount; i++) {
                steps.add(new RoomStep(weightedProfile(branchProfiles, random), "B" + (i + 1), SegmentKind.BRANCH_ROOM,
                        "branch room " + (i + 1) + " of sampled " + roomCount, true));
            }
            steps.add(new RoomStep(weightedProfile(settings.branchCapProfiles(), random), "Branch Cap",
                    SegmentKind.BRANCH_CAP, "terminal branch cap", false));
        }
        return List.copyOf(steps);
    }

    private int sampledCount(int min, int max, float sprawl, Random random) {
        if (min >= max) {
            return Math.max(0, max);
        }
        int span = max - min;
        if (sprawl <= 0.0f) {
            return min;
        }
        float sample = (sprawl * 0.75f) + (random.nextFloat() * sprawl * 0.25f);
        return min + Math.round(span * Math.max(0.0f, Math.min(1.0f, sample)));
    }

    private MKWorkspaceFloorRoomProfile weightedProfile(List<MKWorkspaceFloorRoomProfile> profiles, Random random) {
        if (profiles.isEmpty()) {
            throw new IllegalStateException("floor topology room profile list cannot be empty");
        }
        int totalWeight = profiles.stream().mapToInt(MKWorkspaceFloorRoomProfile::weight).sum();
        int roll = random.nextInt(Math.max(1, totalWeight));
        int cursor = 0;
        for (MKWorkspaceFloorRoomProfile profile : profiles) {
            cursor += profile.weight();
            if (roll < cursor) {
                return profile;
            }
        }
        return profiles.getLast();
    }

    private LogicalRect appendHallway(ArrayList<LogicalSegment> segments, MKWorkspaceFloorTopologySettings settings,
                                      LogicalRect cursor, Direction direction, int hallwayLength, boolean main,
                                      boolean leadIn) {
        MKWorkspaceFloorRoomProfile reference = main ? settings.mainRoomProfiles().getFirst() :
                settings.branchRoomProfiles().getFirst();
        int hallwayMinor = main ? Math.max(3, Math.min(7, reference.width() / 2)) :
                Math.max(3, Math.min(5, reference.width() / 2));
        LogicalRect rect = rectAfter(cursor, direction, hallwayLength, hallwayMinor);
        String label = main ? "Main Hall" : "Branch Hall";
        segments.add(new LogicalSegment(rect, main ? SegmentKind.MAIN_HALL : SegmentKind.BRANCH_HALL, direction,
                label, label + "\n" + direction.getSerializedName() +
                (leadIn ? "\nlead-in " + hallwayLength : "\nlink " + hallwayLength), false, ""));
        return rect;
    }

    private LogicalRect appendRoom(ArrayList<LogicalSegment> segments, LogicalRect cursor, Direction direction,
                                   RoomStep step) {
        int major = direction == Direction.NORTH || direction == Direction.SOUTH ?
                step.profile().length() : step.profile().width();
        int minor = direction == Direction.NORTH || direction == Direction.SOUTH ?
                step.profile().width() : step.profile().length();
        LogicalRect rect = rectAfter(cursor, direction, major, minor);
        segments.add(new LogicalSegment(rect, step.kind(), direction, step.label(),
                step.profile().label() + "\n" + step.profile().width() + " x " + step.profile().length() +
                        "\nheight " + step.profile().height() + "\n" + step.tooltip(), true,
                "none"));
        return rect;
    }

    private String addBranchesFromRoom(ArrayList<LogicalSegment> segments, ArrayList<RejectedExit> rejected,
                                       MKWorkspaceFloorTopologySettings settings, LogicalRect room,
                                       Direction pathDirection, MKWorkspaceFloorRoomProfile profile,
                                       int branchDepth, int leadIn, Random random, boolean explicitRootExit) {
        if (branchDepth >= 1) {
            return "none";
        }
        ArrayList<MKWorkspaceFamilyHorizontalExitDefinition> candidates = new ArrayList<>(profile.horizontalExits().stream()
                .filter(exit -> exit.pathKind() == MKWorkspaceHorizontalExitPathKind.BRANCH)
                .filter(exit -> exit.direction() != Direction.SOUTH)
                .toList());
        Collections.shuffle(candidates, random);
        int accepted = 0;
        ArrayList<Direction> acceptedLocalDirections = new ArrayList<>();
        for (MKWorkspaceFamilyHorizontalExitDefinition exit : candidates) {
            if (!shouldKeepOptionalBranch(settings.sprawl(), random, explicitRootExit, accepted)) {
                rejected.add(new RejectedExit(rotateRoomExit(exit.direction(), pathDirection), profile.kind(),
                        "sprawl rejected"));
                continue;
            }
            Direction branchDirection = rotateRoomExit(exit.direction(), pathDirection);
            int before = segments.size();
            addPath(segments, rejected, settings, room, branchDirection, false, branchDepth + 1, leadIn, random,
                    false);
            if (segments.size() > before) {
                accepted++;
                acceptedLocalDirections.add(exit.direction());
            }
        }
        return maskName(acceptedLocalDirections);
    }

    private boolean shouldKeepOptionalBranch(float sprawl, Random random, boolean explicitRootExit, int accepted) {
        if (sprawl <= 0.0f) {
            return explicitRootExit && accepted == 0;
        }
        return random.nextFloat() <= Math.max(0.05f, sprawl);
    }

    private String maskName(List<Direction> directions) {
        StringBuilder mask = new StringBuilder();
        for (Direction direction : List.of(Direction.NORTH, Direction.EAST, Direction.WEST)) {
            if (directions.contains(direction)) {
                mask.append(direction.getSerializedName().charAt(0));
            }
        }
        return mask.isEmpty() ? "none" : mask.toString();
    }

    private boolean collides(List<LogicalSegment> existing, List<LogicalSegment> candidate) {
        for (LogicalSegment candidateSegment : candidate) {
            if (candidateSegment.kind() == SegmentKind.ROOT) {
                continue;
            }
            for (LogicalSegment existingSegment : existing) {
                if (existingSegment.rect().intersects(candidateSegment.rect())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean candidateFits(List<LogicalSegment> existing, List<LogicalSegment> candidate) {
        return !collides(existing, candidate) && fitsHardLimit(combined(existing, candidate));
    }

    private String rejectionReason(List<LogicalSegment> existing, List<LogicalSegment> candidate) {
        if (collides(existing, candidate)) {
            return "footprint collision";
        }
        if (!fitsHardLimit(combined(existing, candidate))) {
            return "horizontal bound";
        }
        return "fit rejected";
    }

    private List<LogicalSegment> combined(List<LogicalSegment> existing, List<LogicalSegment> candidate) {
        ArrayList<LogicalSegment> combined = new ArrayList<>(existing);
        combined.addAll(candidate);
        return combined;
    }

    private LogicalRect rectAfter(LogicalRect previous, Direction direction, int major, int minor) {
        float width = direction == Direction.EAST || direction == Direction.WEST ? major : minor;
        float height = direction == Direction.NORTH || direction == Direction.SOUTH ? major : minor;
        float distance = previous.major(direction) / 2.0f + major / 2.0f + 1.0f;
        return new LogicalRect(
                previous.centerX() + directionX(direction) * distance,
                previous.centerY() + directionY(direction) * distance,
                width,
                height
        );
    }

    private Direction rotateRoomExit(Direction localDirection, Direction pathDirection) {
        if (pathDirection == Direction.NORTH) {
            return localDirection;
        }
        if (pathDirection == Direction.SOUTH) {
            return localDirection.getOpposite();
        }
        if (pathDirection == Direction.EAST) {
            return switch (localDirection) {
                case NORTH -> Direction.EAST;
                case EAST -> Direction.SOUTH;
                case SOUTH -> Direction.WEST;
                case WEST -> Direction.NORTH;
                default -> localDirection;
            };
        }
        if (pathDirection == Direction.WEST) {
            return switch (localDirection) {
                case NORTH -> Direction.WEST;
                case EAST -> Direction.NORTH;
                case SOUTH -> Direction.EAST;
                case WEST -> Direction.SOUTH;
                default -> localDirection;
            };
        }
        return localDirection;
    }

    private float directionX(Direction direction) {
        return switch (direction) {
            case EAST -> 1.0f;
            case WEST -> -1.0f;
            default -> 0.0f;
        };
    }

    private float directionY(Direction direction) {
        return switch (direction) {
            case SOUTH -> 1.0f;
            case NORTH -> -1.0f;
            default -> 0.0f;
        };
    }

    private LayoutExtents extents(List<LogicalSegment> segments) {
        float minX = 0.0f;
        float maxX = 0.0f;
        float minY = 0.0f;
        float maxY = 0.0f;
        for (LogicalSegment segment : segments) {
            minX = Math.min(minX, segment.rect().left());
            maxX = Math.max(maxX, segment.rect().right());
            minY = Math.min(minY, segment.rect().top());
            maxY = Math.max(maxY, segment.rect().bottom());
        }
        return new LayoutExtents(minX, maxX, minY, maxY);
    }

    private boolean fitsHardLimit(List<LogicalSegment> segments) {
        LayoutExtents extents = extents(segments);
        return Math.max(Math.abs(extents.minX()), Math.abs(extents.maxX())) <= STRUCTURE_RADIUS_LIMIT &&
                Math.max(Math.abs(extents.minY()), Math.abs(extents.maxY())) <= STRUCTURE_RADIUS_LIMIT;
    }

    private record RoomStep(MKWorkspaceFloorRoomProfile profile, String label, SegmentKind kind, String tooltip,
                            boolean allowBranches) {
    }

    private record StepCandidate(List<LogicalSegment> segments, LogicalRect cursor) {
    }

    private record PendingBranchSource(int segmentIndex, LogicalRect room, Direction pathDirection,
                                       MKWorkspaceFloorRoomProfile profile, int branchDepth,
                                       boolean explicitRootExit) {
    }

    public record FloorLayoutResult(List<LogicalSegment> segments, List<RejectedExit> rejectedExits,
                                    LayoutExtents extents, boolean fitsHardLimit) {
        public boolean hasRequiredRejections() {
            return rejectedExits.stream().anyMatch(RejectedExit::required);
        }
    }

    public record LogicalSegment(LogicalRect rect, SegmentKind kind, Direction direction, String label,
                                 String tooltip, boolean roomRegion, String acceptedMask) {
        public LogicalSegment withAcceptedMask(String mask) {
            return new LogicalSegment(rect, kind, direction, label, tooltip, roomRegion, mask);
        }
    }

    public record RejectedExit(Direction direction, MKWorkspaceFloorRoomKind sourceKind, String reason,
                               boolean required) {
        public RejectedExit(Direction direction, MKWorkspaceFloorRoomKind sourceKind, String reason) {
            this(direction, sourceKind, reason, false);
        }
    }

    public record LayoutExtents(float minX, float maxX, float minY, float maxY) {
    }

    public record LogicalRect(float centerX, float centerY, float width, float height) {
        public float left() {
            return centerX - width / 2.0f;
        }

        public float right() {
            return centerX + width / 2.0f;
        }

        public float top() {
            return centerY - height / 2.0f;
        }

        public float bottom() {
            return centerY + height / 2.0f;
        }

        public float major(Direction direction) {
            return direction == Direction.EAST || direction == Direction.WEST ? width : height;
        }

        public boolean intersects(LogicalRect other) {
            return left() < other.right() && right() > other.left() &&
                    top() < other.bottom() && bottom() > other.top();
        }
    }

    public enum SegmentKind {
        ROOT,
        MAIN_HALL,
        BRANCH_HALL,
        MAIN_ROOM,
        BRANCH_ROOM,
        MAIN_CAP,
        BRANCH_CAP
    }
}
