package com.chaosbuffalo.mkworkspace.world.gen.workspace.planner;

import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;

import java.util.ArrayList;
import java.util.List;

public record MKWalledKeepSizingReport(
        int northDistance,
        int southDistance,
        int westDistance,
        int eastDistance,
        int footprintWidth,
        int footprintLength,
        int requiredJigsawRadius,
        int maxDistanceFromCenter,
        int headroom,
        TerrainAdjustment terrainAdjustment,
        int courtyardSocketMaxSize,
        int courtyardRequestedSocketSize,
        int courtyardFreeHorizontalSpan,
        int courtyardFreeVerticalSpan,
        int courtyardRequiredHorizontalSpan,
        int courtyardRequiredVerticalSpan,
        int courtyardRealizedHorizontalSpan,
        int courtyardRealizedVerticalSpan,
        int horizontalWallSegments,
        int verticalWallSegments,
        int frontBranchSegments,
        int backWallSegments,
        int courtyardPathSize,
        int entryApproachLength,
        int recommendedWallUnitSpan,
        int recommendedWallExcessHorizontal,
        int recommendedWallExcessVertical,
        int recommendedWallPieceCount
) {
    public boolean fitsJigsawCap() {
        return requiredJigsawRadius <= maxDistanceFromCenter;
    }

    public boolean courtyardSocketFits() {
        return courtyardRequestedSocketSize <= courtyardSocketMaxSize;
    }

    public List<Integer> allowedCourtyardContentSizes() {
        if (courtyardSocketMaxSize < 3) {
            return List.of();
        }
        ArrayList<Integer> sizes = new ArrayList<>();
        for (int size = 3; size <= courtyardSocketMaxSize; size += 2) {
            sizes.add(size);
        }
        return List.copyOf(sizes);
    }

    public int snappedCourtyardContentSize(int requestedSize) {
        List<Integer> sizes = allowedCourtyardContentSizes();
        if (sizes.isEmpty()) {
            return Math.max(3, requestedSize);
        }
        int snapped = requestedSize % 2 == 0 ? requestedSize + 1 : requestedSize;
        if (snapped < sizes.getFirst()) {
            return sizes.getFirst();
        }
        if (snapped > sizes.getLast()) {
            return sizes.getLast();
        }
        return snapped;
    }
}
