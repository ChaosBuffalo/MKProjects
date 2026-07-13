package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record MKVerticalAccessProfile(MKWorkspaceStairMode mode, MKWorkspaceStairRiseType riseType, int cycleLength,
                                  int riseHalfBlocksPerRiseStep, int flatRunLength, int stairWidth) {
    public static final int ROOM_VERTICAL_SHELL_LAYERS = 2;

    public enum BoundaryStatus {
        EXACT,
        BRIDGEABLE,
        INVALID_OVERLAP,
        INVALID_GAP
    }

    public record BoundaryCompatibility(BoundaryStatus status, int bridgeSteps) {
        public boolean isUsable() {
            return status == BoundaryStatus.EXACT || status == BoundaryStatus.BRIDGEABLE;
        }
    }

    public static MKVerticalAccessProfile forTemplateReuse(MKWorkspaceStairAuthoringConfig config, int shaftWidth,
                                                           int shaftLength, int interiorHeight) {
        return forTemplateReuse(config, shaftWidth, shaftLength, interiorHeight, 1);
    }

    public static MKVerticalAccessProfile forTemplateReuse(MKWorkspaceStairAuthoringConfig config, int shaftWidth,
                                                           int shaftLength, int interiorHeight,
                                                           int verticalShellMargin) {
        return MKResolvedVerticalAccessProfile.resolve(config, shaftWidth, shaftLength, interiorHeight,
                        verticalShellMargin)
                .map(MKResolvedVerticalAccessProfile::asUniformProfile)
                .orElseGet(() -> new MKVerticalAccessProfile(normalizeMode(config.mode()), config.riseType(), 1, 2,
                        0, Math.max(1, config.stairWidth())));
    }

    public static MKWorkspaceStairMode normalizeMode(MKWorkspaceStairMode requestedMode) {
        return switch (requestedMode) {
            case AUTO -> MKWorkspaceStairMode.RUN_PROFILE;
            case STAIR_STAIRS, SLAB_STAIRS -> MKWorkspaceStairMode.RUN_PROFILE;
            default -> requestedMode;
        };
    }

    public static int getPerimeterStepCount(int shaftWidth, int shaftLength) {
        if (shaftWidth <= 1 && shaftLength <= 1) {
            return 1;
        }
        return Math.max(1, (2 * shaftWidth) + (2 * shaftLength) - 4);
    }

    public static List<Integer> getAllowedHeights(MKWorkspaceStairMode requestedMode, int shaftWidth, int minimumHeight,
                                                  int count) {
        MKVerticalAccessProfile profile = forTemplateReuse(
                new MKWorkspaceStairAuthoringConfig(requestedMode,
                        requestedMode == MKWorkspaceStairMode.SLAB_STAIRS ? MKWorkspaceStairRiseType.SLAB :
                                requestedMode == MKWorkspaceStairMode.STAIR_STAIRS ? MKWorkspaceStairRiseType.STAIR :
                                        MKWorkspaceStairRiseType.MIXED,
                        1),
                shaftWidth, shaftWidth, minimumHeight);
        if (profile.mode() == MKWorkspaceStairMode.LADDER || profile.mode() == MKWorkspaceStairMode.NONE) {
            List<Integer> values = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                values.add(minimumHeight + i);
            }
            return values;
        }

        List<Integer> values = new ArrayList<>();
        int candidate = Math.max(1, minimumHeight);
        int maxCandidate = Math.max(candidate + 255, candidate + (count * 64));
        while (values.size() < count && candidate <= maxCandidate) {
            if (MKResolvedVerticalAccessProfile.resolve(new MKWorkspaceStairAuthoringConfig(requestedMode,
                            requestedMode == MKWorkspaceStairMode.SLAB_STAIRS ? MKWorkspaceStairRiseType.SLAB :
                                    requestedMode == MKWorkspaceStairMode.STAIR_STAIRS ? MKWorkspaceStairRiseType.STAIR :
                                            MKWorkspaceStairRiseType.MIXED,
                            1),
                    shaftWidth, shaftWidth, candidate).isPresent()) {
                values.add(candidate);
            }
            candidate++;
        }
        if (values.isEmpty()) {
            values.add(Math.max(3, minimumHeight));
        }
        while (values.size() < count) {
            values.add(values.getLast());
        }
        return values;
    }

    public static int snapToNearestAllowedHeight(MKWorkspaceStairMode requestedMode, int shaftWidth, int requestedHeight,
                                                 int minimumHeight, int count) {
        List<Integer> allowed = getAllowedHeights(requestedMode, shaftWidth, minimumHeight, count);
        return allowed.stream()
                .min(java.util.Comparator.comparingInt(value -> Math.abs(value - requestedHeight)))
                .orElseGet(() -> allowed.getFirst());
    }

    public boolean isReusableHeight(int fullBlockHeight) {
        return getBoundaryCompatibilityForHeight(fullBlockHeight).isUsable();
    }

    public BoundaryCompatibility getBoundaryCompatibilityForHeight(int fullBlockHeight) {
        int totalHalfBlocks = fullBlockHeight * 2;
        if (totalHalfBlocks % riseHalfBlocksPerRiseStep != 0) {
            return new BoundaryCompatibility(BoundaryStatus.INVALID_GAP, 0);
        }
        int pathSteps = getPathStepsForHeight(fullBlockHeight);
        int segmentLength = flatRunLength + 1;
        return getBoundaryCompatibility(cycleLength, pathSteps, segmentLength);
    }

    public static BoundaryCompatibility getBoundaryCompatibility(int cycleLength, int pathSteps, int segmentLength) {
        Set<Integer> bottomIndices = new HashSet<>();
        bottomIndices.add(Math.floorMod(-1, cycleLength));
        for (int step = 0; step < segmentLength; step++) {
            bottomIndices.add(Math.floorMod(step, cycleLength));
        }

        Set<Integer> topIndices = new HashSet<>();
        for (int step = pathSteps - segmentLength; step < pathSteps; step++) {
            topIndices.add(Math.floorMod(step, cycleLength));
        }
        for (int topIndex : topIndices) {
            if (bottomIndices.contains(topIndex)) {
                return new BoundaryCompatibility(BoundaryStatus.INVALID_OVERLAP, 0);
            }
        }

        int lastTopIndex = Math.floorMod(pathSteps - 1, cycleLength);
        int landingIndex = Math.floorMod(-1, cycleLength);
        int clockwiseDistance = Math.floorMod(landingIndex - lastTopIndex, cycleLength);
        if (clockwiseDistance == 1) {
            return new BoundaryCompatibility(BoundaryStatus.EXACT, 0);
        }
        if (clockwiseDistance == 2) {
            return new BoundaryCompatibility(BoundaryStatus.BRIDGEABLE, 1);
        }
        return new BoundaryCompatibility(BoundaryStatus.INVALID_GAP, Math.max(0, clockwiseDistance - 1));
    }

    public boolean isReusableInteriorHeight(int interiorHeight) {
        return getBoundaryCompatibilityForInteriorHeight(interiorHeight).isUsable();
    }

    public boolean isReusableInteriorHeight(int interiorHeight, int verticalShellMargin) {
        return getBoundaryCompatibilityForInteriorHeight(interiorHeight, verticalShellMargin).isUsable();
    }

    public BoundaryCompatibility getBoundaryCompatibilityForInteriorHeight(int interiorHeight) {
        return getBoundaryCompatibilityForHeight(getTraversalHeightForInterior(interiorHeight));
    }

    public BoundaryCompatibility getBoundaryCompatibilityForInteriorHeight(int interiorHeight,
                                                                           int verticalShellMargin) {
        return getBoundaryCompatibilityForHeight(getTraversalHeightForInterior(interiorHeight, verticalShellMargin));
    }

    public int getPathStepsForHeight(int fullBlockHeight) {
        int totalHalfBlocks = fullBlockHeight * 2;
        int riseEvents = totalHalfBlocks / riseHalfBlocksPerRiseStep;
        return riseEvents * (flatRunLength + 1);
    }

    public int getPathStepsForInteriorHeight(int interiorHeight) {
        return getPathStepsForHeight(getTraversalHeightForInterior(interiorHeight));
    }

    public int getPathStepsForInteriorHeight(int interiorHeight, int verticalShellMargin) {
        return getPathStepsForHeight(getTraversalHeightForInterior(interiorHeight, verticalShellMargin));
    }

    public int getPhaseForHeight(int fullBlockHeight) {
        return Math.floorMod(getPathStepsForHeight(fullBlockHeight), cycleLength);
    }

    public int getPhaseForInteriorHeight(int interiorHeight) {
        return getPhaseForHeight(getTraversalHeightForInterior(interiorHeight));
    }

    public int getPhaseForInteriorHeight(int interiorHeight, int verticalShellMargin) {
        return getPhaseForHeight(getTraversalHeightForInterior(interiorHeight, verticalShellMargin));
    }

    public boolean isPhaseAligned(int referenceHeight, int candidateHeight) {
        return getPhaseForHeight(referenceHeight) == getPhaseForHeight(candidateHeight);
    }

    public boolean isInteriorPhaseAligned(int referenceInteriorHeight, int candidateInteriorHeight) {
        return getPhaseForInteriorHeight(referenceInteriorHeight) == getPhaseForInteriorHeight(candidateInteriorHeight);
    }

    public boolean isInteriorPhaseAligned(int referenceInteriorHeight, int candidateInteriorHeight,
                                          int verticalShellMargin) {
        return getPhaseForInteriorHeight(referenceInteriorHeight, verticalShellMargin) ==
                getPhaseForInteriorHeight(candidateInteriorHeight, verticalShellMargin);
    }

    public int getTraversalHeightForInterior(int interiorHeight) {
        return interiorHeight + ROOM_VERTICAL_SHELL_LAYERS;
    }

    public int getTraversalHeightForInterior(int interiorHeight, int verticalShellMargin) {
        return interiorHeight + (2 * Math.max(0, verticalShellMargin));
    }
}

