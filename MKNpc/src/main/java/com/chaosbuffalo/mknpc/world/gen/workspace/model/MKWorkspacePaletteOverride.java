package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record MKWorkspacePaletteOverride(
        Optional<ResourceLocation> floorBlock,
        Optional<ResourceLocation> wallBlock,
        Optional<ResourceLocation> ceilingBlock,
        Optional<ResourceLocation> stairBlock,
        Optional<ResourceLocation> slabBlock,
        Optional<ResourceLocation> ladderBlock
) {
    public static final MKWorkspacePaletteOverride EMPTY = new MKWorkspacePaletteOverride(
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
    );

    public static final Codec<MKWorkspacePaletteOverride> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("floorBlock").forGetter(MKWorkspacePaletteOverride::floorBlock),
            ResourceLocation.CODEC.optionalFieldOf("wallBlock").forGetter(MKWorkspacePaletteOverride::wallBlock),
            ResourceLocation.CODEC.optionalFieldOf("ceilingBlock").forGetter(MKWorkspacePaletteOverride::ceilingBlock),
            ResourceLocation.CODEC.optionalFieldOf("stairBlock").forGetter(MKWorkspacePaletteOverride::stairBlock),
            ResourceLocation.CODEC.optionalFieldOf("slabBlock").forGetter(MKWorkspacePaletteOverride::slabBlock),
            ResourceLocation.CODEC.optionalFieldOf("ladderBlock").forGetter(MKWorkspacePaletteOverride::ladderBlock)
    ).apply(instance, MKWorkspacePaletteOverride::new));

    public static MKWorkspacePaletteOverride of(ResourceLocation floorBlock, ResourceLocation wallBlock,
                                                ResourceLocation ceilingBlock) {
        return new MKWorkspacePaletteOverride(
                Optional.of(floorBlock),
                Optional.of(wallBlock),
                Optional.of(ceilingBlock),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
    }

    public static MKWorkspacePaletteOverride of(ResourceLocation floorBlock, ResourceLocation wallBlock,
                                                ResourceLocation ceilingBlock, ResourceLocation stairBlock,
                                                ResourceLocation slabBlock, ResourceLocation ladderBlock) {
        return new MKWorkspacePaletteOverride(
                Optional.of(floorBlock),
                Optional.of(wallBlock),
                Optional.of(ceilingBlock),
                Optional.of(stairBlock),
                Optional.of(slabBlock),
                Optional.of(ladderBlock)
        );
    }

    public boolean isEmpty() {
        return floorBlock.isEmpty() && wallBlock.isEmpty() && ceilingBlock.isEmpty() &&
                stairBlock.isEmpty() && slabBlock.isEmpty() && ladderBlock.isEmpty();
    }

    public MKWorkspaceMaterialPalette resolve(MKWorkspaceMaterialPalette fallback) {
        return new MKWorkspaceMaterialPalette(
                floorBlock.orElse(fallback.floorBlock()),
                wallBlock.orElse(fallback.wallBlock()),
                ceilingBlock.orElse(fallback.ceilingBlock()),
                stairBlock.orElse(fallback.stairBlock()),
                slabBlock.orElse(fallback.slabBlock()),
                ladderBlock.orElse(fallback.ladderBlock())
        );
    }

    public MKWorkspacePaletteOverride withFloorBlock(Optional<ResourceLocation> value) {
        return new MKWorkspacePaletteOverride(value, wallBlock, ceilingBlock, stairBlock, slabBlock, ladderBlock);
    }

    public MKWorkspacePaletteOverride withWallBlock(Optional<ResourceLocation> value) {
        return new MKWorkspacePaletteOverride(floorBlock, value, ceilingBlock, stairBlock, slabBlock, ladderBlock);
    }

    public MKWorkspacePaletteOverride withCeilingBlock(Optional<ResourceLocation> value) {
        return new MKWorkspacePaletteOverride(floorBlock, wallBlock, value, stairBlock, slabBlock, ladderBlock);
    }

    public MKWorkspacePaletteOverride withStairBlock(Optional<ResourceLocation> value) {
        return new MKWorkspacePaletteOverride(floorBlock, wallBlock, ceilingBlock, value, slabBlock, ladderBlock);
    }

    public MKWorkspacePaletteOverride withSlabBlock(Optional<ResourceLocation> value) {
        return new MKWorkspacePaletteOverride(floorBlock, wallBlock, ceilingBlock, stairBlock, value, ladderBlock);
    }

    public MKWorkspacePaletteOverride withLadderBlock(Optional<ResourceLocation> value) {
        return new MKWorkspacePaletteOverride(floorBlock, wallBlock, ceilingBlock, stairBlock, slabBlock, value);
    }
}
