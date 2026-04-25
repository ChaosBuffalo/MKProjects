package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKConnectorRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceRole;
import net.minecraft.core.Direction;

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
                Map.of(
                        "topology_role", "entry",
                        "tower_piece_kind", "room",
                        "tower_stair_placement", stairPlacement,
                        "supports_stair_generation", "true",
                        "stair_direction", "both"
                )
        );
        MKPlannedPiece floorMain = new MKPlannedPiece(
                MKWorkspacePieceRole.FLOOR_MAIN,
                "floor_main",
                roomWidth,
                roomLength,
                roomHeight,
                List.of(
                        new MKPlannedConnector(MKConnectorRole.CONNECT_DOWN, Direction.DOWN, hallWidth, hallWidth, "minecraft:empty"),
                        new MKPlannedConnector(MKConnectorRole.CONNECT_UP, Direction.UP, hallWidth, hallWidth, "connect_up")
                ),
                Map.of(
                        "topology_role", "floor",
                        "tower_piece_kind", "room",
                        "tower_stair_placement", stairPlacement,
                        "supports_stair_generation", "true",
                        "stair_direction", "both"
                )
        );
        MKPlannedPiece bossApproach = new MKPlannedPiece(
                MKWorkspacePieceRole.BOSS_APPROACH,
                "boss_approach",
                roomWidth,
                roomLength,
                roomHeight,
                List.of(
                        new MKPlannedConnector(MKConnectorRole.CONNECT_DOWN, Direction.DOWN, hallWidth, hallWidth, "minecraft:empty"),
                        new MKPlannedConnector(MKConnectorRole.BOSS_FORWARD, Direction.UP, hallWidth, hallWidth, "boss_cap")
                ),
                Map.of(
                        "topology_role", "boss_approach",
                        "tower_piece_kind", "room",
                        "tower_stair_placement", stairPlacement,
                        "supports_stair_generation", "true",
                        "stair_direction", "up"
                )
        );
        MKPlannedPiece basementEntry = new MKPlannedPiece(
                MKWorkspacePieceRole.BASEMENT_ENTRY,
                "basement_entry",
                roomWidth,
                roomLength,
                basementHeight,
                List.of(
                        new MKPlannedConnector(MKConnectorRole.CONNECT_UP, Direction.UP, hallWidth, hallWidth, "minecraft:empty"),
                        new MKPlannedConnector(MKConnectorRole.CONNECT_DOWN, Direction.DOWN, hallWidth, hallWidth, "connect_down")
                ),
                Map.of(
                        "topology_role", "basement_entry",
                        "tower_piece_kind", "room",
                        "tower_stair_placement", stairPlacement,
                        "supports_stair_generation", "true",
                        "stair_direction", "down"
                )
        );
        MKPlannedPiece basementMain = new MKPlannedPiece(
                MKWorkspacePieceRole.BASEMENT_MAIN,
                "basement_main",
                roomWidth,
                roomLength,
                basementHeight,
                List.of(
                        new MKPlannedConnector(MKConnectorRole.CONNECT_UP, Direction.UP, hallWidth, hallWidth, "minecraft:empty"),
                        new MKPlannedConnector(MKConnectorRole.CONNECT_DOWN, Direction.DOWN, hallWidth, hallWidth, "connect_down")
                ),
                Map.of(
                        "topology_role", "basement_main",
                        "tower_piece_kind", "room",
                        "tower_stair_placement", stairPlacement,
                        "supports_stair_generation", "true",
                        "stair_direction", "down"
                )
        );
        MKPlannedPiece basementCap = new MKPlannedPiece(
                MKWorkspacePieceRole.BASEMENT_CAP,
                "basement_cap",
                roomWidth,
                roomLength,
                basementHeight,
                List.of(new MKPlannedConnector(MKConnectorRole.CONNECT_UP, Direction.UP, hallWidth, hallWidth, "minecraft:empty")),
                Map.of(
                        "topology_role", "basement_cap",
                        "tower_piece_kind", "room",
                        "tower_stair_placement", stairPlacement,
                        "supports_stair_generation", "true",
                        "stair_direction", "down"
                )
        );
        MKPlannedPiece bossCap = new MKPlannedPiece(
                MKWorkspacePieceRole.BOSS_CAP,
                "boss_cap",
                roomWidth,
                roomLength,
                roomHeight,
                List.of(new MKPlannedConnector(MKConnectorRole.BOSS_BACK, Direction.DOWN, hallWidth, hallWidth, "minecraft:empty")),
                Map.of(
                        "topology_role", "boss_cap",
                        "tower_piece_kind", "room",
                        "tower_stair_placement", stairPlacement,
                        "supports_stair_generation", "true",
                        "stair_direction", "up"
                )
        );
        return List.of(entry, floorMain, bossApproach, bossCap, basementEntry, basementMain, basementCap);
    }
}
