package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;

public class MKTowerWorkspaceCategoryProfile {
    private final MKTowerWorkspaceCategory category;
    private final int roomWidth;
    private final int roomLength;
    private final int defaultHeight;
    private final int minHeight;
    private final int maxHeight;
    private final boolean supportsVerticalAccess;
    private final int mainOpeningWidth;
    private final int mainOpeningHeight;
    private final int branchOpeningWidth;
    private final int branchOpeningHeight;

    public MKTowerWorkspaceCategoryProfile(MKTowerWorkspaceCategory category, int roomWidth, int roomLength,
                                           int defaultHeight, int minHeight, int maxHeight,
                                           boolean supportsVerticalAccess, int mainOpeningWidth, int mainOpeningHeight,
                                           int branchOpeningWidth, int branchOpeningHeight) {
        this.category = category;
        this.roomWidth = roomWidth;
        this.roomLength = roomLength;
        this.defaultHeight = defaultHeight;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
        this.supportsVerticalAccess = supportsVerticalAccess;
        this.mainOpeningWidth = mainOpeningWidth;
        this.mainOpeningHeight = mainOpeningHeight;
        this.branchOpeningWidth = branchOpeningWidth;
        this.branchOpeningHeight = branchOpeningHeight;
    }

    public static List<MKTowerWorkspaceCategoryProfile> createDefaults(MKWorkspaceDimensions dimensions,
                                                                       MKWorkspaceVerticalAccessSpec verticalAccessSpec) {
        return List.of(
                new MKTowerWorkspaceCategoryProfile(MKTowerWorkspaceCategory.ENTRY,
                        dimensions.roomWidth(), dimensions.roomLength(), dimensions.entranceHeight(),
                        3, dimensions.entranceHeight(), true, dimensions.doorwayWidth(), dimensions.doorwayHeight(),
                        dimensions.doorwayWidth(), dimensions.doorwayHeight()),
                new MKTowerWorkspaceCategoryProfile(MKTowerWorkspaceCategory.MAIN,
                        dimensions.roomWidth(), dimensions.roomLength(), dimensions.roomHeight(),
                        3, dimensions.roomHeight(), true, dimensions.doorwayWidth(), dimensions.doorwayHeight(),
                        dimensions.doorwayWidth(), dimensions.doorwayHeight()),
                new MKTowerWorkspaceCategoryProfile(MKTowerWorkspaceCategory.BASEMENT,
                        dimensions.roomWidth(), dimensions.roomLength(), dimensions.basementHeight(),
                        3, dimensions.basementHeight(), true, dimensions.doorwayWidth(), dimensions.doorwayHeight(),
                        dimensions.doorwayWidth(), dimensions.doorwayHeight()),
                new MKTowerWorkspaceCategoryProfile(MKTowerWorkspaceCategory.BOSS,
                        dimensions.roomWidth(), dimensions.roomLength(), dimensions.roomHeight(),
                        3, dimensions.roomHeight(), true, dimensions.doorwayWidth(), dimensions.doorwayHeight(),
                        dimensions.doorwayWidth(), dimensions.doorwayHeight())
        );
    }

    public static MKTowerWorkspaceCategoryProfile fromTag(CompoundTag tag) {
        return new MKTowerWorkspaceCategoryProfile(
                MKTowerWorkspaceCategory.fromSerializedName(tag.getString("category")),
                tag.getInt("roomWidth"),
                tag.getInt("roomLength"),
                tag.getInt("defaultHeight"),
                tag.contains("minHeight") ? tag.getInt("minHeight") : 3,
                tag.contains("maxHeight") ? tag.getInt("maxHeight") : tag.getInt("defaultHeight"),
                tag.contains("supportsVerticalAccess") ? tag.getBoolean("supportsVerticalAccess") : true,
                tag.contains("mainOpeningWidth") ? tag.getInt("mainOpeningWidth") : 3,
                tag.contains("mainOpeningHeight") ? tag.getInt("mainOpeningHeight") : 3,
                tag.contains("branchOpeningWidth") ? tag.getInt("branchOpeningWidth") :
                        (tag.contains("mainOpeningWidth") ? tag.getInt("mainOpeningWidth") : 3),
                tag.contains("branchOpeningHeight") ? tag.getInt("branchOpeningHeight") :
                        (tag.contains("mainOpeningHeight") ? tag.getInt("mainOpeningHeight") : 3)
        );
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("category", category.getSerializedName());
        tag.putInt("roomWidth", roomWidth);
        tag.putInt("roomLength", roomLength);
        tag.putInt("defaultHeight", defaultHeight);
        tag.putInt("minHeight", minHeight);
        tag.putInt("maxHeight", maxHeight);
        tag.putBoolean("supportsVerticalAccess", supportsVerticalAccess);
        tag.putInt("mainOpeningWidth", mainOpeningWidth);
        tag.putInt("mainOpeningHeight", mainOpeningHeight);
        tag.putInt("branchOpeningWidth", branchOpeningWidth);
        tag.putInt("branchOpeningHeight", branchOpeningHeight);
        return tag;
    }

