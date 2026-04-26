package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;

public class MKWorkspaceVerticalAccessSpec {
    private final int shaftSize;
    private final MKVerticalAccessPlacement placement;
    private final MKWorkspaceStairAuthoringConfig stairConfig;

    public MKWorkspaceVerticalAccessSpec(int shaftSize, MKVerticalAccessPlacement placement,
                                         MKWorkspaceStairAuthoringConfig stairConfig) {
        this.shaftSize = shaftSize;
        this.placement = placement;
        this.stairConfig = stairConfig;
    }

    public static MKWorkspaceVerticalAccessSpec defaultSpec() {
        MKWorkspaceDimensions defaults = MKWorkspaceDimensions.defaultDimensions();
        return new MKWorkspaceVerticalAccessSpec(defaults.hallwayWidth(), MKVerticalAccessPlacement.CENTER,
                MKWorkspaceStairAuthoringConfig.defaultConfig());
    }

    public static MKWorkspaceVerticalAccessSpec fromLegacy(MKWorkspaceDimensions dimensions,
                                                           MKVerticalAccessPlacement placement,
                                                           MKWorkspaceStairAuthoringConfig stairConfig) {
        return new MKWorkspaceVerticalAccessSpec(dimensions.hallwayWidth(), placement, stairConfig);
    }

    public static MKWorkspaceVerticalAccessSpec fromTag(CompoundTag tag) {
        return new MKWorkspaceVerticalAccessSpec(
                tag.getInt("shaftSize"),
                MKVerticalAccessPlacement.fromSerializedName(tag.getString("placement")),
                tag.contains("stairConfig") ? MKWorkspaceStairAuthoringConfig.fromTag(tag.getCompound("stairConfig")) :
                        MKWorkspaceStairAuthoringConfig.defaultConfig()
        );
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("shaftSize", shaftSize);
        tag.putString("placement", placement.getSerializedName());
        tag.put("stairConfig", stairConfig.toTag());
        return tag;
    }

    public List<String> validate() {
        List<String> errors = new ArrayList<>();
        if (shaftSize < 1) {
            errors.add("vertical shaft size must be at least 1");
        }
        if (shaftSize % 2 == 0) {
            errors.add("vertical shaft size must be odd");
        }
        List<Integer> allowedStairWidths = MKWorkspaceDimensions.getAllowedStairWidths(shaftSize);
        if (!allowedStairWidths.contains(stairConfig.stairWidth())) {
            errors.add("stair width must be one of " + allowedStairWidths + " for shaft size " + shaftSize);
        }
        List<Integer> allowedHeights = MKWorkspaceDimensions.getAllowedTowerHeights(stairConfig, shaftSize, 3, 4);
        if (allowedHeights.isEmpty()) {
            errors.add("vertical access profile did not produce any reusable room heights");
        } else {
            List<Integer> allowedFlatRuns = MKWorkspaceDimensions.getAllowedFlatRunLengths(stairConfig, shaftSize,
                    allowedHeights.getFirst(), 4);
            if (!allowedFlatRuns.contains(stairConfig.flatRunLength())) {
                errors.add("flat run length must be one of " + allowedFlatRuns + " for shaft size " + shaftSize);
            }
        }
        return errors;
    }

    public boolean supportsReusableHeight(int interiorHeight) {
        MKVerticalAccessProfile profile = MKVerticalAccessProfile.forTemplateReuse(stairConfig, shaftSize, shaftSize);
        if (profile.mode() == MKWorkspaceStairMode.LADDER || profile.mode() == MKWorkspaceStairMode.NONE) {
            return interiorHeight >= 3;
        }
        return profile.isReusableInteriorHeight(interiorHeight);
    }

    public List<Integer> getAllowedReusableHeights(int minimumHeight, int count) {
        return MKWorkspaceDimensions.getAllowedTowerHeights(stairConfig, shaftSize, minimumHeight, count);
    }

    public int shaftSize() {
        return shaftSize;
    }

    public MKVerticalAccessPlacement placement() {
        return placement;
    }

    public MKWorkspaceStairAuthoringConfig stairConfig() {
        return stairConfig;
    }
}
