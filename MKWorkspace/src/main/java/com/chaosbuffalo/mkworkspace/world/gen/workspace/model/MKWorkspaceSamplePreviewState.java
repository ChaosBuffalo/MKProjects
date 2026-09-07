package com.chaosbuffalo.mkworkspace.world.gen.workspace.model;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public record MKWorkspaceSamplePreviewState(
        BlockPos origin,
        BoundingBox bounds,
        long seed,
        boolean seedLocked,
        long generatedAt,
        int placedPieceCount,
        int templateFallbackCount
) {
    public static final Codec<MKWorkspaceSamplePreviewState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MKWorkspaceCodecs.BLOCK_POS_CODEC.fieldOf("origin").forGetter(MKWorkspaceSamplePreviewState::origin),
            MKWorkspaceCodecs.BOUNDING_BOX_CODEC.fieldOf("bounds").forGetter(MKWorkspaceSamplePreviewState::bounds),
            Codec.LONG.fieldOf("seed").forGetter(MKWorkspaceSamplePreviewState::seed),
            Codec.BOOL.fieldOf("seedLocked").forGetter(MKWorkspaceSamplePreviewState::seedLocked),
            Codec.LONG.fieldOf("generatedAt").forGetter(MKWorkspaceSamplePreviewState::generatedAt),
            Codec.INT.fieldOf("placedPieceCount").forGetter(MKWorkspaceSamplePreviewState::placedPieceCount),
            Codec.INT.fieldOf("templateFallbackCount").forGetter(MKWorkspaceSamplePreviewState::templateFallbackCount)
    ).apply(instance, MKWorkspaceSamplePreviewState::new));

    public static MKWorkspaceSamplePreviewState fromTag(CompoundTag tag) {
        return MKWorkspaceCodecs.parseNbt(CODEC, tag, "workspace sample preview state");
    }

    public CompoundTag toTag() {
        return MKWorkspaceCodecs.encodeNbt(CODEC, this, "workspace sample preview state");
    }

    public MKWorkspaceSamplePreviewState withSeedLock(boolean seedLocked) {
        return new MKWorkspaceSamplePreviewState(origin, bounds, seed, seedLocked, generatedAt, placedPieceCount,
                templateFallbackCount);
    }
}
