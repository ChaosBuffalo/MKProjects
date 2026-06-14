package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawPieceRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKHorizontalOpeningProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceStackSlot;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorRoomKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorRoomProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorTopologySettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationPolicy;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHallwayLeadInMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitPathKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunPieceShape;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunProjection;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteResolver;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceResolvedFamilySettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRuntimePieceInfo;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologySlotMetadata;
import com.chaosbuffalo.mknpc.world.gen.workspace.export.MKFloorMaskVariantExporter;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MKFloorTopologyPlanner {
    private final MKWorkspacePaletteResolver paletteResolver = new MKWorkspacePaletteResolver();

    private static final String EMPTY_POOL = "minecraft:empty";
    private static final String LINEAR_RUN_POOL_PREFIX = "linear_runs";
    private static final String ROOM_POOL_PREFIX = "rooms";
    private static final String FLOOR_PLAN_POOL_PREFIX = "floor_plan";
    private static final String MAIN_CAP_APPROACH_POOL_PREFIX = "main_cap_approaches";
    private static final String MAIN_CAP_POOL_PREFIX = "main_caps";
    private static final String FLOOR_ROOM_SLOT_PREFIX = "tower.floor_plan";

    private enum PathPoolKind {
        MAIN("main"),
        BRANCH("branch");

        private final String serializedName;

        PathPoolKind(String serializedName) {
            this.serializedName = serializedName;
        }
    }

    private record ResolvedOpeningProfile(String profileId, int openingWidth, int openingHeight) {
    }

    private record FloorOpeningContext(
            String stackId,
            String floorRole,
            String topologyGroupId,
            int roomHeight,
            MKWorkspaceMaterialPalette palette,
            ResolvedOpeningProfile mainOpening,
            ResolvedOpeningProfile branchOpening
    ) {
    }

    public List<MKPlannedPiece> createFloorTopologyPieces(MKStructureWorkspace workspace,
                                                          List<MKTowerWorkspaceFamilyDefinition> rootFamilies) {
        ArrayList<MKPlannedPiece> pieces = new ArrayList<>();
        for (MKTowerWorkspaceFamilyDefinition rootFamily : rootFamilies) {
            Optional<FloorOpeningContext> contextOpt = contextForRootFamily(workspace, rootFamily);
            if (contextOpt.isEmpty()) {
                continue;
            }
            FloorOpeningContext context = contextOpt.get();
            MKWorkspaceFloorTopologySettings settings = workspace.topologyProfile()
                    .floorTopologySettingsOrDefault(context.stackId(), context.floorRole());
            for (int i = 0; i < settings.mainRoomProfiles().size(); i++) {
                pieces.add(createRoomPiece(workspace, settings, context, settings.mainRoomProfiles().get(i), i));
            }
            for (int i = 0; i < settings.branchRoomProfiles().size(); i++) {
                pieces.add(createRoomPiece(workspace, settings, context, settings.branchRoomProfiles().get(i), i));
            }
            for (int i = 0; i < settings.branchCapProfiles().size(); i++) {
                pieces.add(createRoomPiece(workspace, settings, context, settings.branchCapProfiles().get(i), i));
            }
            if (settings.mainCapApproachEnabled()) {
                for (int i = 0; i < settings.mainCapApproachProfiles().size(); i++) {
                    pieces.add(createRoomPiece(workspace, settings, context,
                            settings.mainCapApproachProfiles().get(i), i));
                }
            }
            for (int i = 0; i < settings.mainCapProfiles().size(); i++) {
                pieces.add(createRoomPiece(workspace, settings, context, settings.mainCapProfiles().get(i), i));
            }
            pieces.addAll(createFloorLinearRunPieces(workspace, settings, context));
        }
        return List.copyOf(pieces);
    }

    private List<MKPlannedPiece> createFloorLinearRunPieces(MKStructureWorkspace workspace,
                                                            MKWorkspaceFloorTopologySettings settings,
                                                            FloorOpeningContext context) {
        ArrayList<MKPlannedPiece> pieces = new ArrayList<>();
        if (settings.mainHallwaysEnabled()) {
            pieces.addAll(createFloorLinearRunPieces(workspace, settings, context, context.mainOpening(),
                    PathPoolKind.MAIN));
        }
        if (settings.branchHallwaysEnabled()) {
            pieces.addAll(createFloorLinearRunPieces(workspace, settings, context, context.branchOpening(),
                    PathPoolKind.BRANCH));
        }
        return List.copyOf(pieces);
    }

    private List<MKPlannedPiece> createFloorLinearRunPieces(MKStructureWorkspace workspace,
                                                            MKWorkspaceFloorTopologySettings settings,
                                                            FloorOpeningContext context,
                                                            ResolvedOpeningProfile opening,
                                                            PathPoolKind pathKind) {
        ArrayList<MKPlannedPiece> pieces = new ArrayList<>();
        for (MKWorkspaceLinearRunFamilyDefinition linearRun : floorLinearRuns(workspace, settings, context, opening,
                pathKind)) {
            if (!linearRun.openingProfileId().equals(opening.profileId())) {
                continue;
            }
            if (pathKind == PathPoolKind.MAIN && !linearRun.allowOnMainPath()) {
                continue;
            }
            if (pathKind == PathPoolKind.BRANCH && !linearRun.allowOnBranchPath()) {
                continue;
            }
            int westOffset = Math.max(0, -linearRun.slopeDelta());
            int eastOffset = Math.max(0, linearRun.slopeDelta());
            LinkedHashMap<String, String> tags = new LinkedHashMap<>();
            String pieceName = "floor_plan_" + safeId(context.stackId()) + "_" + safeId(context.floorRole()) +
                    "_linear_run_" + safeId(linearRun.linearRunId()) + "_" + pathKind.serializedName;
            String slotId = FLOOR_ROOM_SLOT_PREFIX + ".linear_run." + pathKind.serializedName;
            tags.put("topology_role", slotId);
            tags.put("workspace_topology_slot_id", slotId);
            tags.put("workspace_topology_role_id", slotId);
            tags.put("tower_piece_kind", "floor_plan_linear_run");
            tags.put("workspace_floor_topology_stack_id", context.stackId());
            tags.put("workspace_floor_topology_floor_role", context.floorRole());
            tags.put("workspace_topology_group", context.topologyGroupId());
            tags.put("workspace_linear_run_family_id", linearRun.linearRunId());
            tags.put("workspace_linear_run_kind", linearRun.kind().getSerializedName());
            tags.put("workspace_linear_run_projection", linearRun.projection().getSerializedName());
            tags.put("workspace_linear_run_shape", "straight");
            tags.put("workspace_linear_run_path_kind", pathKind.serializedName);
            tags.put("workspace_linear_run_slope_delta", Integer.toString(linearRun.slopeDelta()));
            tags.put("workspace_opening_profile_id", linearRun.openingProfileId());
            MKWorkspacePaletteTags.apply(tags, linearRun.paletteOverrideOpt()
                    .map(override -> override.resolve(context.palette()))
                    .orElse(context.palette()));
            new MKWorkspaceRuntimePieceInfo(false, MKJigsawPieceRole.ROOM, 0, 0,
                    pathKind == PathPoolKind.MAIN, pathKind == PathPoolKind.BRANCH, false, false,
                    context.topologyGroupId(), false)
                    .applyToTags(tags);
            MKConnectorRole westRole = pathKind == PathPoolKind.MAIN ? MKConnectorRole.MAIN_FORWARD :
                    MKConnectorRole.BRANCH;
            MKConnectorRole eastRole = pathKind == PathPoolKind.MAIN ? MKConnectorRole.MAIN_BACK :
                    MKConnectorRole.BRANCH;
            String incomingPool = floorLinearRunPoolName(context.topologyGroupId(), opening.profileId(), pathKind);
            String targetPool = floorRoomPoolName(context.topologyGroupId(), opening.profileId(), pathKind);
            int hallwayInteriorWidth = Math.max(linearRun.interiorWidth(), opening.openingWidth());
            int hallwayInteriorHeight = context.roomHeight() + Math.abs(linearRun.slopeDelta());
            pieces.add(new MKPlannedPiece(
                    slotId,
                    pieceName,
                    linearRun.length(),
                    hallwayInteriorWidth,
                    hallwayInteriorHeight,
                    List.of(
                            new MKPlannedConnector(westRole, Direction.WEST,
                                    opening.openingWidth(), opening.openingHeight(), 0, westOffset,
                                    targetPool, incomingPool),
                            new MKPlannedConnector(eastRole, Direction.EAST,
                                    opening.openingWidth(), opening.openingHeight(), 0, eastOffset,
                                    targetPool, incomingPool)
                    ),
                    tags
            ));
        }
        return List.copyOf(pieces);
    }

    private List<MKWorkspaceLinearRunFamilyDefinition> floorLinearRuns(MKStructureWorkspace workspace,
                                                                       MKWorkspaceFloorTopologySettings settings,
                                                                       FloorOpeningContext context,
                                                                       ResolvedOpeningProfile opening,
                                                                       PathPoolKind pathKind) {
        List<MKWorkspaceLinearRunFamilyDefinition> candidates = workspace.linearRunFamilies().stream()
                .filter(linearRun -> isFloorTopologyLinearRun(linearRun, pathKind))
                .filter(linearRun -> linearRun.openingProfileId().equals(opening.profileId()))
                .toList();
        if (!candidates.isEmpty()) {
            return candidates;
        }
        return List.of(fallbackFloorLinearRun(workspace, settings, context, opening, pathKind));
    }

    private boolean isFloorTopologyLinearRun(MKWorkspaceLinearRunFamilyDefinition linearRun, PathPoolKind pathKind) {
        if (pathKind == PathPoolKind.MAIN && !linearRun.allowOnMainPath()) {
            return false;
        }
        if (pathKind == PathPoolKind.BRANCH && !linearRun.allowOnBranchPath()) {
            return false;
        }
        return !linearRun.topologySlotId().startsWith("keep.");
    }

    private MKWorkspaceLinearRunFamilyDefinition fallbackFloorLinearRun(MKStructureWorkspace workspace,
                                                                        MKWorkspaceFloorTopologySettings settings,
                                                                        FloorOpeningContext context,
                                                                        ResolvedOpeningProfile opening,
                                                                        PathPoolKind pathKind) {
        int length = effectiveHallwayLeadInPieces(workspace, settings, context);
        return new MKWorkspaceLinearRunFamilyDefinition(
                "floor_" + pathKind.serializedName + "_hallway",
                FLOOR_ROOM_SLOT_PREFIX + ".linear_run." + pathKind.serializedName,
                MKWorkspaceLinearRunKind.ENCLOSED_CORRIDOR,
                opening.profileId(),
                length,
                opening.openingWidth(),
                opening.openingHeight(),
                0,
                pathKind == PathPoolKind.MAIN,
                pathKind == PathPoolKind.BRANCH,
                MKWorkspaceLinearRunProjection.RIGID,
                List.of(MKWorkspaceLinearRunPieceShape.STRAIGHT),
                MKWorkspaceFoundationPolicy.none(),
                null
        );
    }

    private int effectiveHallwayLeadInPieces(MKStructureWorkspace workspace,
                                             MKWorkspaceFloorTopologySettings settings,
                                             FloorOpeningContext context) {
        if (settings.hallwayLeadInMode() == MKWorkspaceHallwayLeadInMode.MANUAL) {
            return Math.max(1, settings.manualHallwayLeadInPieces());
        }
        return Math.max(1, Math.ceilDiv(Math.max(
                workspace.topologyProfile().towerStackSettingsOrDefault(context.stackId()).width(),
                workspace.topologyProfile().towerStackSettingsOrDefault(context.stackId()).length()), 8));
    }

    private Optional<FloorOpeningContext> contextForRootFamily(MKStructureWorkspace workspace,
                                                               MKTowerWorkspaceFamilyDefinition rootFamily) {
        Optional<MKTowerWorkspaceStackSlot> slotOpt = MKTowerWorkspaceStackSlot.fromTopologySlotId(rootFamily.topologySlotId());
        if (slotOpt.isEmpty()) {
            return Optional.empty();
        }
        MKTowerWorkspaceStackSlot slot = slotOpt.get();
        if (!"floor".equals(slot.roleKind())) {
            return Optional.empty();
        }
        if (slot == MKTowerWorkspaceStackSlot.ENTRY) {
            return Optional.empty();
        }
        Optional<ResolvedOpeningProfile> mainOpening = rootFamily.horizontalOnlyExits().stream()
                .filter(exit -> exit.pathKind() == MKWorkspaceHorizontalExitPathKind.MAIN_EXIT)
                .map(exit -> resolveOpening(workspace, exit.openingProfileId()))
                .flatMap(Optional::stream)
                .findFirst();
        Optional<ResolvedOpeningProfile> branchOpening = rootFamily.horizontalOnlyExits().stream()
                .filter(exit -> exit.pathKind() == MKWorkspaceHorizontalExitPathKind.BRANCH)
                .map(exit -> resolveOpening(workspace, exit.openingProfileId()))
                .flatMap(Optional::stream)
                .findFirst();
        if (mainOpening.isEmpty() && branchOpening.isEmpty()) {
            return Optional.empty();
        }
        String stackId = MKTowerWorkspaceStackSlot.stackIdForTopologySlot(rootFamily.topologySlotId()).orElse("");
        if (stackId.isBlank()) {
            return Optional.empty();
        }
        ResolvedOpeningProfile resolvedMain = mainOpening.orElseGet(() -> firstOpening(workspace, true)
                .orElseThrow(() -> new IllegalStateException("floor topology requires a main-compatible opening profile")));
        ResolvedOpeningProfile resolvedBranch = branchOpening.orElseGet(() -> firstOpening(workspace, false)
                .orElse(resolvedMain));
        MKWorkspaceResolvedFamilySettings resolvedFamily = workspace.resolveFamilySettings(rootFamily);
        return Optional.of(new FloorOpeningContext(
                stackId,
                slot.suffix(),
                floorTopologyGroupId(stackId, slot.suffix()),
                resolvedFamily.roomHeight(),
                paletteResolver.resolveFloorTopologyForFamily(workspace, rootFamily),
                resolvedMain,
                resolvedBranch
        ));
    }

    private MKPlannedPiece createRoomPiece(MKStructureWorkspace workspace,
                                           MKWorkspaceFloorTopologySettings settings,
                                           FloorOpeningContext context,
                                           MKWorkspaceFloorRoomProfile profile,
                                           int index) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>();
        String pieceName = pieceName(context, profile, index);
        String slotId = FLOOR_ROOM_SLOT_PREFIX + "." + profile.kind().getSerializedName();
        tags.put("topology_role", slotId);
        tags.put("workspace_topology_slot_id", slotId);
        tags.put("workspace_topology_role_id", slotId);
        tags.put("tower_piece_kind", "floor_plan_room");
        tags.put("workspace_piece_kind", "template");
        tags.put("workspace_floor_topology_stack_id", context.stackId());
        tags.put("workspace_floor_topology_floor_role", context.floorRole());
        tags.put("workspace_floor_room_profile_id", profile.id());
        tags.put("workspace_floor_room_kind", profile.kind().getSerializedName());
        tags.put(MKFloorMaskVariantExporter.FLOOR_RANDOMIZE_MAIN_EXIT_TAG,
                Boolean.toString(profile.randomizeMainExit()));
        tags.put("workspace_topology_group", context.topologyGroupId());
        tags.put("workspace_floor_min_main_path_pieces", Integer.toString(settings.minMainPathPieces()));
        tags.put("workspace_floor_max_main_path_pieces", Integer.toString(settings.maxMainPathPieces()));
        tags.put("workspace_floor_max_branch_pieces_before_cap", Integer.toString(settings.maxBranchPiecesBeforeCap()));
        tags.put("workspace_floor_main_hallways_enabled", Boolean.toString(settings.mainHallwaysEnabled()));
        tags.put("workspace_floor_branch_hallways_enabled", Boolean.toString(settings.branchHallwaysEnabled()));
        tags.put("workspace_floor_main_cap_approach_enabled", Boolean.toString(settings.mainCapApproachEnabled()));
        tags.put("workspace_floor_sprawl", Float.toString(settings.sprawl()));
        MKWorkspaceMaterialPalette palette = profile.paletteOverride()
                .map(override -> override.resolve(context.palette()))
                .orElse(context.palette());
        MKWorkspacePaletteTags.apply(tags, palette);
        runtimeInfoFor(context, profile).applyToTags(tags);
        return new MKPlannedPiece(
                slotId,
                pieceName,
                profile.width(),
                profile.length(),
                profile.height(),
                connectorsForProfile(workspace, settings, profile, context),
                tags
        );
    }

    private MKWorkspaceRuntimePieceInfo runtimeInfoFor(FloorOpeningContext context, MKWorkspaceFloorRoomProfile profile) {
        boolean branchPath = profile.kind().isBranchPath();
        boolean branchCap = profile.kind().isBranchCap();
        boolean mainPathEnding = profile.kind().isMainPathEnding();
        boolean terminal = branchCap || profile.kind() == MKWorkspaceFloorRoomKind.MAIN_CAP;
        return new MKWorkspaceRuntimePieceInfo(
                false,
                branchPath ? MKJigsawPieceRole.BRANCH : MKJigsawPieceRole.ROOM,
                0,
                0,
                !branchPath,
                branchPath,
                terminal,
                false,
                context.topologyGroupId(),
                mainPathEnding,
                branchCap
        );
    }

    private List<MKPlannedConnector> connectorsForProfile(MKStructureWorkspace workspace,
                                                          MKWorkspaceFloorTopologySettings settings,
                                                          MKWorkspaceFloorRoomProfile profile,
                                                          FloorOpeningContext context) {
        ArrayList<MKPlannedConnector> connectors = new ArrayList<>();
        for (MKWorkspaceFamilyHorizontalExitDefinition exit : profile.horizontalExits()) {
            ResolvedOpeningProfile opening = resolveInheritedOpening(exit, context);
            MKConnectorRole role = connectorRole(exit.pathKind());
            String targetPool = targetPoolFor(workspace, exit, opening.profileId(), profile, context, settings);
            String incomingPool = incomingPoolFor(exit, opening.profileId(), profile, context, settings);
            connectors.add(new MKPlannedConnector(
                    role,
                    exit.direction(),
                    opening.openingWidth(),
                    opening.openingHeight(),
                    toLateralOffset(exit.direction(), exit.sideOffset()),
                    exit.verticalOffset(),
                    targetPool,
                    incomingPool
            ));
        }
        return List.copyOf(connectors);
    }

    private ResolvedOpeningProfile resolveInheritedOpening(MKWorkspaceFamilyHorizontalExitDefinition exit,
                                                           FloorOpeningContext context) {
        if (MKWorkspaceFloorRoomProfile.INHERITED_BRANCH_OPENING_PROFILE_ID.equals(exit.openingProfileId())) {
            return context.branchOpening();
        }
        if (MKWorkspaceFloorRoomProfile.INHERITED_LINK_OPENING_PROFILE_ID.equals(exit.openingProfileId())) {
            return context.branchOpening();
        }
        if (MKWorkspaceFloorRoomProfile.INHERITED_MAIN_OPENING_PROFILE_ID.equals(exit.openingProfileId())) {
            return context.mainOpening();
        }
        return context.mainOpening();
    }

    private MKConnectorRole connectorRole(MKWorkspaceHorizontalExitPathKind pathKind) {
        return switch (pathKind) {
            case MAIN_ENTRY, MAIN_ENDING_ENTRY -> MKConnectorRole.MAIN_FORWARD;
            case MAIN_EXIT -> MKConnectorRole.MAIN_BACK;
            case BRANCH, BRANCH_CAP_ENTRY -> MKConnectorRole.BRANCH;
            case LINK_CANDIDATE -> MKConnectorRole.LINK_CANDIDATE;
            case INGRESS, VERTICAL_ACCESS -> throw new IllegalStateException("unsupported floor room connector kind " +
                    pathKind.getSerializedName());
        };
    }

    private String targetPoolFor(MKStructureWorkspace workspace, MKWorkspaceFamilyHorizontalExitDefinition exit,
                                 String openingProfileId,
                                 MKWorkspaceFloorRoomProfile profile, FloorOpeningContext context,
                                 MKWorkspaceFloorTopologySettings settings) {
        if (exit.pathKind() == MKWorkspaceHorizontalExitPathKind.MAIN_EXIT) {
            return settings.mainHallwaysEnabled() && hasCompatibleLinearRun(workspace, openingProfileId, PathPoolKind.MAIN) ?
                    floorLinearRunPoolName(context.topologyGroupId(), openingProfileId, PathPoolKind.MAIN) :
                    floorRoomPoolName(context.topologyGroupId(), openingProfileId, PathPoolKind.MAIN);
        }
        if (exit.pathKind() == MKWorkspaceHorizontalExitPathKind.BRANCH &&
                !profile.kind().isBranchCap() &&
                exit.direction() != Direction.SOUTH) {
            return settings.branchHallwaysEnabled() && hasCompatibleLinearRun(workspace, openingProfileId, PathPoolKind.BRANCH) ?
                    floorLinearRunPoolName(context.topologyGroupId(), openingProfileId, PathPoolKind.BRANCH) :
                    floorRoomPoolName(context.topologyGroupId(), openingProfileId, PathPoolKind.BRANCH);
        }
        if (exit.pathKind() == MKWorkspaceHorizontalExitPathKind.LINK_CANDIDATE) {
            return EMPTY_POOL;
        }
        return EMPTY_POOL;
    }

    private boolean hasCompatibleLinearRun(MKStructureWorkspace workspace, String openingProfileId,
                                           PathPoolKind pathKind) {
        return workspace.linearRunFamilies().stream().anyMatch(linearRun ->
                linearRun.openingProfileId().equals(openingProfileId) &&
                        (pathKind == PathPoolKind.MAIN ? linearRun.allowOnMainPath() :
                                linearRun.allowOnBranchPath()));
    }

    private String incomingPoolFor(MKWorkspaceFamilyHorizontalExitDefinition exit, String openingProfileId,
                                   MKWorkspaceFloorRoomProfile profile, FloorOpeningContext context,
                                   MKWorkspaceFloorTopologySettings settings) {
        if (exit.pathKind() == MKWorkspaceHorizontalExitPathKind.MAIN_ENTRY ||
                exit.pathKind() == MKWorkspaceHorizontalExitPathKind.MAIN_ENDING_ENTRY) {
            return floorRoomPoolName(context.topologyGroupId(), openingProfileId, PathPoolKind.MAIN);
        }
        if ((exit.pathKind() == MKWorkspaceHorizontalExitPathKind.BRANCH ||
                exit.pathKind() == MKWorkspaceHorizontalExitPathKind.BRANCH_CAP_ENTRY) &&
                exit.direction() == Direction.SOUTH) {
            return floorRoomPoolName(context.topologyGroupId(), openingProfileId, PathPoolKind.BRANCH);
        }
        if (exit.pathKind() == MKWorkspaceHorizontalExitPathKind.LINK_CANDIDATE) {
            return null;
        }
        return null;
    }

    private Optional<ResolvedOpeningProfile> resolveOpening(MKStructureWorkspace workspace, String profileId) {
        return workspace.openingProfiles().stream()
                .filter(profile -> profile.profileId().equals(profileId))
                .findFirst()
                .map(profile -> new ResolvedOpeningProfile(profile.profileId(), profile.openingWidth(), profile.openingHeight()));
    }

    private Optional<ResolvedOpeningProfile> firstOpening(MKStructureWorkspace workspace, boolean mainPath) {
        return workspace.openingProfiles().stream()
                .filter(profile -> mainPath ? profile.allowOnMainPath() : profile.allowOnBranchPath())
                .findFirst()
                .map(profile -> new ResolvedOpeningProfile(profile.profileId(), profile.openingWidth(), profile.openingHeight()));
    }

    private String linearRunPoolName(String openingProfileId, PathPoolKind pathKind) {
        return LINEAR_RUN_POOL_PREFIX + "/" + pathKind.serializedName + "/" + openingProfileId;
    }

    private String roomPoolName(String openingProfileId, PathPoolKind pathKind) {
        return ROOM_POOL_PREFIX + "/" + pathKind.serializedName + "/" + openingProfileId;
    }

    public static String mainCapApproachPoolName(String topologyGroupId) {
        return MAIN_CAP_APPROACH_POOL_PREFIX + "/" + topologyGroupId;
    }

    public static String mainCapPoolName(String topologyGroupId) {
        return MAIN_CAP_POOL_PREFIX + "/" + topologyGroupId;
    }

    public static String floorLinearRunPoolName(String topologyGroupId, String openingProfileId, boolean mainPath) {
        return floorLinearRunPoolName(topologyGroupId, openingProfileId,
                mainPath ? PathPoolKind.MAIN : PathPoolKind.BRANCH);
    }

    public static String floorRoomPoolName(String topologyGroupId, String openingProfileId, boolean mainPath) {
        return floorRoomPoolName(topologyGroupId, openingProfileId,
                mainPath ? PathPoolKind.MAIN : PathPoolKind.BRANCH);
    }

    public static String floorRoomMaskPoolName(String topologyGroupId, String openingProfileId, boolean mainPath,
                                               String maskName) {
        ResourceLocation basePool = ResourceLocation.fromNamespaceAndPath("mknpc",
                floorRoomPoolName(topologyGroupId, openingProfileId, mainPath));
        return MKFloorMaskVariantExporter.maskPool(basePool, maskName).getPath();
    }

    public static String floorTopologyGroupIdFor(String stackId, String floorRole) {
        return floorTopologyGroupId(stackId, floorRole);
    }

    public static String directMainRoomPoolName(String openingProfileId) {
        return ROOM_POOL_PREFIX + "/" + PathPoolKind.MAIN.serializedName + "/" + openingProfileId;
    }

    public static String directBranchRoomPoolName(String openingProfileId) {
        return ROOM_POOL_PREFIX + "/" + PathPoolKind.BRANCH.serializedName + "/" + openingProfileId;
    }

    private static String floorLinearRunPoolName(String topologyGroupId, String openingProfileId,
                                                PathPoolKind pathKind) {
        return FLOOR_PLAN_POOL_PREFIX + "/" + topologyGroupId + "/" + LINEAR_RUN_POOL_PREFIX + "/" +
                pathKind.serializedName + "/" + openingProfileId;
    }

    private static String floorRoomPoolName(String topologyGroupId, String openingProfileId, PathPoolKind pathKind) {
        return FLOOR_PLAN_POOL_PREFIX + "/" + topologyGroupId + "/" + ROOM_POOL_PREFIX + "/" +
                pathKind.serializedName + "/" + openingProfileId;
    }

    private String pieceName(FloorOpeningContext context, MKWorkspaceFloorRoomProfile profile, int index) {
        return "floor_plan_" + safeId(context.stackId()) + "_" + safeId(context.floorRole()) + "_" +
                profile.kind().getSerializedName() + "_" + safeId(profile.id()) + "_" + index;
    }

    private static String floorTopologyGroupId(String stackId, String floorRole) {
        return "floor/" + safeId(stackId) + "/" + safeId(floorRole);
    }

    private static String safeId(String value) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            builder.append(Character.isLetterOrDigit(c) ? c : '_');
        }
        return builder.toString();
    }

    private int toLateralOffset(Direction direction, int sideOffset) {
        return switch (direction) {
            case NORTH, EAST -> sideOffset;
            case SOUTH, WEST -> -sideOffset;
            default -> 0;
        };
    }
}
