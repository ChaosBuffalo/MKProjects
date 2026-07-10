package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record MKResolvedVerticalAccessProfile(
        MKWorkspaceStairMode mode,
        MKWorkspaceStairRiseType riseStrategy,
        int stairWidth,
        int flatRunLength,
        List<RiseStepKind> risePattern,
        MKVerticalAccessProfile.BoundaryCompatibility boundaryCompatibility,
        int topPhase,
        int cycleLength,
        int pathSteps,
        int shaftWidth,
        int shaftLength,
        int interiorHeight
) {
    private static final int MAX_FLAT_RUN_LENGTH = 16;
    private static final int MIN_PASS_CLEARANCE_HALF_BLOCKS = 4;

    public enum RiseStepKind {
        STAIR,
        SLAB_BOTTOM,
        SLAB_TOP
    }

    public MKResolvedVerticalAccessProfile {
        risePattern = List.copyOf(risePattern);
    }

    public static Optional<MKResolvedVerticalAccessProfile> resolve(MKWorkspaceStairAuthoringConfig config,
                                                                    int shaftWidth, int shaftLength,
                                                                    int interiorHeight) {
        MKWorkspaceStairMode mode = MKVerticalAccessProfile.normalizeMode(config.mode());
        if (mode == MKWorkspaceStairMode.LADDER || mode == MKWorkspaceStairMode.NONE) {
            return Optional.of(new MKResolvedVerticalAccessProfile(
                    mode,
                    config.riseType(),
                    1,
                    0,
                    List.of(),
                    new MKVerticalAccessProfile.BoundaryCompatibility(MKVerticalAccessProfile.BoundaryStatus.EXACT, 0),
                    0,
                    1,
                    0,
                    shaftWidth,
                    shaftLength,
                    interiorHeight
            ));
        }

        int stairWidth = Math.max(1, config.stairWidth());
        int pathWidth = Math.max(1, shaftWidth - (2 * (stairWidth - 1)));
        int pathLength = Math.max(1, shaftLength - (2 * (stairWidth - 1)));
        int cycleLength = MKVerticalAccessProfile.getPerimeterStepCount(pathWidth, pathLength);
        int traversalHeight = interiorHeight + MKVerticalAccessProfile.ROOM_VERTICAL_SHELL_LAYERS;
        MKWorkspaceStairRiseType strategy = config.riseType();

        List<MKResolvedVerticalAccessProfile> candidates = new ArrayList<>();
        for (int flatRunLength = 0; flatRunLength <= MAX_FLAT_RUN_LENGTH; flatRunLength++) {
            if (strategy == MKWorkspaceStairRiseType.STAIR || strategy == MKWorkspaceStairRiseType.MIXED) {
                addCandidate(candidates, mode, strategy, stairWidth, flatRunLength, 0, traversalHeight,
                        cycleLength, shaftWidth, shaftLength, interiorHeight);
            }
            if (strategy == MKWorkspaceStairRiseType.SLAB || strategy == MKWorkspaceStairRiseType.MIXED) {
                addCandidate(candidates, mode, strategy, stairWidth, flatRunLength, traversalHeight, traversalHeight,
                        cycleLength, shaftWidth, shaftLength, interiorHeight);
            }
            if (strategy == MKWorkspaceStairRiseType.MIXED) {
                for (int splitCount = 1; splitCount < traversalHeight; splitCount++) {
                    addCandidate(candidates, mode, strategy, stairWidth, flatRunLength, splitCount, traversalHeight,
                            cycleLength, shaftWidth, shaftLength, interiorHeight);
                }
            }
        }
        return candidates.stream().min(MKResolvedVerticalAccessProfile::compareCandidates);
    }

    private static void addCandidate(List<MKResolvedVerticalAccessProfile> candidates, MKWorkspaceStairMode mode,
                                     MKWorkspaceStairRiseType strategy, int stairWidth, int flatRunLength,
                                     int splitCount, int traversalHeight, int cycleLength, int shaftWidth,
                                     int shaftLength, int interiorHeight) {
        List<RiseStepKind> pattern = buildPattern(traversalHeight, splitCount);
        int pathSteps = pattern.size() * (flatRunLength + 1);
        MKVerticalAccessProfile.BoundaryCompatibility compatibility =
                MKVerticalAccessProfile.getBoundaryCompatibility(cycleLength, pathSteps, flatRunLength + 1);
        if (!compatibility.isUsable()) {
            return;
        }
        if (!hasRequiredPassClearance(pattern, flatRunLength, shaftWidth, shaftLength, stairWidth)) {
            return;
        }
        candidates.add(new MKResolvedVerticalAccessProfile(
                mode,
                strategy,
                stairWidth,
                flatRunLength,
                pattern,
                compatibility,
                Math.floorMod(pathSteps, cycleLength),
                cycleLength,
                pathSteps,
                shaftWidth,
                shaftLength,
                interiorHeight
        ));
    }

    private static List<RiseStepKind> buildPattern(int traversalHeight, int splitCount) {
        List<RiseStepKind> pattern = new ArrayList<>();
        for (int block = 0; block < traversalHeight; block++) {
            boolean split = ((block + 1) * splitCount / traversalHeight) > (block * splitCount / traversalHeight);
            if (split) {
                pattern.add(RiseStepKind.SLAB_BOTTOM);
                pattern.add(RiseStepKind.SLAB_TOP);
            } else {
                pattern.add(RiseStepKind.STAIR);
            }
        }
        return pattern;
    }

    private static int compareCandidates(MKResolvedVerticalAccessProfile left, MKResolvedVerticalAccessProfile right) {
        int boundary = Integer.compare(boundaryRank(left), boundaryRank(right));
        if (boundary != 0) {
            return boundary;
        }
        int regularity = Integer.compare(irregularityScore(left), irregularityScore(right));
        if (regularity != 0) {
            return regularity;
        }
        int flat = Integer.compare(left.flatRunLength(), right.flatRunLength());
        if (flat != 0) {
            return flat;
        }
        return Integer.compare(left.mixedCorrectionSteps(), right.mixedCorrectionSteps());
    }

    private static int boundaryRank(MKResolvedVerticalAccessProfile profile) {
        return profile.boundaryCompatibility().status() == MKVerticalAccessProfile.BoundaryStatus.EXACT ? 0 : 1;
    }

    private static int irregularityScore(MKResolvedVerticalAccessProfile profile) {
        int changes = 0;
        RiseStepKind previous = null;
        for (RiseStepKind kind : profile.risePattern()) {
            if (previous != null && previous != kind) {
                changes++;
            }
            previous = kind;
        }
        return changes;
    }

    public int mixedCorrectionSteps() {
        int corrections = 0;
        for (RiseStepKind kind : risePattern) {
            if (kind != RiseStepKind.STAIR) {
                corrections++;
            }
        }
        return corrections;
    }

    public boolean hasRequiredPassClearance() {
        return hasRequiredPassClearance(risePattern, flatRunLength, shaftWidth, shaftLength, stairWidth);
    }

    public MKVerticalAccessProfile asUniformProfile() {
        int riseHalfBlocksPerStep = riseStrategy == MKWorkspaceStairRiseType.SLAB ? 1 : 2;
        return new MKVerticalAccessProfile(mode, riseStrategy, cycleLength, riseHalfBlocksPerStep,
                flatRunLength, stairWidth);
    }

    private static boolean hasRequiredPassClearance(List<RiseStepKind> pattern, int flatRunLength,
                                                    int shaftWidth, int shaftLength, int stairWidth) {
        if (pattern.isEmpty()) {
            return true;
        }
        Bounds outerBounds = new Bounds(0, Math.max(0, shaftWidth - 1), 0, Math.max(0, shaftLength - 1));
        Bounds centerlineBounds = centerlineBounds(outerBounds, stairWidth);
        List<GridPoint> perimeter = perimeterClockwise(centerlineBounds);
        if (perimeter.isEmpty()) {
            return false;
        }

        Map<GridPoint, List<VerticalSpan>> spansByColumn = new HashMap<>();
        GridDirection previousMovement = null;
        int halfHeight = 0;
        int step = 0;
        for (RiseStepKind kind : pattern) {
            for (int segmentStep = 0; segmentStep <= flatRunLength; segmentStep++) {
                GridPoint base = perimeter.get(step % perimeter.size());
                GridPoint next = perimeter.get((step + 1) % perimeter.size());
                GridDirection movement = horizontalDirection(base, next);
                VerticalSpan span = spanForStep(kind, segmentStep == 0, halfHeight);
                if (!addBandSpans(spansByColumn, base, span, centerlineBounds, outerBounds, stairWidth)) {
                    return false;
                }
                if (previousMovement != null && movement != null && previousMovement != movement) {
                    if (!addCornerSpans(spansByColumn, base, previousMovement, movement, span, outerBounds, stairWidth)) {
                        return false;
                    }
                }
                previousMovement = movement;
                step++;
            }
            halfHeight += kind == RiseStepKind.STAIR ? 2 : 1;
        }
        return true;
    }

    private static VerticalSpan spanForStep(RiseStepKind kind, boolean riseStep, int halfHeight) {
        if (!riseStep || kind == RiseStepKind.STAIR) {
            int bottom = (halfHeight / 2) * 2;
            return new VerticalSpan(bottom, bottom + 2);
        }
        if (kind == RiseStepKind.SLAB_TOP) {
            int bottom = (halfHeight / 2) * 2 + 1;
            return new VerticalSpan(bottom, bottom + 1);
        }
        int bottom = (halfHeight / 2) * 2;
        return new VerticalSpan(bottom, bottom + 1);
    }

    private static boolean addBandSpans(Map<GridPoint, List<VerticalSpan>> spansByColumn, GridPoint point,
                                        VerticalSpan span, Bounds centerlineBounds, Bounds outerBounds, int width) {
        GridDirection outward = outwardDirection(point, centerlineBounds);
        for (int offset = 0; offset < Math.max(1, width); offset++) {
            GridPoint target = outward == null ? point : point.relative(outward, offset);
            if (outerBounds.contains(target) && !addSpan(spansByColumn, target, span)) {
                return false;
            }
        }
        return true;
    }

    private static boolean addCornerSpans(Map<GridPoint, List<VerticalSpan>> spansByColumn, GridPoint corner,
                                          GridDirection previousMovement, GridDirection currentMovement,
                                          VerticalSpan span, Bounds outerBounds, int width) {
        GridDirection outwardPrevious = previousMovement.counterClockWise();
        GridDirection outwardCurrent = currentMovement.counterClockWise();
        for (int previousOffset = 0; previousOffset < Math.max(1, width); previousOffset++) {
            for (int currentOffset = 0; currentOffset < Math.max(1, width); currentOffset++) {
                GridPoint target = corner.relative(outwardPrevious, previousOffset)
                        .relative(outwardCurrent, currentOffset);
                if (outerBounds.contains(target) && !addSpan(spansByColumn, target, span)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean addSpan(Map<GridPoint, List<VerticalSpan>> spansByColumn, GridPoint point,
                                   VerticalSpan span) {
        List<VerticalSpan> spans = spansByColumn.computeIfAbsent(point, ignored -> new ArrayList<>());
        for (VerticalSpan existing : spans) {
            if (existing.equals(span)) {
                return true;
            }
            if (span.bottomHalf() >= existing.topHalf() &&
                    span.bottomHalf() - existing.topHalf() < MIN_PASS_CLEARANCE_HALF_BLOCKS) {
                return false;
            }
            if (existing.bottomHalf() >= span.topHalf() &&
                    existing.bottomHalf() - span.topHalf() < MIN_PASS_CLEARANCE_HALF_BLOCKS) {
                return false;
            }
            if (span.bottomHalf() < existing.topHalf() && existing.bottomHalf() < span.topHalf()) {
                return false;
            }
        }
        spans.add(span);
        return true;
    }

    private static Bounds centerlineBounds(Bounds outerBounds, int width) {
        int inset = Math.max(0, width - 1);
        int minX = Math.min(outerBounds.maxX(), outerBounds.minX() + inset);
        int maxX = Math.max(minX, outerBounds.maxX() - inset);
        int minZ = Math.min(outerBounds.maxZ(), outerBounds.minZ() + inset);
        int maxZ = Math.max(minZ, outerBounds.maxZ() - inset);
        return new Bounds(minX, maxX, minZ, maxZ);
    }

    private static List<GridPoint> perimeterClockwise(Bounds bounds) {
        List<GridPoint> perimeter = new ArrayList<>();
        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
            perimeter.add(new GridPoint(x, bounds.minZ()));
        }
        for (int z = bounds.minZ() + 1; z <= bounds.maxZ(); z++) {
            perimeter.add(new GridPoint(bounds.maxX(), z));
        }
        if (bounds.maxZ() > bounds.minZ()) {
            for (int x = bounds.maxX() - 1; x >= bounds.minX(); x--) {
                perimeter.add(new GridPoint(x, bounds.maxZ()));
            }
        }
        if (bounds.maxX() > bounds.minX()) {
            for (int z = bounds.maxZ() - 1; z > bounds.minZ(); z--) {
                perimeter.add(new GridPoint(bounds.minX(), z));
            }
        }
        return perimeter;
    }

    private static GridDirection horizontalDirection(GridPoint from, GridPoint to) {
        int deltaX = Integer.compare(to.x(), from.x());
        int deltaZ = Integer.compare(to.z(), from.z());
        if (deltaX > 0) {
            return GridDirection.EAST;
        }
        if (deltaX < 0) {
            return GridDirection.WEST;
        }
        if (deltaZ > 0) {
            return GridDirection.SOUTH;
        }
        if (deltaZ < 0) {
            return GridDirection.NORTH;
        }
        return null;
    }

    private static GridDirection outwardDirection(GridPoint point, Bounds bounds) {
        if (point.z() == bounds.minZ()) {
            return GridDirection.NORTH;
        }
        if (point.z() == bounds.maxZ()) {
            return GridDirection.SOUTH;
        }
        if (point.x() == bounds.minX()) {
            return GridDirection.WEST;
        }
        if (point.x() == bounds.maxX()) {
            return GridDirection.EAST;
        }
        return null;
    }

    private record Bounds(int minX, int maxX, int minZ, int maxZ) {
        boolean contains(GridPoint point) {
            return point.x() >= minX && point.x() <= maxX && point.z() >= minZ && point.z() <= maxZ;
        }
    }

    private record GridPoint(int x, int z) {
        GridPoint relative(GridDirection direction, int distance) {
            return switch (direction) {
                case NORTH -> new GridPoint(x, z - distance);
                case SOUTH -> new GridPoint(x, z + distance);
                case EAST -> new GridPoint(x + distance, z);
                case WEST -> new GridPoint(x - distance, z);
            };
        }
    }

    private record VerticalSpan(int bottomHalf, int topHalf) {
    }

    private enum GridDirection {
        NORTH,
        SOUTH,
        EAST,
        WEST;

        GridDirection counterClockWise() {
            return switch (this) {
                case NORTH -> WEST;
                case SOUTH -> EAST;
                case EAST -> NORTH;
                case WEST -> SOUTH;
            };
        }
    }
}
