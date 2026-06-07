package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;

public class MKWorkspaceStairAuthoringConfig {
    public static final Codec<MKWorkspaceStairAuthoringConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MKWorkspaceCodecs.STAIR_MODE_CODEC.optionalFieldOf("mode", MKWorkspaceStairMode.AUTO)
                    .forGetter(MKWorkspaceStairAuthoringConfig::mode),
            MKWorkspaceCodecs.STAIR_RISE_TYPE_CODEC.optionalFieldOf("riseType", MKWorkspaceStairRiseType.MIXED)
                    .forGetter(MKWorkspaceStairAuthoringConfig::riseType),
            Codec.INT.optionalFieldOf("stairWidth", 1).forGetter(MKWorkspaceStairAuthoringConfig::stairWidth)
    ).apply(instance, (mode, riseType, stairWidth) ->
            new MKWorkspaceStairAuthoringConfig(
                    mode,
                    normalizeRiseType(mode, riseType),
                    Math.max(1, stairWidth)
            )));

    private final MKWorkspaceStairMode mode;
    private final MKWorkspaceStairRiseType riseType;
    private final int stairWidth;

    public MKWorkspaceStairAuthoringConfig(MKWorkspaceStairMode mode, MKWorkspaceStairRiseType riseType,
                                           int stairWidth) {
        this.mode = mode;
        this.riseType = normalizeRiseType(mode, riseType);
        this.stairWidth = stairWidth;
    }

    public static MKWorkspaceStairAuthoringConfig defaultConfig() {
        return new MKWorkspaceStairAuthoringConfig(
                MKWorkspaceStairMode.AUTO,
                MKWorkspaceStairRiseType.MIXED,
                1
        );
    }

    public static MKWorkspaceStairAuthoringConfig fromTag(CompoundTag tag) {
        return MKWorkspaceCodecs.parseNbt(CODEC, tag, "workspace stair authoring config");
    }

    public CompoundTag toTag() {
        return MKWorkspaceCodecs.encodeNbt(CODEC, this, "workspace stair authoring config");
    }

    private static MKWorkspaceStairRiseType normalizeRiseType(MKWorkspaceStairMode mode, MKWorkspaceStairRiseType riseType) {
        if (mode == MKWorkspaceStairMode.SLAB_STAIRS) {
            return MKWorkspaceStairRiseType.SLAB;
        }
        if (mode == MKWorkspaceStairMode.STAIR_STAIRS) {
            return MKWorkspaceStairRiseType.STAIR;
        }
        return riseType == null ? MKWorkspaceStairRiseType.MIXED : riseType;
    }

    public MKWorkspaceStairMode mode() {
        return mode;
    }

    public MKWorkspaceStairRiseType riseType() {
        return riseType;
    }

    public int stairWidth() {
        return stairWidth;
    }
}
