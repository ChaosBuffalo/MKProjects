package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MKWorkspaceInvalidationReportTest {
    @Test
    void invalidationReportRoundTripsForClientDisplay() {
        MKWorkspaceInvalidationReport report = new MKWorkspaceInvalidationReport(
                List.of(MKWorkspaceGeneratedLayer.HALLWAY_ROUTING, MKWorkspaceGeneratedLayer.HALLWAY_PIECES),
                List.of(MKWorkspacePlannerId.of("keep.main.floor_plan.hallway.main_to_branch_02")),
                List.of(MKWorkspacePlannerId.of("keep.main.floor_plan.room.main_00")),
                List.of(MKWorkspacePlannerId.of("keep.main.floor_plan.hallway.old_branch_01")),
                MKWorkspaceMutationSafety.CONDITIONALLY_SAFE_TOPOLOGY_PATCH,
                "Hallway routing will be regenerated in-place.",
                "regenerate_hallway_routing",
                List.of("One old hallway binding could not be matched."),
                List.of(new MKWorkspaceTemplateRemapSuggestion(
                        MKWorkspacePlannerId.of("keep.main.floor_plan.hallway.old_branch_01"),
                        MKWorkspacePlannerId.of("keep.main.floor_plan.hallway.new_branch_01"),
                        100,
                        "same floor piece kind, dimensions, and connector signature"))
        );

        JsonElement encoded = MKWorkspaceInvalidationReport.CODEC.encodeStart(JsonOps.INSTANCE, report).getOrThrow();
        MKWorkspaceInvalidationReport decoded = MKWorkspaceInvalidationReport.CODEC.parse(JsonOps.INSTANCE, encoded)
                .getOrThrow();

        assertEquals(report, decoded);
        assertTrue(decoded.hasInvalidatedLayer(MKWorkspaceGeneratedLayer.HALLWAY_ROUTING));
        assertTrue(decoded.hasOrphanedBindings());
        assertEquals(1, decoded.remapSuggestions().size());
        JsonObject object = encoded.getAsJsonObject();
        assertEquals("conditionally_safe_topology_patch", object.get("safety").getAsString());
        assertEquals("regenerate_hallway_routing", object.get("recommendedOperation").getAsString());
        assertTrue(object.has("remapSuggestions"));
    }

    @Test
    void invalidationReportCopiesLists() {
        List<MKWorkspaceGeneratedLayer> layers = new java.util.ArrayList<>();
        layers.add(MKWorkspaceGeneratedLayer.PREVIEW_LAYOUT);

        MKWorkspaceInvalidationReport report = new MKWorkspaceInvalidationReport(
                layers,
                List.of(),
                List.of(),
                List.of(),
                MKWorkspaceMutationSafety.SAFE_RELAYOUT,
                "Preview layout changed.",
                "relayout_preview",
                List.of()
        );
        layers.add(MKWorkspaceGeneratedLayer.RUNTIME_METADATA);

        assertEquals(List.of(MKWorkspaceGeneratedLayer.PREVIEW_LAYOUT), report.invalidatedLayers());
        assertThrows(UnsupportedOperationException.class,
                () -> report.invalidatedLayers().add(MKWorkspaceGeneratedLayer.RUNTIME_METADATA));
    }

    @Test
    void generatedLayerStateTracksLockDirtyAndRefreshTransitions() {
        MKWorkspaceGeneratedLayerState initial = MKWorkspaceGeneratedLayerState.unlocked(
                MKWorkspaceGeneratedLayer.ROOM_ENVELOPES, 10L, 1000L);
        MKWorkspaceGeneratedLayerState lockedDirty = initial.lock().markDirty();
        MKWorkspaceGeneratedLayerState refreshed = lockedDirty.refreshed(11L, 2000L);

        assertFalse(initial.locked());
        assertTrue(lockedDirty.locked());
        assertTrue(lockedDirty.dirty());
        assertEquals(1, refreshed.version());
        assertEquals(11L, refreshed.sourceSettingsHash());
        assertEquals(2000L, refreshed.updatedAtEpochMillis());
        assertTrue(refreshed.locked());
        assertFalse(refreshed.dirty());
    }

    @Test
    void generatedLayerStateRoundTrips() {
        MKWorkspaceGeneratedLayerState state = new MKWorkspaceGeneratedLayerState(
                MKWorkspaceGeneratedLayer.TEMPLATE_BINDINGS, 4, 123L, 456L, true, false);

        JsonElement encoded = MKWorkspaceGeneratedLayerState.CODEC.encodeStart(JsonOps.INSTANCE, state).getOrThrow();
        MKWorkspaceGeneratedLayerState decoded = MKWorkspaceGeneratedLayerState.CODEC.parse(JsonOps.INSTANCE, encoded)
                .getOrThrow();

        assertEquals(state, decoded);
        assertEquals("template_bindings", encoded.getAsJsonObject().get("layer").getAsString());
    }

    @Test
    void enumCodecsRejectUnknownNames() {
        assertTrue(MKWorkspaceGeneratedLayer.CODEC.parse(JsonOps.INSTANCE, JsonOps.INSTANCE.createString("unknown"))
                .error().isPresent());
        assertTrue(MKWorkspaceMutationSafety.CODEC.parse(JsonOps.INSTANCE, JsonOps.INSTANCE.createString("unknown"))
                .error().isPresent());
    }
}
