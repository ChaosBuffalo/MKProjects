package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MKStructureWorkspaceLayerStateTest {
    @Test
    void layerStatesRoundTripThroughWorkspaceCodec() {
        MKWorkspaceGeneratedLayerState roomState = new MKWorkspaceGeneratedLayerState(
                MKWorkspaceGeneratedLayer.ROOM_ENVELOPES, 3, 101L, 202L, true, false);
        MKWorkspaceGeneratedLayerState hallwayState = new MKWorkspaceGeneratedLayerState(
                MKWorkspaceGeneratedLayer.HALLWAY_ROUTING, 5, 303L, 404L, false, true);
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withLayerStates(List.of(roomState, hallwayState));

        MKStructureWorkspace decoded = MKStructureWorkspace.fromTag(workspace.toTag());

        assertEquals(List.of(roomState, hallwayState), decoded.layerStates());
    }

    @Test
    void draftWorkspaceStartsWithNoLayerStates() {
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO);

        assertTrue(workspace.layerStates().isEmpty());
    }

    @Test
    void withPiecesPreservesLayerStates() {
        MKWorkspaceGeneratedLayerState state = MKWorkspaceGeneratedLayerState.unlocked(
                MKWorkspaceGeneratedLayer.PREVIEW_LAYOUT, 1L, 2L).lock();
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withLayerStates(List.of(state));

        MKStructureWorkspace updated = workspace.withPieces(List.of());

        assertEquals(List.of(state), updated.layerStates());
    }
}
