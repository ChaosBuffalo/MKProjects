package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawPieceRole;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceCategory;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceCategoryProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKVerticalAccessPlacement;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessSpec;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRuntimePieceInfo;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessTags;
import net.minecraft.core.Direction;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MKTowerWorkspacePlanner implements MKWorkspacePlanner {
    @Override
    public List<MKPlannedPiece> createCanonicalPieces(MKStructureWorkspace workspace) {
        return workspace.familyDefinitions().stream()
                .map(family -> createPieceForFamily(workspace, family))
                .toList();
    }

    private MKTowerWorkspaceCategoryProfile fallbackProfile(MKTowerWorkspaceCategory category,
                                                            MKWorkspaceDimensions dimensions) {
        return MKTowerWorkspaceCategoryProfile.createDefaults(dimensions,
                MKWorkspaceVerticalAccessSpec.fromLegacy(dimensions, MKVerticalAccessPlacement.CENTER,
                        MKWorkspaceStairAuthoringConfig.defaultConfig()))
                .stream()
                .filter(profile -> profile.category() == category)
                .findFirst()
                .orElseThrow();
    }

    private MKPlannedPiece createPieceForFamily(MKStructureWorkspace workspace, MKTowerWorkspaceFamilyDefinition family) {
        MKWorkspaceDimensions dimensions = workspace.dimensions();
        MKTowerWorkspaceCategoryProfile profile = workspace.categoryProfile(family.category())
                .orElseGet(() -> fallbackProfile(family.category(), dimensions));
        String stairPlacement = workspace.verticalAccessSpec().placement().getSerializedName();
        int hallWidth = workspace.verticalAccessSpec().shaftSize();
        return switch (family.pieceRole()) {
            case ENTRY -> new MKPlannedPiece(
                    family.pieceRole(),
                    family.baseName(),
                    profile.roomWidth(),
                    profile.roomLength(),
                    profile.defaultHeight(),
                    connectorsWithBranches(
                            List.of(
                                    new MKPlannedConnector(MKConnectorRole.MAIN_BACK, Direction.SOUTH,
                                            profile.mainOpeningWidth(), profile.mainOpeningHeight(), "minecraft:empty"),
                                    new MKPlannedConnector(MKConnectorRole.CONNECT_UP, Direction.UP, hallWidth, hallWidth, "connect_up"),
                                    new MKPlannedConnector(MKConnectorRole.CONNECT_DOWN, Direction.DOWN, hallWidth, hallWidth, "connect_down_entry")
                            ),
                            family,
                            profile
                    ),
                    buildTags("entry", family, stairPlacement, "both",
                            new MKWorkspaceRuntimePieceInfo(true, MKJigsawPieceRole.ROOM, 0, 0,
                                    true, false, false, false))
            );
            case FLOOR_MAIN -> new MKPlannedPiece(
                    family.pieceRole(),
                    family.baseName(),
                    profile.roomWidth(),
                    profile.roomLength(),
                    profile.defaultHeight(),
                    connectorsWithBranches(
                            List.of(
                                    new MKPlannedConnector(MKConnectorRole.CONNECT_DOWN, Direction.DOWN, hallWidth, hallWidth,
                                            "minecraft:empty", "connect_up"),
                                    new MKPlannedConnector(MKConnectorRole.CONNECT_UP, Direction.UP, hallWidth, hallWidth, "connect_up")
                            ),
                            family,
                            profile
                    ),
                    buildTags("floor", family, stairPlacement, "both",
                            new MKWorkspaceRuntimePieceInfo(false, MKJigsawPieceRole.ROOM, 1, 1,
                                    true, false, false, false))
            );
            case BOSS_APPROACH -> new MKPlannedPiece(
                    family.pieceRole(),
                    family.baseName(),
                    profile.roomWidth(),
                    profile.roomLength(),
                    profile.defaultHeight(),
                    connectorsWithBranches(
                            List.of(
                                    new MKPlannedConnector(MKConnectorRole.CONNECT_DOWN, Direction.DOWN, hallWidth, hallWidth,
                                            "minecraft:empty", "connect_up"),
                                    new MKPlannedConnector(MKConnectorRole.BOSS_FORWARD, Direction.UP, hallWidth, hallWidth, "boss_cap")
                            ),
                            family,
                            profile
                    ),
                    buildTags("boss_approach", family, stairPlacement, "up",
                            new MKWorkspaceRuntimePieceInfo(false, MKJigsawPieceRole.BOSS_APPROACH, 1, 1,
                                    true, false, false, true))
            );
            case BOSS_CAP -> new MKPlannedPiece(
                    family.pieceRole(),
                    family.baseName(),
                    profile.roomWidth(),
                    profile.roomLength(),
                    profile.defaultHeight(),
                    connectorsWithBranches(
                            List.of(new MKPlannedConnector(MKConnectorRole.BOSS_BACK, Direction.DOWN, hallWidth, hallWidth,
                                    "minecraft:empty", "boss_cap")),
                            family,
                            profile
                    ),
                    buildTags("boss_cap", family, stairPlacement, "up", true, false,
                            new MKWorkspaceRuntimePieceInfo(false, MKJigsawPieceRole.BOSS, 0, 0,
                                    true, false, true, true))
            );
            case BASEMENT_ENTRY -> new MKPlannedPiece(
                    family.pieceRole(),
                    family.baseName(),
                    profile.roomWidth(),
                    profile.roomLength(),
                    profile.defaultHeight(),
                    connectorsWithBranches(
                            List.of(
                                    new MKPlannedConnector(MKConnectorRole.CONNECT_UP, Direction.UP, hallWidth, hallWidth,
                                            "minecraft:empty", "connect_down_entry"),
                                    new MKPlannedConnector(MKConnectorRole.CONNECT_DOWN, Direction.DOWN, hallWidth, hallWidth, "connect_down")
                            ),
                            family,
                            profile
                    ),
                    buildTags("basement_entry", family, stairPlacement, "down",
                            new MKWorkspaceRuntimePieceInfo(false, MKJigsawPieceRole.ROOM, 1, -1,
                                    true, false, false, false))
            );
            case BASEMENT_MAIN -> new MKPlannedPiece(
                    family.pieceRole(),
                    family.baseName(),
                    profile.roomWidth(),
                    profile.roomLength(),
                    profile.defaultHeight(),
                    connectorsWithBranches(
                            List.of(
                                    new MKPlannedConnector(MKConnectorRole.CONNECT_UP, Direction.UP, hallWidth, hallWidth,
                                            "minecraft:empty", "connect_down"),
                                    new MKPlannedConnector(MKConnectorRole.CONNECT_DOWN, Direction.DOWN, hallWidth, hallWidth, "connect_down")
                            ),
                            family,
                            profile
                    ),
                    buildTags("basement_main", family, stairPlacement, "down",
                            new MKWorkspaceRuntimePieceInfo(false, MKJigsawPieceRole.ROOM, 1, -1,
                                    true, false, false, false))
            );
            case BASEMENT_CAP -> new MKPlannedPiece(
                    family.pieceRole(),
                    family.baseName(),
                    profile.roomWidth(),
                    profile.roomLength(),
                    profile.defaultHeight(),
                    connectorsWithBranches(
                            List.of(new MKPlannedConnector(MKConnectorRole.CONNECT_UP, Direction.UP, hallWidth, hallWidth,
                                    "minecraft:empty", "connect_down")),
                            family,
                            profile
                    ),
                    buildTags("basement_cap", family, stairPlacement, "down", false, true,
                            new MKWorkspaceRuntimePieceInfo(false, MKJigsawPieceRole.TERMINAL, 1, -1,
                                    true, false, true, false))
            );
        };
    }

    private List<MKPlannedConnector> connectorsWithBranches(List<MKPlannedConnector> baseConnectors,
                                                            MKTowerWorkspaceFamilyDefinition family,
                                                            MKTowerWorkspaceCategoryProfile profile) {
        java.util.ArrayList<MKPlannedConnector> connectors = new java.util.ArrayList<>(baseConnectors);
        for (Direction direction : family.branchExitMask().directions()) {
            connectors.add(new MKPlannedConnector(MKConnectorRole.BRANCH, direction,
                    profile.branchOpeningWidth(), profile.branchOpeningHeight(), "minecraft:empty"));
        }
        return List.copyOf(connectors);
    }

    private Map<String, String> buildTags(String topologyRole, String stairPlacement, String stairDirection,
                                          MKWorkspaceRuntimePieceInfo runtimeInfo) {
        return buildTags(topologyRole, null, stairPlacement, stairDirection, false, false, runtimeInfo);
    }

    private Map<String, String> buildTags(String topologyRole, MKTowerWorkspaceFamilyDefinition family, String stairPlacement,
                                          String stairDirection, MKWorkspaceRuntimePieceInfo runtimeInfo) {
        return buildTags(topologyRole, family, stairPlacement, stairDirection, false, false, runtimeInfo);
    }

    private Map<String, String> buildTags(String topologyRole, String stairPlacement, String stairDirection,
                                          boolean topCap, boolean bottomCap, MKWorkspaceRuntimePieceInfo runtimeInfo) {
        return buildTags(topologyRole, null, stairPlacement, stairDirection, topCap, bottomCap, runtimeInfo);
    }

    private Map<String, String> buildTags(String topologyRole, MKTowerWorkspaceFamilyDefinition family, String stairPlacement,
                                          String stairDirection, boolean topCap, boolean bottomCap,
                                          MKWorkspaceRuntimePieceInfo runtimeInfo) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>();
        tags.put("topology_role", topologyRole);
        tags.put("tower_piece_kind", "room");
        if (family != null) {
            tags.put("workspace_family_id", family.baseName());
            tags.put("workspace_branch_exit_mask", family.branchExitMask().getSerializedName());
            tags.put("workspace_category", family.category().getSerializedName());
        }
        tags.put(MKWorkspaceVerticalAccessTags.ENABLED_TAG, "true");
        tags.put(MKWorkspaceVerticalAccessTags.PLACEMENT_TAG, stairPlacement);
        tags.put(MKWorkspaceVerticalAccessTags.DIRECTION_TAG, stairDirection);
        if (topCap) {
            tags.put(MKWorkspaceVerticalAccessTags.TOP_CAP_TAG, "true");
        }
        if (bottomCap) {
            tags.put(MKWorkspaceVerticalAccessTags.BOTTOM_CAP_TAG, "true");
        }
        runtimeInfo.applyToTags(tags);
        return tags;
    }
}

