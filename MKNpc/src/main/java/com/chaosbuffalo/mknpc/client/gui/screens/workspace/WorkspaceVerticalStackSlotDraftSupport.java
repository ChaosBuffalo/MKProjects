package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalStackBudget;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalStackSlot;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorTopologySettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologySlotMetadata;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalStackFloorCounts;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalStackSettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKWorkspaceSlotSchema;

import java.util.List;
import java.util.Optional;

final class WorkspaceVerticalStackSlotDraftSupport {
    private WorkspaceVerticalStackSlotDraftSupport() {
    }

    static Optional<String> topologyGroupIdForFloorRole(String floorRole) {
        for (MKWorkspaceVerticalStackSlot slot : MKWorkspaceVerticalStackSlot.values()) {
            if (slot.suffix().equals(floorRole)) {
                return Optional.of(slot.topologyGroupId());
            }
        }
        return Optional.empty();
    }

    static Optional<String> topologyGroupIdForTopologySlot(String topologySlotId) {
        Optional<MKWorkspaceVerticalStackSlot> slot = MKWorkspaceVerticalStackSlot.fromTopologySlotId(topologySlotId);
        Optional<String> stackId = MKWorkspaceVerticalStackSlot.stackIdForTopologySlot(topologySlotId);
        if (slot.isEmpty() || stackId.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(MKWorkspaceFloorTopologySettings.key(stackId.get(), slot.get().suffix()));
    }

    static boolean isTopCapSlot(String topologySlotId) {
        return MKWorkspaceVerticalStackSlot.fromTopologySlotId(topologySlotId)
                .filter(slot -> slot == MKWorkspaceVerticalStackSlot.TOP_CAP)
                .isPresent();
    }

    static boolean isBasementCapSlot(String topologySlotId) {
        return MKWorkspaceVerticalStackSlot.fromTopologySlotId(topologySlotId)
                .filter(slot -> slot == MKWorkspaceVerticalStackSlot.BASEMENT_CAP)
                .isPresent();
    }

    static Optional<MKWorkspaceTopologySlotMetadata> topologySlotMetadata(MKWorkspaceSlotSchema slot) {
        return MKWorkspaceVerticalStackSlot.fromTopologySlotId(slot.slotId())
                .map(ignored -> MKWorkspaceTopologySlotMetadata.fromTopologySlotId(slot.slotId()));
    }

    static List<Integer> allowedMainFloorCounts(MKWorkspaceVerticalStackSettings settings, int basementFloors) {
        return MKWorkspaceVerticalStackFloorCounts.allowedMainFloorCounts(MKWorkspaceVerticalStackBudget.fromStackSettings(settings),
                basementFloors, settings.topCapApproachEnabled(), settings.basementEntryEnabled(),
                settings.basementCapApproachEnabled());
    }

    static List<Integer> allowedBasementFloorCounts(MKWorkspaceVerticalStackSettings settings, int mainFloors) {
        return MKWorkspaceVerticalStackFloorCounts.allowedBasementFloorCounts(MKWorkspaceVerticalStackBudget.fromStackSettings(settings),
                mainFloors, settings.topCapApproachEnabled(), settings.basementEntryEnabled(),
                settings.basementCapApproachEnabled());
    }
}
