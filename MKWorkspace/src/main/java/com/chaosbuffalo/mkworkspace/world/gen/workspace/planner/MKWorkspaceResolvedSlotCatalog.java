package com.chaosbuffalo.mkworkspace.world.gen.workspace.planner;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

/** Merges planner-declared static slots and workspace-declared dynamic insert slots into one authoring catalog. */
public record MKWorkspaceResolvedSlotCatalog(List<Slot> slots) {
    public enum Origin { PLANNER, WORKSPACE_INSERT, REFERENCED_CONTENT }

    public record Slot(String slotId, String slotKind, Origin origin) {
    }

    public MKWorkspaceResolvedSlotCatalog {
        slots = List.copyOf(slots);
    }

    public static MKWorkspaceResolvedSlotCatalog resolve(
            Collection<MKWorkspaceSlotSchema> plannerSlots,
            Collection<MKWorkspaceRoomFamilyDefinition> roomFamilies,
            Collection<MKWorkspaceLinearRunFamilyDefinition> linearRunFamilies,
            Collection<MKWorkspaceInsertFamilyDefinition> insertSlots) {
        LinkedHashMap<String, Slot> resolved = new LinkedHashMap<>();
        plannerSlots.forEach(slot -> put(resolved,
                new Slot(slot.slotId(), slot.slotKind(), Origin.PLANNER)));
        roomFamilies.forEach(family -> put(resolved,
                new Slot(family.topologySlotId(), "room", Origin.REFERENCED_CONTENT)));
        linearRunFamilies.forEach(family -> put(resolved,
                new Slot(family.topologySlotId(), "linear_run", Origin.REFERENCED_CONTENT)));
        insertSlots.forEach(slot -> put(resolved,
                new Slot(slot.slotId(), slot.kind().getSerializedName(), Origin.WORKSPACE_INSERT)));
        return new MKWorkspaceResolvedSlotCatalog(List.copyOf(resolved.values()));
    }

    private static void put(LinkedHashMap<String, Slot> slots, Slot candidate) {
        if (candidate.slotId() == null || candidate.slotId().isBlank()) return;
        Slot existing = slots.get(candidate.slotId());
        if (existing == null || candidate.origin() == Origin.WORKSPACE_INSERT ||
                existing.origin() == Origin.REFERENCED_CONTENT) {
            slots.put(candidate.slotId(), candidate);
        }
    }
}
