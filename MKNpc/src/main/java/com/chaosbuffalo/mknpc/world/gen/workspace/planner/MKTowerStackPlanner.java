package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawPieceRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKHorizontalOpeningProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceCategory;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceCategoryProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationPolicy;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitConnectionMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitPathKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteResolver;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRuntimePieceInfo;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessTags;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MKTowerStackPlanner {
    private static final String EMPTY_POOL = "minecraft:empty";
    private static final String LINEAR_RUN_POOL_PREFIX = "linear_runs";
    private static final String ROOM_POOL_PREFIX = "rooms";
    private final MKWorkspacePaletteResolver paletteResolver = new MKWorkspacePaletteResolver();

    private record ResolvedOpeningProfile(String profileId, int openingWidth, int openingHeight) {
    }

    private enum HallwayPathKind {
        MAIN("main"),
        BRANCH("branch");

        private final String serializedName;

        HallwayPathKind(String serializedName) {
            this.serializedName = serializedName;
        }
    }

    public List<MKPlannedPiece> createRoomPieces(MKStructureWorkspace workspace,
                                                 List<MKTowerWorkspaceFamilyDefinition> families) {
        return createRoomPieces(workspace, MKTowerStackDefinition.legacyTower(workspace.floorSettings()), families);
    }

    public List<MKPlannedPiece> createRoomPieces(MKStructureWorkspace workspace,
                                                 MKTowerStackDefinition stackDefinition,
                                                 List<MKTowerWorkspaceFamilyDefinition> families) {
        return families.stream()
                .filter(family -> shouldCreateFamily(stackDefinition, family))
                .map(family -> createPieceForFamily(workspace, stackDefinition, family))
                .toList();
    }

    public boolean shouldCreateFamily(MKTowerStackDefinition stackDefinition, MKTowerWorkspaceFamilyDefinition family) {
        return switch (family.pieceRole()) {
            case TOP_CAP_APPROACH -> stackDefinition.topCapApproachEnabled();
            case BASEMENT_CAP_APPROACH -> stackDefinition.basementCapApproachEnabled();
            default -> true;
        };
    }

    public MKPlannedPiece createPieceForFamily(MKStructureWorkspace workspace, MKTowerWorkspaceFamilyDefinition family) {
        return createPieceForFamily(workspace, MKTowerStackDefinition.legacyTower(workspace.floorSettings()), family);
    }

    public MKPlannedPiece createPieceForFamily(MKStructureWorkspace workspace,
                                               MKTowerStackDefinition stackDefinition,
                                               MKTowerWorkspaceFamilyDefinition family) {
        String stairPlacement = stackDefinition.verticalAccessPlacement() == null ?
                workspace.verticalAccessSpec().placement().getSerializedName() :
                stackDefinition.verticalAccessPlacement().getSerializedName();
        int hallWidth = stackDefinition.shaftSize() > 0 ?
                stackDefinition.shaftSize() :
                workspace.verticalAccessSpec().shaftSize();
        return switch (family.pieceRole()) {
            case ENTRY -> new MKPlannedPiece(
                    family.pieceRole(),
                    family.baseName(),
                    family.roomWidth(),
                    family.roomLength(),
                    family.roomHeight(),
                    connectorsWithHorizontalExits(
                            family.supportsVerticalAccess() ?
                                    List.of(
                                            new MKPlannedConnector(MKConnectorRole.CONNECT_UP, Direction.UP, hallWidth, hallWidth,
                                                    stackDefinition.connectUpPool()),
                                            new MKPlannedConnector(MKConnectorRole.CONNECT_DOWN, Direction.DOWN, hallWidth, hallWidth,
                                                    stackDefinition.connectDownEntryPool())
                                    ) : List.of(),
                            family,
                            workspace
                    ),
                    buildRoomTags(workspace, "entry", stackDefinition, family, stairPlacement, "both",
                            roomRuntimeInfo(stackDefinition.startPiece(), MKJigsawPieceRole.ROOM, 0, 0, false, false, family))
            );
            case FLOOR_MAIN -> new MKPlannedPiece(
                    family.pieceRole(),
                    family.baseName(),
                    family.roomWidth(),
                    family.roomLength(),
                    family.roomHeight(),
                    connectorsWithHorizontalExits(
                            family.supportsVerticalAccess() ?
                                    List.of(
                                            new MKPlannedConnector(MKConnectorRole.CONNECT_DOWN, Direction.DOWN, hallWidth, hallWidth,
                                                    EMPTY_POOL, stackDefinition.connectUpPool()),
                                            new MKPlannedConnector(MKConnectorRole.CONNECT_UP, Direction.UP, hallWidth, hallWidth,
                                                    stackDefinition.connectUpPool())
                                    ) : List.of(),
                            family,
                            workspace
                    ),
                    buildRoomTags(workspace, "floor", stackDefinition, family, stairPlacement, "both",
                            roomRuntimeInfo(false, MKJigsawPieceRole.ROOM, 1, 1, false, false, family))
            );
            case TOP_CAP_APPROACH -> new MKPlannedPiece(
                    family.pieceRole(),
                    family.baseName(),
                    family.roomWidth(),
                    family.roomLength(),
                    family.roomHeight(),
                    connectorsWithHorizontalExits(
                            family.supportsVerticalAccess() ?
                                    List.of(
                                            new MKPlannedConnector(MKConnectorRole.CONNECT_DOWN, Direction.DOWN, hallWidth, hallWidth,
                                                    EMPTY_POOL, stackDefinition.connectUpPool()),
                                            new MKPlannedConnector(MKConnectorRole.TOP_CAP_FORWARD, Direction.UP, hallWidth, hallWidth,
                                                    stackDefinition.topCapPool())
                                    ) : List.of(),
                            family,
                            workspace
                    ),
                    buildRoomTags(workspace, "top_cap_approach", stackDefinition, family, stairPlacement, "up",
                            roomRuntimeInfo(false, MKJigsawPieceRole.TOP_CAP_APPROACH, 1, 1, false, true, family))
            );
            case TOP_CAP -> new MKPlannedPiece(
                    family.pieceRole(),
                    family.baseName(),
                    family.roomWidth(),
                    family.roomLength(),
                    family.roomHeight(),
                    connectorsWithHorizontalExits(
                            family.supportsVerticalAccess() ?
                                    topCapConnectors(stackDefinition, hallWidth) : List.of(),
                            family,
                            workspace
                    ),
                    buildRoomTags(workspace, "top_cap", stackDefinition, family, stairPlacement, "up", true, false,
                            topCapRuntimeInfo(stackDefinition, family))
            );
            case BASEMENT_ENTRY -> new MKPlannedPiece(
                    family.pieceRole(),
                    family.baseName(),
                    family.roomWidth(),
                    family.roomLength(),
                    family.roomHeight(),
                    connectorsWithHorizontalExits(
                            family.supportsVerticalAccess() ?
                                    List.of(
                                            new MKPlannedConnector(MKConnectorRole.CONNECT_UP, Direction.UP, hallWidth, hallWidth,
                                                    EMPTY_POOL, stackDefinition.connectDownEntryPool()),
                                            new MKPlannedConnector(MKConnectorRole.CONNECT_DOWN, Direction.DOWN, hallWidth, hallWidth,
                                                    stackDefinition.connectDownPool())
                                    ) : List.of(),
                            family,
                            workspace
                    ),
                    buildRoomTags(workspace, "basement_entry", stackDefinition, family, stairPlacement, "down",
                            roomRuntimeInfo(false, MKJigsawPieceRole.ROOM, 1, -1, false, false, family))
            );
            case BASEMENT_MAIN -> new MKPlannedPiece(
                    family.pieceRole(),
                    family.baseName(),
                    family.roomWidth(),
                    family.roomLength(),
                    family.roomHeight(),
                    connectorsWithHorizontalExits(
                            family.supportsVerticalAccess() ?
                                    List.of(
                                            new MKPlannedConnector(MKConnectorRole.CONNECT_UP, Direction.UP, hallWidth, hallWidth,
                                                    EMPTY_POOL, stackDefinition.connectDownPool()),
                                            new MKPlannedConnector(MKConnectorRole.CONNECT_DOWN, Direction.DOWN, hallWidth, hallWidth,
                                                    stackDefinition.connectDownPool())
                                    ) : List.of(),
                            family,
                            workspace
                    ),
                    buildRoomTags(workspace, "basement_main", stackDefinition, family, stairPlacement, "down",
                            roomRuntimeInfo(false, MKJigsawPieceRole.ROOM, 1, -1, false, false, family))
            );
            case BASEMENT_CAP_APPROACH -> new MKPlannedPiece(
                    family.pieceRole(),
                    family.baseName(),
                    family.roomWidth(),
                    family.roomLength(),
                    family.roomHeight(),
                    connectorsWithHorizontalExits(
                            family.supportsVerticalAccess() ?
                                    List.of(
                                            new MKPlannedConnector(MKConnectorRole.CONNECT_UP, Direction.UP, hallWidth, hallWidth,
                                                    EMPTY_POOL, stackDefinition.connectDownPool()),
                                            new MKPlannedConnector(MKConnectorRole.TOP_CAP_FORWARD, Direction.DOWN, hallWidth, hallWidth,
                                                    stackDefinition.bottomCapPool())
                                    ) : List.of(),
                            family,
                            workspace
                    ),
                    buildRoomTags(workspace, "basement_cap_approach", stackDefinition, family, stairPlacement, "down",
                            roomRuntimeInfo(false, MKJigsawPieceRole.BASEMENT_CAP_APPROACH, 1, -1, false, true, family))
            );
            case BASEMENT_CAP -> new MKPlannedPiece(
                    family.pieceRole(),
                    family.baseName(),
                    family.roomWidth(),
                    family.roomLength(),
                    family.roomHeight(),
                    connectorsWithHorizontalExits(
                            family.supportsVerticalAccess() ?
                                    basementCapConnectors(stackDefinition, hallWidth) : List.of(),
                            family,
                            workspace
                    ),
                    buildRoomTags(workspace, "basement_cap", stackDefinition, family, stairPlacement, "down", false, true,
                            basementCapRuntimeInfo(stackDefinition, family))
            );
            case HALLWAY -> throw new IllegalStateException("tower families do not directly create hallway pieces");
        };
    }

    private List<MKPlannedConnector> topCapConnectors(MKTowerStackDefinition stackDefinition, int hallWidth) {
        if (stackDefinition.topCapApproachEnabled()) {
            return List.of(new MKPlannedConnector(MKConnectorRole.TOP_CAP_BACK, Direction.DOWN, hallWidth, hallWidth,
                    EMPTY_POOL, stackDefinition.topCapPool()));
        }
        return List.of(new MKPlannedConnector(MKConnectorRole.CONNECT_DOWN, Direction.DOWN, hallWidth, hallWidth,
                EMPTY_POOL, stackDefinition.connectUpPool()));
    }

    private MKWorkspaceRuntimePieceInfo topCapRuntimeInfo(MKTowerStackDefinition stackDefinition,
                                                          MKTowerWorkspaceFamilyDefinition family) {
        if (stackDefinition.topCapApproachEnabled()) {
            return roomRuntimeInfo(false, MKJigsawPieceRole.TOP_CAP, 0, 0, true, true, family);
        }
        return roomRuntimeInfo(false, MKJigsawPieceRole.TOP_CAP, 1, 1, true, true, family);
    }

    private List<MKPlannedConnector> basementCapConnectors(MKTowerStackDefinition stackDefinition, int hallWidth) {
        if (stackDefinition.basementCapApproachEnabled()) {
            return List.of(new MKPlannedConnector(MKConnectorRole.TOP_CAP_BACK, Direction.UP, hallWidth, hallWidth,
                    EMPTY_POOL, stackDefinition.bottomCapPool()));
        }
        return List.of(new MKPlannedConnector(MKConnectorRole.CONNECT_UP, Direction.UP, hallWidth, hallWidth,
                EMPTY_POOL, stackDefinition.connectDownPool()));
    }

    private MKWorkspaceRuntimePieceInfo basementCapRuntimeInfo(MKTowerStackDefinition stackDefinition,
                                                               MKTowerWorkspaceFamilyDefinition family) {
        if (stackDefinition.basementCapApproachEnabled()) {
            return roomRuntimeInfo(false, MKJigsawPieceRole.TERMINAL, 0, 0, true, true, family);
        }
        return roomRuntimeInfo(false, MKJigsawPieceRole.TERMINAL, 1, -1, true, false, family);
    }

    private List<MKPlannedConnector> connectorsWithHorizontalExits(List<MKPlannedConnector> baseConnectors,
                                                                   MKTowerWorkspaceFamilyDefinition family,
                                                                   MKStructureWorkspace workspace) {
        ArrayList<MKPlannedConnector> connectors = new ArrayList<>(baseConnectors.stream()
                .filter(connector -> !connector.facing().getAxis().isVertical() ||
                        family.hasVerticalAccess(connector.facing()))
                .toList());
        for (MKWorkspaceFamilyHorizontalExitDefinition exit : family.horizontalOnlyExits()) {
            ResolvedOpeningProfile opening = resolveOpeningProfile(workspace, exit.openingProfileId())
                    .orElseThrow(() -> new IllegalStateException("missing opening profile " + exit.openingProfileId() +
                            " for family " + family.baseName()));
            HallwayPathKind hallwayPathKind = exit.pathKind().usesMainPath() ? HallwayPathKind.MAIN : HallwayPathKind.BRANCH;
            MKConnectorRole role = switch (exit.pathKind()) {
                case MAIN_ENTRY, MAIN_ENDING_ENTRY -> MKConnectorRole.MAIN_FORWARD;
                case MAIN_EXIT -> MKConnectorRole.MAIN_BACK;
                case BRANCH, BRANCH_CAP_ENTRY -> MKConnectorRole.BRANCH;
                case VERTICAL_ACCESS -> throw new IllegalStateException("vertical access exits are not horizontal connectors");
            };
            int lateralOffset = toLateralOffset(exit.direction(), exit.sideOffset());
            if (exit.pathKind() == MKWorkspaceHorizontalExitPathKind.MAIN_ENDING_ENTRY) {
                connectors.add(new MKPlannedConnector(role, exit.direction(),
                        opening.openingWidth(), opening.openingHeight(), lateralOffset, exit.verticalOffset(),
                        EMPTY_POOL, mainEndingPoolName(family.category())));
                continue;
            }
            if (exit.pathKind() == MKWorkspaceHorizontalExitPathKind.BRANCH_CAP_ENTRY) {
                connectors.add(new MKPlannedConnector(role, exit.direction(),
                        opening.openingWidth(), opening.openingHeight(), lateralOffset, exit.verticalOffset(),
                        EMPTY_POOL, branchCapPoolName(opening.profileId())));
                continue;
            }
            if (exit.connectionMode() == MKWorkspaceHorizontalExitConnectionMode.NO_CONNECTION) {
                connectors.add(MKPlannedConnector.openingOnly(role, exit.direction(),
                        opening.openingWidth(), opening.openingHeight(), lateralOffset, exit.verticalOffset()));
                continue;
            }
            String targetPool = exit.connectionMode() == MKWorkspaceHorizontalExitConnectionMode.DIRECT_ROOM ?
                    directRoomTargetPoolName(opening.profileId(), role) :
                    resolveHallwayPool(workspace, opening.profileId(), hallwayPathKind);
            String incomingPool = exit.connectionMode() == MKWorkspaceHorizontalExitConnectionMode.DIRECT_ROOM ?
                    directRoomIncomingPoolName(opening.profileId(), role) : null;
            connectors.add(new MKPlannedConnector(role, exit.direction(),
                    opening.openingWidth(), opening.openingHeight(), lateralOffset,
                    exit.verticalOffset(), targetPool, incomingPool));
        }
        return List.copyOf(connectors);
    }

    private int toLateralOffset(Direction direction, int sideOffset) {
        return switch (direction) {
            case NORTH, EAST -> sideOffset;
            case SOUTH, WEST -> -sideOffset;
            default -> 0;
        };
    }

    private Optional<ResolvedOpeningProfile> resolveOpeningProfile(MKStructureWorkspace workspace, String profileId) {
        return workspace.openingProfiles().stream()
                .filter(profile -> profile.profileId().equals(profileId))
                .findFirst()
                .map(profile -> new ResolvedOpeningProfile(profile.profileId(), profile.openingWidth(), profile.openingHeight()));
    }

    private String resolveHallwayPool(MKStructureWorkspace workspace, String openingProfileId, HallwayPathKind pathKind) {
        boolean hasCompatibleLinearRun = workspace.linearRunFamilies().stream().anyMatch(linearRun ->
                linearRun.openingProfileId().equals(openingProfileId) &&
                        (pathKind == HallwayPathKind.MAIN ? linearRun.allowOnMainPath() : linearRun.allowOnBranchPath()));
        return hasCompatibleLinearRun ? hallwayPoolName(openingProfileId, pathKind) : EMPTY_POOL;
    }

    private String hallwayPoolName(String openingProfileId, HallwayPathKind pathKind) {
        return LINEAR_RUN_POOL_PREFIX + "/" + pathKind.serializedName + "/" + openingProfileId;
    }

    private String directRoomTargetPoolName(String openingProfileId, MKConnectorRole role) {
        return ROOM_POOL_PREFIX + "/" + directRoomTargetRoleName(role) + "/" + openingProfileId;
    }

    private String directRoomIncomingPoolName(String openingProfileId, MKConnectorRole role) {
        return ROOM_POOL_PREFIX + "/" + role.getSerializedName() + "/" + openingProfileId;
    }

    public static String mainEndingPoolName(MKTowerWorkspaceCategory category) {
        return "main_endings/" + category.getSerializedName();
    }

    public static String branchCapPoolName(String openingProfileId) {
        return "branch_caps/" + openingProfileId;
    }

    private String directRoomTargetRoleName(MKConnectorRole role) {
        return switch (role) {
            case MAIN_FORWARD -> MKConnectorRole.MAIN_BACK.getSerializedName();
            case MAIN_BACK -> MKConnectorRole.MAIN_FORWARD.getSerializedName();
            case BRANCH -> MKConnectorRole.BRANCH.getSerializedName();
            default -> throw new IllegalStateException("unsupported direct room connector role " + role);
        };
    }

    private MKWorkspaceRuntimePieceInfo roomRuntimeInfo(boolean start, MKJigsawPieceRole role,
                                                        int progressionDelta, int verticalLevelDelta,
                                                        boolean terminal, boolean topCapOnly,
                                                        MKTowerWorkspaceFamilyDefinition family) {
        boolean branchOnlyHorizontalFamily = family.mainEntry().isEmpty() && family.mainExit().isEmpty() &&
                family.mainEndingEntry().isEmpty() &&
                (!family.branchExits().isEmpty() || family.branchCap());
        boolean branchCap = family.branchCap();
        return new MKWorkspaceRuntimePieceInfo(
                start,
                role,
                progressionDelta,
                verticalLevelDelta,
                !branchOnlyHorizontalFamily,
                branchOnlyHorizontalFamily,
                terminal || branchCap,
                topCapOnly,
                family.category().getSerializedName(),
                family.mainPathEnding(),
                branchCap
        );
    }

    private Map<String, String> buildRoomTags(MKStructureWorkspace workspace, String topologyRole,
                                              MKTowerStackDefinition stackDefinition,
                                              MKTowerWorkspaceFamilyDefinition family, String stairPlacement,
                                              String stairDirection, MKWorkspaceRuntimePieceInfo runtimeInfo) {
        return buildRoomTags(workspace, topologyRole, stackDefinition, family, stairPlacement, stairDirection,
                false, false, runtimeInfo);
    }

    private Map<String, String> buildRoomTags(MKStructureWorkspace workspace, String topologyRole,
                                              MKTowerStackDefinition stackDefinition,
                                              MKTowerWorkspaceFamilyDefinition family, String stairPlacement,
                                              String stairDirection, boolean topCap, boolean bottomCap,
                                              MKWorkspaceRuntimePieceInfo runtimeInfo) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>();
        tags.put("topology_role", stackDefinition.useFamilyTopologyRole() ? family.topologySlotId() : topologyRole);
        tags.put("workspace_topology_slot_id", family.topologySlotId());
        tags.put("workspace_topology_role_id", family.topologySlotId());
        tags.put("tower_piece_kind", "room");
        tags.put("workspace_family_id", family.baseName());
        tags.put("workspace_horizontal_exits", family.horizontalExitSummary());
        tags.put("workspace_horizontal_extrusion_mode", family.horizontalExtrusionMode().getSerializedName());
        tags.put("workspace_category", family.category().getSerializedName());
        if (!stackDefinition.stackId().isBlank()) {
            tags.put("workspace_tower_stack_id", stackDefinition.stackId());
            tags.put("workspace_tower_stack_main_floors", Integer.toString(stackDefinition.mainFloors()));
            tags.put("workspace_tower_stack_basement_floors", Integer.toString(stackDefinition.basementFloors()));
        }
        applyVoidMarginTags(family, tags);
        applyFoundationTags(family.foundationPolicy(), tags);
        tags.put(MKWorkspaceVerticalAccessTags.ENABLED_TAG, Boolean.toString(family.supportsVerticalAccess()));
        if (family.supportsVerticalAccess()) {
            tags.put("workspace_vertical_access_group_id", family.verticalAccessGroupId());
            tags.put(MKWorkspaceVerticalAccessTags.PLACEMENT_TAG, stairPlacement);
            tags.put(MKWorkspaceVerticalAccessTags.DIRECTION_TAG, verticalAccessDirectionTag(family, stairDirection));
        }
        if (family.supportsVerticalAccess() && topCap) {
            tags.put(MKWorkspaceVerticalAccessTags.TOP_CAP_TAG, "true");
        }
        if (family.supportsVerticalAccess() && bottomCap) {
            tags.put(MKWorkspaceVerticalAccessTags.BOTTOM_CAP_TAG, "true");
        }
        runtimeInfo.applyToTags(tags);
        MKWorkspacePaletteTags.apply(tags, paletteResolver.resolveFamily(workspace, family));
        return tags;
    }

    private void applyVoidMarginTags(MKTowerWorkspaceFamilyDefinition family, Map<String, String> tags) {
        if (family.supportsVerticalAccess()) {
            return;
        }
        if (family.topVoidMargin() > 0) {
            tags.put(MKTowerWorkspaceCategoryProfile.TOP_VOID_MARGIN_TAG, Integer.toString(family.topVoidMargin()));
        }
        if (family.bottomVoidMargin() > 0) {
            tags.put(MKTowerWorkspaceCategoryProfile.BOTTOM_VOID_MARGIN_TAG, Integer.toString(family.bottomVoidMargin()));
        }
    }

    private void applyFoundationTags(MKWorkspaceFoundationPolicy policy, Map<String, String> tags) {
        if (policy.enabled()) {
            tags.put(MKWorkspaceFoundationPolicy.MODE_TAG, policy.mode().getSerializedName());
        }
    }

    private String verticalAccessDirectionTag(MKTowerWorkspaceFamilyDefinition family, String fallback) {
        boolean up = family.hasVerticalAccess(Direction.UP);
        boolean down = family.hasVerticalAccess(Direction.DOWN);
        if (up && down) {
            return "both";
        }
        if (up) {
            return "up";
        }
        if (down) {
            return "down";
        }
        return fallback;
    }
}
