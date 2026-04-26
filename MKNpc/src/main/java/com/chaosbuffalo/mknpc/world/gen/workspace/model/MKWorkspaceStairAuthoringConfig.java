package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class MKWorkspaceStairAuthoringConfig {
    private static final ResourceLocation DEFAULT_STAIR_BLOCK = ResourceLocation.parse("minecraft:stone_brick_stairs");
    private static final ResourceLocation DEFAULT_SLAB_BLOCK = ResourceLocation.parse("minecraft:stone_brick_slab");
    private static final ResourceLocation DEFAULT_LADDER_BLOCK = ResourceLocation.parse("minecraft:ladder");
    public static final Codec<MKWorkspaceStairAuthoringConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MKWorkspaceCodecs.STAIR_MODE_CODEC.optionalFieldOf("mode", MKWorkspaceStairMode.AUTO)
                    .forGetter(MKWorkspaceStairAuthoringConfig::mode),
            MKWorkspaceCodecs.STAIR_RISE_TYPE_CODEC.optionalFieldOf("riseType")
                    .forGetter(config -> java.util.Optional.of(config.riseType())),
            Codec.INT.optionalFieldOf("flatRunLength", 0).forGetter(MKWorkspaceStairAuthoringConfig::flatRunLength),
            Codec.INT.optionalFieldOf("stairWidth", 1).forGetter(MKWorkspaceStairAuthoringConfig::stairWidth),
            ResourceLocation.CODEC.optionalFieldOf("stairBlock", DEFAULT_STAIR_BLOCK)
                    .forGetter(MKWorkspaceStairAuthoringConfig::stairBlock),
            ResourceLocation.CODEC.optionalFieldOf("slabBlock", DEFAULT_SLAB_BLOCK)
                    .forGetter(MKWorkspaceStairAuthoringConfig::slabBlock),
            ResourceLocation.CODEC.optionalFieldOf("ladderBlock", DEFAULT_LADDER_BLOCK)
                    .forGetter(MKWorkspaceStairAuthoringConfig::ladderBlock)
    ).apply(instance, (mode, riseType, flatRunLength, stairWidth, stairBlock, slabBlock, ladderBlock) ->
            new MKWorkspaceStairAuthoringConfig(
                    mode,
                    riseType.orElse(inferLegacyRiseType(mode)),
                    flatRunLength,
                    Math.max(1, stairWidth),
                    stairBlock,
                    slabBlock,
                    ladderBlock
            )));

    private final MKWorkspaceStairMode mode;
    private final MKWorkspaceStairRiseType riseType;
    private final int flatRunLength;
    private final int stairWidth;
    private final ResourceLocation stairBlock;
    private final ResourceLocation slabBlock;
    private final ResourceLocation ladderBlock;

    public MKWorkspaceStairAuthoringConfig(MKWorkspaceStairMode mode, MKWorkspaceStairRiseType riseType,
                                           int flatRunLength, int stairWidth, ResourceLocation stairBlock,
                                           ResourceLocation slabBlock, ResourceLocation ladderBlock) {
        this.mode = mode;
        this.riseType = riseType;
        this.flatRunLength = flatRunLength;
        this.stairWidth = stairWidth;
        this.stairBlock = stairBlock;
        this.slabBlock = slabBlock;
        this.ladderBlock = ladderBlock;
    }

    public static MKWorkspaceStairAuthoringConfig defaultConfig() {
        return new MKWorkspaceStairAuthoringConfig(
                MKWorkspaceStairMode.AUTO,
                MKWorkspaceStairRiseType.STAIR,
                0,
                1,
                DEFAULT_STAIR_BLOCK,
                DEFAULT_SLAB_BLOCK,
                DEFAULT_LADDER_BLOCK
        );
    }

    public static MKWorkspaceStairAuthoringConfig fromTag(CompoundTag tag) {
        return MKWorkspaceCodecs.parseNbt(CODEC, tag, "workspace stair authoring config");
    }

    public CompoundTag toTag() {
        return MKWorkspaceCodecs.encodeNbt(CODEC, this, "workspace stair authoring config");
    }

    private static MKWorkspaceStairRiseType inferLegacyRiseType(MKWorkspaceStairMode mode) {
        return mode == MKWorkspaceStairMode.SLAB_STAIRS ? MKWorkspaceStairRiseType.SLAB : MKWorkspaceStairRiseType.STAIR;
    }

    public MKWorkspaceStairMode mode() {
        return mode;
    }

    public MKWorkspaceStairRiseType riseType() {
        return riseType;
    }

    public int flatRunLength() {
        return flatRunLength;
    }

    public int stairWidth() {
        return stairWidth;
    }

    public ResourceLocation stairBlock() {
        return stairBlock;
    }

    public ResourceLocation slabBlock() {
        return slabBlock;
    }

    public ResourceLocation ladderBlock() {
        return ladderBlock;
    }
}
