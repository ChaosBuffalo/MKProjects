package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class MKWorkspaceStairAuthoringConfig {
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
                ResourceLocation.parse("minecraft:stone_brick_stairs"),
                ResourceLocation.parse("minecraft:stone_brick_slab"),
                ResourceLocation.parse("minecraft:ladder")
        );
    }

    public static MKWorkspaceStairAuthoringConfig fromTag(CompoundTag tag) {
        if (tag.isEmpty()) {
            return defaultConfig();
        }
        MKWorkspaceStairMode mode = MKWorkspaceStairMode.fromSerializedName(tag.getString("mode"));
        MKWorkspaceStairRiseType riseType = tag.contains("riseType")
                ? MKWorkspaceStairRiseType.fromSerializedName(tag.getString("riseType"))
                : inferLegacyRiseType(mode);
        return new MKWorkspaceStairAuthoringConfig(
                mode,
                riseType,
                tag.contains("flatRunLength") ? tag.getInt("flatRunLength") : 0,
                tag.contains("stairWidth") ? Math.max(1, tag.getInt("stairWidth")) : 1,
                ResourceLocation.parse(tag.getString("stairBlock")),
                ResourceLocation.parse(tag.getString("slabBlock")),
                ResourceLocation.parse(tag.getString("ladderBlock"))
        );
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("mode", mode.getSerializedName());
        tag.putString("riseType", riseType.getSerializedName());
        tag.putInt("flatRunLength", flatRunLength);
        tag.putInt("stairWidth", stairWidth);
        tag.putString("stairBlock", stairBlock.toString());
        tag.putString("slabBlock", slabBlock.toString());
        tag.putString("ladderBlock", ladderBlock.toString());
        return tag;
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
