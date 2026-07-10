package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKFloorTopologySettings;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKHallwayLeadInMode;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MKWorkspaceFloorTopologyMutationPreflightServiceTest {
    private final MKWorkspaceFloorTopologyMutationPreflightService service =
            new MKWorkspaceFloorTopologyMutationPreflightService();

    @Test
    void hallwayPreflightReturnsReportAndMarksHallwayLayersDirty() {
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        MKFloorTopologySettings previous = settings();
        MKFloorTopologySettings updated = previous.withManualHallwayLeadInPieces(5);

        MKWorkspaceMutationPreflight preflight = service.preflight(
                workspace,
                MKWorkspacePlannerId.of("keep.main.tower.center.floor.main_01.floor_plan"),
                previous,
                updated,
                100L
        );

        assertEquals("regenerate_hallway_routing", preflight.report().recommendedOperation());
        assertTrue(preflight.workspaceWithDirtyLayers()
                .layerState(MKWorkspaceGeneratedLayer.HALLWAY_ROUTING).orElseThrow().dirty());
        assertTrue(preflight.workspaceWithDirtyLayers()
                .layerState(MKWorkspaceGeneratedLayer.HALLWAY_PIECES).orElseThrow().dirty());
        assertFalse(preflight.workspaceWithDirtyLayers()
                .layerState(MKWorkspaceGeneratedLayer.ROOM_ENVELOPES).orElseThrow().dirty());
    }

    @Test
    void noOpPreflightEnsuresLayerStatesWithoutDirtyingLayers() {
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        MKFloorTopologySettings settings = settings();

        MKWorkspaceMutationPreflight preflight = service.preflight(
                workspace,
                MKWorkspacePlannerId.of("keep.main.tower.center.floor.main_01.floor_plan"),
                settings,
                settings,
                100L
        );

        assertEquals("none", preflight.report().recommendedOperation());
        assertEquals(MKWorkspaceGeneratedLayer.values().length,
                preflight.workspaceWithDirtyLayers().layerStates().size());
        assertTrue(preflight.workspaceWithDirtyLayers().layerStates().stream()
                .noneMatch(MKWorkspaceGeneratedLayerState::dirty));
    }

    private static MKFloorTopologySettings settings() {
        return new MKFloorTopologySettings(
                "tower.primary",
                "main_01",
                1,
                1,
                0,
                MKHallwayLeadInMode.AUTO,
                1,
                true,
                true,
                false,
                MKFloorTopologySettings.DEFAULT_SPRAWL,
                Optional.empty(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }
}
