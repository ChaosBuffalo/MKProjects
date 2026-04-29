package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;

public class MKWorkspaceVerticalAccessSpec {
    public static final Codec<MKWorkspaceVerticalAccessSpec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("shaftSize").forGetter(MKWorkspaceVerticalAccessSpec::shaftSize),
            MKWorkspaceCodecs.VERTICAL_ACCESS_PLACEMENT_CODEC.fieldOf("placement")
                    .forGetter(MKWorkspaceVerticalAccessSpec::placement),
            MKWorkspaceStairAuthoringConfig.CODEC.optionalFieldOf("stairConfig", MKWorkspaceStairAuthoringConfig.defaultConfig())
                    .forGetter(MKWorkspaceVerticalAccessSpec::stairConfig)
    ).apply(instance, MKWorkspaceVerticalAccessSpec::new));

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
        return MKWorkspaceCodecs.parseNbt(CODEC, tag, "workspace vertical access spec");
    }

    public CompoundTag toTag() {
        return MKWorkspaceCodecs.encodeNbt(CODEC, this, "workspace vertical access spec");
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
        List<Integer> allowedHeights = MKWorkspaceDimensions.getAllowedBandHeights(stairConfig, shaftSize, 3, 3,
                MKWorkspaceDimensions.MAX_BAND_HEIGHT_EXCLUSIVE);
        if (allowedHeights.isEmpty()) {
            errors.add("vertical access profile did not produce any reusable room heights below " +
                    MKWorkspaceDimensions.MAX_BAND_HEIGHT_EXCLUSIVE);
        }
        return errors;
    }

    public boolean supportsReusableHeight(int interiorHeight) {
        MKWorkspaceStairMode mode = MKVerticalAccessProfile.normalizeMode(stairConfig.mode());
        if (mode == MKWorkspaceStairMode.LADDER || mode == MKWorkspaceStairMode.NONE) {
            return interiorHeight >= 3;
        }
        return MKResolvedVerticalAccessProfile.resolve(stairConfig, shaftSize, shaftSize, interiorHeight).isPresent();
    }

    public List<Integer> getAllowedReusableHeights(int minimumHeight, int count) {
        return MKWorkspaceDimensions.getAllowedTowerHeights(stairConfig, shaftSize, minimumHeight, count);
    }

    public int getBandCapForReusableHeight(int reusableHeight) {
        List<Integer> allowedHeights = getAllowedReusableHeights(3, 16);
        for (int allowedHeight : allowedHeights) {
            if (allowedHeight > reusableHeight) {
                return allowedHeight - 1;
            }
        }
        return reusableHeight;
    }

    public int getBandCapForRequestedHeight(int requestedHeight) {
        List<Integer> allowedHeights = getAllowedReusableHeights(3, 16);
        for (int allowedHeight : allowedHeights) {
            if (requestedHeight <= allowedHeight) {
                return getBandCapForReusableHeight(allowedHeight);
            }
        }
        return allowedHeights.isEmpty() ? requestedHeight : getBandCapForReusableHeight(allowedHeights.getLast());
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
