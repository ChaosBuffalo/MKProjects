package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawPieceRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKHorizontalOpeningProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceCategory;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceCategoryProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitConnectionMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitPathKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunPieceShape;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKVerticalAccessPlacement;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteResolver;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRuntimePieceInfo;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessSpec;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class MKTowerWorkspacePlanner implements MKWorkspaceTopologyPlanner {
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

    @Override
    public String profileType() {
        return MKWorkspaceTopologyProfile.TOWER_PROFILE_TYPE;
    }

    @Override
    public MKWorkspaceTopologySchema schema() {
        return new MKWorkspaceTopologySchema(
                profileType(),
                List.of(
                        new MKWorkspaceRegionSchema("tower.entry", "tower_stack", true),
                        new MKWorkspaceRegionSchema("tower.main", "tower_stack", true),
                        new MKWorkspaceRegionSchema("tower.basement", "tower_stack", true),
                        new MKWorkspaceRegionSchema("tower.top_cap", "tower_cap", true),
                        new MKWorkspaceRegionSchema("tower.basement_cap", "tower_cap", true),
                        new MKWorkspaceRegionSchema("tower.linear_runs", "linear_run", true)
                ),
                List.of(
                        new MKWorkspaceSlotSchema("tower.basement_cap", "tower.basement_cap", "cap", "tower.basement_cap", MKWorkspaceSlotSchema.Repeat.FIXED),
                        new MKWorkspaceSlotSchema("tower.basement_cap_approach", "tower.basement_cap", "approach", "tower.basement_cap_approach", MKWorkspaceSlotSchema.Repeat.OPTIONAL),
                        new MKWorkspaceSlotSchema("tower.basement_floor", "tower.basement", "floor", "tower.basement_floor", MKWorkspaceSlotSchema.Repeat.RANGE),
                        new MKWorkspaceSlotSchema("tower.basement_entry", "tower.basement", "entry", "tower.basement_entry", MKWorkspaceSlotSchema.Repeat.FIXED),
                        new MKWorkspaceSlotSchema("tower.entry", "tower.entry", "entry", "tower.entry", MKWorkspaceSlotSchema.Repeat.FIXED),
                        new MKWorkspaceSlotSchema("tower.main_floor", "tower.main", "floor", "tower.main_floor", MKWorkspaceSlotSchema.Repeat.RANGE),
                        new MKWorkspaceSlotSchema("tower.top_cap_approach", "tower.top_cap", "approach", "tower.top_cap_approach", MKWorkspaceSlotSchema.Repeat.OPTIONAL),
                        new MKWorkspaceSlotSchema("tower.top_cap", "tower.top_cap", "cap", "tower.top_cap", MKWorkspaceSlotSchema.Repeat.FIXED),
                        new MKWorkspaceSlotSchema("tower.linear_run.main", "tower.linear_runs", "enclosed_corridor", "tower.linear_run.main", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema("tower.linear_run.branch", "tower.linear_runs", "enclosed_corridor", "tower.linear_run.branch", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema("tower.linear_run.branch_cap", "tower.linear_runs", "enclosed_corridor", "tower.linear_run.branch_cap", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema("tower.linear_run.main_ending", "tower.linear_runs", "enclosed_corridor", "tower.linear_run.main_ending", MKWorkspaceSlotSchema.Repeat.DERIVED)
                ),
                List.of(
                        new MKWorkspaceLinkSchema("tower.vertical.basement_cap_to_entry", "tower.basement_cap", "tower.entry", "vertical_stack"),
                        new MKWorkspaceLinkSchema("tower.vertical.entry_to_top_cap", "tower.entry", "tower.top_cap", "vertical_stack")
                ),
                List.of(
                        new MKWorkspaceRoleSchema("tower.entry", "floor", "room", false, true, Set.of("vertical_access")),
                        new MKWorkspaceRoleSchema("tower.main_floor", "floor", "room", false, false, Set.of("vertical_access")),
                        new MKWorkspaceRoleSchema("tower.top_cap_approach", "approach", "top_cap_approach", false, false, Set.of("vertical_access")),
                        new MKWorkspaceRoleSchema("tower.top_cap", "cap", "top_cap", true, false, Set.of("terminal_top")),
                        new MKWorkspaceRoleSchema("tower.basement_entry", "floor", "room", false, false, Set.of("vertical_access")),
                        new MKWorkspaceRoleSchema("tower.basement_floor", "floor", "room", false, false, Set.of("vertical_access")),
                        new MKWorkspaceRoleSchema("tower.basement_cap_approach", "approach", "basement_cap_approach", false, false, Set.of("vertical_access")),
                        new MKWorkspaceRoleSchema("tower.basement_cap", "cap", "terminal", true, false, Set.of("terminal_bottom")),
                        new MKWorkspaceRoleSchema("tower.linear_run.main", "connector", "room", false, false, Set.of("linear_run", "main_path")),
                        new MKWorkspaceRoleSchema("tower.linear_run.branch", "connector", "room", false, false, Set.of("linear_run", "branch_path")),
                        new MKWorkspaceRoleSchema("tower.linear_run.branch_cap", "connector", "room", true, false, Set.of("linear_run", "branch_cap")),
                        new MKWorkspaceRoleSchema("tower.linear_run.main_ending", "connector", "room", true, false, Set.of("linear_run", "main_ending"))
                )
        );
    }

    @Override
    public List<MKPlannedPiece> createCanonicalPieces(MKStructureWorkspace workspace) {
        ArrayList<MKPlannedPiece> pieces = workspace.familyDefinitions().stream()
                .filter(family -> shouldCreateFamily(workspace, family))
                .map(family -> createPieceForFamily(workspace, family))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        pieces.addAll(createLinearRunPieces(workspace));
        return List.copyOf(pieces);
    }

    private boolean shouldCreateFamily(MKStructureWorkspace workspace, MKTowerWorkspaceFamilyDefinition family) {
        return switch (family.pieceRole()) {
            case TOP_CAP_APPROACH -> workspace.floorSettings().topCapApproachEnabled();
            case BASEMENT_CAP_APPROACH -> workspace.floorSettings().basementCapApproachEnabled();
            default -> true;
        };
    }

    private MKTowerWorkspaceCategoryProfile fallbackProfile(MKTowerWorkspaceCategory category,
                                                            MKWorkspaceDimensions dimensions) {
        return MKTowerWorkspaceCategoryProfile.createDefaults(dimensions)
                .stream()
                .filter(profile -> profile.category() == category)
                .findFirst()
                .orElseThrow();
    }

    private MKPlannedPiece createPieceForFamily(MKStructureWorkspace workspace, MKTowerWorkspaceFamilyDefinition family) {
        String stairPlacement = workspace.verticalAccessSpec().placement().getSerializedName();
        int hallWidth = workspace.verticalAccessSpec().shaftSize();
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
                                            new MKPlannedConnector(MKConnectorRole.CONNECT_UP, Direction.UP, hallWidth, hallWidth, "connect_up"),
                                            new MKPlannedConnector(MKConnectorRole.CONNECT_DOWN, Direction.DOWN, hallWidth, hallWidth, "connect_down_entry")
                                    ) : List.of(),
                            family,
                            workspace
                    ),
                    buildRoomTags(workspace, "entry", family, stairPlacement, "both",
                            roomRuntimeInfo(true, MKJigsawPieceRole.ROOM, 0, 0, false, false, family))
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
                                                    EMPTY_POOL, "connect_up"),
                                            new MKPlannedConnector(MKConnectorRole.CONNECT_UP, Direction.UP, hallWidth, hallWidth, "connect_up")
                                    ) : List.of(),
                            family,
                            workspace
                    ),
                    buildRoomTags(workspace, "floor", family, stairPlacement, "both",
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
                                                    EMPTY_POOL, "connect_up"),
                                            new MKPlannedConnector(MKConnectorRole.TOP_CAP_FORWARD, Direction.UP, hallWidth, hallWidth, "top_cap")
                                    ) : List.of(),
                            family,
                            workspace
                    ),
                    buildRoomTags(workspace, "top_cap_approach", family, stairPlacement, "up",
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
                                    topCapConnectors(workspace, hallWidth) : List.of(),
                            family,
                            workspace
                    ),
                    buildRoomTags(workspace, "top_cap", family, stairPlacement, "up", true, false,
                            topCapRuntimeInfo(workspace, family))
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
                                                    EMPTY_POOL, "connect_down_entry"),
                                            new MKPlannedConnector(MKConnectorRole.CONNECT_DOWN, Direction.DOWN, hallWidth, hallWidth, "connect_down")
                                    ) : List.of(),
                            family,
                            workspace
                    ),
                    buildRoomTags(workspace, "basement_entry", family, stairPlacement, "down",
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
                                                    EMPTY_POOL, "connect_down"),
                                            new MKPlannedConnector(MKConnectorRole.CONNECT_DOWN, Direction.DOWN, hallWidth, hallWidth, "connect_down")
                                    ) : List.of(),
                            family,
                            workspace
                    ),
                    buildRoomTags(workspace, "basement_main", family, stairPlacement, "down",
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
                                                    EMPTY_POOL, "connect_down"),
                                            new MKPlannedConnector(MKConnectorRole.TOP_CAP_FORWARD, Direction.DOWN, hallWidth, hallWidth, "bottom_cap")
                                    ) : List.of(),
                            family,
                            workspace
                    ),
                    buildRoomTags(workspace, "basement_cap_approach", family, stairPlacement, "down",
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
                                    basementCapConnectors(workspace, hallWidth) : List.of(),
                            family,
                            workspace
                    ),
                    buildRoomTags(workspace, "basement_cap", family, stairPlacement, "down", false, true,
                            basementCapRuntimeInfo(workspace, family))
            );
            case HALLWAY -> throw new IllegalStateException("tower families do not directly create hallway pieces");
        };
    }

    private List<MKPlannedConnector> topCapConnectors(MKStructureWorkspace workspace, int hallWidth) {
        if (workspace.floorSettings().topCapApproachEnabled()) {
            return List.of(new MKPlannedConnector(MKConnectorRole.TOP_CAP_BACK, Direction.DOWN, hallWidth, hallWidth,
                    EMPTY_POOL, "top_cap"));
        }
        return List.of(new MKPlannedConnector(MKConnectorRole.CONNECT_DOWN, Direction.DOWN, hallWidth, hallWidth,
                EMPTY_POOL, "connect_up"));
    }

    private MKWorkspaceRuntimePieceInfo topCapRuntimeInfo(MKStructureWorkspace workspace,
                                                          MKTowerWorkspaceFamilyDefinition family) {
        if (workspace.floorSettings().topCapApproachEnabled()) {
            return roomRuntimeInfo(false, MKJigsawPieceRole.TOP_CAP, 0, 0, true, true, family);
        }
        return roomRuntimeInfo(false, MKJigsawPieceRole.TOP_CAP, 1, 1, true, true, family);
    }

    private List<MKPlannedConnector> basementCapConnectors(MKStructureWorkspace workspace, int hallWidth) {
        if (workspace.floorSettings().basementCapApproachEnabled()) {
            return List.of(new MKPlannedConnector(MKConnectorRole.TOP_CAP_BACK, Direction.UP, hallWidth, hallWidth,
                    EMPTY_POOL, "bottom_cap"));
        }
        return List.of(new MKPlannedConnector(MKConnectorRole.CONNECT_UP, Direction.UP, hallWidth, hallWidth,
                EMPTY_POOL, "connect_down"));
    }

    private MKWorkspaceRuntimePieceInfo basementCapRuntimeInfo(MKStructureWorkspace workspace,
                                                               MKTowerWorkspaceFamilyDefinition family) {
        if (workspace.floorSettings().basementCapApproachEnabled()) {
            return roomRuntimeInfo(false, MKJigsawPieceRole.TERMINAL, 0, 0, true, true, family);
        }
        return roomRuntimeInfo(false, MKJigsawPieceRole.TERMINAL, 1, -1, true, false, family);
    }

    private List<MKPlannedPiece> createLinearRunPieces(MKStructureWorkspace workspace) {
        return workspace.linearRunFamilies().stream()
                .flatMap(linearRun -> createLinearRunPieces(workspace, linearRun).stream())
                .toList();
    }

    private List<MKPlannedPiece> createLinearRunPieces(MKStructureWorkspace workspace,
                                                       MKWorkspaceLinearRunFamilyDefinition linearRun) {
        ResolvedOpeningProfile opening = workspace.openingProfiles().stream()
                .filter(profile -> profile.profileId().equals(linearRun.openingProfileId()))
                .findFirst()
                .map(profile -> new ResolvedOpeningProfile(profile.profileId(), profile.openingWidth(), profile.openingHeight()))
                .orElseThrow(() -> new IllegalStateException("missing linear run opening profile " + linearRun.openingProfileId()));
        ArrayList<MKPlannedPiece> pieces = new ArrayList<>();
        if (!linearRun.supportedShapes().contains(MKWorkspaceLinearRunPieceShape.STRAIGHT)) {
            return List.of();
        }
        if (linearRun.allowOnMainPath()) {
            pieces.add(createLinearRunPiece(workspace, linearRun, opening, HallwayPathKind.MAIN));
        }
        if (linearRun.allowOnBranchPath()) {
            pieces.add(createLinearRunPiece(workspace, linearRun, opening, HallwayPathKind.BRANCH));
        }
        return List.copyOf(pieces);
    }

    private MKPlannedPiece createLinearRunPiece(MKStructureWorkspace workspace,
                                                MKWorkspaceLinearRunFamilyDefinition linearRun,
                                                ResolvedOpeningProfile opening, HallwayPathKind pathKind) {
        int westOffset = Math.max(0, -linearRun.slopeDelta());
        int eastOffset = Math.max(0, linearRun.slopeDelta());
        LinkedHashMap<String, String> tags = new LinkedHashMap<>();
        tags.put("topology_role", "linear_run_" + linearRun.linearRunId() + "_" + pathKind.serializedName);
        tags.put("tower_piece_kind", "linear_run");
        tags.put("workspace_linear_run_family_id", linearRun.linearRunId());
        tags.put("workspace_linear_run_kind", linearRun.kind().getSerializedName());
        tags.put("workspace_linear_run_projection", linearRun.projection().getSerializedName());
        tags.put("workspace_linear_run_shape", MKWorkspaceLinearRunPieceShape.STRAIGHT.getSerializedName());
        tags.put("workspace_linear_run_path_kind", pathKind.serializedName);
        tags.put("workspace_linear_run_slope_delta", Integer.toString(linearRun.slopeDelta()));
        tags.put("workspace_opening_profile_id", linearRun.openingProfileId());
        tags.put("workspace_hallway_family_id", linearRun.linearRunId());
        tags.put("workspace_hallway_path_kind", pathKind.serializedName);
        tags.put("workspace_hallway_slope_delta", Integer.toString(linearRun.slopeDelta()));
        MKWorkspacePaletteTags.apply(tags, paletteResolver.resolveFamily(workspace, linearRun));
        new MKWorkspaceRuntimePieceInfo(false, MKJigsawPieceRole.ROOM, 0, 0,
                pathKind == HallwayPathKind.MAIN, pathKind == HallwayPathKind.BRANCH, false, false).applyToTags(tags);
        String hallwayPool = hallwayPoolName(linearRun.openingProfileId(), pathKind);
        MKConnectorRole westRole = pathKind == HallwayPathKind.MAIN ? MKConnectorRole.MAIN_FORWARD : MKConnectorRole.BRANCH;
        MKConnectorRole eastRole = pathKind == HallwayPathKind.MAIN ? MKConnectorRole.MAIN_BACK : MKConnectorRole.BRANCH;
        return new MKPlannedPiece(
                MKWorkspacePieceRole.HALLWAY,
                "linear_run_" + linearRun.linearRunId() + "_" + pathKind.serializedName,
                linearRun.length(),
                linearRun.interiorWidth(),
                linearRun.interiorHeight() + Math.abs(linearRun.slopeDelta()),
                List.of(
                        new MKPlannedConnector(westRole, Direction.WEST,
                                opening.openingWidth(), opening.openingHeight(), 0, westOffset,
                                EMPTY_POOL, hallwayPool),
                        new MKPlannedConnector(eastRole, Direction.EAST,
                                opening.openingWidth(), opening.openingHeight(), 0, eastOffset,
                                EMPTY_POOL, hallwayPool)
                ),
                tags
        );
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
                                              MKTowerWorkspaceFamilyDefinition family, String stairPlacement,
                                              String stairDirection, MKWorkspaceRuntimePieceInfo runtimeInfo) {
        return buildRoomTags(workspace, topologyRole, family, stairPlacement, stairDirection, false, false, runtimeInfo);
    }

    private Map<String, String> buildRoomTags(MKStructureWorkspace workspace, String topologyRole,
                                              MKTowerWorkspaceFamilyDefinition family, String stairPlacement,
                                              String stairDirection, boolean topCap, boolean bottomCap,
                                              MKWorkspaceRuntimePieceInfo runtimeInfo) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>();
        tags.put("topology_role", topologyRole);
        tags.put("tower_piece_kind", "room");
        tags.put("workspace_family_id", family.baseName());
        tags.put("workspace_horizontal_exits", family.horizontalExitSummary());
        tags.put("workspace_horizontal_extrusion_mode", family.horizontalExtrusionMode().getSerializedName());
        tags.put("workspace_category", family.category().getSerializedName());
        applyVoidMarginTags(family, tags);
        tags.put(MKWorkspaceVerticalAccessTags.ENABLED_TAG, Boolean.toString(family.supportsVerticalAccess()));
        if (family.supportsVerticalAccess()) {
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

