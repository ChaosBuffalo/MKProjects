package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class MKWorkspaceDimensions {
    public static final int MAX_BAND_HEIGHT_EXCLUSIVE = 48;
    private static final Map<BandHeightCacheKey, List<Integer>> ALLOWED_BAND_HEIGHT_CACHE = new ConcurrentHashMap<>();

    public static final Codec<MKWorkspaceDimensions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("roomWidth").forGetter(MKWorkspaceDimensions::roomWidth),
            Codec.INT.fieldOf("roomLength").forGetter(MKWorkspaceDimensions::roomLength),
            Codec.INT.optionalFieldOf("entranceHeight").forGetter(dimensions -> java.util.Optional.of(dimensions.entranceHeight())),
            Codec.INT.fieldOf("roomHeight").forGetter(MKWorkspaceDimensions::roomHeight),
            Codec.INT.optionalFieldOf("basementHeight").forGetter(dimensions -> java.util.Optional.of(dimensions.basementHeight())),
            Codec.INT.fieldOf("shaftWidth").forGetter(MKWorkspaceDimensions::shaftWidth),
            Codec.INT.fieldOf("doorwayWidth").forGetter(MKWorkspaceDimensions::doorwayWidth),
            Codec.INT.fieldOf("doorwayHeight").forGetter(MKWorkspaceDimensions::doorwayHeight)
    ).apply(instance, (roomWidth, roomLength, entranceHeight, roomHeight, basementHeight, shaftWidth,
                       doorwayWidth, doorwayHeight) -> new MKWorkspaceDimensions(
            roomWidth,
            roomLength,
            entranceHeight.orElse(roomHeight),
            roomHeight,
            basementHeight.orElse(roomHeight),
            shaftWidth,
            doorwayWidth,
            doorwayHeight
    )));

    private final int roomWidth;
    private final int roomLength;
    private final int entranceHeight;
    private final int roomHeight;
    private final int basementHeight;
    private final int shaftWidth;
    private final int doorwayWidth;
    private final int doorwayHeight;

    public MKWorkspaceDimensions(int roomWidth, int roomLength, int entranceHeight, int roomHeight, int basementHeight,
                                 int shaftWidth, int doorwayWidth, int doorwayHeight) {
        this.roomWidth = roomWidth;
        this.roomLength = roomLength;
        this.entranceHeight = entranceHeight;
        this.roomHeight = roomHeight;
        this.basementHeight = basementHeight;
        this.shaftWidth = shaftWidth;
        this.doorwayWidth = doorwayWidth;
        this.doorwayHeight = doorwayHeight;
    }

    public static MKWorkspaceDimensions defaultDimensions() {
        int defaultShaftWidth = 3;
        int defaultHeight = MKVerticalAccessProfile.getAllowedHeights(MKWorkspaceStairMode.AUTO, defaultShaftWidth, 3, 4)
                .getFirst();
        return new MKWorkspaceDimensions(9, 9, defaultHeight, defaultHeight, defaultHeight, defaultShaftWidth, 3, 3);
    }

    public static MKWorkspaceDimensions fromTag(CompoundTag tag) {
        return MKWorkspaceCodecs.parseNbt(CODEC, tag, "workspace dimensions");
    }

    public CompoundTag toTag() {
        return MKWorkspaceCodecs.encodeNbt(CODEC, this, "workspace dimensions");
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
        validateOdd(errors, "shaft width", shaftWidth, 1);
        validateOdd(errors, "doorway width", doorwayWidth, 1);
        if (doorwayHeight < 2) {
            errors.add("doorway height must be at least 2");
        }
        if (doorwayWidth > shaftWidth) {
            errors.add("doorway width must be less than or equal to shaft width");
        }
        if (shaftWidth > roomWidth) {
            errors.add("shaft width must be less than or equal to room width");
        }
        if (shaftWidth > roomLength) {
            errors.add("shaft width must be less than or equal to room length");
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

    public static int snapToNearestUsableShaftSize(MKWorkspaceStairAuthoringConfig stairConfig, int roomWidth,
                                                   int roomLength, int requestedSize, int minimumHeight) {
        List<Integer> allowed = getAllowedShaftSizes(roomWidth, roomLength);
        int snapped = snapToNearestAllowedShaftSize(roomWidth, roomLength, requestedSize);
        if (hasReusableBandHeight(stairConfig, snapped, minimumHeight)) {
            return snapped;
        }
        return allowed.stream()
                .filter(size -> hasReusableBandHeight(stairConfig, size, minimumHeight))
                .min(java.util.Comparator.comparingInt(value -> Math.abs(value - requestedSize)))
                .orElse(snapped);
    }

    public static int getTowerShaftPerimeter(int shaftWidth) {
        return MKVerticalAccessProfile.getPerimeterStepCount(shaftWidth, shaftWidth);
    }

    public static List<Integer> getAllowedTowerHeights(MKWorkspaceStairMode stairMode, int shaftWidth, int minimumHeight,
                                                        int count) {
        return MKVerticalAccessProfile.getAllowedHeights(stairMode, shaftWidth, minimumHeight, count);
    }

    public static List<Integer> getAllowedTowerHeights(MKWorkspaceStairAuthoringConfig stairConfig, int shaftWidth,
                                                       int minimumHeight, int count) {
        MKWorkspaceStairMode mode = MKVerticalAccessProfile.normalizeMode(stairConfig.mode());
        if (mode == MKWorkspaceStairMode.LADDER || mode == MKWorkspaceStairMode.NONE) {
            return MKVerticalAccessProfile.getAllowedHeights(MKWorkspaceStairMode.LADDER, shaftWidth, minimumHeight, count);
        }
        java.util.List<Integer> values = new java.util.ArrayList<>();
        int candidate = Math.max(1, minimumHeight);
        int maxCandidate = Math.max(candidate + 255, candidate + (count * 64));
        while (values.size() < count && candidate <= maxCandidate) {
            if (MKResolvedVerticalAccessProfile.resolve(stairConfig, shaftWidth, shaftWidth, candidate).isPresent()) {
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

    public static int snapToNearestAllowedTowerHeight(MKWorkspaceStairMode stairMode, int shaftWidth, int requestedHeight,
                                                      int minimumHeight, int count) {
        return MKVerticalAccessProfile.snapToNearestAllowedHeight(stairMode, shaftWidth, requestedHeight, minimumHeight,
                count);
    }

    public static int snapToNearestAllowedTowerHeight(MKWorkspaceStairAuthoringConfig stairConfig, int shaftWidth,
                                                      int requestedHeight, int minimumHeight, int count) {
        List<Integer> allowedHeights = getAllowedTowerHeights(stairConfig, shaftWidth, minimumHeight, count);
        return allowedHeights.stream()
                .min(java.util.Comparator.comparingInt(value -> Math.abs(value - requestedHeight)))
                .orElse(Math.max(minimumHeight, Math.min(MAX_BAND_HEIGHT_EXCLUSIVE - 1, requestedHeight)));
    }

    public static List<Integer> getAllowedEntranceHeights(MKWorkspaceStairAuthoringConfig stairConfig, int shaftWidth,
                                                          int referenceRoomHeight, int minimumHeight, int count) {
        List<Integer> allowedHeights = getAllowedTowerHeights(stairConfig, shaftWidth, minimumHeight, Math.max(count * 3, count));
        List<Integer> aligned = new java.util.ArrayList<>();
        for (int height : allowedHeights) {
            if (MKResolvedVerticalAccessProfile.resolve(stairConfig, shaftWidth, shaftWidth, height).isPresent()) {
                aligned.add(height);
                if (aligned.size() >= count) {
                    break;
                }
            }
        }
        if (aligned.isEmpty()) {
            aligned.add(snapToNearestAllowedTowerHeight(stairConfig, shaftWidth, referenceRoomHeight, minimumHeight, count));
        }
        return aligned;
    }

    public static List<Integer> getAllowedBandHeights(MKWorkspaceStairAuthoringConfig stairConfig, int shaftWidth,
                                                      int referenceRoomHeight, int minimumHeight, int count) {
        BandHeightCacheKey key = BandHeightCacheKey.from(stairConfig, shaftWidth, minimumHeight);
        List<Integer> allowedHeights = ALLOWED_BAND_HEIGHT_CACHE.computeIfAbsent(key,
                ignored -> computeAllowedBandHeights(stairConfig, key.shaftWidth(), key.minimumHeight()));
        return allowedHeights;
    }

    public static int snapToNearestAllowedEntranceHeight(MKWorkspaceStairAuthoringConfig stairConfig, int shaftWidth,
                                                         int referenceRoomHeight, int requestedHeight, int minimumHeight,
                                                         int count) {
        List<Integer> allowedHeights = getAllowedEntranceHeights(stairConfig, shaftWidth, referenceRoomHeight, minimumHeight,
                count);
        return allowedHeights.stream()
                .min(java.util.Comparator.comparingInt(value -> Math.abs(value - requestedHeight)))
                .orElse(Math.max(minimumHeight, Math.min(MAX_BAND_HEIGHT_EXCLUSIVE - 1, requestedHeight)));
    }

    public static int snapToNearestAllowedBandHeight(MKWorkspaceStairAuthoringConfig stairConfig, int shaftWidth,
                                                     int referenceRoomHeight, int requestedHeight, int minimumHeight,
                                                     int count) {
        List<Integer> allowedHeights = getAllowedBandHeights(stairConfig, shaftWidth, referenceRoomHeight,
                minimumHeight, count);
        return allowedHeights.stream()
                .min(java.util.Comparator.comparingInt(value -> Math.abs(value - requestedHeight)))
                .orElse(Math.max(minimumHeight, Math.min(MAX_BAND_HEIGHT_EXCLUSIVE - 1, requestedHeight)));
    }

    public static List<Integer> getAllowedStairWidths(int shaftWidth) {
        List<Integer> allowed = new ArrayList<>();
        int maxWidth = Math.max(1, shaftWidth / 2);
        for (int width = 1; width <= maxWidth; width++) {
            allowed.add(width);
        }
        return allowed;
    }

    public static int snapToNearestAllowedStairWidth(int shaftWidth, int requestedWidth) {
        List<Integer> allowedWidths = getAllowedStairWidths(shaftWidth);
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

    private static List<Integer> computeAllowedBandHeights(MKWorkspaceStairAuthoringConfig stairConfig,
                                                           int shaftWidth, int minimumHeight) {
        List<Integer> aligned = new java.util.ArrayList<>();
        for (int height = minimumHeight; height < MAX_BAND_HEIGHT_EXCLUSIVE; height++) {
            if (MKResolvedVerticalAccessProfile.resolve(stairConfig, shaftWidth, shaftWidth, height).isPresent()) {
                aligned.add(height);
            }
        }
        return List.copyOf(aligned);
    }

    private static boolean hasReusableBandHeight(MKWorkspaceStairAuthoringConfig stairConfig, int shaftSize,
                                                 int minimumHeight) {
        int stairWidth = snapToNearestAllowedStairWidth(shaftSize, stairConfig.stairWidth());
        MKWorkspaceStairAuthoringConfig config = new MKWorkspaceStairAuthoringConfig(
                stairConfig.mode(),
                stairConfig.riseType(),
                stairWidth
        );
        return !getAllowedBandHeights(config, shaftSize, minimumHeight, minimumHeight,
                MAX_BAND_HEIGHT_EXCLUSIVE).isEmpty();
    }

    private record BandHeightCacheKey(MKWorkspaceStairMode mode, MKWorkspaceStairRiseType riseType, int stairWidth,
                                      int shaftWidth, int minimumHeight) {
        private static BandHeightCacheKey from(MKWorkspaceStairAuthoringConfig stairConfig, int shaftWidth,
                                               int minimumHeight) {
            return new BandHeightCacheKey(
                    MKVerticalAccessProfile.normalizeMode(stairConfig.mode()),
                    stairConfig.riseType(),
                    Math.max(1, stairConfig.stairWidth()),
                    Math.max(1, shaftWidth),
                    Math.max(1, minimumHeight)
            );
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

    public int shaftWidth() {
        return shaftWidth;
    }

    public int doorwayWidth() {
        return doorwayWidth;
    }

    public int doorwayHeight() {
        return doorwayHeight;
    }
}

