package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawPieceRole;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRuntimePieceInfo;
import net.minecraft.core.Direction;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MKTowerWorkspacePlanner implements MKWorkspacePlanner {
    @Override
    public List<MKPlannedPiece> createCanonicalPieces(MKStructureWorkspace workspace) {
        MKWorkspaceDimensions dimensions = workspace.dimensions();
        String stairPlacement = workspace.towerStairPlacement().getSerializedName();
        int roomWidth = dimensions.roomWidth();
        int roomLength = dimensions.roomLength();
        int entranceHeight = dimensions.entranceHeight();
        int roomHeight = dimensions.roomHeight();
        int basementHeight = dimensions.basementHeight();
        int hallWidth = dimensions.hallwayWidth();
        int doorwayWidth = dimensions.doorwayWidth();
        int doorwayHeight = dimensions.doorwayHeight();

        MKPlannedPiece entry = new MKPlannedPiece(
                MKWorkspacePieceRole.ENTRY,
                "entry",
                roomWidth,
                roomLength,
                entranceHeight,
                List.of(
                        new MKPlannedConnector(MKConnectorRole.MAIN_BACK, Direction.SOUTH, doorwayWidth, doorwayHeight, "minecraft:empty"),
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
                roomWidth,
                roomLength,
                roomHeight,
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
                roomWidth,
                roomLength,
                roomHeight,
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
                roomWidth,
                roomLength,
                basementHeight,
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
                roomWidth,
                roomLength,
                basementHeight,
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
                roomWidth,
                roomLength,
                basementHeight,
                List.of(new MKPlannedConnector(MKConnectorRole.CONNECT_UP, Direction.UP, hallWidth, hallWidth,
                        "minecraft:empty", "connect_down")),
                buildTags("basement_cap", stairPlacement, "down",
                        new MKWorkspaceRuntimePieceInfo(false, MKJigsawPieceRole.TERMINAL, 1, -1,
                                true, false, true, false))
        );
        MKPlannedPiece bossCap = new MKPlannedPiece(
                MKWorkspacePieceRole.BOSS_CAP,
                "boss_cap",
                roomWidth,
                roomLength,
                roomHeight,
                List.of(new MKPlannedConnector(MKConnectorRole.BOSS_BACK, Direction.DOWN, hallWidth, hallWidth,
                        "minecraft:empty", "boss_cap")),
                buildTags("boss_cap", stairPlacement, "up",
                        new MKWorkspaceRuntimePieceInfo(false, MKJigsawPieceRole.BOSS, 0, 0,
                                true, false, true, true))
        );
        return List.of(entry, floorMain, bossApproach, bossCap, basementEntry, basementMain, basementCap);
    }

    private Map<String, String> buildTags(String topologyRole, String stairPlacement, String stairDirection,
                                          MKWorkspaceRuntimePieceInfo runtimeInfo) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>();
        tags.put("topology_role", topologyRole);
        tags.put("tower_piece_kind", "room");
        tags.put("tower_stair_placement", stairPlacement);
        tags.put("supports_stair_generation", "true");
        tags.put("stair_direction", stairDirection);
        runtimeInfo.applyToTags(tags);
        return tags;
    }
}
