package com.chaosbuffalo.mkworkspace.world.gen.workspace.planner;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertFamilyDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MKWorkspaceResolvedSlotCatalogTest {
    @Test
    void mergesStaticPlannerSlotsWithDynamicUserInsertSlots() {
        MKWorkspaceSlotSchema plannerSlot = new MKWorkspaceSlotSchema("tower.main", "tower", "room",
                "main_room", MKWorkspaceSlotSchema.Repeat.FIXED);
        MKWorkspaceInsertFamilyDefinition insertSlot =
                MKWorkspaceInsertFamilyDefinition.insertSlot("fire_shrine_platform_contents", 9, 5, 9);

        MKWorkspaceResolvedSlotCatalog catalog = MKWorkspaceResolvedSlotCatalog.resolve(
                List.of(plannerSlot), List.of(), List.of(), List.of(insertSlot));

        assertEquals(List.of("tower.main", "fire_shrine_platform_contents"), catalog.slots().stream()
                .map(MKWorkspaceResolvedSlotCatalog.Slot::slotId).toList());
        assertEquals(MKWorkspaceResolvedSlotCatalog.Origin.WORKSPACE_INSERT,
                catalog.slots().get(1).origin());
    }
}
