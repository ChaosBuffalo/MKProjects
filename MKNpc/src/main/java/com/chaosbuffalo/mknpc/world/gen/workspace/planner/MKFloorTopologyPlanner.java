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
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitPathKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRuntimePieceInfo;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologySlotMetadata;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MKFloorTopologyPlanner {
    private static final String EMPTY_POOL = "minecraft:empty";
    private static final String LINEAR_RUN_POOL_PREFIX = "linear_runs";
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
        }
        return List.copyOf(pieces);
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
        return Optional.of(new FloorOpeningContext(
                stackId,
                slot.suffix(),
                floorTopologyGroupId(stackId, slot.suffix()),
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
        tags.put("workspace_piece_kind", "instance");
        tags.put("workspace_floor_topology_stack_id", context.stackId());
        tags.put("workspace_floor_topology_floor_role", context.floorRole());
        tags.put("workspace_floor_room_profile_id", profile.id());
        tags.put("workspace_floor_room_kind", profile.kind().getSerializedName());
        tags.put("workspace_topology_group", context.topologyGroupId());
        tags.put("workspace_floor_min_main_path_pieces", Integer.toString(settings.minMainPathPieces()));
        tags.put("workspace_floor_max_main_path_pieces", Integer.toString(settings.maxMainPathPieces()));
        tags.put("workspace_floor_max_branch_pieces_before_cap", Integer.toString(settings.maxBranchPiecesBeforeCap()));
        MKWorkspaceMaterialPalette palette = profile.paletteOverride()
                .map(override -> override.resolve(workspace.palette()))
                .orElse(workspace.palette());
        MKWorkspacePaletteTags.apply(tags, palette);
        runtimeInfoFor(context, profile).applyToTags(tags);
        return new MKPlannedPiece(
                slotId,
                pieceName,
                profile.width(),
                profile.length(),
                profile.height(),
                connectorsForProfile(profile, context),
                tags
        );
    }

    private MKWorkspaceRuntimePieceInfo runtimeInfoFor(FloorOpeningContext context, MKWorkspaceFloorRoomProfile profile) {
        boolean branchRoom = profile.kind() == MKWorkspaceFloorRoomKind.BRANCH_ROOM;
        boolean branchCap = profile.terminalBranchRoom();
        return new MKWorkspaceRuntimePieceInfo(
                false,
                branchRoom ? MKJigsawPieceRole.BRANCH : MKJigsawPieceRole.ROOM,
                0,
                0,
                !branchRoom,
                branchRoom,
                branchCap,
                false,
                context.topologyGroupId(),
                false,
                branchCap
        );
    }

    private List<MKPlannedConnector> connectorsForProfile(MKWorkspaceFloorRoomProfile profile,
                                                          FloorOpeningContext context) {
        ArrayList<MKPlannedConnector> connectors = new ArrayList<>();
        for (MKWorkspaceFamilyHorizontalExitDefinition exit : profile.horizontalExits()) {
            ResolvedOpeningProfile opening = resolveInheritedOpening(exit, context);
            MKConnectorRole role = connectorRole(exit.pathKind());
            String targetPool = targetPoolFor(exit, opening.profileId(), profile);
            String incomingPool = incomingPoolFor(exit, opening.profileId(), profile);
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
            case INGRESS, VERTICAL_ACCESS -> throw new IllegalStateException("unsupported floor room connector kind " +
                    pathKind.getSerializedName());
        };
    }

    private String targetPoolFor(MKWorkspaceFamilyHorizontalExitDefinition exit, String openingProfileId,
                                 MKWorkspaceFloorRoomProfile profile) {
        if (exit.pathKind() == MKWorkspaceHorizontalExitPathKind.MAIN_EXIT) {
            return linearRunPoolName(openingProfileId, PathPoolKind.MAIN);
        }
        if (exit.pathKind() == MKWorkspaceHorizontalExitPathKind.BRANCH &&
                !profile.terminalBranchRoom() &&
                exit.direction() != Direction.SOUTH) {
            return linearRunPoolName(openingProfileId, PathPoolKind.BRANCH);
        }
        return EMPTY_POOL;
    }

    private String incomingPoolFor(MKWorkspaceFamilyHorizontalExitDefinition exit, String openingProfileId,
                                   MKWorkspaceFloorRoomProfile profile) {
        if (profile.terminalBranchRoom()) {
            return MKTowerStackPlanner.branchCapPoolName(openingProfileId);
        }
        if (exit.pathKind().usesMainPath()) {
            return linearRunPoolName(openingProfileId, PathPoolKind.MAIN);
        }
        return linearRunPoolName(openingProfileId, PathPoolKind.BRANCH);
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
