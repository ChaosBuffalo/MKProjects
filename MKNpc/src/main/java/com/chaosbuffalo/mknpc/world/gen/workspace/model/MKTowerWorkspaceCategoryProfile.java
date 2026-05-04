package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MKTowerWorkspaceCategoryProfile {
    public static final int MIN_ROOM_HEIGHT = 2;
    public static final int DEFAULT_MIN_MAIN_PATH_PIECES = 1;
    public static final int DEFAULT_MAX_MAIN_PATH_PIECES = 2;
    public static final int DEFAULT_MAX_BRANCH_PIECES_BEFORE_CAP = 10;
    public static final String TOP_VOID_MARGIN_TAG = "workspace_top_void_margin";
    public static final String BOTTOM_VOID_MARGIN_TAG = "workspace_bottom_void_margin";

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
            Codec.INT.fieldOf("fullHeight").forGetter(MKTowerWorkspaceCategoryProfile::fullHeight),
            Codec.INT.optionalFieldOf("topVoidMargin", 0).forGetter(MKTowerWorkspaceCategoryProfile::topVoidMargin),
            Codec.INT.optionalFieldOf("bottomVoidMargin", 0).forGetter(MKTowerWorkspaceCategoryProfile::bottomVoidMargin),
            MKWorkspacePaletteOverride.CODEC.optionalFieldOf("paletteOverride")
                    .forGetter(MKTowerWorkspaceCategoryProfile::paletteOverrideOpt)
    ).apply(instance, (category, roomWidth, roomLength, minMainPathPieces, maxMainPathPieces, maxBranchPiecesBeforeCap,
                       fullHeight, topVoidMargin, bottomVoidMargin, paletteOverride) ->
            new MKTowerWorkspaceCategoryProfile(
                    category,
                    roomWidth,
                    roomLength,
                    fullHeight,
                    minMainPathPieces,
                    maxMainPathPieces,
                    maxBranchPiecesBeforeCap,
                    topVoidMargin,
                    bottomVoidMargin,
                    paletteOverride.orElse(null)
            )));

    private final MKTowerWorkspaceCategory category;
    private final int roomWidth;
    private final int roomLength;
    private final int fullHeight;
    private final int topVoidMargin;
    private final int bottomVoidMargin;
    private final int minMainPathPieces;
    private final int maxMainPathPieces;
    private final int maxBranchPiecesBeforeCap;
    @Nullable
    private final MKWorkspacePaletteOverride paletteOverride;

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
        this(category, roomWidth, roomLength, fullHeight, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, null);
    }

    public MKTowerWorkspaceCategoryProfile(MKTowerWorkspaceCategory category, int roomWidth, int roomLength,
                                           int fullHeight, int minMainPathPieces, int maxMainPathPieces,
                                           int maxBranchPiecesBeforeCap,
                                           @Nullable MKWorkspacePaletteOverride paletteOverride) {
        this(category, roomWidth, roomLength, fullHeight, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, 0, 0, paletteOverride);
    }

    public MKTowerWorkspaceCategoryProfile(MKTowerWorkspaceCategory category, int roomWidth, int roomLength,
                                           int fullHeight, int minMainPathPieces, int maxMainPathPieces,
                                           int maxBranchPiecesBeforeCap, int topVoidMargin, int bottomVoidMargin,
                                           @Nullable MKWorkspacePaletteOverride paletteOverride) {
        this.category = category;
        this.roomWidth = roomWidth;
        this.roomLength = roomLength;
        this.fullHeight = fullHeight;
        this.topVoidMargin = Math.max(0, topVoidMargin);
        this.bottomVoidMargin = Math.max(0, bottomVoidMargin);
        this.minMainPathPieces = minMainPathPieces;
        this.maxMainPathPieces = maxMainPathPieces;
        this.maxBranchPiecesBeforeCap = maxBranchPiecesBeforeCap;
        this.paletteOverride = paletteOverride != null && !paletteOverride.isEmpty() ? paletteOverride : null;
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
        if (topVoidMargin < 0) {
            errors.add(category.getSerializedName() + " top void margin must be at least 0");
        }
        if (bottomVoidMargin < 0) {
            errors.add(category.getSerializedName() + " bottom void margin must be at least 0");
        }
        if (category != MKTowerWorkspaceCategory.TOP_CAP && topVoidMargin > 0) {
            errors.add(category.getSerializedName() + " top void margin is only supported on top_cap");
        }
        if (category != MKTowerWorkspaceCategory.BASEMENT_CAP && bottomVoidMargin > 0) {
            errors.add(category.getSerializedName() + " bottom void margin is only supported on basement_cap");
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

    public int topVoidMargin() {
        return topVoidMargin;
    }

    public int bottomVoidMargin() {
        return bottomVoidMargin;
    }

    public int exportedFullHeight() {
        return fullHeight + topVoidMargin + bottomVoidMargin;
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

    @Nullable
    public MKWorkspacePaletteOverride paletteOverride() {
        return paletteOverride;
    }

    public Optional<MKWorkspacePaletteOverride> paletteOverrideOpt() {
        return Optional.ofNullable(paletteOverride);
    }
}

