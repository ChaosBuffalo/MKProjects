package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Optional;

public record MKWorkspacePaletteOverride(
        @Nullable ResourceLocation floorBlock,
        @Nullable ResourceLocation wallBlock,
        @Nullable ResourceLocation ceilingBlock,
        @Nullable ResourceLocation stairBlock,
        @Nullable ResourceLocation slabBlock,
        @Nullable ResourceLocation ladderBlock
) {
    public static final MKWorkspacePaletteOverride EMPTY = new MKWorkspacePaletteOverride(
            null,
            null,
            null,
            null,
            null,
            null
    );

    public static final Codec<MKWorkspacePaletteOverride> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("floorBlock").forGetter(MKWorkspacePaletteOverride::floorBlockOpt),
            ResourceLocation.CODEC.optionalFieldOf("wallBlock").forGetter(MKWorkspacePaletteOverride::wallBlockOpt),
            ResourceLocation.CODEC.optionalFieldOf("ceilingBlock").forGetter(MKWorkspacePaletteOverride::ceilingBlockOpt),
            ResourceLocation.CODEC.optionalFieldOf("stairBlock").forGetter(MKWorkspacePaletteOverride::stairBlockOpt),
            ResourceLocation.CODEC.optionalFieldOf("slabBlock").forGetter(MKWorkspacePaletteOverride::slabBlockOpt),
            ResourceLocation.CODEC.optionalFieldOf("ladderBlock").forGetter(MKWorkspacePaletteOverride::ladderBlockOpt)
    ).apply(instance, (floorBlock, wallBlock, ceilingBlock, stairBlock, slabBlock, ladderBlock) ->
            new MKWorkspacePaletteOverride(
                    floorBlock.orElse(null),
                    wallBlock.orElse(null),
                    ceilingBlock.orElse(null),
                    stairBlock.orElse(null),
                    slabBlock.orElse(null),
                    ladderBlock.orElse(null)
            )));

    public static MKWorkspacePaletteOverride of(ResourceLocation floorBlock, ResourceLocation wallBlock,
                                                ResourceLocation ceilingBlock) {
        return new MKWorkspacePaletteOverride(
                floorBlock,
                wallBlock,
                ceilingBlock,
                null,
                null,
                null
        );
    }

    public static MKWorkspacePaletteOverride of(ResourceLocation floorBlock, ResourceLocation wallBlock,
                                                ResourceLocation ceilingBlock, ResourceLocation stairBlock,
                                                ResourceLocation slabBlock, ResourceLocation ladderBlock) {
        return new MKWorkspacePaletteOverride(
                floorBlock,
                wallBlock,
                ceilingBlock,
                stairBlock,
                slabBlock,
                ladderBlock
        );
    }

    public boolean isEmpty() {
        return floorBlock == null && wallBlock == null && ceilingBlock == null &&
                stairBlock == null && slabBlock == null && ladderBlock == null;
    }

    public MKWorkspaceMaterialPalette resolve(MKWorkspaceMaterialPalette fallback) {
        return new MKWorkspaceMaterialPalette(
                floorBlock != null ? floorBlock : fallback.floorBlock(),
                wallBlock != null ? wallBlock : fallback.wallBlock(),
                ceilingBlock != null ? ceilingBlock : fallback.ceilingBlock(),
                stairBlock != null ? stairBlock : fallback.stairBlock(),
                slabBlock != null ? slabBlock : fallback.slabBlock(),
                ladderBlock != null ? ladderBlock : fallback.ladderBlock()
        );
    }

    public Optional<ResourceLocation> floorBlockOpt() {
        return Optional.ofNullable(floorBlock);
    }

    public Optional<ResourceLocation> wallBlockOpt() {
        return Optional.ofNullable(wallBlock);
    }

    public Optional<ResourceLocation> ceilingBlockOpt() {
        return Optional.ofNullable(ceilingBlock);
    }

    public Optional<ResourceLocation> stairBlockOpt() {
        return Optional.ofNullable(stairBlock);
    }

    public Optional<ResourceLocation> slabBlockOpt() {
        return Optional.ofNullable(slabBlock);
    }

    public Optional<ResourceLocation> ladderBlockOpt() {
        return Optional.ofNullable(ladderBlock);
    }

    public MKWorkspacePaletteOverride withFloorBlock(@Nullable ResourceLocation value) {
        return new MKWorkspacePaletteOverride(value, wallBlock, ceilingBlock, stairBlock, slabBlock, ladderBlock);
    }

    public MKWorkspacePaletteOverride withWallBlock(@Nullable ResourceLocation value) {
        return new MKWorkspacePaletteOverride(floorBlock, value, ceilingBlock, stairBlock, slabBlock, ladderBlock);
    }

    public MKWorkspacePaletteOverride withCeilingBlock(@Nullable ResourceLocation value) {
        return new MKWorkspacePaletteOverride(floorBlock, wallBlock, value, stairBlock, slabBlock, ladderBlock);
    }

    public MKWorkspacePaletteOverride withStairBlock(@Nullable ResourceLocation value) {
        return new MKWorkspacePaletteOverride(floorBlock, wallBlock, ceilingBlock, value, slabBlock, ladderBlock);
    }

    public MKWorkspacePaletteOverride withSlabBlock(@Nullable ResourceLocation value) {
        return new MKWorkspacePaletteOverride(floorBlock, wallBlock, ceilingBlock, stairBlock, value, ladderBlock);
    }

    public MKWorkspacePaletteOverride withLadderBlock(@Nullable ResourceLocation value) {
        return new MKWorkspacePaletteOverride(floorBlock, wallBlock, ceilingBlock, stairBlock, slabBlock, value);
    }
}