    public List<String> validate(MKWorkspaceVerticalAccessSpec verticalAccessSpec) {
        List<String> errors = new ArrayList<>();
        validateOdd(errors, category.getSerializedName() + " room width", roomWidth, 3);
        validateOdd(errors, category.getSerializedName() + " room length", roomLength, 3);
        validateOdd(errors, category.getSerializedName() + " main opening width", mainOpeningWidth, 1);
        validateOdd(errors, category.getSerializedName() + " branch opening width", branchOpeningWidth, 1);
        if (defaultHeight < 3) {
            errors.add(category.getSerializedName() + " default height must be at least 3");
        }
        if (minHeight < 3) {
            errors.add(category.getSerializedName() + " min height must be at least 3");
        }
        if (maxHeight < minHeight) {
            errors.add(category.getSerializedName() + " max height must be greater than or equal to min height");
        }
        if (defaultHeight < minHeight || defaultHeight > maxHeight) {
            errors.add(category.getSerializedName() + " default height must be within min/max height");
        }
        if (mainOpeningHeight < 2) {
            errors.add(category.getSerializedName() + " main opening height must be at least 2");
        }
        if (branchOpeningHeight < 2) {
            errors.add(category.getSerializedName() + " branch opening height must be at least 2");
        }
        if (supportsVerticalAccess) {
            if (verticalAccessSpec.shaftSize() > roomWidth) {
                errors.add(category.getSerializedName() + " room width must be at least the shared shaft size");
            }
            if (verticalAccessSpec.shaftSize() > roomLength) {
                errors.add(category.getSerializedName() + " room length must be at least the shared shaft size");
            }
            if (!verticalAccessSpec.supportsReusableHeight(defaultHeight)) {
                errors.add(category.getSerializedName() + " default height " + defaultHeight +
                        " is not reusable for shaft size " + verticalAccessSpec.shaftSize());
            }
            if (maxHeight > defaultHeight) {
                errors.add(category.getSerializedName() + " shaft rooms cannot exceed their default reusable band height");
            }
        } else if (maxHeight < defaultHeight) {
            errors.add(category.getSerializedName() + " non-shaft rooms must allow their default height");
        }
        if (mainOpeningWidth > roomWidth || mainOpeningWidth > roomLength) {
            errors.add(category.getSerializedName() + " main opening width must fit inside the room footprint");
        }
        if (branchOpeningWidth > roomWidth || branchOpeningWidth > roomLength) {
            errors.add(category.getSerializedName() + " branch opening width must fit inside the room footprint");
        }
        if (mainOpeningHeight > maxHeight) {
            errors.add(category.getSerializedName() + " main opening height must fit inside the room height band");
        }
        if (branchOpeningHeight > maxHeight) {
            errors.add(category.getSerializedName() + " branch opening height must fit inside the room height band");
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

    public int defaultHeight() {
        return defaultHeight;
    }

    public int minHeight() {
        return minHeight;
    }

    public int maxHeight() {
        return maxHeight;
    }

    public boolean supportsVerticalAccess() {
        return supportsVerticalAccess;
    }

    public int mainOpeningWidth() {
        return mainOpeningWidth;
    }

    public int mainOpeningHeight() {
        return mainOpeningHeight;
    }

    public int branchOpeningWidth() {
        return branchOpeningWidth;
    }

    public int branchOpeningHeight() {
        return branchOpeningHeight;
    }
}
