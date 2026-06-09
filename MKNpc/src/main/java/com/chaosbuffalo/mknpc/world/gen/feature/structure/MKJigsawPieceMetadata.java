package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationPolicy;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
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
        String towerStackId,
        String towerStackSlot,
        int minMainFloors,
        int maxMainFloors,
        int minBasementFloors,
        int maxBasementFloors,
        boolean topCapApproachEnabled,
        boolean basementEntryEnabled,
        boolean basementCapApproachEnabled,
        String floorExitMask,
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
            TowerStackMetadata.CODEC.forGetter(MKJigsawPieceMetadata::towerStackMetadata),
            Codec.STRING.optionalFieldOf("floor_exit_mask", "").forGetter(MKJigsawPieceMetadata::floorExitMask),
            MKWorkspaceFoundationPolicy.CODEC.optionalFieldOf("foundation_policy", MKWorkspaceFoundationPolicy.none())
                    .forGetter(MKJigsawPieceMetadata::foundationPolicy)
    ).apply(instance, (pieceRole, progressionDelta, verticalLevelDelta, allowOnMainPath, allowOnBranchPath,
                       terminal, topCapOnly, topologyGroup, mainPathEnding, branchCap, towerStackMetadata,
                       floorExitMask, foundationPolicy) ->
            new MKJigsawPieceMetadata(pieceRole, progressionDelta, verticalLevelDelta, allowOnMainPath,
                    allowOnBranchPath, terminal, topCapOnly, topologyGroup, mainPathEnding, branchCap,
                    towerStackMetadata.towerStackId(), towerStackMetadata.towerStackSlot(),
                    towerStackMetadata.minMainFloors(), towerStackMetadata.maxMainFloors(),
                    towerStackMetadata.minBasementFloors(), towerStackMetadata.maxBasementFloors(),
                    towerStackMetadata.topCapApproachEnabled(), towerStackMetadata.basementEntryEnabled(),
                    towerStackMetadata.basementCapApproachEnabled(), floorExitMask, foundationPolicy)));

    public MKJigsawPieceMetadata {
        floorExitMask = floorExitMask == null ? "" : floorExitMask;
    }

    public MKJigsawPieceMetadata(MKJigsawPieceRole pieceRole, int progressionDelta, int verticalLevelDelta,
                                 boolean allowOnMainPath, boolean allowOnBranchPath, boolean terminal,
                                 boolean topCapOnly) {
        this(pieceRole, progressionDelta, verticalLevelDelta, allowOnMainPath, allowOnBranchPath, terminal,
                topCapOnly, "", false, false, "", "", 0, 0, 0, 0, true, true, false,
                "", MKWorkspaceFoundationPolicy.none());
    }

    public MKJigsawPieceMetadata(MKJigsawPieceRole pieceRole, int progressionDelta, int verticalLevelDelta,
                                 boolean allowOnMainPath, boolean allowOnBranchPath, boolean terminal,
                                 boolean topCapOnly, String topologyGroup, boolean mainPathEnding) {
        this(pieceRole, progressionDelta, verticalLevelDelta, allowOnMainPath, allowOnBranchPath, terminal,
                topCapOnly, topologyGroup, mainPathEnding, false, "", "", 0, 0, 0, 0, true, true, false,
                "", MKWorkspaceFoundationPolicy.none());
    }

    public MKJigsawPieceMetadata(MKJigsawPieceRole pieceRole, int progressionDelta, int verticalLevelDelta,
                                 boolean allowOnMainPath, boolean allowOnBranchPath, boolean terminal,
                                 boolean topCapOnly, String topologyGroup, boolean mainPathEnding, boolean branchCap) {
        this(pieceRole, progressionDelta, verticalLevelDelta, allowOnMainPath, allowOnBranchPath, terminal,
                topCapOnly, topologyGroup, mainPathEnding, branchCap, "", "", 0, 0, 0, 0, true, true, false,
                "", MKWorkspaceFoundationPolicy.none());
    }

    public MKJigsawPieceMetadata(MKJigsawPieceRole pieceRole, int progressionDelta, int verticalLevelDelta,
                                 boolean allowOnMainPath, boolean allowOnBranchPath, boolean terminal,
                                 boolean topCapOnly, String topologyGroup, boolean mainPathEnding,
                                 boolean branchCap, MKWorkspaceFoundationPolicy foundationPolicy) {
        this(pieceRole, progressionDelta, verticalLevelDelta, allowOnMainPath, allowOnBranchPath, terminal,
                topCapOnly, topologyGroup, mainPathEnding, branchCap, "", "", 0, 0, 0, 0, true, true, false,
                "", foundationPolicy);
    }

    public MKJigsawPieceMetadata(MKJigsawPieceRole pieceRole, int progressionDelta, int verticalLevelDelta,
                                 boolean allowOnMainPath, boolean allowOnBranchPath, boolean terminal,
                                 boolean topCapOnly, String topologyGroup, boolean mainPathEnding,
                                 boolean branchCap, String towerStackId, String towerStackSlot,
                                 int minMainFloors, int maxMainFloors, int minBasementFloors, int maxBasementFloors,
                                 boolean topCapApproachEnabled, boolean basementEntryEnabled,
                                 boolean basementCapApproachEnabled, MKWorkspaceFoundationPolicy foundationPolicy) {
        this(pieceRole, progressionDelta, verticalLevelDelta, allowOnMainPath, allowOnBranchPath, terminal,
                topCapOnly, topologyGroup, mainPathEnding, branchCap, towerStackId, towerStackSlot, minMainFloors,
                maxMainFloors, minBasementFloors, maxBasementFloors, topCapApproachEnabled, basementEntryEnabled,
                basementCapApproachEnabled, "", foundationPolicy);
    }

    public boolean hasTowerStackLayout() {
        return towerStackId != null && !towerStackId.isBlank() &&
                towerStackSlot != null && !towerStackSlot.isBlank();
    }

    private TowerStackMetadata towerStackMetadata() {
        return new TowerStackMetadata(towerStackId, towerStackSlot, minMainFloors, maxMainFloors,
                minBasementFloors, maxBasementFloors, topCapApproachEnabled, basementEntryEnabled,
                basementCapApproachEnabled);
    }

    private record TowerStackMetadata(
            String towerStackId,
            String towerStackSlot,
            int minMainFloors,
            int maxMainFloors,
            int minBasementFloors,
            int maxBasementFloors,
            boolean topCapApproachEnabled,
            boolean basementEntryEnabled,
            boolean basementCapApproachEnabled
    ) {
        private static final MapCodec<TowerStackMetadata> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("tower_stack_id", "").forGetter(TowerStackMetadata::towerStackId),
                Codec.STRING.optionalFieldOf("tower_stack_slot", "").forGetter(TowerStackMetadata::towerStackSlot),
                Codec.INT.optionalFieldOf("min_main_floors", 0).forGetter(TowerStackMetadata::minMainFloors),
                Codec.INT.optionalFieldOf("max_main_floors", 0).forGetter(TowerStackMetadata::maxMainFloors),
                Codec.INT.optionalFieldOf("min_basement_floors", 0).forGetter(TowerStackMetadata::minBasementFloors),
                Codec.INT.optionalFieldOf("max_basement_floors", 0).forGetter(TowerStackMetadata::maxBasementFloors),
                Codec.BOOL.optionalFieldOf("top_cap_approach_enabled", true)
                        .forGetter(TowerStackMetadata::topCapApproachEnabled),
                Codec.BOOL.optionalFieldOf("basement_entry_enabled", true)
                        .forGetter(TowerStackMetadata::basementEntryEnabled),
                Codec.BOOL.optionalFieldOf("basement_cap_approach_enabled", false)
                        .forGetter(TowerStackMetadata::basementCapApproachEnabled)
        ).apply(instance, TowerStackMetadata::new));
    }
}

