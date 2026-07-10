package com.chaosbuffalo.mkworkspace.world.gen.workspace.model;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceGeneratedLayer;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceGeneratedLayerState;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceMutationSafety;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MKWorkspaceLayerStateServiceTest {
    private final MKWorkspaceLayerStateService service = new MKWorkspaceLayerStateService();

    @Test
    void ensureLayerStatesAddsMissingLayersAndPreservesExistingState() {
        MKWorkspaceGeneratedLayerState roomState = new MKWorkspaceGeneratedLayerState(
                MKWorkspaceGeneratedLayer.ROOM_ENVELOPES, 7, 123L, 456L, true, false);
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withLayerStates(List.of(roomState));

        MKStructureWorkspace updated = service.ensureLayerStates(workspace, 999L);

        assertEquals(MKWorkspaceGeneratedLayer.values().length, updated.layerStates().size());
        assertEquals(roomState, updated.layerState(MKWorkspaceGeneratedLayer.ROOM_ENVELOPES).orElseThrow());
        assertEquals(999L, updated.layerState(MKWorkspaceGeneratedLayer.HALLWAY_ROUTING)
                .orElseThrow().updatedAtEpochMillis());
    }

    @Test
    void withLayerStateReplacesExistingLayer() {
        MKWorkspaceGeneratedLayerState initial = MKWorkspaceGeneratedLayerState.unlocked(
                MKWorkspaceGeneratedLayer.PREVIEW_LAYOUT, 1L, 2L);
        MKWorkspaceGeneratedLayerState replacement = initial.lock().markDirty();
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withLayerStates(List.of(initial));

        MKStructureWorkspace updated = workspace.withLayerState(replacement);

        assertEquals(List.of(replacement), updated.layerStates());
        assertTrue(updated.layerLocked(MKWorkspaceGeneratedLayer.PREVIEW_LAYOUT));
    }

    @Test
    void applyInvalidationMarksOnlyInvalidatedLayersDirty() {
        MKStructureWorkspace workspace = service.ensureLayerStates(MKStructureWorkspace.createDraft(BlockPos.ZERO), 1L);
        MKWorkspaceInvalidationReport report = new MKWorkspaceInvalidationReport(
                List.of(MKWorkspaceGeneratedLayer.HALLWAY_ROUTING, MKWorkspaceGeneratedLayer.HALLWAY_PIECES),
                List.of(),
                List.of(),
                List.of(),
                MKWorkspaceMutationSafety.CONDITIONALLY_SAFE_TOPOLOGY_PATCH,
                "Hallways changed.",
                "regenerate_hallway_routing",
                List.of()
        );

        MKStructureWorkspace updated = service.applyInvalidation(workspace, report, 2L);

        assertTrue(updated.layerState(MKWorkspaceGeneratedLayer.HALLWAY_ROUTING).orElseThrow().dirty());
        assertTrue(updated.layerState(MKWorkspaceGeneratedLayer.HALLWAY_PIECES).orElseThrow().dirty());
        assertFalse(updated.layerState(MKWorkspaceGeneratedLayer.ROOM_ENVELOPES).orElseThrow().dirty());
    }

    @Test
    void refreshLayersClearsDirtyAndIncrementsVersion() {
        MKWorkspaceGeneratedLayerState dirty = MKWorkspaceGeneratedLayerState.unlocked(
                MKWorkspaceGeneratedLayer.HALLWAY_ROUTING, 1L, 2L).markDirty();
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withLayerStates(List.of(dirty));

        MKStructureWorkspace updated = service.refreshLayers(
                workspace, List.of(MKWorkspaceGeneratedLayer.HALLWAY_ROUTING), 99L, 100L);
        MKWorkspaceGeneratedLayerState refreshed = updated.layerState(MKWorkspaceGeneratedLayer.HALLWAY_ROUTING)
                .orElseThrow();

        assertEquals(1, refreshed.version());
        assertEquals(99L, refreshed.sourceSettingsHash());
        assertEquals(100L, refreshed.updatedAtEpochMillis());
        assertFalse(refreshed.dirty());
    }

    @Test
    void lockAndUnlockLayersUpdateLockState() {
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO);

        MKStructureWorkspace locked = service.lockLayers(workspace, List.of(MKWorkspaceGeneratedLayer.TEMPLATE_BINDINGS));
        MKStructureWorkspace unlocked = service.unlockLayers(locked, List.of(MKWorkspaceGeneratedLayer.TEMPLATE_BINDINGS));

        assertTrue(locked.layerLocked(MKWorkspaceGeneratedLayer.TEMPLATE_BINDINGS));
        assertFalse(unlocked.layerLocked(MKWorkspaceGeneratedLayer.TEMPLATE_BINDINGS));
    }

    @Test
    void staleLayersReportMissingDirtyAndHashMismatchedLayers() {
        MKWorkspaceGeneratedLayerState clean = MKWorkspaceGeneratedLayerState.unlocked(
                MKWorkspaceGeneratedLayer.ROOM_ENVELOPES, 10L, 1L);
        MKWorkspaceGeneratedLayerState dirty = MKWorkspaceGeneratedLayerState.unlocked(
                MKWorkspaceGeneratedLayer.HALLWAY_ROUTING, 20L, 1L).markDirty();
        MKWorkspaceGeneratedLayerState mismatched = MKWorkspaceGeneratedLayerState.unlocked(
                MKWorkspaceGeneratedLayer.RUNTIME_METADATA, 30L, 1L);
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withLayerStates(List.of(clean, dirty, mismatched));

        List<MKWorkspaceGeneratedLayer> staleLayers = service.staleLayers(workspace, Map.of(
                MKWorkspaceGeneratedLayer.ROOM_ENVELOPES, 10L,
                MKWorkspaceGeneratedLayer.HALLWAY_ROUTING, 20L,
                MKWorkspaceGeneratedLayer.RUNTIME_METADATA, 31L,
                MKWorkspaceGeneratedLayer.TEMPLATE_BINDINGS, 40L
        ));

        assertEquals(List.of(
                MKWorkspaceGeneratedLayer.HALLWAY_ROUTING,
                MKWorkspaceGeneratedLayer.TEMPLATE_BINDINGS,
                MKWorkspaceGeneratedLayer.RUNTIME_METADATA
        ), staleLayers);
    }
}
