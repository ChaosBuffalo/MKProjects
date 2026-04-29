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
        boolean topCapOnly,
        String category,
        boolean mainPathEnding,
        boolean branchCap
) {
    public static final Codec<MKJigsawPieceMetadata> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MKJigsawPieceRole.CODEC.fieldOf("role").forGetter(MKJigsawPieceMetadata::pieceRole),
            Codec.INT.optionalFieldOf("progression_delta", 0).forGetter(MKJigsawPieceMetadata::progressionDelta),
            Codec.INT.optionalFieldOf("vertical_level_delta", 0).forGetter(MKJigsawPieceMetadata::verticalLevelDelta),
            Codec.BOOL.optionalFieldOf("allow_on_main_path", true).forGetter(MKJigsawPieceMetadata::allowOnMainPath),
            Codec.BOOL.optionalFieldOf("allow_on_branch_path", false).forGetter(MKJigsawPieceMetadata::allowOnBranchPath),
            Codec.BOOL.optionalFieldOf("terminal", false).forGetter(MKJigsawPieceMetadata::terminal),
            Codec.BOOL.optionalFieldOf("top_cap_only", false).forGetter(MKJigsawPieceMetadata::topCapOnly),
            Codec.STRING.optionalFieldOf("category", "").forGetter(MKJigsawPieceMetadata::category),
            Codec.BOOL.optionalFieldOf("main_path_ending", false).forGetter(MKJigsawPieceMetadata::mainPathEnding),
            Codec.BOOL.optionalFieldOf("branch_cap", false).forGetter(MKJigsawPieceMetadata::branchCap)
    ).apply(instance, MKJigsawPieceMetadata::new));

    public MKJigsawPieceMetadata(MKJigsawPieceRole pieceRole, int progressionDelta, int verticalLevelDelta,
                                 boolean allowOnMainPath, boolean allowOnBranchPath, boolean terminal,
                                 boolean topCapOnly) {
        this(pieceRole, progressionDelta, verticalLevelDelta, allowOnMainPath, allowOnBranchPath, terminal,
                topCapOnly, "", false, false);
    }

    public MKJigsawPieceMetadata(MKJigsawPieceRole pieceRole, int progressionDelta, int verticalLevelDelta,
                                 boolean allowOnMainPath, boolean allowOnBranchPath, boolean terminal,
                                 boolean topCapOnly, String category, boolean mainPathEnding) {
        this(pieceRole, progressionDelta, verticalLevelDelta, allowOnMainPath, allowOnBranchPath, terminal,
                topCapOnly, category, mainPathEnding, false);
    }
}

