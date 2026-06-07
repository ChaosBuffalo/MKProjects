package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKVerticalAccessPlacement;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTowerStackSettings;

public record MKTowerStackDefinition(
        String stackId,
        int minMainFloors,
        int mainFloors,
        int minBasementFloors,
        int basementFloors,
        int shaftSize,
        MKVerticalAccessPlacement verticalAccessPlacement,
        MKWorkspaceStairAuthoringConfig stairConfig,
        boolean topCapApproachEnabled,
        boolean basementEntryEnabled,
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

    public static MKTowerStackDefinition towerPrimary(MKWorkspaceTowerStackSettings stackSettings) {
        return new MKTowerStackDefinition(
                stackSettings.stackId(),
                stackSettings.minMainFloors(),
                stackSettings.mainFloors(),
                stackSettings.minBasementFloors(),
                stackSettings.basementFloors(),
                stackSettings.shaftSize(),
                stackSettings.verticalAccessPlacement(),
                stackSettings.stairConfig(),
                stackSettings.topCapApproachEnabled(),
                stackSettings.basementEntryEnabled(),
                stackSettings.basementCapApproachEnabled(),
                true,
                "connect_up",
                "connect_down_entry",
                "connect_down",
                "top_cap",
                "bottom_cap",
                false
        );
    }

    public static MKTowerStackDefinition scoped(String stackId, boolean startPiece,
                                                MKWorkspaceTowerStackSettings stackSettings) {
        String prefix = "tower_stacks/" + stackId.replace('.', '/');
        return new MKTowerStackDefinition(
                stackId,
                stackSettings.minMainFloors(),
                stackSettings.mainFloors(),
                stackSettings.minBasementFloors(),
                stackSettings.basementFloors(),
                stackSettings.shaftSize(),
                stackSettings.verticalAccessPlacement(),
                stackSettings.stairConfig(),
                stackSettings.topCapApproachEnabled(),
                stackSettings.basementEntryEnabled(),
                stackSettings.basementCapApproachEnabled(),
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
