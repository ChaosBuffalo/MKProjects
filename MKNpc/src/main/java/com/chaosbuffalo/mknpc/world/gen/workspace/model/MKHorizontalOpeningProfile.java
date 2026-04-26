package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;

public class MKHorizontalOpeningProfile {
    private final String profileId;
    private final int openingWidth;
    private final int openingHeight;
    private final boolean allowOnMainPath;
    private final boolean allowOnBranchPath;

    public MKHorizontalOpeningProfile(String profileId, int openingWidth, int openingHeight,
                                      boolean allowOnMainPath, boolean allowOnBranchPath) {
        this.profileId = profileId;
        this.openingWidth = openingWidth;
        this.openingHeight = openingHeight;
        this.allowOnMainPath = allowOnMainPath;
        this.allowOnBranchPath = allowOnBranchPath;
    }

    public static List<MKHorizontalOpeningProfile> createDefaults(List<MKTowerWorkspaceCategoryProfile> categoryProfiles) {
        List<MKHorizontalOpeningProfile> defaults = new ArrayList<>();
        for (MKTowerWorkspaceCategoryProfile profile : categoryProfiles) {
            defaults.add(new MKHorizontalOpeningProfile(
                    profile.category().getSerializedName() + "_main",
                    profile.mainOpeningWidth(),
                    profile.mainOpeningHeight(),
                    true,
                    false
            ));
            defaults.add(new MKHorizontalOpeningProfile(
                    profile.category().getSerializedName() + "_branch",
                    profile.branchOpeningWidth(),
                    profile.branchOpeningHeight(),
                    false,
                    true
            ));
        }
        return List.copyOf(defaults);
    }

    public static MKHorizontalOpeningProfile fromTag(CompoundTag tag) {
        return new MKHorizontalOpeningProfile(
                tag.getString("profileId"),
                tag.getInt("openingWidth"),
                tag.getInt("openingHeight"),
                tag.contains("allowOnMainPath") && tag.getBoolean("allowOnMainPath"),
                !tag.contains("allowOnBranchPath") || tag.getBoolean("allowOnBranchPath")
        );
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("profileId", profileId);
        tag.putInt("openingWidth", openingWidth);
        tag.putInt("openingHeight", openingHeight);
        tag.putBoolean("allowOnMainPath", allowOnMainPath);
        tag.putBoolean("allowOnBranchPath", allowOnBranchPath);
        return tag;
    }

    public List<String> validate() {
        List<String> errors = new ArrayList<>();
        if (profileId.isBlank()) {
            errors.add("horizontal opening profile id cannot be blank");
        }
        if (openingWidth < 1) {
            errors.add("horizontal opening width must be at least 1");
        }
        if (openingWidth % 2 == 0) {
            errors.add("horizontal opening width must be odd");
        }
        if (openingHeight < 2) {
            errors.add("horizontal opening height must be at least 2");
        }
        if (!allowOnMainPath && !allowOnBranchPath) {
            errors.add("horizontal opening profile " + profileId + " must be usable on the main path or branch path");
        }
        return errors;
    }

    public String profileId() {
        return profileId;
    }

    public int openingWidth() {
        return openingWidth;
    }

    public int openingHeight() {
        return openingHeight;
    }

    public boolean allowOnMainPath() {
        return allowOnMainPath;
    }

    public boolean allowOnBranchPath() {
        return allowOnBranchPath;
    }
}
