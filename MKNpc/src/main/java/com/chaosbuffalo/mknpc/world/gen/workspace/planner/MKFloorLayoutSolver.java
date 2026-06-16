package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorRoomKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorRoomProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorTopologySettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitPathKind;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class MKFloorLayoutSolver {
    private static final int STRUCTURE_RADIUS_LIMIT = 128;

    public FloorLayoutResult solve(MKWorkspaceFloorTopologySettings settings, int rootWidth, int rootLength,
                                   List<MKWorkspaceFamilyHorizontalExitDefinition> rootExits,
                                   int effectiveHallwayLeadInPieces, long seed) {
        return solve(settings, rootWidth, rootLength, rootExits, effectiveHallwayLeadInPieces, seed,
                STRUCTURE_RADIUS_LIMIT);
    }

    public FloorLayoutResult solve(MKWorkspaceFloorTopologySettings settings, int rootWidth, int rootLength,
                                   List<MKWorkspaceFamilyHorizontalExitDefinition> rootExits,
                                   int effectiveHallwayLeadInPieces, long seed, int maxHorizontalRadius) {
        Random random = new Random(seed);
        int radiusLimit = Math.max(1, Math.min(STRUCTURE_RADIUS_LIMIT, maxHorizontalRadius));
        ArrayList<LogicalSegment> segments = new ArrayList<>();
        ArrayList<RejectedExit> rejected = new ArrayList<>();
        LogicalRect root = new LogicalRect(0.0f, 0.0f, Math.max(3, rootWidth), Math.max(3, rootLength));
        segments.add(new LogicalSegment(root, SegmentKind.ROOT, null, "Root",
                "Floor Root\n" + rootWidth + " x " + rootLength, false, "", 0, -1, null));

        for (MKWorkspaceFamilyHorizontalExitDefinition exit : horizontalRootExits(rootExits)) {
            if (exit.pathKind() == MKWorkspaceHorizontalExitPathKind.MAIN_EXIT) {
                addPath(segments, rejected, settings, root, exit.direction(), true, 0,
                        effectiveHallwayLeadInPieces, random, true, 0, radiusLimit);
            }
        }
        for (MKWorkspaceFamilyHorizontalExitDefinition exit : horizontalRootExits(rootExits)) {
            if (exit.pathKind() == MKWorkspaceHorizontalExitPathKind.BRANCH) {
                addPath(segments, rejected, settings, root, exit.direction(), false, 0,
                        effectiveHallwayLeadInPieces, random, true, 0, radiusLimit);
            }
        }
        List<AcceptedLink> acceptedLinks = settings.linksEnabled() ?
                addLinks(segments, settings, random, radiusLimit) : List.of();
        return new FloorLayoutResult(List.copyOf(segments), List.copyOf(rejected), acceptedLinks,
                extents(segments), fitsHardLimit(segments, radiusLimit) &&
                rejected.stream().noneMatch(RejectedExit::required));
    }

    private void addPath(ArrayList<LogicalSegment> segments, ArrayList<RejectedExit> rejected,
                         MKWorkspaceFloorTopologySettings settings, LogicalRect start, Direction direction,
                         boolean main, int branchDepth, int leadIn, Random random, boolean explicitRootExit,
                         int parentSegmentIndex, int radiusLimit) {
        if (!main) {
            addBranchPath(segments, rejected, settings, start, direction, branchDepth, leadIn, random,
                    explicitRootExit, parentSegmentIndex, radiusLimit);
            return;
        }
        addMainPath(segments, rejected, settings, start, direction, branchDepth, leadIn, random, parentSegmentIndex,
                radiusLimit);
    }

    private void addMainPath(ArrayList<LogicalSegment> segments, ArrayList<RejectedExit> rejected,
                             MKWorkspaceFloorTopologySettings settings, LogicalRect start, Direction direction,
                             int branchDepth, int leadIn, Random random, int parentSegmentIndex, int radiusLimit) {
        boolean hallwaysEnabled = settings.mainHallwaysEnabled();
        int hallwayLength = hallwayLength(settings, true, leadIn);
        ArrayList<PendingBranchSource> pendingBranches = new ArrayList<>();
        List<MainStepSpec> steps = mainStepSpecs(settings, random);
        Optional<MainFailure> failure = placeMainStep(segments, settings, start, direction, branchDepth, leadIn,
                hallwayLength, hallwaysEnabled, steps, 0, pendingBranches, parentSegmentIndex, radiusLimit);
        if (failure.isPresent()) {
            MainFailure failed = failure.orElseThrow();
            rejected.add(new RejectedExit(failed.direction(), failed.kind(), failed.reason(), true));
            return;
        }
        for (PendingBranchSource source : pendingBranches) {
            String acceptedMask = addBranchesFromRoom(segments, rejected, settings, source.room(),
                    source.pathDirection(), source.profile(), source.branchDepth(), leadIn, random,
                    source.explicitRootExit(), source.segmentIndex(), radiusLimit);
            segments.set(source.segmentIndex(), segments.get(source.segmentIndex()).withAcceptedMask(acceptedMask));
        }
    }

    private Optional<MainFailure> placeMainStep(ArrayList<LogicalSegment> segments,
                                                MKWorkspaceFloorTopologySettings settings,
                                                LogicalRect cursor,
                                                Direction currentDirection,
                                                int branchDepth,
                                                int leadIn,
                                                int hallwayLength,
                                                boolean hallwaysEnabled,
                                                List<MainStepSpec> steps,
                                                int stepIndex,
                                                ArrayList<PendingBranchSource> pendingBranches,
                                                int parentSegmentIndex,
                                                int radiusLimit) {
        if (stepIndex >= steps.size()) {
            return Optional.empty();
        }
        MainStepSpec spec = steps.get(stepIndex);
        MainFailure failure = null;
        for (MKWorkspaceFloorRoomProfile profile : spec.profiles()) {
            RoomStep step = new RoomStep(profile, spec.label(), spec.kind(), spec.tooltip(), spec.allowBranches());
            StepCandidate candidate = stepCandidate(settings, cursor, currentDirection, hallwayLength, true,
                    hallwaysEnabled, stepIndex == 0, step, segments.size(), parentSegmentIndex);
            if (!candidateFits(segments, candidate.segments(), radiusLimit)) {
                failure = new MainFailure(currentDirection, step.profile().kind(),
                        rejectionReason(segments, candidate.segments(), radiusLimit));
                continue;
            }

            int segmentCount = segments.size();
            int pendingCount = pendingBranches.size();
            segments.addAll(candidate.segments());
            int roomSegmentIndex = segments.size() - 1;
            if (step.allowBranches()) {
                pendingBranches.add(new PendingBranchSource(roomSegmentIndex, candidate.cursor(), currentDirection,
                        step.profile(), branchDepth, false));
            }
            Direction nextDirection = currentDirection;
            if (step.profile().mainExitDirection().isPresent()) {
                nextDirection = rotateRoomExit(step.profile().mainExitDirection().orElseThrow(), currentDirection);
            }
            Optional<MainFailure> downstreamFailure = placeMainStep(segments, settings, candidate.cursor(),
                    nextDirection, branchDepth, leadIn, hallwayLength, hallwaysEnabled, steps, stepIndex + 1,
                    pendingBranches, roomSegmentIndex, radiusLimit);
            if (downstreamFailure.isEmpty()) {
                return Optional.empty();
            }
            failure = downstreamFailure.orElseThrow();
            rollbackSegments(segments, segmentCount);
            rollbackPendingBranches(pendingBranches, pendingCount);
        }
        return Optional.ofNullable(failure)
                .or(() -> Optional.of(new MainFailure(currentDirection, spec.profiles().getFirst().kind(),
                        "no fitting main profile")));
    }

    private void rollbackSegments(ArrayList<LogicalSegment> segments, int size) {
        while (segments.size() > size) {
            segments.remove(segments.size() - 1);
        }
    }

    private void rollbackPendingBranches(ArrayList<PendingBranchSource> pendingBranches, int size) {
        while (pendingBranches.size() > size) {
            pendingBranches.remove(pendingBranches.size() - 1);
        }
    }

    private List<MKWorkspaceFamilyHorizontalExitDefinition> horizontalRootExits(
            List<MKWorkspaceFamilyHorizontalExitDefinition> rootExits) {
        return rootExits.stream()
                .filter(exit -> exit.direction().getAxis().isHorizontal())
                .sorted((left, right) -> {
                    int pathComparison = Integer.compare(rootExitPathOrder(left), rootExitPathOrder(right));
                    if (pathComparison != 0) {
                        return pathComparison;
                    }
                    return Integer.compare(directionOrder(left.direction()), directionOrder(right.direction()));
                })
                .toList();
    }

    private int rootExitPathOrder(MKWorkspaceFamilyHorizontalExitDefinition exit) {
        return exit.pathKind() == MKWorkspaceHorizontalExitPathKind.MAIN_EXIT ? 0 : 1;
    }

    private int directionOrder(Direction direction) {
        return switch (direction) {
            case SOUTH -> 0;
            case EAST -> 1;
            case NORTH -> 2;
            case WEST -> 3;
            default -> 4;
        };
    }

    private void addBranchPath(ArrayList<LogicalSegment> segments, ArrayList<RejectedExit> rejected,
                               MKWorkspaceFloorTopologySettings settings, LogicalRect start, Direction direction,
                               int branchDepth, int leadIn, Random random, boolean explicitRootExit,
                               int parentSegmentIndex, int radiusLimit) {
        boolean hallwaysEnabled = settings.branchHallwaysEnabled();
        int hallwayLength = hallwayLength(settings, false, leadIn);
        LogicalRect cursor = start;
        int cursorParentSegmentIndex = parentSegmentIndex;
        int desiredRoomCount = sampledCount(0, settings.maxBranchPiecesBeforeCap(), settings.sprawl(), random);
        for (int placedRooms = 0; placedRooms <= desiredRoomCount; placedRooms++) {
            if (placedRooms >= desiredRoomCount) {
                tryAppendBranchCap(segments, rejected, settings, cursor, direction, hallwayLength, hallwaysEnabled,
                        placedRooms == 0, random, cursorParentSegmentIndex, radiusLimit);
                return;
            }

            RoomStep branchRoom = new RoomStep(weightedProfile(settings.branchRoomProfiles(), random),
                    "B" + (placedRooms + 1), SegmentKind.BRANCH_ROOM,
                    "branch room " + (placedRooms + 1) + " of sampled " + desiredRoomCount, true);
            StepCandidate candidate = stepCandidate(settings, cursor, direction, hallwayLength, false,
                    hallwaysEnabled, placedRooms == 0, branchRoom, segments.size(), cursorParentSegmentIndex);
            boolean canContinue = candidateFits(segments, candidate.segments(), radiusLimit) &&
                    branchCanTerminateLater(combined(segments, candidate.segments()), settings, candidate.cursor(),
                            direction, hallwayLength, hallwaysEnabled, placedRooms + 1, desiredRoomCount,
                            candidate.segments().getLast().segmentIndex(), radiusLimit);
            if (!canContinue) {
                tryAppendBranchCap(segments, rejected, settings, cursor, direction, hallwayLength, hallwaysEnabled,
                        placedRooms == 0, random, cursorParentSegmentIndex, radiusLimit);
                return;
            }

            segments.addAll(candidate.segments());
            cursor = candidate.cursor();
            int roomSegmentIndex = segments.size() - 1;
            cursorParentSegmentIndex = roomSegmentIndex;
            String acceptedMask = addBranchesFromRoom(segments, rejected, settings, cursor, direction,
                    branchRoom.profile(), branchDepth, leadIn, random, explicitRootExit, roomSegmentIndex,
                    radiusLimit);
            segments.set(roomSegmentIndex, segments.get(roomSegmentIndex)
                    .withAcceptedMask(maskWithDirection(acceptedMask, Direction.NORTH)));
        }
    }

    private boolean branchCanTerminateLater(List<LogicalSegment> existing, MKWorkspaceFloorTopologySettings settings,
                                            LogicalRect cursor, Direction direction, int hallwayLength,
                                            boolean hallwaysEnabled, int placedRooms, int desiredRoomCount,
                                            int parentSegmentIndex, int radiusLimit) {
        RoomStep nextStep = placedRooms < desiredRoomCount ?
                new RoomStep(settings.branchRoomProfiles().getFirst(), "B" + (placedRooms + 1),
                        SegmentKind.BRANCH_ROOM, "branch continuation fit probe", true) :
                new RoomStep(settings.branchCapProfiles().getFirst(), "Branch Cap",
                        SegmentKind.BRANCH_CAP, "terminal branch cap fit probe", false);
        StepCandidate nextCandidate = stepCandidate(settings, cursor, direction, hallwayLength, false,
                hallwaysEnabled, false, nextStep, existing.size(), parentSegmentIndex);
        return candidateFits(existing, nextCandidate.segments(), radiusLimit);
    }

    private void tryAppendBranchCap(ArrayList<LogicalSegment> segments, ArrayList<RejectedExit> rejected,
                                    MKWorkspaceFloorTopologySettings settings, LogicalRect cursor,
                                    Direction direction, int hallwayLength, boolean hallwaysEnabled,
                                    boolean leadIn, Random random, int parentSegmentIndex, int radiusLimit) {
        RoomStep cap = branchCapStep(settings, random);
        StepCandidate candidate = stepCandidate(settings, cursor, direction, hallwayLength, false, hallwaysEnabled,
                leadIn, cap, segments.size(), parentSegmentIndex);
        if (!candidateFits(segments, candidate.segments(), radiusLimit)) {
            rejected.add(new RejectedExit(direction, MKWorkspaceFloorRoomKind.BRANCH_CAP,
                    rejectionReason(segments, candidate.segments(), radiusLimit)));
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
                                        boolean hallwaysEnabled, boolean leadIn, RoomStep step,
                                        int nextSegmentIndex, int parentSegmentIndex) {
        ArrayList<LogicalSegment> candidate = new ArrayList<>();
        LogicalRect candidateCursor = cursor;
        int cursorParentSegmentIndex = parentSegmentIndex;
        int cursorSegmentIndex = nextSegmentIndex;
        if (hallwaysEnabled) {
            candidateCursor = appendHallway(candidate, settings, candidateCursor, direction, hallwayLength, main,
                    leadIn, cursorSegmentIndex, cursorParentSegmentIndex);
            cursorParentSegmentIndex = cursorSegmentIndex;
            cursorSegmentIndex++;
        }
        candidateCursor = appendRoom(candidate, candidateCursor, direction, step, cursorSegmentIndex,
                cursorParentSegmentIndex);
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

    private List<MainStepSpec> mainStepSpecs(MKWorkspaceFloorTopologySettings settings, Random random) {
        ArrayList<MainStepSpec> steps = new ArrayList<>();
        int roomCount = sampledCount(settings.minMainPathPieces(), settings.maxMainPathPieces(),
                settings.sprawl(), random);
        for (int i = 0; i < roomCount; i++) {
            steps.add(new MainStepSpec(orderedProfiles(settings.mainRoomProfiles(), random), "M" + (i + 1),
                    SegmentKind.MAIN_ROOM, "main room " + (i + 1) + " of sampled " + roomCount, true));
        }
        if (settings.mainCapApproachEnabled()) {
            steps.add(new MainStepSpec(orderedProfiles(settings.mainCapApproachProfiles(), random), "Approach",
                    SegmentKind.MAIN_CAP, "main cap approach", false));
        }
        steps.add(new MainStepSpec(orderedProfiles(settings.mainCapProfiles(), random), "Main Cap",
                SegmentKind.MAIN_CAP, "terminal main cap", false));
        return List.copyOf(steps);
    }

    private List<MKWorkspaceFloorRoomProfile> orderedProfiles(List<MKWorkspaceFloorRoomProfile> profiles,
                                                              Random random) {
        MKWorkspaceFloorRoomProfile selected = weightedProfile(profiles, random);
        ArrayList<MKWorkspaceFloorRoomProfile> ordered = new ArrayList<>();
        ordered.addAll(randomizedProfileVariants(selected, random));
        ArrayList<MKWorkspaceFloorRoomProfile> remaining = new ArrayList<>(profiles);
        remaining.remove(selected);
        Collections.shuffle(remaining, random);
        for (MKWorkspaceFloorRoomProfile profile : remaining) {
            ordered.addAll(randomizedProfileVariants(profile, random));
        }
        return List.copyOf(ordered);
    }

    private List<MKWorkspaceFloorRoomProfile> randomizedProfileVariants(MKWorkspaceFloorRoomProfile profile,
                                                                        Random random) {
        List<Direction> candidates = profile.randomizedMainExitCandidates();
        if (!profile.randomizeMainExit() || candidates.size() <= 1) {
            return List.of(profile);
        }
        ArrayList<Direction> ordered = new ArrayList<>(candidates);
        Collections.shuffle(ordered, random);
        return ordered.stream()
                .map(profile::withResolvedRandomMainExit)
                .toList();
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
                                      boolean leadIn, int segmentIndex, int parentSegmentIndex) {
        MKWorkspaceFloorRoomProfile reference = main ? settings.mainRoomProfiles().getFirst() :
                settings.branchRoomProfiles().getFirst();
        int hallwayMinor = hallwayWidth(settings, main, reference);
        LogicalRect rect = rectAfter(cursor, direction, hallwayLength, hallwayMinor);
        String label = main ? "Main Hall" : "Branch Hall";
        segments.add(new LogicalSegment(rect, main ? SegmentKind.MAIN_HALL : SegmentKind.BRANCH_HALL, direction,
                label, label + "\n" + direction.getSerializedName() +
                (leadIn ? "\nlead-in " + hallwayLength : "\nlink " + hallwayLength), false, "",
                segmentIndex, parentSegmentIndex, null));
        return rect;
    }

    private int hallwayLength(MKWorkspaceFloorTopologySettings settings, boolean main, int leadIn) {
        int explicit = main ? settings.mainHallwayLength() : settings.branchHallwayLength();
        return Math.max(1, explicit > 0 ? explicit : leadIn);
    }

    private int hallwayWidth(MKWorkspaceFloorTopologySettings settings, boolean main,
                             MKWorkspaceFloorRoomProfile reference) {
        int explicit = main ? settings.mainHallwayWidth() : settings.branchHallwayWidth();
        if (explicit > 0) {
            return explicit;
        }
        return main ? Math.max(3, Math.min(7, reference.width() / 2)) :
                Math.max(3, Math.min(5, reference.width() / 2));
    }

    private LogicalRect appendRoom(ArrayList<LogicalSegment> segments, LogicalRect cursor, Direction direction,
                                   RoomStep step, int segmentIndex, int parentSegmentIndex) {
        int major = step.profile().length();
        int minor = step.profile().width();
        LogicalRect rect = rectAfter(cursor, direction, major, minor);
        segments.add(new LogicalSegment(rect, step.kind(), direction, step.label(),
                step.profile().label() + "\n" + step.profile().width() + " x " + step.profile().length() +
                        "\nheight " + step.profile().height() + "\n" + step.tooltip(), true,
                "none", segmentIndex, parentSegmentIndex, step.profile()));
        return rect;
    }

    private List<AcceptedLink> addLinks(ArrayList<LogicalSegment> segments, MKWorkspaceFloorTopologySettings settings,
                                        Random random, int radiusLimit) {
        if (settings.maxLinksPerFloor() <= 0 || settings.maxLinksPerRoom() <= 0 ||
                settings.linkDensity() <= 0.0f || settings.maxLinkLength() <= 0) {
            return List.of();
        }
        List<LinkEndpoint> endpoints = linkEndpoints(segments);
        ArrayList<LinkCandidate> candidates = new ArrayList<>();
        for (int i = 0; i < endpoints.size(); i++) {
            for (int j = i + 1; j < endpoints.size(); j++) {
                routeBetween(endpoints.get(i), endpoints.get(j), settings.maxLinkLength())
                        .filter(route -> routeFits(segments, route, radiusLimit))
                        .ifPresent(candidates::add);
            }
        }
        candidates.sort((a, b) -> Integer.compare(a.length(), b.length()));
        ArrayList<AcceptedLink> accepted = new ArrayList<>();
        Map<Integer, Integer> linksByRoom = new HashMap<>();
        for (LinkCandidate candidate : candidates) {
            if (accepted.size() >= settings.maxLinksPerFloor()) {
                break;
            }
            if (linksByRoom.getOrDefault(candidate.a().segmentIndex(), 0) >= settings.maxLinksPerRoom() ||
                    linksByRoom.getOrDefault(candidate.b().segmentIndex(), 0) >= settings.maxLinksPerRoom()) {
                continue;
            }
            if (random.nextFloat() > settings.linkDensity()) {
                continue;
            }
            ArrayList<LogicalSegment> routeSegments = new ArrayList<>();
            for (LogicalRect rect : candidate.route()) {
                routeSegments.add(new LogicalSegment(rect, SegmentKind.LINK_HALL, null, "Loop Link",
                        "Loop Link\n" + candidate.a().label() + " -> " + candidate.b().label() +
                                "\nlength " + candidate.length(),
                        false, "", -1, -1, null));
            }
            if (!candidateFits(segments, routeSegments, radiusLimit)) {
                continue;
            }
            segments.addAll(routeSegments);
            linksByRoom.merge(candidate.a().segmentIndex(), 1, Integer::sum);
            linksByRoom.merge(candidate.b().segmentIndex(), 1, Integer::sum);
            accepted.add(new AcceptedLink(candidate.a(), candidate.b(), candidate.route(), candidate.length()));
        }
        return List.copyOf(accepted);
    }

    private List<LinkEndpoint> linkEndpoints(List<LogicalSegment> segments) {
        ArrayList<LinkEndpoint> endpoints = new ArrayList<>();
        for (int index = 0; index < segments.size(); index++) {
            LogicalSegment segment = segments.get(index);
            if (!segment.roomRegion() || segment.profile() == null || segment.direction() == null) {
                continue;
            }
            for (MKWorkspaceFamilyHorizontalExitDefinition exit : segment.profile().horizontalExits()) {
                if (exit.direction() == Direction.SOUTH || exit.direction().getAxis().isVertical()) {
                    continue;
                }
                boolean linkCandidate = exit.pathKind() == MKWorkspaceHorizontalExitPathKind.LINK_CANDIDATE;
                boolean closedBranch = exit.pathKind() == MKWorkspaceHorizontalExitPathKind.BRANCH &&
                        !maskContains(segment.acceptedMask(), exit.direction());
                if (!linkCandidate && !closedBranch) {
                    continue;
                }
                Direction worldDirection = rotateRoomExit(exit.direction(), segment.direction());
                endpoints.add(new LinkEndpoint(index, segment.rect(), segment.label(), segment.profile().kind(),
                        exit.direction(), worldDirection, linkCandidate ? LinkEndpointKind.LINK_ONLY :
                        LinkEndpointKind.CLOSED_BRANCH));
            }
        }
        return List.copyOf(endpoints);
    }

    private boolean maskContains(String mask, Direction direction) {
        if (mask == null || mask.isBlank() || "none".equals(mask)) {
            return false;
        }
        return mask.indexOf(direction.getSerializedName().charAt(0)) >= 0;
    }

    private Optional<LinkCandidate> routeBetween(LinkEndpoint a, LinkEndpoint b, int maxLength) {
        return straightRoute(a, b, maxLength)
                .or(() -> doglegRoute(a, b, true, maxLength))
                .or(() -> doglegRoute(a, b, false, maxLength));
    }

    private Optional<LinkCandidate> straightRoute(LinkEndpoint a, LinkEndpoint b, int maxLength) {
        if (a.worldDirection() != b.worldDirection().getOpposite()) {
            return Optional.empty();
        }
        EndpointPoint start = endpointPoint(a);
        EndpointPoint end = endpointPoint(b);
        if (a.worldDirection().getAxis() == Direction.Axis.X && start.z() != end.z()) {
            return Optional.empty();
        }
        if (a.worldDirection().getAxis() == Direction.Axis.Z && start.x() != end.x()) {
            return Optional.empty();
        }
        if (directionBetween(start, end).orElse(null) != a.worldDirection()) {
            return Optional.empty();
        }
        return routeCandidate(a, b, List.of(start, end), maxLength);
    }

    private Optional<LinkCandidate> doglegRoute(LinkEndpoint a, LinkEndpoint b, boolean xThenZ, int maxLength) {
        if (a.worldDirection().getAxis() == b.worldDirection().getAxis()) {
            return Optional.empty();
        }
        EndpointPoint start = endpointPoint(a);
        EndpointPoint end = endpointPoint(b);
        EndpointPoint corner = xThenZ ? new EndpointPoint(end.x(), start.z()) :
                new EndpointPoint(start.x(), end.z());
        if (directionBetween(start, corner).orElse(null) != a.worldDirection()) {
            return Optional.empty();
        }
        if (directionBetween(corner, end).orElse(null) != b.worldDirection().getOpposite()) {
            return Optional.empty();
        }
        return routeCandidate(a, b, List.of(start, corner, end), maxLength);
    }

    private Optional<LinkCandidate> routeCandidate(LinkEndpoint a, LinkEndpoint b, List<EndpointPoint> points,
                                                   int maxLength) {
        ArrayList<LogicalRect> route = new ArrayList<>();
        int length = 0;
        for (int i = 0; i < points.size() - 1; i++) {
            EndpointPoint start = points.get(i);
            EndpointPoint end = points.get(i + 1);
            int segmentLength = Math.abs(end.x() - start.x()) + Math.abs(end.z() - start.z());
            if (segmentLength <= 0) {
                continue;
            }
            length += segmentLength;
            route.add(routeRect(start, end));
        }
        if (route.isEmpty() || length > maxLength) {
            return Optional.empty();
        }
        return Optional.of(new LinkCandidate(a, b, List.copyOf(route), length));
    }

    private boolean routeFits(List<LogicalSegment> segments, LinkCandidate candidate, int radiusLimit) {
        for (LogicalRect rect : candidate.route()) {
            for (int index = 0; index < segments.size(); index++) {
                if (index == candidate.a().segmentIndex() || index == candidate.b().segmentIndex()) {
                    continue;
                }
                if (segments.get(index).rect().intersects(rect)) {
                    return false;
                }
            }
        }
        return fitsHardLimit(combined(segments, candidate.route().stream()
                .map(rect -> new LogicalSegment(rect, SegmentKind.LINK_HALL, null, "", "", false, "", -1, -1, null))
                .toList()), radiusLimit);
    }

    private EndpointPoint endpointPoint(LinkEndpoint endpoint) {
        LogicalRect rect = endpoint.room();
        return switch (endpoint.worldDirection()) {
            case NORTH -> new EndpointPoint(Math.round(rect.centerX()), Math.round(rect.top() - 1));
            case SOUTH -> new EndpointPoint(Math.round(rect.centerX()), Math.round(rect.bottom() + 1));
            case EAST -> new EndpointPoint(Math.round(rect.right() + 1), Math.round(rect.centerY()));
            case WEST -> new EndpointPoint(Math.round(rect.left() - 1), Math.round(rect.centerY()));
            default -> new EndpointPoint(Math.round(rect.centerX()), Math.round(rect.centerY()));
        };
    }

    private Optional<Direction> directionBetween(EndpointPoint start, EndpointPoint end) {
        if (start.x() == end.x()) {
            if (end.z() > start.z()) {
                return Optional.of(Direction.SOUTH);
            }
            if (end.z() < start.z()) {
                return Optional.of(Direction.NORTH);
            }
        }
        if (start.z() == end.z()) {
            if (end.x() > start.x()) {
                return Optional.of(Direction.EAST);
            }
            if (end.x() < start.x()) {
                return Optional.of(Direction.WEST);
            }
        }
        return Optional.empty();
    }

    private LogicalRect routeRect(EndpointPoint start, EndpointPoint end) {
        int minX = Math.min(start.x(), end.x());
        int maxX = Math.max(start.x(), end.x());
        int minZ = Math.min(start.z(), end.z());
        int maxZ = Math.max(start.z(), end.z());
        if (start.z() == end.z()) {
            return new LogicalRect((minX + maxX) / 2.0f, start.z(), Math.max(1, maxX - minX + 1), 3);
        }
        return new LogicalRect(start.x(), (minZ + maxZ) / 2.0f, 3, Math.max(1, maxZ - minZ + 1));
    }

    private String addBranchesFromRoom(ArrayList<LogicalSegment> segments, ArrayList<RejectedExit> rejected,
                                       MKWorkspaceFloorTopologySettings settings, LogicalRect room,
                                       Direction pathDirection, MKWorkspaceFloorRoomProfile profile,
                                       int branchDepth, int leadIn, Random random, boolean explicitRootExit,
                                       int parentSegmentIndex, int radiusLimit) {
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
                    false, parentSegmentIndex, radiusLimit);
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
        for (Direction direction : List.of(Direction.EAST, Direction.NORTH, Direction.WEST)) {
            if (directions.contains(direction)) {
                mask.append(direction.getSerializedName().charAt(0));
            }
        }
        return mask.isEmpty() ? "none" : mask.toString();
    }

    private String maskWithDirection(String mask, Direction direction) {
        ArrayList<Direction> directions = new ArrayList<>();
        if (mask != null && !mask.isBlank() && !"none".equals(mask)) {
            for (Direction candidate : List.of(Direction.EAST, Direction.NORTH, Direction.WEST)) {
                if (mask.indexOf(candidate.getSerializedName().charAt(0)) >= 0) {
                    directions.add(candidate);
                }
            }
        }
        if (!directions.contains(direction)) {
            directions.add(direction);
        }
        return maskName(directions);
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

    private boolean candidateFits(List<LogicalSegment> existing, List<LogicalSegment> candidate, int radiusLimit) {
        return !collides(existing, candidate) && fitsHardLimit(combined(existing, candidate), radiusLimit);
    }

    private String rejectionReason(List<LogicalSegment> existing, List<LogicalSegment> candidate, int radiusLimit) {
        if (collides(existing, candidate)) {
            return "footprint collision";
        }
        if (!fitsHardLimit(combined(existing, candidate), radiusLimit)) {
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

    private boolean fitsHardLimit(List<LogicalSegment> segments, int radiusLimit) {
        LayoutExtents extents = extents(segments);
        return Math.max(Math.abs(extents.minX()), Math.abs(extents.maxX())) <= radiusLimit &&
                Math.max(Math.abs(extents.minY()), Math.abs(extents.maxY())) <= radiusLimit;
    }

    private record RoomStep(MKWorkspaceFloorRoomProfile profile, String label, SegmentKind kind, String tooltip,
                            boolean allowBranches) {
    }

    private record StepCandidate(List<LogicalSegment> segments, LogicalRect cursor) {
    }

    private record MainStepSpec(List<MKWorkspaceFloorRoomProfile> profiles, String label, SegmentKind kind,
                                String tooltip, boolean allowBranches) {
    }

    private record MainFailure(Direction direction, MKWorkspaceFloorRoomKind kind, String reason) {
    }

    private record PendingBranchSource(int segmentIndex, LogicalRect room, Direction pathDirection,
                                       MKWorkspaceFloorRoomProfile profile, int branchDepth,
                                       boolean explicitRootExit) {
    }

    public record FloorLayoutResult(List<LogicalSegment> segments, List<RejectedExit> rejectedExits,
                                    List<AcceptedLink> acceptedLinks, LayoutExtents extents,
                                    boolean fitsHardLimit) {
        public boolean hasRequiredRejections() {
            return rejectedExits.stream().anyMatch(RejectedExit::required);
        }
    }

    public record LogicalSegment(LogicalRect rect, SegmentKind kind, Direction direction, String label,
                                 String tooltip, boolean roomRegion, String acceptedMask,
                                 int segmentIndex, int parentSegmentIndex, MKWorkspaceFloorRoomProfile profile) {
        public LogicalSegment withAcceptedMask(String mask) {
            return new LogicalSegment(rect, kind, direction, label, tooltip, roomRegion, mask,
                    segmentIndex, parentSegmentIndex, profile);
        }
    }

    public record AcceptedLink(LinkEndpoint a, LinkEndpoint b, List<LogicalRect> route, int length) {
    }

    public record LinkEndpoint(int segmentIndex, LogicalRect room, String label, MKWorkspaceFloorRoomKind roomKind,
                               Direction localDirection, Direction worldDirection, LinkEndpointKind endpointKind) {
    }

    private record LinkCandidate(LinkEndpoint a, LinkEndpoint b, List<LogicalRect> route, int length) {
    }

    private record EndpointPoint(int x, int z) {
    }

    public enum LinkEndpointKind {
        LINK_ONLY,
        CLOSED_BRANCH
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
        BRANCH_CAP,
        LINK_HALL
    }
}
