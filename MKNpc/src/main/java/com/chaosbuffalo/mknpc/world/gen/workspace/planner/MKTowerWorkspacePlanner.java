package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawPieceRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKHallwayFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKHorizontalOpeningProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceCategory;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceCategoryProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitPathKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKVerticalAccessPlacement;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRuntimePieceInfo;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessSpec;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessTags;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MKTowerWorkspacePlanner implements MKWorkspacePlanner {
    private static final String EMPTY_POOL = "minecraft:empty";
    private static final String HALLWAY_POOL_PREFIX = "hallways";
    private static final String FLOOR_BLOCK_TAG = "workspace_palette_floor";
    private static final String WALL_BLOCK_TAG = "workspace_palette_wall";
    private static final String CEILING_BLOCK_TAG = "workspace_palette_ceiling";

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
    public List<MKPlannedPiece> createCanonicalPieces(MKStructureWorkspace workspace) {
        ArrayList<MKPlannedPiece> pieces = workspace.familyDefinitions().stream()
                .map(family -> createPieceForFamily(workspace, family))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        pieces.addAll(createHallwayPieces(workspace));
        return List.copyOf(pieces);
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
                    buildRoomTags("entry", family, stairPlacement, "both",
                            new MKWorkspaceRuntimePieceInfo(true, MKJigsawPieceRole.ROOM, 0, 0,
                                    true, false, false, false))
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
                    buildRoomTags("floor", family, stairPlacement, "both",
                            new MKWorkspaceRuntimePieceInfo(false, MKJigsawPieceRole.ROOM, 1, 1,
                                    true, false, false, false))
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
                    buildRoomTags("top_cap_approach", family, stairPlacement, "up",
                            new MKWorkspaceRuntimePieceInfo(false, MKJigsawPieceRole.TOP_CAP_APPROACH, 1, 1,
                                    true, false, false, true))
            );
            case TOP_CAP -> new MKPlannedPiece(
                    family.pieceRole(),
                    family.baseName(),
                    family.roomWidth(),
                    family.roomLength(),
                    family.roomHeight(),
                    connectorsWithHorizontalExits(
                            family.supportsVerticalAccess() ?
                                    List.of(new MKPlannedConnector(MKConnectorRole.TOP_CAP_BACK, Direction.DOWN, hallWidth, hallWidth,
                                            EMPTY_POOL, "top_cap")) : List.of(),
                            family,
                            workspace
                    ),
                    buildRoomTags("top_cap", family, stairPlacement, "up", true, false,
                            new MKWorkspaceRuntimePieceInfo(false, MKJigsawPieceRole.TOP_CAP, 0, 0,
                                    true, false, true, true))
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
                    buildRoomTags("basement_entry", family, stairPlacement, "down",
                            new MKWorkspaceRuntimePieceInfo(false, MKJigsawPieceRole.ROOM, 1, -1,
                                    true, false, false, false))
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
                    buildRoomTags("basement_main", family, stairPlacement, "down",
                            new MKWorkspaceRuntimePieceInfo(false, MKJigsawPieceRole.ROOM, 1, -1,
                                    true, false, false, false))
            );
            case BASEMENT_CAP -> new MKPlannedPiece(
                    family.pieceRole(),
                    family.baseName(),
                    family.roomWidth(),
                    family.roomLength(),
                    family.roomHeight(),
                    connectorsWithHorizontalExits(
                            family.supportsVerticalAccess() ?
                                    List.of(new MKPlannedConnector(MKConnectorRole.CONNECT_UP, Direction.UP, hallWidth, hallWidth,
                                            EMPTY_POOL, "connect_down")) : List.of(),
                            family,
                            workspace
                    ),
                    buildRoomTags("basement_cap", family, stairPlacement, "down", false, true,
                            new MKWorkspaceRuntimePieceInfo(false, MKJigsawPieceRole.TERMINAL, 1, -1,
                                    true, false, true, false))
            );
            case HALLWAY -> throw new IllegalStateException("tower families do not directly create hallway pieces");
        };
    }

    private List<MKPlannedPiece> createHallwayPieces(MKStructureWorkspace workspace) {
        return workspace.hallwayFamilies().stream()
                .flatMap(hallway -> createHallwayPieces(workspace, hallway).stream())
                .toList();
    }

    private List<MKPlannedPiece> createHallwayPieces(MKStructureWorkspace workspace, MKHallwayFamilyDefinition hallway) {
        ResolvedOpeningProfile opening = workspace.openingProfiles().stream()
                .filter(profile -> profile.profileId().equals(hallway.openingProfileId()))
                .findFirst()
                .map(profile -> new ResolvedOpeningProfile(profile.profileId(), profile.openingWidth(), profile.openingHeight()))
                .orElseThrow(() -> new IllegalStateException("missing hallway opening profile " + hallway.openingProfileId()));
        ArrayList<MKPlannedPiece> pieces = new ArrayList<>();
        if (hallway.allowOnMainPath()) {
            pieces.add(createHallwayPiece(workspace, hallway, opening, HallwayPathKind.MAIN));
        }
        if (hallway.allowOnBranchPath()) {
            pieces.add(createHallwayPiece(workspace, hallway, opening, HallwayPathKind.BRANCH));
        }
        return List.copyOf(pieces);
    }

    private MKPlannedPiece createHallwayPiece(MKStructureWorkspace workspace, MKHallwayFamilyDefinition hallway,
                                              ResolvedOpeningProfile opening, HallwayPathKind pathKind) {
        int westOffset = Math.max(0, -hallway.slopeDelta());
        int eastOffset = Math.max(0, hallway.slopeDelta());
        LinkedHashMap<String, String> tags = new LinkedHashMap<>();
        tags.put("topology_role", "hallway_" + hallway.hallwayId() + "_" + pathKind.serializedName);
        tags.put("tower_piece_kind", "hallway");
        tags.put("workspace_opening_profile_id", hallway.openingProfileId());
        tags.put("workspace_hallway_family_id", hallway.hallwayId());
        tags.put("workspace_hallway_path_kind", pathKind.serializedName);
        tags.put("workspace_hallway_slope_delta", Integer.toString(hallway.slopeDelta()));
        tags.put(FLOOR_BLOCK_TAG, hallway.floorBlock().toString());
        tags.put(WALL_BLOCK_TAG, hallway.wallBlock().toString());
        tags.put(CEILING_BLOCK_TAG, hallway.ceilingBlock().toString());
        new MKWorkspaceRuntimePieceInfo(false, MKJigsawPieceRole.ROOM, 0, 0,
                pathKind == HallwayPathKind.MAIN, pathKind == HallwayPathKind.BRANCH, false, false).applyToTags(tags);
        String hallwayPool = hallwayPoolName(hallway.openingProfileId(), pathKind);
        MKConnectorRole westRole = pathKind == HallwayPathKind.MAIN ? MKConnectorRole.MAIN_FORWARD : MKConnectorRole.BRANCH;
        MKConnectorRole eastRole = pathKind == HallwayPathKind.MAIN ? MKConnectorRole.MAIN_BACK : MKConnectorRole.BRANCH;
        return new MKPlannedPiece(
                MKWorkspacePieceRole.HALLWAY,
                "hallway_" + hallway.hallwayId() + "_" + pathKind.serializedName,
                hallway.length(),
                hallway.interiorWidth(),
                hallway.interiorHeight() + Math.abs(hallway.slopeDelta()),
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
        ArrayList<MKPlannedConnector> connectors = new ArrayList<>(baseConnectors);
        for (MKWorkspaceFamilyHorizontalExitDefinition exit : family.horizontalExits()) {
            ResolvedOpeningProfile opening = resolveOpeningProfile(workspace, exit.openingProfileId())
                    .orElseThrow(() -> new IllegalStateException("missing opening profile " + exit.openingProfileId() +
                            " for family " + family.baseName()));
            HallwayPathKind hallwayPathKind = exit.pathKind() == MKWorkspaceHorizontalExitPathKind.MAIN ?
                    HallwayPathKind.MAIN : HallwayPathKind.BRANCH;
            MKConnectorRole role = exit.pathKind() == MKWorkspaceHorizontalExitPathKind.MAIN ?
                    MKConnectorRole.MAIN_BACK : MKConnectorRole.BRANCH;
            String hallwayPool = resolveHallwayPool(workspace, opening.profileId(), hallwayPathKind);
            connectors.add(new MKPlannedConnector(role, exit.direction(),
                    opening.openingWidth(), opening.openingHeight(), hallwayPool));
        }
        return List.copyOf(connectors);
    }

    private Optional<ResolvedOpeningProfile> resolveOpeningProfile(MKStructureWorkspace workspace, String profileId) {
        return workspace.openingProfiles().stream()
                .filter(profile -> profile.profileId().equals(profileId))
                .findFirst()
                .map(profile -> new ResolvedOpeningProfile(profile.profileId(), profile.openingWidth(), profile.openingHeight()));
    }

    private String resolveHallwayPool(MKStructureWorkspace workspace, String openingProfileId, HallwayPathKind pathKind) {
        boolean hasCompatibleHallway = workspace.hallwayFamilies().stream().anyMatch(hallway ->
                hallway.openingProfileId().equals(openingProfileId) &&
                        (pathKind == HallwayPathKind.MAIN ? hallway.allowOnMainPath() : hallway.allowOnBranchPath()));
        return hasCompatibleHallway ? hallwayPoolName(openingProfileId, pathKind) : EMPTY_POOL;
    }

    private String hallwayPoolName(String openingProfileId, HallwayPathKind pathKind) {
        return HALLWAY_POOL_PREFIX + "/" + pathKind.serializedName + "/" + openingProfileId;
    }

    private Map<String, String> buildRoomTags(String topologyRole, MKTowerWorkspaceFamilyDefinition family, String stairPlacement,
                                              String stairDirection, MKWorkspaceRuntimePieceInfo runtimeInfo) {
        return buildRoomTags(topologyRole, family, stairPlacement, stairDirection, false, false, runtimeInfo);
    }

    private Map<String, String> buildRoomTags(String topologyRole, MKTowerWorkspaceFamilyDefinition family, String stairPlacement,
                                              String stairDirection, boolean topCap, boolean bottomCap,
                                              MKWorkspaceRuntimePieceInfo runtimeInfo) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>();
        tags.put("topology_role", topologyRole);
        tags.put("tower_piece_kind", "room");
        tags.put("workspace_family_id", family.baseName());
        tags.put("workspace_branch_exit_mask", family.legacyBranchExitMask().getSerializedName());
        tags.put("workspace_horizontal_exits", family.horizontalExitSummary());
        tags.put("workspace_category", family.category().getSerializedName());
        tags.put(MKWorkspaceVerticalAccessTags.ENABLED_TAG, Boolean.toString(family.supportsVerticalAccess()));
        if (family.supportsVerticalAccess()) {
            tags.put(MKWorkspaceVerticalAccessTags.PLACEMENT_TAG, stairPlacement);
            tags.put(MKWorkspaceVerticalAccessTags.DIRECTION_TAG, stairDirection);
        }
        if (family.supportsVerticalAccess() && topCap) {
            tags.put(MKWorkspaceVerticalAccessTags.TOP_CAP_TAG, "true");
        }
        if (family.supportsVerticalAccess() && bottomCap) {
            tags.put(MKWorkspaceVerticalAccessTags.BOTTOM_CAP_TAG, "true");
        }
        runtimeInfo.applyToTags(tags);
        return tags;
    }
}

