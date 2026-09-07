package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;

public class MKHorizontalOpeningProfile {
    public static final Codec<MKHorizontalOpeningProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("profileId").forGetter(MKHorizontalOpeningProfile::profileId),
            Codec.INT.fieldOf("openingWidth").forGetter(MKHorizontalOpeningProfile::openingWidth),
            Codec.INT.fieldOf("openingHeight").forGetter(MKHorizontalOpeningProfile::openingHeight),
            Codec.BOOL.optionalFieldOf("allowOnMainPath", false).forGetter(MKHorizontalOpeningProfile::allowOnMainPath),
            Codec.BOOL.optionalFieldOf("allowOnBranchPath", true).forGetter(MKHorizontalOpeningProfile::allowOnBranchPath)
    ).apply(instance, MKHorizontalOpeningProfile::new));

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

    public static List<MKHorizontalOpeningProfile> createDefaults(MKWorkspaceDimensions dimensions) {
        return List.of(
                new MKHorizontalOpeningProfile(
                        "main_opening",
                        dimensions.doorwayWidth(),
                        dimensions.doorwayHeight(),
                        true,
                        false
                ),
                new MKHorizontalOpeningProfile(
                        "branch_opening",
                        dimensions.doorwayWidth(),
                        dimensions.doorwayHeight(),
                        false,
                        true
                )
        );
    }

    public static MKHorizontalOpeningProfile fromTag(CompoundTag tag) {
        return MKWorkspaceCodecs.parseNbt(CODEC, tag, "horizontal opening profile");
    }

    public CompoundTag toTag() {
        return MKWorkspaceCodecs.encodeNbt(CODEC, this, "horizontal opening profile");
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
