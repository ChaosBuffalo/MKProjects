package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;

public class MKWorkspaceMaterialPalette {
    private static final ResourceLocation DEFAULT_STAIR_BLOCK = ResourceLocation.parse("minecraft:stone_brick_stairs");
    private static final ResourceLocation DEFAULT_SLAB_BLOCK = ResourceLocation.parse("minecraft:stone_brick_slab");
    private static final ResourceLocation DEFAULT_LADDER_BLOCK = ResourceLocation.parse("minecraft:ladder");

    public static final Codec<MKWorkspaceMaterialPalette> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("floorBlock").forGetter(MKWorkspaceMaterialPalette::floorBlock),
            ResourceLocation.CODEC.fieldOf("wallBlock").forGetter(MKWorkspaceMaterialPalette::wallBlock),
            ResourceLocation.CODEC.fieldOf("ceilingBlock").forGetter(MKWorkspaceMaterialPalette::ceilingBlock),
            ResourceLocation.CODEC.optionalFieldOf("stairBlock", DEFAULT_STAIR_BLOCK)
                    .forGetter(MKWorkspaceMaterialPalette::stairBlock),
            ResourceLocation.CODEC.optionalFieldOf("slabBlock", DEFAULT_SLAB_BLOCK)
                    .forGetter(MKWorkspaceMaterialPalette::slabBlock),
            ResourceLocation.CODEC.optionalFieldOf("ladderBlock", DEFAULT_LADDER_BLOCK)
                    .forGetter(MKWorkspaceMaterialPalette::ladderBlock)
    ).apply(instance, MKWorkspaceMaterialPalette::new));

    private final ResourceLocation floorBlock;
    private final ResourceLocation wallBlock;
    private final ResourceLocation ceilingBlock;
    private final ResourceLocation stairBlock;
    private final ResourceLocation slabBlock;
    private final ResourceLocation ladderBlock;

    public MKWorkspaceMaterialPalette(ResourceLocation floorBlock, ResourceLocation wallBlock, ResourceLocation ceilingBlock) {
        this(floorBlock, wallBlock, ceilingBlock, DEFAULT_STAIR_BLOCK, DEFAULT_SLAB_BLOCK, DEFAULT_LADDER_BLOCK);
    }

    public MKWorkspaceMaterialPalette(ResourceLocation floorBlock, ResourceLocation wallBlock, ResourceLocation ceilingBlock,
                                      ResourceLocation stairBlock, ResourceLocation slabBlock,
                                      ResourceLocation ladderBlock) {
        this.floorBlock = floorBlock;
        this.wallBlock = wallBlock;
        this.ceilingBlock = ceilingBlock;
        this.stairBlock = stairBlock;
        this.slabBlock = slabBlock;
        this.ladderBlock = ladderBlock;
    }

    public static MKWorkspaceMaterialPalette defaultPalette() {
        ResourceLocation stoneBricks = ResourceLocation.parse("minecraft:stone_bricks");
        ResourceLocation smoothStone = ResourceLocation.parse("minecraft:smooth_stone");
        return new MKWorkspaceMaterialPalette(smoothStone, stoneBricks, smoothStone,
                DEFAULT_STAIR_BLOCK, DEFAULT_SLAB_BLOCK, DEFAULT_LADDER_BLOCK);
    }

    public static MKWorkspaceMaterialPalette fromTag(CompoundTag tag) {
        return MKWorkspaceCodecs.parseNbt(CODEC, tag, "workspace material palette");
    }

    public CompoundTag toTag() {
        return MKWorkspaceCodecs.encodeNbt(CODEC, this, "workspace material palette");
    }

    public ResourceLocation floorBlock() {
        return floorBlock;
    }

    public ResourceLocation wallBlock() {
        return wallBlock;
    }

    public ResourceLocation ceilingBlock() {
        return ceilingBlock;
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
