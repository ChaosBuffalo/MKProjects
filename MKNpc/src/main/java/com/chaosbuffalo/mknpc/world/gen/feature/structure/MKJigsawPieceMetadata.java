package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationPolicy;
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
        String topologyGroup,
        boolean mainPathEnding,
        boolean branchCap,
        MKWorkspaceFoundationPolicy foundationPolicy
) {
    public static final Codec<MKJigsawPieceMetadata> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MKJigsawPieceRole.CODEC.fieldOf("role").forGetter(MKJigsawPieceMetadata::pieceRole),
            Codec.INT.optionalFieldOf("progression_delta", 0).forGetter(MKJigsawPieceMetadata::progressionDelta),
            Codec.INT.optionalFieldOf("vertical_level_delta", 0).forGetter(MKJigsawPieceMetadata::verticalLevelDelta),
            Codec.BOOL.optionalFieldOf("allow_on_main_path", true).forGetter(MKJigsawPieceMetadata::allowOnMainPath),
            Codec.BOOL.optionalFieldOf("allow_on_branch_path", false).forGetter(MKJigsawPieceMetadata::allowOnBranchPath),
            Codec.BOOL.optionalFieldOf("terminal", false).forGetter(MKJigsawPieceMetadata::terminal),
            Codec.BOOL.optionalFieldOf("top_cap_only", false).forGetter(MKJigsawPieceMetadata::topCapOnly),
            Codec.STRING.optionalFieldOf("topology_group", "").forGetter(MKJigsawPieceMetadata::topologyGroup),
            Codec.BOOL.optionalFieldOf("main_path_ending", false).forGetter(MKJigsawPieceMetadata::mainPathEnding),
            Codec.BOOL.optionalFieldOf("branch_cap", false).forGetter(MKJigsawPieceMetadata::branchCap),
            MKWorkspaceFoundationPolicy.CODEC.optionalFieldOf("foundation_policy", MKWorkspaceFoundationPolicy.none())
                    .forGetter(MKJigsawPieceMetadata::foundationPolicy)
    ).apply(instance, MKJigsawPieceMetadata::new));

    public MKJigsawPieceMetadata(MKJigsawPieceRole pieceRole, int progressionDelta, int verticalLevelDelta,
                                 boolean allowOnMainPath, boolean allowOnBranchPath, boolean terminal,
                                 boolean topCapOnly) {
        this(pieceRole, progressionDelta, verticalLevelDelta, allowOnMainPath, allowOnBranchPath, terminal,
                topCapOnly, "", false, false, MKWorkspaceFoundationPolicy.none());
    }

    public MKJigsawPieceMetadata(MKJigsawPieceRole pieceRole, int progressionDelta, int verticalLevelDelta,
                                 boolean allowOnMainPath, boolean allowOnBranchPath, boolean terminal,
                                 boolean topCapOnly, String topologyGroup, boolean mainPathEnding) {
        this(pieceRole, progressionDelta, verticalLevelDelta, allowOnMainPath, allowOnBranchPath, terminal,
                topCapOnly, topologyGroup, mainPathEnding, false, MKWorkspaceFoundationPolicy.none());
    }

    public MKJigsawPieceMetadata(MKJigsawPieceRole pieceRole, int progressionDelta, int verticalLevelDelta,
                                 boolean allowOnMainPath, boolean allowOnBranchPath, boolean terminal,
                                 boolean topCapOnly, String topologyGroup, boolean mainPathEnding, boolean branchCap) {
        this(pieceRole, progressionDelta, verticalLevelDelta, allowOnMainPath, allowOnBranchPath, terminal,
                topCapOnly, topologyGroup, mainPathEnding, branchCap, MKWorkspaceFoundationPolicy.none());
    }
}

