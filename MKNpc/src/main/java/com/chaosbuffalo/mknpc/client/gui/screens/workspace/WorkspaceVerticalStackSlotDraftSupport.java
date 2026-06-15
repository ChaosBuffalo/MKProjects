package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerStackBudget;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceStackSlot;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologySlotMetadata;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTowerStackFloorCounts;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalStackSettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKWorkspaceSlotSchema;

import java.util.List;
import java.util.Optional;

final class WorkspaceVerticalStackSlotDraftSupport {
    private WorkspaceVerticalStackSlotDraftSupport() {
    }

    static Optional<String> topologyGroupIdForFloorRole(String floorRole) {
        for (MKTowerWorkspaceStackSlot slot : MKTowerWorkspaceStackSlot.values()) {
            if (slot.suffix().equals(floorRole)) {
                return Optional.of(slot.topologyGroupId());
            }
        }
        return Optional.empty();
    }

    static boolean isTopCapSlot(String topologySlotId) {
        return MKTowerWorkspaceStackSlot.fromTopologySlotId(topologySlotId)
                .filter(slot -> slot == MKTowerWorkspaceStackSlot.TOP_CAP)
                .isPresent();
    }

    static boolean isBasementCapSlot(String topologySlotId) {
        return MKTowerWorkspaceStackSlot.fromTopologySlotId(topologySlotId)
                .filter(slot -> slot == MKTowerWorkspaceStackSlot.BASEMENT_CAP)
                .isPresent();
    }

    static Optional<MKWorkspaceTopologySlotMetadata> topologySlotMetadata(MKWorkspaceSlotSchema slot) {
        return MKTowerWorkspaceStackSlot.fromTopologySlotId(slot.slotId())
                .map(ignored -> MKWorkspaceTopologySlotMetadata.fromTopologySlotId(slot.slotId()));
    }

    static List<Integer> allowedMainFloorCounts(MKWorkspaceVerticalStackSettings settings, int basementFloors) {
        return MKWorkspaceTowerStackFloorCounts.allowedMainFloorCounts(MKTowerStackBudget.fromStackSettings(settings),
                basementFloors, settings.topCapApproachEnabled(), settings.basementEntryEnabled(),
                settings.basementCapApproachEnabled());
    }

    static List<Integer> allowedBasementFloorCounts(MKWorkspaceVerticalStackSettings settings, int mainFloors) {
        return MKWorkspaceTowerStackFloorCounts.allowedBasementFloorCounts(MKTowerStackBudget.fromStackSettings(settings),
                mainFloors, settings.topCapApproachEnabled(), settings.basementEntryEnabled(),
                settings.basementCapApproachEnabled());
    }
}
