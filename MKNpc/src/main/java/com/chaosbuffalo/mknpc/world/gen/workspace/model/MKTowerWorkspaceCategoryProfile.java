package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;

public class MKTowerWorkspaceCategoryProfile {
    public static final int MIN_ROOM_HEIGHT = 2;
    public static final int DEFAULT_MIN_MAIN_PATH_PIECES = 1;
    public static final int DEFAULT_MAX_MAIN_PATH_PIECES = 2;
    public static final int DEFAULT_MAX_BRANCH_PIECES_BEFORE_CAP = 10;

    public static final Codec<MKTowerWorkspaceCategoryProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MKWorkspaceCodecs.TOWER_CATEGORY_CODEC.fieldOf("category").forGetter(MKTowerWorkspaceCategoryProfile::category),
            Codec.INT.fieldOf("roomWidth").forGetter(MKTowerWorkspaceCategoryProfile::roomWidth),
            Codec.INT.fieldOf("roomLength").forGetter(MKTowerWorkspaceCategoryProfile::roomLength),
            Codec.INT.optionalFieldOf("minMainPathPieces", DEFAULT_MIN_MAIN_PATH_PIECES)
                    .forGetter(MKTowerWorkspaceCategoryProfile::minMainPathPieces),
            Codec.INT.optionalFieldOf("maxMainPathPieces", DEFAULT_MAX_MAIN_PATH_PIECES)
                    .forGetter(MKTowerWorkspaceCategoryProfile::maxMainPathPieces),
            Codec.INT.optionalFieldOf("maxBranchPiecesBeforeCap", DEFAULT_MAX_BRANCH_PIECES_BEFORE_CAP)
                    .forGetter(MKTowerWorkspaceCategoryProfile::maxBranchPiecesBeforeCap),
            Codec.INT.optionalFieldOf("fullHeight").forGetter(profile -> java.util.Optional.of(profile.fullHeight())),
            Codec.INT.optionalFieldOf("defaultHeight").forGetter(profile -> java.util.Optional.of(profile.fullHeight())),
            Codec.INT.optionalFieldOf("minHeight").forGetter(profile -> java.util.Optional.<Integer>empty()),
            Codec.INT.optionalFieldOf("maxHeight").forGetter(profile -> java.util.Optional.of(profile.fullHeight())),
            Codec.BOOL.optionalFieldOf("supportsVerticalAccess", true)
                    .forGetter(profile -> true)
    ).apply(instance, (category, roomWidth, roomLength, minMainPathPieces, maxMainPathPieces, maxBranchPiecesBeforeCap,
                       fullHeight, defaultHeight, minHeight, maxHeight, supportsVerticalAccess) ->
            new MKTowerWorkspaceCategoryProfile(
                    category,
                    roomWidth,
                    roomLength,
                    fullHeight.orElseGet(() -> defaultHeight.orElseGet(() -> maxHeight.orElse(3))),
                    minMainPathPieces,
                    maxMainPathPieces,
                    maxBranchPiecesBeforeCap
            )));

    private final MKTowerWorkspaceCategory category;
    private final int roomWidth;
    private final int roomLength;
    private final int fullHeight;
    private final int minMainPathPieces;
    private final int maxMainPathPieces;
    private final int maxBranchPiecesBeforeCap;

    public MKTowerWorkspaceCategoryProfile(MKTowerWorkspaceCategory category, int roomWidth, int roomLength,
                                           int fullHeight) {
        this(category, roomWidth, roomLength, fullHeight, DEFAULT_MIN_MAIN_PATH_PIECES,
                DEFAULT_MAX_MAIN_PATH_PIECES, DEFAULT_MAX_BRANCH_PIECES_BEFORE_CAP);
    }

    public MKTowerWorkspaceCategoryProfile(MKTowerWorkspaceCategory category, int roomWidth, int roomLength,
                                           int fullHeight, int minMainPathPieces, int maxMainPathPieces) {
        this(category, roomWidth, roomLength, fullHeight, minMainPathPieces, maxMainPathPieces,
                DEFAULT_MAX_BRANCH_PIECES_BEFORE_CAP);
    }

    public MKTowerWorkspaceCategoryProfile(MKTowerWorkspaceCategory category, int roomWidth, int roomLength,
                                           int fullHeight, int minMainPathPieces, int maxMainPathPieces,
                                           int maxBranchPiecesBeforeCap) {
        this.category = category;
        this.roomWidth = roomWidth;
        this.roomLength = roomLength;
        this.fullHeight = fullHeight;
        this.minMainPathPieces = minMainPathPieces;
        this.maxMainPathPieces = maxMainPathPieces;
        this.maxBranchPiecesBeforeCap = maxBranchPiecesBeforeCap;
    }

    public static List<MKTowerWorkspaceCategoryProfile> createDefaults(MKWorkspaceDimensions dimensions) {
        return List.of(
                new MKTowerWorkspaceCategoryProfile(MKTowerWorkspaceCategory.ENTRY,
                        dimensions.roomWidth(), dimensions.roomLength(), dimensions.entranceHeight()),
                new MKTowerWorkspaceCategoryProfile(MKTowerWorkspaceCategory.MAIN,
                        dimensions.roomWidth(), dimensions.roomLength(), dimensions.roomHeight()),
                new MKTowerWorkspaceCategoryProfile(MKTowerWorkspaceCategory.BASEMENT,
                        dimensions.roomWidth(), dimensions.roomLength(), dimensions.basementHeight()),
                new MKTowerWorkspaceCategoryProfile(MKTowerWorkspaceCategory.TOP_CAP,
                        dimensions.roomWidth(), dimensions.roomLength(), dimensions.roomHeight()),
                new MKTowerWorkspaceCategoryProfile(MKTowerWorkspaceCategory.BASEMENT_CAP,
                        dimensions.roomWidth(), dimensions.roomLength(), dimensions.basementHeight())
        );
    }

    public static MKTowerWorkspaceCategoryProfile fromTag(CompoundTag tag) {
        return MKWorkspaceCodecs.parseNbt(CODEC, tag, "tower workspace category profile");
    }

    public CompoundTag toTag() {
        return MKWorkspaceCodecs.encodeNbt(CODEC, this, "tower workspace category profile");
    }

    public List<String> validate(MKWorkspaceVerticalAccessSpec verticalAccessSpec) {
        List<String> errors = new ArrayList<>();
        validateOdd(errors, category.getSerializedName() + " room width", roomWidth, 3);
        validateOdd(errors, category.getSerializedName() + " room length", roomLength, 3);
        if (fullHeight < 3) {
            errors.add(category.getSerializedName() + " full height must be at least 3");
        }
        if (verticalAccessSpec.shaftSize() > roomWidth) {
            errors.add(category.getSerializedName() + " room width must be at least the shared shaft size");
        }
        if (verticalAccessSpec.shaftSize() > roomLength) {
            errors.add(category.getSerializedName() + " room length must be at least the shared shaft size");
        }
        if (minMainPathPieces < 0) {
            errors.add(category.getSerializedName() + " min main path pieces must be at least 0");
        }
        if (maxMainPathPieces < 0) {
            errors.add(category.getSerializedName() + " max main path pieces must be at least 0");
        }
        if (minMainPathPieces > maxMainPathPieces) {
            errors.add(category.getSerializedName() + " min main path pieces must be <= max main path pieces");
        }
        if (maxBranchPiecesBeforeCap < 0) {
            errors.add(category.getSerializedName() + " max branch pieces before cap must be at least 0");
        }
        if (maxBranchPiecesBeforeCap > DEFAULT_MAX_BRANCH_PIECES_BEFORE_CAP) {
            errors.add(category.getSerializedName() + " max branch pieces before cap must be at most " +
                    DEFAULT_MAX_BRANCH_PIECES_BEFORE_CAP);
        }
        return errors;
    }

    private static void validateOdd(List<String> errors, String label, int value, int min) {
        if (value < min) {
            errors.add(label + " must be at least " + min);
        }
        if (value % 2 == 0) {
            errors.add(label + " must be odd");
        }
    }

    public MKTowerWorkspaceCategory category() {
        return category;
    }

    public int roomWidth() {
        return roomWidth;
    }

    public int roomLength() {
        return roomLength;
    }

    public int fullHeight() {
        return fullHeight;
    }

    public int minMainPathPieces() {
        return minMainPathPieces;
    }

    public int maxMainPathPieces() {
        return maxMainPathPieces;
    }

    public int maxBranchPiecesBeforeCap() {
        return maxBranchPiecesBeforeCap;
    }
}

