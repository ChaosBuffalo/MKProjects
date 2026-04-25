package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;
public class MKWorkspaceDimensions {
    private final int roomWidth;
    private final int roomLength;
    private final int entranceHeight;
    private final int roomHeight;
    private final int basementHeight;
    private final int hallwayWidth;
    private final int doorwayWidth;
    private final int doorwayHeight;

    public MKWorkspaceDimensions(int roomWidth, int roomLength, int entranceHeight, int roomHeight, int basementHeight,
                                 int hallwayWidth, int doorwayWidth, int doorwayHeight) {
        this.roomWidth = roomWidth;
        this.roomLength = roomLength;
        this.entranceHeight = entranceHeight;
        this.roomHeight = roomHeight;
        this.basementHeight = basementHeight;
        this.hallwayWidth = hallwayWidth;
        this.doorwayWidth = doorwayWidth;
        this.doorwayHeight = doorwayHeight;
    }

    public static MKWorkspaceDimensions defaultDimensions() {
        int defaultHallwayWidth = 3;
        int defaultHeight = MKTowerStairProfile.getAllowedHeights(MKWorkspaceStairMode.AUTO, defaultHallwayWidth, 3, 4)
                .getFirst();
        return new MKWorkspaceDimensions(9, 9, defaultHeight, defaultHeight, defaultHeight, defaultHallwayWidth, 3, 3);
    }

    public static MKWorkspaceDimensions fromTag(CompoundTag tag) {
        return new MKWorkspaceDimensions(
                tag.getInt("roomWidth"),
                tag.getInt("roomLength"),
                tag.contains("entranceHeight") ? tag.getInt("entranceHeight") : tag.getInt("roomHeight"),
                tag.getInt("roomHeight"),
                tag.contains("basementHeight") ? tag.getInt("basementHeight") : tag.getInt("roomHeight"),
                tag.getInt("hallwayWidth"),
                tag.getInt("doorwayWidth"),
                tag.getInt("doorwayHeight")
        );
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("roomWidth", roomWidth);
        tag.putInt("roomLength", roomLength);
        tag.putInt("entranceHeight", entranceHeight);
        tag.putInt("roomHeight", roomHeight);
        tag.putInt("basementHeight", basementHeight);
        tag.putInt("hallwayWidth", hallwayWidth);
        tag.putInt("doorwayWidth", doorwayWidth);
        tag.putInt("doorwayHeight", doorwayHeight);
        return tag;
    }

    public List<String> validate() {
        List<String> errors = new ArrayList<>();
        validateOdd(errors, "room width", roomWidth, 3);
        validateOdd(errors, "room length", roomLength, 3);
        if (entranceHeight < 3) {
            errors.add("entrance height must be at least 3");
        }
        if (roomHeight < 3) {
            errors.add("room height must be at least 3");
        }
        if (basementHeight < 3) {
            errors.add("basement height must be at least 3");
        }
        validateOdd(errors, "hallway width", hallwayWidth, 1);
        validateOdd(errors, "doorway width", doorwayWidth, 1);
        if (doorwayHeight < 2) {
            errors.add("doorway height must be at least 2");
        }
        if (doorwayWidth > hallwayWidth) {
            errors.add("doorway width must be less than or equal to hallway width");
        }
        if (hallwayWidth > roomWidth) {
            errors.add("hallway width must be less than or equal to room width");
        }
        if (hallwayWidth > roomLength) {
            errors.add("hallway width must be less than or equal to room length");
        }
        if (doorwayHeight > entranceHeight) {
            errors.add("doorway height must be less than or equal to entrance height");
        }
        return errors;
    }

    public static List<Integer> getAllowedShaftSizes(int roomWidth, int roomLength) {
        List<Integer> allowed = new ArrayList<>();
        int maxSize = Math.max(1, Math.min(roomWidth, roomLength));
        int first = maxSize % 2 == 0 ? maxSize - 1 : maxSize;
        for (int size = first; size >= 1; size -= 2) {
            allowed.add(size);
        }
        if (allowed.isEmpty()) {
            allowed.add(1);
        }
        java.util.Collections.reverse(allowed);
        return allowed;
    }

    public static int snapToNearestAllowedShaftSize(int roomWidth, int roomLength, int requestedSize) {
        List<Integer> allowed = getAllowedShaftSizes(roomWidth, roomLength);
        return allowed.stream()
                .min(java.util.Comparator.comparingInt(value -> Math.abs(value - requestedSize)))
                .orElseGet(() -> allowed.getFirst());
    }

    public static int getTowerShaftPerimeter(int hallwayWidth) {
        return MKTowerStairProfile.getPerimeterStepCount(hallwayWidth, hallwayWidth);
    }

    public static List<Integer> getAllowedTowerHeights(MKWorkspaceStairMode stairMode, int hallwayWidth, int minimumHeight,
                                                        int count) {
        return MKTowerStairProfile.getAllowedHeights(stairMode, hallwayWidth, minimumHeight, count);
    }

