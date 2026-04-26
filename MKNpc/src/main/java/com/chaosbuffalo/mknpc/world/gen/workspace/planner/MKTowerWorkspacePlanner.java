package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawPieceRole;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceCategory;
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
        MKWorkspaceDimensions dimensions = workspace.dimensions();
        String stairPlacement = workspace.verticalAccessSpec().placement().getSerializedName();
        MKTowerWorkspaceCategoryProfile entryProfile = workspace.categoryProfile(MKTowerWorkspaceCategory.ENTRY)
                .orElseGet(() -> fallbackProfile(MKTowerWorkspaceCategory.ENTRY, dimensions));
        MKTowerWorkspaceCategoryProfile mainProfile = workspace.categoryProfile(MKTowerWorkspaceCategory.MAIN)
                .orElseGet(() -> fallbackProfile(MKTowerWorkspaceCategory.MAIN, dimensions));
        MKTowerWorkspaceCategoryProfile basementProfile = workspace.categoryProfile(MKTowerWorkspaceCategory.BASEMENT)
                .orElseGet(() -> fallbackProfile(MKTowerWorkspaceCategory.BASEMENT, dimensions));
        MKTowerWorkspaceCategoryProfile bossProfile = workspace.categoryProfile(MKTowerWorkspaceCategory.BOSS)
                .orElseGet(() -> fallbackProfile(MKTowerWorkspaceCategory.BOSS, dimensions));
        int hallWidth = workspace.verticalAccessSpec().shaftSize();

        MKPlannedPiece entry = new MKPlannedPiece(
                MKWorkspacePieceRole.ENTRY,
                "entry",
                entryProfile.roomWidth(),
                entryProfile.roomLength(),
                entryProfile.defaultHeight(),
                List.of(
                        new MKPlannedConnector(MKConnectorRole.MAIN_BACK, Direction.SOUTH,
                                entryProfile.mainOpeningWidth(), entryProfile.mainOpeningHeight(), "minecraft:empty"),
                        new MKPlannedConnector(MKConnectorRole.CONNECT_UP, Direction.UP, hallWidth, hallWidth, "connect_up"),
                        new MKPlannedConnector(MKConnectorRole.CONNECT_DOWN, Direction.DOWN, hallWidth, hallWidth, "connect_down_entry")
                ),
                buildTags("entry", stairPlacement, "both",
                        new MKWorkspaceRuntimePieceInfo(true, MKJigsawPieceRole.ROOM, 0, 0,
                                true, false, false, false))
        );
        MKPlannedPiece floorMain = new MKPlannedPiece(
                MKWorkspacePieceRole.FLOOR_MAIN,
                "floor_main",
                mainProfile.roomWidth(),
                mainProfile.roomLength(),
                mainProfile.defaultHeight(),
                List.of(
                        new MKPlannedConnector(MKConnectorRole.CONNECT_DOWN, Direction.DOWN, hallWidth, hallWidth,
                                "minecraft:empty", "connect_up"),
                        new MKPlannedConnector(MKConnectorRole.CONNECT_UP, Direction.UP, hallWidth, hallWidth, "connect_up")
                ),
                buildTags("floor", stairPlacement, "both",
                        new MKWorkspaceRuntimePieceInfo(false, MKJigsawPieceRole.ROOM, 1, 1,
                                true, false, false, false))
        );
        MKPlannedPiece bossApproach = new MKPlannedPiece(
                MKWorkspacePieceRole.BOSS_APPROACH,
                "boss_approach",
                bossProfile.roomWidth(),
                bossProfile.roomLength(),
                bossProfile.defaultHeight(),
                List.of(
                        new MKPlannedConnector(MKConnectorRole.CONNECT_DOWN, Direction.DOWN, hallWidth, hallWidth,
                                "minecraft:empty", "connect_up"),
                        new MKPlannedConnector(MKConnectorRole.BOSS_FORWARD, Direction.UP, hallWidth, hallWidth, "boss_cap")
                ),
                buildTags("boss_approach", stairPlacement, "up",
                        new MKWorkspaceRuntimePieceInfo(false, MKJigsawPieceRole.BOSS_APPROACH, 1, 1,
                                true, false, false, true))
        );
        MKPlannedPiece basementEntry = new MKPlannedPiece(
                MKWorkspacePieceRole.BASEMENT_ENTRY,
                "basement_entry",
                basementProfile.roomWidth(),
                basementProfile.roomLength(),
                basementProfile.defaultHeight(),
                List.of(
                        new MKPlannedConnector(MKConnectorRole.CONNECT_UP, Direction.UP, hallWidth, hallWidth,
                                "minecraft:empty", "connect_down_entry"),
                        new MKPlannedConnector(MKConnectorRole.CONNECT_DOWN, Direction.DOWN, hallWidth, hallWidth, "connect_down")
                ),
                buildTags("basement_entry", stairPlacement, "down",
                        new MKWorkspaceRuntimePieceInfo(false, MKJigsawPieceRole.ROOM, 1, -1,
                                true, false, false, false))
        );
        MKPlannedPiece basementMain = new MKPlannedPiece(
                MKWorkspacePieceRole.BASEMENT_MAIN,
                "basement_main",
                basementProfile.roomWidth(),
                basementProfile.roomLength(),
                basementProfile.defaultHeight(),
                List.of(
                        new MKPlannedConnector(MKConnectorRole.CONNECT_UP, Direction.UP, hallWidth, hallWidth,
                                "minecraft:empty", "connect_down"),
                        new MKPlannedConnector(MKConnectorRole.CONNECT_DOWN, Direction.DOWN, hallWidth, hallWidth, "connect_down")
                ),
                buildTags("basement_main", stairPlacement, "down",
                        new MKWorkspaceRuntimePieceInfo(false, MKJigsawPieceRole.ROOM, 1, -1,
                                true, false, false, false))
        );
        MKPlannedPiece basementCap = new MKPlannedPiece(
                MKWorkspacePieceRole.BASEMENT_CAP,
                "basement_cap",
                basementProfile.roomWidth(),
                basementProfile.roomLength(),
                basementProfile.defaultHeight(),
                List.of(new MKPlannedConnector(MKConnectorRole.CONNECT_UP, Direction.UP, hallWidth, hallWidth,
                        "minecraft:empty", "connect_down")),
                buildTags("basement_cap", stairPlacement, "down", false, true,
                        new MKWorkspaceRuntimePieceInfo(false, MKJigsawPieceRole.TERMINAL, 1, -1,
                                true, false, true, false))
        );
        MKPlannedPiece bossCap = new MKPlannedPiece(
                MKWorkspacePieceRole.BOSS_CAP,
                "boss_cap",
                bossProfile.roomWidth(),
                bossProfile.roomLength(),
                bossProfile.defaultHeight(),
                List.of(new MKPlannedConnector(MKConnectorRole.BOSS_BACK, Direction.DOWN, hallWidth, hallWidth,
                        "minecraft:empty", "boss_cap")),
                buildTags("boss_cap", stairPlacement, "up", true, false,
                        new MKWorkspaceRuntimePieceInfo(false, MKJigsawPieceRole.BOSS, 0, 0,
                                true, false, true, true))
        );
        return List.of(entry, floorMain, bossApproach, bossCap, basementEntry, basementMain, basementCap);
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

    private Map<String, String> buildTags(String topologyRole, String stairPlacement, String stairDirection,
                                          MKWorkspaceRuntimePieceInfo runtimeInfo) {
        return buildTags(topologyRole, stairPlacement, stairDirection, false, false, runtimeInfo);
    }

    private Map<String, String> buildTags(String topologyRole, String stairPlacement, String stairDirection,
                                          boolean topCap, boolean bottomCap, MKWorkspaceRuntimePieceInfo runtimeInfo) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>();
        tags.put("topology_role", topologyRole);
        tags.put("tower_piece_kind", "room");
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

