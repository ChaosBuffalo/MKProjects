package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;

public class MKWorkspaceMaterialPalette {
    public static final Codec<MKWorkspaceMaterialPalette> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("floorBlock").forGetter(MKWorkspaceMaterialPalette::floorBlock),
            ResourceLocation.CODEC.fieldOf("wallBlock").forGetter(MKWorkspaceMaterialPalette::wallBlock),
            ResourceLocation.CODEC.fieldOf("ceilingBlock").forGetter(MKWorkspaceMaterialPalette::ceilingBlock)
    ).apply(instance, MKWorkspaceMaterialPalette::new));

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
}