    public static List<Integer> getAllowedTowerHeights(MKWorkspaceStairAuthoringConfig stairConfig, int hallwayWidth,
                                                       int minimumHeight, int count) {
        MKTowerStairProfile profile = MKTowerStairProfile.forTemplateReuse(stairConfig, hallwayWidth, hallwayWidth);
        if (profile.mode() == MKWorkspaceStairMode.LADDER || profile.mode() == MKWorkspaceStairMode.NONE) {
            return MKTowerStairProfile.getAllowedHeights(MKWorkspaceStairMode.LADDER, hallwayWidth, minimumHeight, count);
        }
        java.util.List<Integer> values = new java.util.ArrayList<>();
        int candidate = Math.max(1, minimumHeight);
        int maxCandidate = Math.max(candidate + 255, candidate + (count * 64));
        while (values.size() < count && candidate <= maxCandidate) {
            if (profile.isReusableInteriorHeight(candidate)) {
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

    public static int snapToNearestAllowedTowerHeight(MKWorkspaceStairMode stairMode, int hallwayWidth, int requestedHeight,
                                                      int minimumHeight, int count) {
        return MKTowerStairProfile.snapToNearestAllowedHeight(stairMode, hallwayWidth, requestedHeight, minimumHeight,
                count);
    }

    public static int snapToNearestAllowedTowerHeight(MKWorkspaceStairAuthoringConfig stairConfig, int hallwayWidth,
                                                      int requestedHeight, int minimumHeight, int count) {
        List<Integer> allowedHeights = getAllowedTowerHeights(stairConfig, hallwayWidth, minimumHeight, count);
        return allowedHeights.stream()
                .min(java.util.Comparator.comparingInt(value -> Math.abs(value - requestedHeight)))
                .orElseGet(() -> allowedHeights.getFirst());
    }

    public static List<Integer> getAllowedEntranceHeights(MKWorkspaceStairAuthoringConfig stairConfig, int hallwayWidth,
                                                          int referenceRoomHeight, int minimumHeight, int count) {
        MKTowerStairProfile profile = MKTowerStairProfile.forTemplateReuse(stairConfig, hallwayWidth, hallwayWidth);
        List<Integer> allowedHeights = getAllowedTowerHeights(stairConfig, hallwayWidth, minimumHeight, Math.max(count * 3, count));
        List<Integer> aligned = new java.util.ArrayList<>();
        for (int height : allowedHeights) {
            if (profile.isInteriorPhaseAligned(referenceRoomHeight, height)) {
                aligned.add(height);
                if (aligned.size() >= count) {
                    break;
                }
            }
        }
        if (aligned.isEmpty()) {
            aligned.add(snapToNearestAllowedTowerHeight(stairConfig, hallwayWidth, referenceRoomHeight, minimumHeight, count));
        }
        return aligned;
    }

    public static int snapToNearestAllowedEntranceHeight(MKWorkspaceStairAuthoringConfig stairConfig, int hallwayWidth,
                                                         int referenceRoomHeight, int requestedHeight, int minimumHeight,
                                                         int count) {
        List<Integer> allowedHeights = getAllowedEntranceHeights(stairConfig, hallwayWidth, referenceRoomHeight, minimumHeight,
                count);
        return allowedHeights.stream()
                .min(java.util.Comparator.comparingInt(value -> Math.abs(value - requestedHeight)))
                .orElseGet(() -> allowedHeights.getFirst());
    }

    public static List<Integer> getAllowedFlatRunLengths(MKWorkspaceStairAuthoringConfig stairConfig, int hallwayWidth,
                                                         int referenceRoomHeight, int count) {
        return MKTowerStairProfile.getAllowedFlatRunLengths(stairConfig, hallwayWidth, hallwayWidth,
                referenceRoomHeight, count);
    }

    public static int snapToNearestAllowedFlatRunLength(MKWorkspaceStairAuthoringConfig stairConfig, int hallwayWidth,
                                                        int referenceRoomHeight, int requestedFlatRunLength, int count) {
        List<Integer> allowedFlatRuns = getAllowedFlatRunLengths(stairConfig, hallwayWidth, referenceRoomHeight, count);
        return allowedFlatRuns.stream()
                .min(java.util.Comparator.comparingInt(value -> Math.abs(value - requestedFlatRunLength)))
                .orElseGet(() -> allowedFlatRuns.getFirst());
    }

    public static List<Integer> getAllowedStairWidths(int hallwayWidth) {
        List<Integer> allowed = new ArrayList<>();
        int maxWidth = Math.max(1, hallwayWidth / 2);
        for (int width = 1; width <= maxWidth; width++) {
            allowed.add(width);
        }
        return allowed;
    }

    public static int snapToNearestAllowedStairWidth(int hallwayWidth, int requestedWidth) {
        List<Integer> allowedWidths = getAllowedStairWidths(hallwayWidth);
        return allowedWidths.stream()
                .min(java.util.Comparator.comparingInt(value -> Math.abs(value - requestedWidth)))
                .orElseGet(() -> allowedWidths.getFirst());
    }

    private static void validateOdd(List<String> errors, String label, int value, int min) {
        if (value < min) {
            errors.add(label + " must be at least " + min);
        }
        if (value % 2 == 0) {
            errors.add(label + " must be odd");
        }
    }

    public int roomWidth() {
        return roomWidth;
    }

    public int roomLength() {
        return roomLength;
    }

    public int entranceHeight() {
        return entranceHeight;
    }

    public int roomHeight() {
        return roomHeight;
    }

    public int basementHeight() {
        return basementHeight;
    }

    public int hallwayWidth() {
        return hallwayWidth;
    }

    public int doorwayWidth() {
        return doorwayWidth;
    }

    public int doorwayHeight() {
        return doorwayHeight;
    }
}
