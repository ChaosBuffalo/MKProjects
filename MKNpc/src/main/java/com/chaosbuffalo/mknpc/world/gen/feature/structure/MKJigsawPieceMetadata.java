package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationPolicy;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

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
        String verticalStackId,
        String verticalStackSlot,
        int minMainFloors,
        int maxMainFloors,
        int minBasementFloors,
        int maxBasementFloors,
        boolean topCapApproachEnabled,
        boolean basementEntryEnabled,
        boolean basementCapApproachEnabled,
        String floorExitMask,
        MKWorkspaceFoundationPolicy foundationPolicy,
        ResourceLocation floorBlock,
        ResourceLocation wallBlock,
        ResourceLocation ceilingBlock,
        List<FloorLinkCandidate> floorLinkCandidates,
        List<FloorClosableOpening> floorClosableOpenings,
        List<FloorRootExit> floorRootExits
) {
    private static final ResourceLocation DEFAULT_FLOOR_BLOCK = MKWorkspaceMaterialPalette.defaultPalette().floorBlock();
    private static final ResourceLocation DEFAULT_WALL_BLOCK = MKWorkspaceMaterialPalette.defaultPalette().wallBlock();
    private static final ResourceLocation DEFAULT_CEILING_BLOCK = MKWorkspaceMaterialPalette.defaultPalette().ceilingBlock();

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
            VerticalStackMetadata.CODEC.forGetter(MKJigsawPieceMetadata::verticalStackMetadata),
            FloorRuntimeMetadata.CODEC.forGetter(MKJigsawPieceMetadata::floorRuntimeMetadata)
    ).apply(instance, (pieceRole, progressionDelta, verticalLevelDelta, allowOnMainPath, allowOnBranchPath,
                       terminal, topCapOnly, topologyGroup, mainPathEnding, branchCap, verticalStackMetadata,
                       floorRuntimeMetadata) ->
            new MKJigsawPieceMetadata(pieceRole, progressionDelta, verticalLevelDelta, allowOnMainPath,
                    allowOnBranchPath, terminal, topCapOnly, topologyGroup, mainPathEnding, branchCap,
                    verticalStackMetadata.verticalStackId(), verticalStackMetadata.verticalStackSlot(),
                    verticalStackMetadata.minMainFloors(), verticalStackMetadata.maxMainFloors(),
                    verticalStackMetadata.minBasementFloors(), verticalStackMetadata.maxBasementFloors(),
                    verticalStackMetadata.topCapApproachEnabled(), verticalStackMetadata.basementEntryEnabled(),
                    verticalStackMetadata.basementCapApproachEnabled(), floorRuntimeMetadata.floorExitMask(),
                    floorRuntimeMetadata.foundationPolicy(), floorRuntimeMetadata.floorBlock(),
                    floorRuntimeMetadata.wallBlock(), floorRuntimeMetadata.ceilingBlock(),
                    floorRuntimeMetadata.floorLinkCandidates(), floorRuntimeMetadata.floorClosableOpenings(),
                    floorRuntimeMetadata.floorRootExits())));

    public MKJigsawPieceMetadata {
        floorExitMask = floorExitMask == null ? "" : floorExitMask;
        floorBlock = floorBlock == null ? DEFAULT_FLOOR_BLOCK : floorBlock;
        wallBlock = wallBlock == null ? DEFAULT_WALL_BLOCK : wallBlock;
        ceilingBlock = ceilingBlock == null ? DEFAULT_CEILING_BLOCK : ceilingBlock;
        floorLinkCandidates = floorLinkCandidates == null ? List.of() : List.copyOf(floorLinkCandidates);
        floorClosableOpenings = floorClosableOpenings == null ? List.of() : List.copyOf(floorClosableOpenings);
        floorRootExits = floorRootExits == null ? List.of() : List.copyOf(floorRootExits);
    }

    public MKJigsawPieceMetadata(MKJigsawPieceRole pieceRole, int progressionDelta, int verticalLevelDelta,
                                 boolean allowOnMainPath, boolean allowOnBranchPath, boolean terminal,
                                 boolean topCapOnly) {
        this(pieceRole, progressionDelta, verticalLevelDelta, allowOnMainPath, allowOnBranchPath, terminal,
                topCapOnly, "", false, false, "", "", 0, 0, 0, 0, true, true, false,
                "", MKWorkspaceFoundationPolicy.none(), DEFAULT_FLOOR_BLOCK, DEFAULT_WALL_BLOCK,
                DEFAULT_CEILING_BLOCK, List.of(), List.of(), List.of());
    }

    public MKJigsawPieceMetadata(MKJigsawPieceRole pieceRole, int progressionDelta, int verticalLevelDelta,
                                 boolean allowOnMainPath, boolean allowOnBranchPath, boolean terminal,
                                 boolean topCapOnly, String topologyGroup, boolean mainPathEnding) {
        this(pieceRole, progressionDelta, verticalLevelDelta, allowOnMainPath, allowOnBranchPath, terminal,
                topCapOnly, topologyGroup, mainPathEnding, false, "", "", 0, 0, 0, 0, true, true, false,
                "", MKWorkspaceFoundationPolicy.none(), DEFAULT_FLOOR_BLOCK, DEFAULT_WALL_BLOCK,
                DEFAULT_CEILING_BLOCK, List.of(), List.of(), List.of());
    }

    public MKJigsawPieceMetadata(MKJigsawPieceRole pieceRole, int progressionDelta, int verticalLevelDelta,
                                 boolean allowOnMainPath, boolean allowOnBranchPath, boolean terminal,
                                 boolean topCapOnly, String topologyGroup, boolean mainPathEnding, boolean branchCap) {
        this(pieceRole, progressionDelta, verticalLevelDelta, allowOnMainPath, allowOnBranchPath, terminal,
                topCapOnly, topologyGroup, mainPathEnding, branchCap, "", "", 0, 0, 0, 0, true, true, false,
                "", MKWorkspaceFoundationPolicy.none(), DEFAULT_FLOOR_BLOCK, DEFAULT_WALL_BLOCK,
                DEFAULT_CEILING_BLOCK, List.of(), List.of(), List.of());
    }

    public MKJigsawPieceMetadata(MKJigsawPieceRole pieceRole, int progressionDelta, int verticalLevelDelta,
                                 boolean allowOnMainPath, boolean allowOnBranchPath, boolean terminal,
                                 boolean topCapOnly, String topologyGroup, boolean mainPathEnding,
                                 boolean branchCap, MKWorkspaceFoundationPolicy foundationPolicy) {
        this(pieceRole, progressionDelta, verticalLevelDelta, allowOnMainPath, allowOnBranchPath, terminal,
                topCapOnly, topologyGroup, mainPathEnding, branchCap, "", "", 0, 0, 0, 0, true, true, false,
                "", foundationPolicy, DEFAULT_FLOOR_BLOCK, DEFAULT_WALL_BLOCK, DEFAULT_CEILING_BLOCK,
                List.of(), List.of(), List.of());
    }

    public MKJigsawPieceMetadata(MKJigsawPieceRole pieceRole, int progressionDelta, int verticalLevelDelta,
                                 boolean allowOnMainPath, boolean allowOnBranchPath, boolean terminal,
                                 boolean topCapOnly, String topologyGroup, boolean mainPathEnding,
                                 boolean branchCap, String verticalStackId, String verticalStackSlot,
                                 int minMainFloors, int maxMainFloors, int minBasementFloors, int maxBasementFloors,
                                 boolean topCapApproachEnabled, boolean basementEntryEnabled,
                                 boolean basementCapApproachEnabled, MKWorkspaceFoundationPolicy foundationPolicy) {
        this(pieceRole, progressionDelta, verticalLevelDelta, allowOnMainPath, allowOnBranchPath, terminal,
                topCapOnly, topologyGroup, mainPathEnding, branchCap, verticalStackId, verticalStackSlot, minMainFloors,
                maxMainFloors, minBasementFloors, maxBasementFloors, topCapApproachEnabled, basementEntryEnabled,
                basementCapApproachEnabled, "", foundationPolicy, DEFAULT_FLOOR_BLOCK, DEFAULT_WALL_BLOCK,
                DEFAULT_CEILING_BLOCK, List.of(), List.of(), List.of());
    }

    public boolean hasVerticalStackLayout() {
        return verticalStackId != null && !verticalStackId.isBlank() &&
                verticalStackSlot != null && !verticalStackSlot.isBlank();
    }

    private VerticalStackMetadata verticalStackMetadata() {
        return new VerticalStackMetadata(verticalStackId, verticalStackSlot, minMainFloors, maxMainFloors,
                minBasementFloors, maxBasementFloors, topCapApproachEnabled, basementEntryEnabled,
                basementCapApproachEnabled);
    }

    private FloorRuntimeMetadata floorRuntimeMetadata() {
        return new FloorRuntimeMetadata(floorExitMask, foundationPolicy, floorBlock, wallBlock, ceilingBlock,
                floorLinkCandidates, floorClosableOpenings, floorRootExits);
    }

    public record FloorLinkCandidate(
            Direction facing,
            int x,
            int y,
            int z,
            int openingWidth,
            int openingHeight,
            int lateralOffset,
            int verticalOffset,
            int closureDepth
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
                Codec.INT.optionalFieldOf("vertical_offset", 0).forGetter(FloorLinkCandidate::verticalOffset),
                Codec.INT.optionalFieldOf("closure_depth", 2).forGetter(FloorLinkCandidate::closureDepth)
        ).apply(instance, FloorLinkCandidate::new));

        public FloorLinkCandidate(Direction facing, int x, int y, int z, int openingWidth, int openingHeight,
                                  int lateralOffset, int verticalOffset) {
            this(facing, x, y, z, openingWidth, openingHeight, lateralOffset, verticalOffset, 2);
        }

        public FloorLinkCandidate {
            closureDepth = Math.max(1, closureDepth);
        }

        private static Direction directionFromName(String name) {
            Direction direction = Direction.byName(name);
            return direction == null ? Direction.NORTH : direction;
        }
    }

    public record FloorClosableOpening(
            Direction facing,
            int x,
            int y,
            int z,
            int openingWidth,
            int openingHeight,
            int closureDepth
    ) {
        private static final Codec<Direction> DIRECTION_CODEC = Codec.STRING.xmap(
                FloorClosableOpening::directionFromName,
                Direction::getSerializedName
        );

        public static final Codec<FloorClosableOpening> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                DIRECTION_CODEC.fieldOf("facing").forGetter(FloorClosableOpening::facing),
                Codec.INT.fieldOf("x").forGetter(FloorClosableOpening::x),
                Codec.INT.fieldOf("y").forGetter(FloorClosableOpening::y),
                Codec.INT.fieldOf("z").forGetter(FloorClosableOpening::z),
                Codec.INT.fieldOf("opening_width").forGetter(FloorClosableOpening::openingWidth),
                Codec.INT.fieldOf("opening_height").forGetter(FloorClosableOpening::openingHeight),
                Codec.INT.optionalFieldOf("closure_depth", 2).forGetter(FloorClosableOpening::closureDepth)
        ).apply(instance, FloorClosableOpening::new));

        public FloorClosableOpening(Direction facing, int x, int y, int z, int openingWidth, int openingHeight) {
            this(facing, x, y, z, openingWidth, openingHeight, 2);
        }

        public FloorClosableOpening {
            closureDepth = Math.max(1, closureDepth);
        }

        private static Direction directionFromName(String name) {
            Direction direction = Direction.byName(name);
            return direction == null ? Direction.NORTH : direction;
        }
    }

    public record FloorRootExit(
            String topologyGroup,
            Direction facing,
            String pathKind,
            String openingProfileId
    ) {
        private static final Codec<Direction> DIRECTION_CODEC = Codec.STRING.xmap(
                FloorRootExit::directionFromName,
                Direction::getSerializedName
        );

        public static final Codec<FloorRootExit> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("topology_group").forGetter(FloorRootExit::topologyGroup),
                DIRECTION_CODEC.fieldOf("facing").forGetter(FloorRootExit::facing),
                Codec.STRING.fieldOf("path_kind").forGetter(FloorRootExit::pathKind),
                Codec.STRING.fieldOf("opening_profile_id").forGetter(FloorRootExit::openingProfileId)
        ).apply(instance, FloorRootExit::new));

        private static Direction directionFromName(String name) {
            Direction direction = Direction.byName(name);
            return direction == null ? Direction.NORTH : direction;
        }
    }

    private record VerticalStackMetadata(
            String verticalStackId,
            String verticalStackSlot,
            int minMainFloors,
            int maxMainFloors,
            int minBasementFloors,
            int maxBasementFloors,
            boolean topCapApproachEnabled,
            boolean basementEntryEnabled,
            boolean basementCapApproachEnabled
    ) {
        private static final MapCodec<VerticalStackMetadata> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("vertical_stack_id", "").forGetter(VerticalStackMetadata::verticalStackId),
                Codec.STRING.optionalFieldOf("vertical_stack_slot", "").forGetter(VerticalStackMetadata::verticalStackSlot),
                Codec.INT.optionalFieldOf("min_main_floors", 0).forGetter(VerticalStackMetadata::minMainFloors),
                Codec.INT.optionalFieldOf("max_main_floors", 0).forGetter(VerticalStackMetadata::maxMainFloors),
                Codec.INT.optionalFieldOf("min_basement_floors", 0).forGetter(VerticalStackMetadata::minBasementFloors),
                Codec.INT.optionalFieldOf("max_basement_floors", 0).forGetter(VerticalStackMetadata::maxBasementFloors),
                Codec.BOOL.optionalFieldOf("top_cap_approach_enabled", true)
                        .forGetter(VerticalStackMetadata::topCapApproachEnabled),
                Codec.BOOL.optionalFieldOf("basement_entry_enabled", true)
                        .forGetter(VerticalStackMetadata::basementEntryEnabled),
                Codec.BOOL.optionalFieldOf("basement_cap_approach_enabled", false)
                        .forGetter(VerticalStackMetadata::basementCapApproachEnabled)
        ).apply(instance, VerticalStackMetadata::new));
    }

    private record FloorRuntimeMetadata(
            String floorExitMask,
            MKWorkspaceFoundationPolicy foundationPolicy,
            ResourceLocation floorBlock,
            ResourceLocation wallBlock,
            ResourceLocation ceilingBlock,
            List<FloorLinkCandidate> floorLinkCandidates,
            List<FloorClosableOpening> floorClosableOpenings,
            List<FloorRootExit> floorRootExits
    ) {
        private static final MapCodec<FloorRuntimeMetadata> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("floor_exit_mask", "").forGetter(FloorRuntimeMetadata::floorExitMask),
                MKWorkspaceFoundationPolicy.CODEC.optionalFieldOf("foundation_policy", MKWorkspaceFoundationPolicy.none())
                        .forGetter(FloorRuntimeMetadata::foundationPolicy),
                ResourceLocation.CODEC.optionalFieldOf("floor_block", DEFAULT_FLOOR_BLOCK)
                        .forGetter(FloorRuntimeMetadata::floorBlock),
                ResourceLocation.CODEC.optionalFieldOf("wall_block", DEFAULT_WALL_BLOCK)
                        .forGetter(FloorRuntimeMetadata::wallBlock),
                ResourceLocation.CODEC.optionalFieldOf("ceiling_block", DEFAULT_CEILING_BLOCK)
                        .forGetter(FloorRuntimeMetadata::ceilingBlock),
                FloorLinkCandidate.CODEC.listOf().optionalFieldOf("floor_link_candidates", List.of())
                        .forGetter(FloorRuntimeMetadata::floorLinkCandidates),
                FloorClosableOpening.CODEC.listOf().optionalFieldOf("floor_closable_openings", List.of())
                        .forGetter(FloorRuntimeMetadata::floorClosableOpenings),
                FloorRootExit.CODEC.listOf().optionalFieldOf("floor_root_exits", List.of())
                        .forGetter(FloorRuntimeMetadata::floorRootExits)
        ).apply(instance, FloorRuntimeMetadata::new));
    }
}

