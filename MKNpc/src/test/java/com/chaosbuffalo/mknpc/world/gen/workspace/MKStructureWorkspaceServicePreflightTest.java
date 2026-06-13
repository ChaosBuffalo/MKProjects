package com.chaosbuffalo.mknpc.world.gen.workspace;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorTopologySettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceGeneratedLayer;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHallwayLeadInMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLayerStateService;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMutationPreflight;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MKStructureWorkspaceServicePreflightTest {
    private final MKStructureWorkspaceService service = new MKStructureWorkspaceService();

    @Test
    void preflightWorkspaceUpdateAggregatesFloorTopologyChanges() {
        MKWorkspaceFloorTopologySettings previous = settings("tower.primary", "main_floor");
        MKWorkspaceFloorTopologySettings updated = previous.withManualHallwayLeadInPieces(6);
        MKStructureWorkspace existing = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of());
        existing = withTopologyProfile(existing, existing.topologyProfile().withFloorTopologySettings(previous));
        MKStructureWorkspace requested = withTopologyProfile(existing,
                existing.topologyProfile().withFloorTopologySettings(updated));

        MKWorkspaceMutationPreflight preflight = service.preflightWorkspaceUpdate(existing, requested, 123L);

        assertEquals("regenerate_hallway_routing", preflight.report().recommendedOperation());
        assertTrue(preflight.report().invalidatedLayers().contains(MKWorkspaceGeneratedLayer.HALLWAY_ROUTING));
        assertTrue(preflight.report().invalidatedLayers().contains(MKWorkspaceGeneratedLayer.HALLWAY_PIECES));
        assertTrue(preflight.workspaceWithDirtyLayers()
                .layerState(MKWorkspaceGeneratedLayer.HALLWAY_ROUTING).orElseThrow().dirty());
    }

    @Test
    void unchangedDraftReturnsSerializableNoChangeReport() {
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO);

        MKWorkspaceMutationPreflight preflight = service.preflightWorkspaceUpdate(workspace, workspace, 123L);

        assertEquals("none", preflight.report().recommendedOperation());
        assertEquals("none", MKWorkspaceMutationPreflight.CODEC.parse(
                com.mojang.serialization.JsonOps.INSTANCE,
                MKWorkspaceMutationPreflight.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, preflight)
                        .getOrThrow()
        ).getOrThrow().report().recommendedOperation());
    }

    @Test
    void lockedInvalidatedLayersReportsBlockedHallwayLayer() {
        MKWorkspaceFloorTopologySettings previous = settings("tower.primary", "main_floor");
        MKWorkspaceFloorTopologySettings updated = previous.withManualHallwayLeadInPieces(6);
        MKStructureWorkspace existing = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        existing = withTopologyProfile(existing, existing.topologyProfile().withFloorTopologySettings(previous));
        existing = new MKWorkspaceLayerStateService()
                .lockLayers(new MKWorkspaceLayerStateService().ensureLayerStates(existing, 100L),
                        List.of(MKWorkspaceGeneratedLayer.HALLWAY_ROUTING));
        MKStructureWorkspace requested = withTopologyProfile(existing,
                existing.topologyProfile().withFloorTopologySettings(updated));

        MKWorkspaceMutationPreflight preflight = service.preflightWorkspaceUpdate(existing, requested, 123L);

        assertEquals(List.of(MKWorkspaceGeneratedLayer.HALLWAY_ROUTING),
                service.lockedInvalidatedLayers(existing, preflight.report()));
    }

    private static MKWorkspaceFloorTopologySettings settings(String stackId, String floorRole) {
        return new MKWorkspaceFloorTopologySettings(
                stackId,
                floorRole,
                1,
                1,
                0,
                MKWorkspaceHallwayLeadInMode.AUTO,
                1,
                true,
                true,
                false,
                MKWorkspaceFloorTopologySettings.DEFAULT_SPRAWL,
                Optional.empty(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }

    private static MKStructureWorkspace withTopologyProfile(MKStructureWorkspace workspace,
                                                            MKWorkspaceTopologyProfile topologyProfile) {
        return new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                topologyProfile,
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                workspace.familyDefinitions(),
                workspace.openingProfiles(),
                workspace.linearRunFamilies(),
                workspace.createdAt(),
                workspace.updatedAt(),
                workspace.pieces(),
                workspace.layerStates()
        );
    }
}
