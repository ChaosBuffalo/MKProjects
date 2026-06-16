package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawPieceRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKHorizontalOpeningProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationPolicy;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorTopologySettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitConnectionMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExtrusionMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitPathKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteResolver;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceResolvedFamilySettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRuntimePieceInfo;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologySlotMetadata;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalStackSlot;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessSpec;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVoidMarginTags;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MKWorkspaceVerticalStackPlanner {
    private final MKWorkspacePaletteResolver paletteResolver = new MKWorkspacePaletteResolver();

    private static final String EMPTY_POOL = "minecraft:empty";
    private static final String LINEAR_RUN_POOL_PREFIX = "linear_runs";
    private static final String ROOM_POOL_PREFIX = "rooms";

    private record ResolvedOpeningProfile(String profileId, int openingWidth, int openingHeight) {
    }

    private enum LinearRunPathKind {
        MAIN("main"),
        BRANCH("branch");

        private final String serializedName;

        LinearRunPathKind(String serializedName) {
            this.serializedName = serializedName;
        }
    }

    public List<MKPlannedPiece> createRoomPieces(MKStructureWorkspace workspace,
                                                 List<MKWorkspaceRoomFamilyDefinition> families) {
        return createRoomPieces(workspace, primaryStackDefinition(workspace), families);
    }

    public List<MKPlannedPiece> createRoomPieces(MKStructureWorkspace workspace,
                                                 MKWorkspaceVerticalStackDefinition stackDefinition,
                                                 List<MKWorkspaceRoomFamilyDefinition> families) {
        return families.stream()
                .filter(family -> shouldCreateFamily(stackDefinition, family))
                .map(family -> createPieceForFamily(workspace, stackDefinition, family))
                .toList();
    }

    public boolean shouldCreateFamily(MKWorkspaceVerticalStackDefinition stackDefinition, MKWorkspaceRoomFamilyDefinition family) {
        Optional<MKWorkspaceVerticalStackSlot> stackSlot = MKWorkspaceVerticalStackSlot.fromTopologySlotId(
                MKWorkspaceTopologySlotMetadata.fromFamily(family).topologySlotId());
        return stackSlot.map(slot -> switch (slot) {
            case MAIN_FLOOR -> stackDefinition.mainFloors() > 0;
            case TOP_CAP_APPROACH -> stackDefinition.topCapApproachEnabled();
            case BASEMENT_ENTRY -> stackDefinition.basementFloors() > 0 && stackDefinition.basementEntryEnabled();
            case BASEMENT_FLOOR, BASEMENT_CAP -> stackDefinition.basementFloors() > 0;
            case BASEMENT_CAP_APPROACH -> stackDefinition.basementFloors() > 0 &&
                    stackDefinition.basementCapApproachEnabled();
            default -> true;
        }).orElse(true);
    }

    public MKPlannedPiece createPieceForFamily(MKStructureWorkspace workspace, MKWorkspaceRoomFamilyDefinition family) {
        return createPieceForFamily(workspace, primaryStackDefinition(workspace), family);
    }

    public MKPlannedPiece createPieceForFamily(MKStructureWorkspace workspace,
                                               MKWorkspaceVerticalStackDefinition stackDefinition,
                                               MKWorkspaceRoomFamilyDefinition family) {
        MKWorkspaceResolvedFamilySettings resolvedFamily = workspace.resolveFamilySettings(family);
        MKWorkspaceVerticalAccessSpec verticalAccessSpec = resolvedFamily.verticalAccessSpec();
        String stairPlacement = verticalAccessSpec.placement().getSerializedName();
        int shaftWidth = verticalAccessSpec.shaftSize();
        MKWorkspaceStairAuthoringConfig stairConfig = verticalAccessSpec.stairConfig();
        MKWorkspaceTopologySlotMetadata slotMetadata = resolvedFamily.slotMetadata();
        MKWorkspaceVerticalStackSlot stackSlot = MKWorkspaceVerticalStackSlot.fromTopologySlotId(slotMetadata.topologySlotId())
                .orElseThrow(() -> new IllegalStateException("family " + family.baseName() +
                        " is not a vertical stack slot: " + slotMetadata.topologySlotId()));
        return switch (stackSlot) {
            case ENTRY -> new MKPlannedPiece(
                    slotMetadata.topologySlotId(),
                    family.baseName(),
                    resolvedFamily.roomWidth(),
                    resolvedFamily.roomLength(),
                    resolvedFamily.roomHeight(),
                    connectorsWithHorizontalExits(
                            family.supportsVerticalAccess() ?
                                    entryConnectors(stackDefinition, shaftWidth) : List.of(),
                            family,
                            workspace
                    ),
                    buildRoomTags(workspace, "entry", stackDefinition, family, stairPlacement, stairConfig, "both",
                            roomRuntimeInfo(stackDefinition.startPiece(), MKJigsawPieceRole.ROOM, 0, 0, false, false, family))
            );
            case MAIN_FLOOR -> new MKPlannedPiece(
                    slotMetadata.topologySlotId(),
                    family.baseName(),
                    resolvedFamily.roomWidth(),
                    resolvedFamily.roomLength(),
                    resolvedFamily.roomHeight(),
                    connectorsWithHorizontalExits(
                            family.supportsVerticalAccess() ?
                                    List.of(
                                            new MKPlannedConnector(MKConnectorRole.CONNECT_DOWN, Direction.DOWN, shaftWidth, shaftWidth,
                                                    EMPTY_POOL, stackDefinition.connectUpPool()),
                                            new MKPlannedConnector(MKConnectorRole.CONNECT_UP, Direction.UP, shaftWidth, shaftWidth,
                                                    stackDefinition.connectUpPool())
                                    ) : List.of(),
                            family,
                            workspace
                    ),
                    buildRoomTags(workspace, "floor", stackDefinition, family, stairPlacement, stairConfig, "both",
                            roomRuntimeInfo(false, MKJigsawPieceRole.ROOM, 1, 1, false, false, family))
            );
            case TOP_CAP_APPROACH -> new MKPlannedPiece(
                    slotMetadata.topologySlotId(),
                    family.baseName(),
                    resolvedFamily.roomWidth(),
                    resolvedFamily.roomLength(),
                    resolvedFamily.roomHeight(),
                    connectorsWithHorizontalExits(
                            family.supportsVerticalAccess() ?
                                    List.of(
                                            new MKPlannedConnector(MKConnectorRole.CONNECT_DOWN, Direction.DOWN, shaftWidth, shaftWidth,
                                                    EMPTY_POOL, stackDefinition.connectUpPool()),
                                            new MKPlannedConnector(MKConnectorRole.TOP_CAP_FORWARD, Direction.UP, shaftWidth, shaftWidth,
                                                    stackDefinition.topCapPool())
                                    ) : List.of(),
                            family,
                            workspace
                    ),
                    buildRoomTags(workspace, "top_cap_approach", stackDefinition, family, stairPlacement, stairConfig, "up",
                            roomRuntimeInfo(false, MKJigsawPieceRole.TOP_CAP_APPROACH, 1, 1, false, true, family))
            );
            case TOP_CAP -> new MKPlannedPiece(
                    slotMetadata.topologySlotId(),
                    family.baseName(),
                    resolvedFamily.roomWidth(),
                    resolvedFamily.roomLength(),
                    resolvedFamily.roomHeight(),
                    connectorsWithHorizontalExits(
                            family.supportsVerticalAccess() ?
                                    topCapConnectors(stackDefinition, shaftWidth) : List.of(),
                            family,
                            workspace
                    ),
                    buildRoomTags(workspace, "top_cap", stackDefinition, family, stairPlacement, stairConfig, "up", true, false,
                            topCapRuntimeInfo(stackDefinition, family))
            );
            case BASEMENT_ENTRY -> new MKPlannedPiece(
                    slotMetadata.topologySlotId(),
                    family.baseName(),
                    resolvedFamily.roomWidth(),
                    resolvedFamily.roomLength(),
                    resolvedFamily.roomHeight(),
                    connectorsWithHorizontalExits(
                            family.supportsVerticalAccess() ?
                                    List.of(
                                            new MKPlannedConnector(MKConnectorRole.CONNECT_UP, Direction.UP, shaftWidth, shaftWidth,
                                                    EMPTY_POOL, stackDefinition.connectDownEntryPool()),
                                            new MKPlannedConnector(MKConnectorRole.CONNECT_DOWN, Direction.DOWN, shaftWidth, shaftWidth,
                                                    stackDefinition.connectDownPool())
                                    ) : List.of(),
                            family,
                            workspace
                    ),
                    buildRoomTags(workspace, "basement_entry", stackDefinition, family, stairPlacement, stairConfig, "down",
                            roomRuntimeInfo(false, MKJigsawPieceRole.ROOM, 1, -1, false, false, family))
            );
            case BASEMENT_FLOOR -> new MKPlannedPiece(
                    slotMetadata.topologySlotId(),
                    family.baseName(),
                    resolvedFamily.roomWidth(),
                    resolvedFamily.roomLength(),
                    resolvedFamily.roomHeight(),
                    connectorsWithHorizontalExits(
                            family.supportsVerticalAccess() ?
                                    List.of(
                                            new MKPlannedConnector(MKConnectorRole.CONNECT_UP, Direction.UP, shaftWidth, shaftWidth,
                                                    EMPTY_POOL, stackDefinition.connectDownPool()),
                                            new MKPlannedConnector(MKConnectorRole.CONNECT_DOWN, Direction.DOWN, shaftWidth, shaftWidth,
                                                    stackDefinition.connectDownPool())
                                    ) : List.of(),
                            family,
                            workspace
                    ),
                    buildRoomTags(workspace, "basement_main", stackDefinition, family, stairPlacement, stairConfig, "down",
                            roomRuntimeInfo(false, MKJigsawPieceRole.ROOM, 1, -1, false, false, family))
            );
            case BASEMENT_CAP_APPROACH -> new MKPlannedPiece(
                    slotMetadata.topologySlotId(),
                    family.baseName(),
                    resolvedFamily.roomWidth(),
                    resolvedFamily.roomLength(),
                    resolvedFamily.roomHeight(),
                    connectorsWithHorizontalExits(
                            family.supportsVerticalAccess() ?
                                    List.of(
                                            new MKPlannedConnector(MKConnectorRole.CONNECT_UP, Direction.UP, shaftWidth, shaftWidth,
                                                    EMPTY_POOL, stackDefinition.connectDownPool()),
                                            new MKPlannedConnector(MKConnectorRole.TOP_CAP_FORWARD, Direction.DOWN, shaftWidth, shaftWidth,
                                                    stackDefinition.bottomCapPool())
                                    ) : List.of(),
                            family,
                            workspace
                    ),
                    buildRoomTags(workspace, "basement_cap_approach", stackDefinition, family, stairPlacement, stairConfig, "down",
                            roomRuntimeInfo(false, MKJigsawPieceRole.BASEMENT_CAP_APPROACH, 1, -1, false, true, family))
            );
            case BASEMENT_CAP -> new MKPlannedPiece(
                    slotMetadata.topologySlotId(),
                    family.baseName(),
                    resolvedFamily.roomWidth(),
                    resolvedFamily.roomLength(),
                    resolvedFamily.roomHeight(),
                    connectorsWithHorizontalExits(
                            family.supportsVerticalAccess() ?
                                    basementCapConnectors(stackDefinition, shaftWidth) : List.of(),
                            family,
                            workspace
                    ),
                    buildRoomTags(workspace, "basement_cap", stackDefinition, family, stairPlacement, stairConfig, "down", false, true,
                            basementCapRuntimeInfo(stackDefinition, family))
            );
        };
    }

    private MKWorkspaceVerticalStackDefinition primaryStackDefinition(MKStructureWorkspace workspace) {
        return workspace.topologyProfile().verticalStackSettings("tower.primary")
                .map(MKWorkspaceVerticalStackDefinition::towerPrimary)
                .orElseThrow(() -> new IllegalStateException("tower topology is missing tower.primary stack settings"));
    }

    private List<MKPlannedConnector> entryConnectors(MKWorkspaceVerticalStackDefinition stackDefinition, int shaftWidth) {
        ArrayList<MKPlannedConnector> connectors = new ArrayList<>();
        connectors.add(new MKPlannedConnector(MKConnectorRole.CONNECT_UP, Direction.UP, shaftWidth, shaftWidth,
                stackDefinition.connectUpPool()));
        if (stackDefinition.basementFloors() > 0) {
            connectors.add(new MKPlannedConnector(MKConnectorRole.CONNECT_DOWN, Direction.DOWN, shaftWidth, shaftWidth,
                    stackDefinition.basementEntryEnabled() ? stackDefinition.connectDownEntryPool() :
                            stackDefinition.connectDownPool()));
        }
        return List.copyOf(connectors);
    }

    private List<MKPlannedConnector> topCapConnectors(MKWorkspaceVerticalStackDefinition stackDefinition, int shaftWidth) {
        if (stackDefinition.topCapApproachEnabled()) {
            return List.of(new MKPlannedConnector(MKConnectorRole.TOP_CAP_BACK, Direction.DOWN, shaftWidth, shaftWidth,
                    EMPTY_POOL, stackDefinition.topCapPool()));
        }
        return List.of(new MKPlannedConnector(MKConnectorRole.CONNECT_DOWN, Direction.DOWN, shaftWidth, shaftWidth,
                EMPTY_POOL, stackDefinition.connectUpPool()));
    }

    private MKWorkspaceRuntimePieceInfo topCapRuntimeInfo(MKWorkspaceVerticalStackDefinition stackDefinition,
                                                          MKWorkspaceRoomFamilyDefinition family) {
        if (stackDefinition.topCapApproachEnabled()) {
            return roomRuntimeInfo(false, MKJigsawPieceRole.TOP_CAP, 0, 0, true, true, family);
        }
        return roomRuntimeInfo(false, MKJigsawPieceRole.TOP_CAP, 1, 1, true, true, family);
    }

    private List<MKPlannedConnector> basementCapConnectors(MKWorkspaceVerticalStackDefinition stackDefinition, int shaftWidth) {
        if (stackDefinition.basementCapApproachEnabled()) {
            return List.of(new MKPlannedConnector(MKConnectorRole.TOP_CAP_BACK, Direction.UP, shaftWidth, shaftWidth,
                    EMPTY_POOL, stackDefinition.bottomCapPool()));
        }
        return List.of(new MKPlannedConnector(MKConnectorRole.CONNECT_UP, Direction.UP, shaftWidth, shaftWidth,
                EMPTY_POOL, stackDefinition.connectDownPool()));
    }

    private MKWorkspaceRuntimePieceInfo basementCapRuntimeInfo(MKWorkspaceVerticalStackDefinition stackDefinition,
                                                               MKWorkspaceRoomFamilyDefinition family) {
        if (stackDefinition.basementCapApproachEnabled()) {
            return roomRuntimeInfo(false, MKJigsawPieceRole.TERMINAL, 0, 0, true, true, family);
        }
        return roomRuntimeInfo(false, MKJigsawPieceRole.TERMINAL, 1, -1, true, false, family);
    }

    private List<MKPlannedConnector> connectorsWithHorizontalExits(List<MKPlannedConnector> baseConnectors,
                                                                   MKWorkspaceRoomFamilyDefinition family,
                                                                   MKStructureWorkspace workspace) {
        MKWorkspaceTopologySlotMetadata slotMetadata = workspace.resolveFamilySettings(family).slotMetadata();
        ArrayList<MKPlannedConnector> connectors = new ArrayList<>(baseConnectors.stream()
                .filter(connector -> !connector.facing().getAxis().isVertical() ||
                        family.hasVerticalAccess(connector.facing()))
                .toList());
        for (MKWorkspaceFamilyHorizontalExitDefinition exit : family.horizontalOnlyExits()) {
            ResolvedOpeningProfile opening = resolveOpeningProfile(workspace, exit.openingProfileId())
                    .orElseThrow(() -> new IllegalStateException("missing opening profile " + exit.openingProfileId() +
                            " for family " + family.baseName()));
            int lateralOffset = toLateralOffset(exit.direction(), exit.sideOffset());
            if (exit.pathKind() == MKWorkspaceHorizontalExitPathKind.INGRESS) {
                connectors.add(MKPlannedConnector.openingOnly(MKConnectorRole.MAIN_BACK, exit.direction(),
                        opening.openingWidth(), opening.openingHeight(), lateralOffset, exit.verticalOffset(),
                        exit.horizontalExtrusionModeOverride()));
                continue;
            }
            LinearRunPathKind linearRunPathKind = exit.pathKind().usesMainPath() ? LinearRunPathKind.MAIN : LinearRunPathKind.BRANCH;
            MKConnectorRole role = switch (exit.pathKind()) {
                case MAIN_ENTRY, MAIN_ENDING_ENTRY -> MKConnectorRole.MAIN_FORWARD;
                case MAIN_EXIT -> MKConnectorRole.MAIN_BACK;
                case BRANCH, BRANCH_CAP_ENTRY -> MKConnectorRole.BRANCH;
                case INGRESS, LINK_CANDIDATE, VERTICAL_ACCESS -> throw new IllegalStateException("unsupported horizontal connector kind " +
                        exit.pathKind().getSerializedName());
            };
            if (exit.pathKind() == MKWorkspaceHorizontalExitPathKind.MAIN_ENDING_ENTRY) {
                connectors.add(new MKPlannedConnector(role, exit.direction(),
                        opening.openingWidth(), opening.openingHeight(), lateralOffset, exit.verticalOffset(),
                        EMPTY_POOL, mainEndingPoolName(slotMetadata.topologyGroupId()),
                        exit.horizontalExtrusionModeOverride()));
                continue;
            }
            if (exit.pathKind() == MKWorkspaceHorizontalExitPathKind.BRANCH_CAP_ENTRY) {
                connectors.add(new MKPlannedConnector(role, exit.direction(),
                        opening.openingWidth(), opening.openingHeight(), lateralOffset, exit.verticalOffset(),
                        EMPTY_POOL, branchCapPoolName(opening.profileId()),
                        exit.horizontalExtrusionModeOverride()));
                continue;
            }
            if (exit.connectionMode() == MKWorkspaceHorizontalExitConnectionMode.NO_CONNECTION) {
                connectors.add(MKPlannedConnector.openingOnly(role, exit.direction(),
                        opening.openingWidth(), opening.openingHeight(), lateralOffset, exit.verticalOffset(),
                        exit.horizontalExtrusionModeOverride()));
                continue;
            }
            Optional<MKWorkspaceFloorTopologySettings> floorSettings =
                    floorTopologySettingsForFamily(workspace, family, exit.pathKind());
            boolean floorDirectMain = floorSettings
                    .map(settings -> exit.pathKind().usesMainPath() && !settings.mainHallwaysEnabled())
                    .orElse(false);
            boolean floorDirectBranch = floorSettings
                    .map(settings -> !exit.pathKind().usesMainPath() && !settings.branchHallwaysEnabled())
                    .orElse(false);
            String targetPool;
            String incomingPool;
            if (floorDirectMain) {
                targetPool = floorTopologyTargetPool(family, opening.profileId(), true, false);
                incomingPool = null;
            } else if (floorDirectBranch) {
                targetPool = floorTopologyTargetPool(family, opening.profileId(), false, false);
                incomingPool = null;
            } else if (exit.connectionMode() == MKWorkspaceHorizontalExitConnectionMode.DIRECT_ROOM) {
                targetPool = directRoomTargetPoolName(opening.profileId(), role);
                incomingPool = directRoomIncomingPoolName(opening.profileId(), role);
            } else if (floorSettings.isPresent()) {
                boolean floorHallwayAvailable = exit.pathKind().usesMainPath() ?
                        floorSettings.get().mainHallwaysEnabled() : floorSettings.get().branchHallwaysEnabled();
                targetPool = floorTopologyTargetPool(family, opening.profileId(), exit.pathKind().usesMainPath(),
                        floorHallwayAvailable);
                incomingPool = null;
            } else {
                targetPool = resolveLinearRunPool(workspace, opening.profileId(), linearRunPathKind);
                incomingPool = null;
            }
            connectors.add(new MKPlannedConnector(role, exit.direction(),
                    opening.openingWidth(), opening.openingHeight(), lateralOffset,
                    exit.verticalOffset(), targetPool, incomingPool, exit.horizontalExtrusionModeOverride()));
        }
        return List.copyOf(connectors);
    }

    private String floorTopologyTargetPool(MKWorkspaceRoomFamilyDefinition family, String openingProfileId,
                                           boolean mainPath, boolean useHallwayPool) {
        Optional<MKWorkspaceVerticalStackSlot> slot = MKWorkspaceVerticalStackSlot.fromTopologySlotId(family.topologySlotId());
        Optional<String> stackId = MKWorkspaceVerticalStackSlot.stackIdForTopologySlot(family.topologySlotId());
        if (slot.isEmpty() || stackId.isEmpty()) {
            return mainPath ? MKFloorTopologyPlanner.directMainRoomPoolName(openingProfileId) :
                    MKFloorTopologyPlanner.directBranchRoomPoolName(openingProfileId);
        }
        String topologyGroup = MKFloorTopologyPlanner.floorTopologyGroupIdFor(stackId.get(), slot.get().suffix());
        return useHallwayPool ?
                MKFloorTopologyPlanner.floorLinearRunPoolName(topologyGroup, openingProfileId, mainPath) :
                MKFloorTopologyPlanner.floorRoomPoolName(topologyGroup, openingProfileId, mainPath);
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

    private Optional<MKWorkspaceFloorTopologySettings> floorTopologySettingsForFamily(
            MKStructureWorkspace workspace,
            MKWorkspaceRoomFamilyDefinition family,
            MKWorkspaceHorizontalExitPathKind pathKind) {
        if (!pathKind.usesMainPath() && pathKind != MKWorkspaceHorizontalExitPathKind.BRANCH) {
            return Optional.empty();
        }
        Optional<MKWorkspaceVerticalStackSlot> slot = MKWorkspaceVerticalStackSlot.fromTopologySlotId(family.topologySlotId());
        if (slot.isEmpty() || !"floor".equals(slot.get().roleKind()) || slot.get() == MKWorkspaceVerticalStackSlot.ENTRY) {
            return Optional.empty();
        }
        return MKWorkspaceVerticalStackSlot.stackIdForTopologySlot(family.topologySlotId())
                .map(stackId -> workspace.topologyProfile().floorTopologySettingsOrDefault(stackId, slot.get().suffix()));
    }

    private String resolveLinearRunPool(MKStructureWorkspace workspace, String openingProfileId, LinearRunPathKind pathKind) {
        return hasCompatibleLinearRun(workspace, openingProfileId, pathKind) ?
                linearRunPoolName(openingProfileId, pathKind) : EMPTY_POOL;
    }

    private boolean hasCompatibleLinearRun(MKStructureWorkspace workspace, String openingProfileId,
                                           LinearRunPathKind pathKind) {
        return workspace.linearRunFamilies().stream().anyMatch(linearRun ->
                linearRun.openingProfileId().equals(openingProfileId) &&
                        (pathKind == LinearRunPathKind.MAIN ? linearRun.allowOnMainPath() :
                                linearRun.allowOnBranchPath()));
    }

    private String linearRunPoolName(String openingProfileId, LinearRunPathKind pathKind) {
        return LINEAR_RUN_POOL_PREFIX + "/" + pathKind.serializedName + "/" + openingProfileId;
    }

    private String directRoomTargetPoolName(String openingProfileId, MKConnectorRole role) {
        return ROOM_POOL_PREFIX + "/" + directRoomTargetRoleName(role) + "/" + openingProfileId;
    }

    private String directRoomIncomingPoolName(String openingProfileId, MKConnectorRole role) {
        return ROOM_POOL_PREFIX + "/" + role.getSerializedName() + "/" + openingProfileId;
    }

    public static String mainEndingPoolName(String topologyGroupId) {
        return "main_endings/" + topologyGroupId;
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
                                                        MKWorkspaceRoomFamilyDefinition family) {
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
                MKWorkspaceTopologySlotMetadata.fromFamily(family).topologyGroupId(),
                family.mainPathEnding(),
                branchCap
        );
    }

    private Map<String, String> buildRoomTags(MKStructureWorkspace workspace, String topologyRole,
                                              MKWorkspaceVerticalStackDefinition stackDefinition,
                                              MKWorkspaceRoomFamilyDefinition family, String stairPlacement,
                                              MKWorkspaceStairAuthoringConfig stairConfig,
                                              String stairDirection, MKWorkspaceRuntimePieceInfo runtimeInfo) {
        return buildRoomTags(workspace, topologyRole, stackDefinition, family, stairPlacement, stairConfig, stairDirection,
                false, false, runtimeInfo);
    }

    private Map<String, String> buildRoomTags(MKStructureWorkspace workspace, String topologyRole,
                                              MKWorkspaceVerticalStackDefinition stackDefinition,
                                              MKWorkspaceRoomFamilyDefinition family, String stairPlacement,
                                              MKWorkspaceStairAuthoringConfig stairConfig,
                                              String stairDirection, boolean topCap, boolean bottomCap,
                                              MKWorkspaceRuntimePieceInfo runtimeInfo) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>();
        tags.put("topology_role", stackDefinition.useFamilyTopologyRole() ? family.topologySlotId() : topologyRole);
        tags.put("workspace_topology_slot_id", family.topologySlotId());
        tags.put("workspace_topology_role_id", family.topologySlotId());
        tags.put("tower_piece_kind", "room");
        tags.put("workspace_family_id", family.baseName());
        tags.put("workspace_horizontal_exits", family.horizontalExitSummary());
        tags.put("workspace_horizontal_extrusion_mode", effectiveHorizontalExtrusionMode(family).getSerializedName());
        MKWorkspaceResolvedFamilySettings resolvedFamily = workspace.resolveFamilySettings(family);
        tags.put("workspace_topology_group", resolvedFamily.slotMetadata().topologyGroupId());
        if (!stackDefinition.stackId().isBlank()) {
            tags.put("workspace_vertical_stack_id", stackDefinition.stackId());
            tags.put("workspace_vertical_stack_min_main_floors", Integer.toString(stackDefinition.minMainFloors()));
            tags.put("workspace_vertical_stack_main_floors", Integer.toString(stackDefinition.mainFloors()));
            tags.put("workspace_vertical_stack_min_basement_floors", Integer.toString(stackDefinition.minBasementFloors()));
            tags.put("workspace_vertical_stack_basement_floors", Integer.toString(stackDefinition.basementFloors()));
            MKWorkspaceVerticalStackSlot.fromTopologySlotId(family.topologySlotId())
                    .ifPresent(slot -> tags.put("workspace_vertical_stack_slot", slot.suffix()));
            tags.put("workspace_vertical_stack_top_cap_approach_enabled",
                    Boolean.toString(stackDefinition.topCapApproachEnabled()));
            tags.put("workspace_vertical_stack_basement_entry_enabled",
                    Boolean.toString(stackDefinition.basementEntryEnabled()));
            tags.put("workspace_vertical_stack_basement_cap_approach_enabled",
                    Boolean.toString(stackDefinition.basementCapApproachEnabled()));
        }
        applyVoidMarginTags(family, resolvedFamily, tags);
        applyFoundationTags(resolvedFamily.foundationPolicy(), tags);
        tags.put(MKWorkspaceVerticalAccessTags.ENABLED_TAG, Boolean.toString(family.supportsVerticalAccess()));
        if (family.supportsVerticalAccess()) {
            tags.put("workspace_vertical_access_group_id", family.verticalAccessGroupId());
            tags.put(MKWorkspaceVerticalAccessTags.PLACEMENT_TAG, stairPlacement);
            tags.put(MKWorkspaceVerticalAccessTags.DIRECTION_TAG, verticalAccessDirectionTag(family, stackDefinition,
                    stairDirection));
            tags.put("workspace_vertical_access_stair_mode", stairConfig.mode().getSerializedName());
            tags.put("workspace_vertical_access_stair_rise_type", stairConfig.riseType().getSerializedName());
            tags.put("workspace_vertical_access_stair_width", Integer.toString(stairConfig.stairWidth()));
        }
        if (family.supportsVerticalAccess() && topCap) {
            tags.put(MKWorkspaceVerticalAccessTags.TOP_CAP_TAG, "true");
        }
        if (family.supportsVerticalAccess() && bottomCap) {
            tags.put(MKWorkspaceVerticalAccessTags.BOTTOM_CAP_TAG, "true");
        }
        runtimeInfo.applyToTags(tags);
        MKWorkspacePaletteTags.apply(tags, paletteResolver.resolveFloorTopologyForFamily(workspace, family));
        return tags;
    }

    private MKWorkspaceHorizontalExtrusionMode effectiveHorizontalExtrusionMode(MKWorkspaceRoomFamilyDefinition family) {
        if (family.horizontalExtrusionMode() != MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION) {
            return family.horizontalExtrusionMode();
        }
        boolean hasGeneratedHorizontalConnection = family.horizontalOnlyExits().stream()
                .anyMatch(exit -> exit.connectionMode() != MKWorkspaceHorizontalExitConnectionMode.NO_CONNECTION);
        return hasGeneratedHorizontalConnection ? MKWorkspaceHorizontalExtrusionMode.TUNNEL_ONLY :
                MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION;
    }

    private void applyVoidMarginTags(MKWorkspaceRoomFamilyDefinition family,
                                     MKWorkspaceResolvedFamilySettings resolvedFamily,
                                     Map<String, String> tags) {
        if (resolvedFamily.topVoidMargin() > 0) {
            tags.put(MKWorkspaceVoidMarginTags.TOP_VOID_MARGIN_TAG,
                    Integer.toString(resolvedFamily.topVoidMargin()));
        }
        if (resolvedFamily.bottomVoidMargin() > 0) {
            tags.put(MKWorkspaceVoidMarginTags.BOTTOM_VOID_MARGIN_TAG,
                    Integer.toString(resolvedFamily.bottomVoidMargin()));
        }
    }

    private void applyFoundationTags(MKWorkspaceFoundationPolicy policy, Map<String, String> tags) {
        if (policy.enabled()) {
            tags.put(MKWorkspaceFoundationPolicy.MODE_TAG, policy.mode().getSerializedName());
        }
    }

    private String verticalAccessDirectionTag(MKWorkspaceRoomFamilyDefinition family,
                                              MKWorkspaceVerticalStackDefinition stackDefinition, String fallback) {
        boolean up = family.hasVerticalAccess(Direction.UP);
        boolean down = family.hasVerticalAccess(Direction.DOWN);
        Optional<MKWorkspaceVerticalStackSlot> stackSlot = MKWorkspaceVerticalStackSlot.fromTopologySlotId(
                MKWorkspaceTopologySlotMetadata.fromFamily(family).topologySlotId());
        if (stackSlot.isPresent() && stackSlot.get() == MKWorkspaceVerticalStackSlot.ENTRY &&
                stackDefinition.basementFloors() <= 0) {
            down = false;
        }
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
