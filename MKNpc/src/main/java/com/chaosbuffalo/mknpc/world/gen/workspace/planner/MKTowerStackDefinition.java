package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFloorSettings;

public record MKTowerStackDefinition(
        String stackId,
        int mainFloors,
        int basementFloors,
        boolean topCapApproachEnabled,
        boolean basementCapApproachEnabled,
        boolean startPiece,
        String connectUpPool,
        String connectDownEntryPool,
        String connectDownPool,
        String topCapPool,
        String bottomCapPool,
        boolean useFamilyTopologyRole
) {
    public MKTowerStackDefinition {
        stackId = stackId == null ? "" : stackId;
    }

    public static MKTowerStackDefinition legacyTower(MKTowerWorkspaceFloorSettings floorSettings) {
        return new MKTowerStackDefinition(
                "",
                floorSettings.mainFloors(),
                floorSettings.basementFloors(),
                floorSettings.topCapApproachEnabled(),
                floorSettings.basementCapApproachEnabled(),
                true,
                "connect_up",
                "connect_down_entry",
                "connect_down",
                "top_cap",
                "bottom_cap",
                false
        );
    }

    public static MKTowerStackDefinition scoped(String stackId, MKTowerWorkspaceFloorSettings floorSettings,
                                                boolean startPiece) {
        String prefix = "tower_stacks/" + stackId.replace('.', '/');
        return new MKTowerStackDefinition(
                stackId,
                floorSettings.mainFloors(),
                floorSettings.basementFloors(),
                floorSettings.topCapApproachEnabled(),
                floorSettings.basementCapApproachEnabled(),
                startPiece,
                prefix + "/connect_up",
                prefix + "/connect_down_entry",
                prefix + "/connect_down",
                prefix + "/top_cap",
                prefix + "/bottom_cap",
                true
        );
    }
}
