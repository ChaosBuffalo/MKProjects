package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationPolicy;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;

import java.util.List;

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
        MKWorkspaceFoundationPolicy foundationPolicy,
        List<FloorLinkCandidate> floorLinkCandidates
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
                    .forGetter(MKJigsawPieceMetadata::foundationPolicy),
            FloorLinkCandidate.CODEC.listOf().optionalFieldOf("floor_link_candidates", List.of())
                    .forGetter(MKJigsawPieceMetadata::floorLinkCandidates)
    ).apply(instance, (pieceRole, progressionDelta, verticalLevelDelta, allowOnMainPath, allowOnBranchPath,
                       terminal, topCapOnly, topologyGroup, mainPathEnding, branchCap, towerStackMetadata,
                       floorExitMask, foundationPolicy, floorLinkCandidates) ->
            new MKJigsawPieceMetadata(pieceRole, progressionDelta, verticalLevelDelta, allowOnMainPath,
                    allowOnBranchPath, terminal, topCapOnly, topologyGroup, mainPathEnding, branchCap,
                    towerStackMetadata.towerStackId(), towerStackMetadata.towerStackSlot(),
                    towerStackMetadata.minMainFloors(), towerStackMetadata.maxMainFloors(),
                    towerStackMetadata.minBasementFloors(), towerStackMetadata.maxBasementFloors(),
                    towerStackMetadata.topCapApproachEnabled(), towerStackMetadata.basementEntryEnabled(),
                    towerStackMetadata.basementCapApproachEnabled(), floorExitMask, foundationPolicy,
                    floorLinkCandidates)));

    public MKJigsawPieceMetadata {
        floorExitMask = floorExitMask == null ? "" : floorExitMask;
        floorLinkCandidates = floorLinkCandidates == null ? List.of() : List.copyOf(floorLinkCandidates);
    }

    public MKJigsawPieceMetadata(MKJigsawPieceRole pieceRole, int progressionDelta, int verticalLevelDelta,
                                 boolean allowOnMainPath, boolean allowOnBranchPath, boolean terminal,
                                 boolean topCapOnly) {
        this(pieceRole, progressionDelta, verticalLevelDelta, allowOnMainPath, allowOnBranchPath, terminal,
                topCapOnly, "", false, false, "", "", 0, 0, 0, 0, true, true, false,
                "", MKWorkspaceFoundationPolicy.none(), List.of());
    }

    public MKJigsawPieceMetadata(MKJigsawPieceRole pieceRole, int progressionDelta, int verticalLevelDelta,
                                 boolean allowOnMainPath, boolean allowOnBranchPath, boolean terminal,
                                 boolean topCapOnly, String topologyGroup, boolean mainPathEnding) {
        this(pieceRole, progressionDelta, verticalLevelDelta, allowOnMainPath, allowOnBranchPath, terminal,
                topCapOnly, topologyGroup, mainPathEnding, false, "", "", 0, 0, 0, 0, true, true, false,
                "", MKWorkspaceFoundationPolicy.none(), List.of());
    }

    public MKJigsawPieceMetadata(MKJigsawPieceRole pieceRole, int progressionDelta, int verticalLevelDelta,
                                 boolean allowOnMainPath, boolean allowOnBranchPath, boolean terminal,
                                 boolean topCapOnly, String topologyGroup, boolean mainPathEnding, boolean branchCap) {
        this(pieceRole, progressionDelta, verticalLevelDelta, allowOnMainPath, allowOnBranchPath, terminal,
                topCapOnly, topologyGroup, mainPathEnding, branchCap, "", "", 0, 0, 0, 0, true, true, false,
                "", MKWorkspaceFoundationPolicy.none(), List.of());
    }

    public MKJigsawPieceMetadata(MKJigsawPieceRole pieceRole, int progressionDelta, int verticalLevelDelta,
                                 boolean allowOnMainPath, boolean allowOnBranchPath, boolean terminal,
                                 boolean topCapOnly, String topologyGroup, boolean mainPathEnding,
                                 boolean branchCap, MKWorkspaceFoundationPolicy foundationPolicy) {
        this(pieceRole, progressionDelta, verticalLevelDelta, allowOnMainPath, allowOnBranchPath, terminal,
                topCapOnly, topologyGroup, mainPathEnding, branchCap, "", "", 0, 0, 0, 0, true, true, false,
                "", foundationPolicy, List.of());
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
                basementCapApproachEnabled, "", foundationPolicy, List.of());
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

    public record FloorLinkCandidate(
            Direction facing,
            int x,
            int y,
            int z,
            int openingWidth,
            int openingHeight,
            int lateralOffset,
            int verticalOffset
    ) {
        private static final Codec<Direction> DIRECTION_CODEC = Codec.STRING.xmap(
                FloorLinkCandidate::directionFromName,
                Direction::getSerializedName
        );

        public static final Codec<FloorLinkCandidate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                DIRECTION_CODEC.fieldOf("facing").forGetter(FloorLinkCandidate::facing),
                Codec.INT.fieldOf("x").forGetter(FloorLinkCandidate::x),
                Codec.INT.fieldOf("y").forGetter(FloorLinkCandidate::y),
                Codec.INT.fieldOf("z").forGetter(FloorLinkCandidate::z),
                Codec.INT.fieldOf("opening_width").forGetter(FloorLinkCandidate::openingWidth),
                Codec.INT.fieldOf("opening_height").forGetter(FloorLinkCandidate::openingHeight),
                Codec.INT.optionalFieldOf("lateral_offset", 0).forGetter(FloorLinkCandidate::lateralOffset),
                Codec.INT.optionalFieldOf("vertical_offset", 0).forGetter(FloorLinkCandidate::verticalOffset)
        ).apply(instance, FloorLinkCandidate::new));

        private static Direction directionFromName(String name) {
            Direction direction = Direction.byName(name);
            return direction == null ? Direction.NORTH : direction;
        }
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

