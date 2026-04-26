package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MKJigsawPieceMetadata(
        MKJigsawPieceRole pieceRole,
        int progressionDelta,
        int verticalLevelDelta,
        boolean allowOnMainPath,
        boolean allowOnBranchPath,
        boolean terminal,
        boolean topCapOnly
) {
    public static final Codec<MKJigsawPieceMetadata> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MKJigsawPieceRole.CODEC.fieldOf("role").forGetter(MKJigsawPieceMetadata::pieceRole),
            Codec.INT.optionalFieldOf("progression_delta", 0).forGetter(MKJigsawPieceMetadata::progressionDelta),
            Codec.INT.optionalFieldOf("vertical_level_delta", 0).forGetter(MKJigsawPieceMetadata::verticalLevelDelta),
            Codec.BOOL.optionalFieldOf("allow_on_main_path", true).forGetter(MKJigsawPieceMetadata::allowOnMainPath),
            Codec.BOOL.optionalFieldOf("allow_on_branch_path", false).forGetter(MKJigsawPieceMetadata::allowOnBranchPath),
            Codec.BOOL.optionalFieldOf("terminal", false).forGetter(MKJigsawPieceMetadata::terminal),
            Codec.BOOL.optionalFieldOf("top_cap_only", false).forGetter(MKJigsawPieceMetadata::topCapOnly)
    ).apply(instance, MKJigsawPieceMetadata::new));
}

