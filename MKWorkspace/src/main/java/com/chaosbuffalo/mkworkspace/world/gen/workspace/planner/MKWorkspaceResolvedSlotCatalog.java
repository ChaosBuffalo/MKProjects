package com.chaosbuffalo.mkworkspace.world.gen.workspace.planner;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertFamilyDefinition;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

/** Merges active planner slots and workspace-declared dynamic insert slots into one authoring catalog. */
public record MKWorkspaceResolvedSlotCatalog(List<Slot> slots) {
    public enum Origin { PLANNER, WORKSPACE_INSERT }

    public record Slot(String slotId, String slotKind, Origin origin) {
    }

    public MKWorkspaceResolvedSlotCatalog {
        slots = List.copyOf(slots);
    }

    public static MKWorkspaceResolvedSlotCatalog resolve(
            Collection<MKWorkspaceSlotSchema> activePlannerSlots,
            Collection<MKWorkspaceInsertFamilyDefinition> insertSlots) {
        LinkedHashMap<String, Slot> resolved = new LinkedHashMap<>();
        activePlannerSlots.forEach(slot -> put(resolved,
                new Slot(slot.slotId(), slot.slotKind(), Origin.PLANNER)));
        insertSlots.forEach(slot -> put(resolved,
                new Slot(slot.slotId(), slot.kind().getSerializedName(), Origin.WORKSPACE_INSERT)));
        return new MKWorkspaceResolvedSlotCatalog(List.copyOf(resolved.values()));
    }

    private static void put(LinkedHashMap<String, Slot> slots, Slot candidate) {
        if (candidate.slotId() == null || candidate.slotId().isBlank()) return;
        Slot existing = slots.get(candidate.slotId());
        if (existing == null || candidate.origin() == Origin.WORKSPACE_INSERT) {
            slots.put(candidate.slotId(), candidate);
        }
    }
}
