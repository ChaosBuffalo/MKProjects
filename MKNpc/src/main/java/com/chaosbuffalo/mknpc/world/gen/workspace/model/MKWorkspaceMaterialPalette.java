package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;

public class MKWorkspaceMaterialPalette {
    private final ResourceLocation floorBlock;
    private final ResourceLocation wallBlock;
    private final ResourceLocation ceilingBlock;

    public MKWorkspaceMaterialPalette(ResourceLocation floorBlock, ResourceLocation wallBlock, ResourceLocation ceilingBlock) {
        this.floorBlock = floorBlock;
        this.wallBlock = wallBlock;
        this.ceilingBlock = ceilingBlock;
    }

    public static MKWorkspaceMaterialPalette defaultPalette() {
        ResourceLocation stoneBricks = ResourceLocation.parse("minecraft:stone_bricks");
        ResourceLocation smoothStone = ResourceLocation.parse("minecraft:smooth_stone");
        return new MKWorkspaceMaterialPalette(smoothStone, stoneBricks, smoothStone);
    }

    public static MKWorkspaceMaterialPalette fromTag(CompoundTag tag) {
        return new MKWorkspaceMaterialPalette(
                ResourceLocation.parse(tag.getString("floorBlock")),
                ResourceLocation.parse(tag.getString("wallBlock")),
                ResourceLocation.parse(tag.getString("ceilingBlock"))
        );
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("floorBlock", floorBlock.toString());
        tag.putString("wallBlock", wallBlock.toString());
        tag.putString("ceilingBlock", ceilingBlock.toString());
        return tag;
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
}
