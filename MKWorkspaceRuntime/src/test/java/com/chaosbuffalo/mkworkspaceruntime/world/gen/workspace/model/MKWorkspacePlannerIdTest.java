package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MKWorkspacePlannerIdTest {
    @Test
    void acceptsDotDelimitedPlannerHierarchy() {
        MKWorkspacePlannerId id = MKWorkspacePlannerId.of(
                "keep.main.tower.center.floor.basement_01.floor_plan.room.main_00");

        assertEquals("keep.main.tower.center.floor.basement_01.floor_plan.room.main_00", id.value());
        assertEquals("main_00", id.lastSegment());
        assertEquals(9, id.segmentCount());
        assertEquals(List.of("keep", "main", "tower", "center", "floor", "basement_01", "floor_plan", "room",
                "main_00"), id.segments());
        assertEquals("keep_main_tower_center_floor_basement_01_floor_plan_room_main_00", id.toExportName());
    }

    @Test
    void composesChildrenSafely() {
        MKWorkspacePlannerId id = MKWorkspacePlannerId.of("keep.main")
                .child("tower")
                .child("center")
                .child("floor")
                .child("basement_01");

        assertEquals("keep.main.tower.center.floor.basement_01", id.value());
        assertEquals("keep.main.tower.center.floor", id.parent().orElseThrow().value());
        assertTrue(id.startsWith(MKWorkspacePlannerId.of("keep.main.tower")));
        assertTrue(id.startsWith(id));
        assertFalse(MKWorkspacePlannerId.of("keep.secondary").startsWith(MKWorkspacePlannerId.of("keep.main")));
    }

    @Test
    void rejectsInvalidPlannerIds() {
        assertThrows(IllegalArgumentException.class, () -> MKWorkspacePlannerId.of(""));
        assertThrows(IllegalArgumentException.class, () -> MKWorkspacePlannerId.of(".keep.main"));
        assertThrows(IllegalArgumentException.class, () -> MKWorkspacePlannerId.of("keep.main."));
        assertThrows(IllegalArgumentException.class, () -> MKWorkspacePlannerId.of("keep..main"));
        assertThrows(IllegalArgumentException.class, () -> MKWorkspacePlannerId.of("keep.main-tower"));
        assertThrows(IllegalArgumentException.class, () -> MKWorkspacePlannerId.of("Keep.main"));
        assertThrows(IllegalArgumentException.class, () -> MKWorkspacePlannerId.of("keep/main"));
        assertThrows(IllegalArgumentException.class, () -> MKWorkspacePlannerId.of("keep.main tower"));
        assertThrows(IllegalArgumentException.class, () -> MKWorkspacePlannerId.of("keep.main").child("room.01"));
    }

    @Test
    void codecSerializesAsPlainDotString() {
        MKWorkspacePlannerId id = MKWorkspacePlannerId.of("keep.main.tower.center");

        JsonElement encoded = MKWorkspacePlannerId.CODEC.encodeStart(JsonOps.INSTANCE, id).getOrThrow();
        MKWorkspacePlannerId decoded = MKWorkspacePlannerId.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();

        assertEquals("keep.main.tower.center", encoded.getAsString());
        assertEquals(id, decoded);
    }

    @Test
    void codecRejectsInvalidString() {
        assertTrue(MKWorkspacePlannerId.CODEC.parse(JsonOps.INSTANCE, JsonOps.INSTANCE.createString("keep..main"))
                .error().isPresent());
    }
}
