package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKFloorLinkGenerationMode;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKFloorTopologySettings;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKHallwayLeadInMode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MKWorkspaceFloorTopologyInvalidationAnalyzerTest {
    private final MKWorkspaceFloorTopologyInvalidationAnalyzer analyzer =
            new MKWorkspaceFloorTopologyInvalidationAnalyzer();
    private final MKWorkspacePlannerId floorPlannerId =
            MKWorkspacePlannerId.of("keep.main.tower.center.floor.main_01.floor_plan");

    @Test
    void unchangedSettingsReportNoInvalidatedLayers() {
        MKFloorTopologySettings settings = settings();

        MKWorkspaceInvalidationReport report = analyzer.analyze(floorPlannerId, settings, settings);

        assertTrue(report.invalidatedLayers().isEmpty());
        assertEquals(MKWorkspaceMutationSafety.SAFE_METADATA_UPDATE, report.safety());
        assertEquals("none", report.recommendedOperation());
    }

    @Test
    void manualHallwayLeadInOnlyInvalidatesHallwayLayers() {
        MKWorkspaceInvalidationReport report = analyzer.analyze(
                floorPlannerId,
                settings(),
                settings().withManualHallwayLeadInPieces(4)
        );

        assertEquals("regenerate_hallway_routing", report.recommendedOperation());
        assertEquals(MKWorkspaceMutationSafety.CONDITIONALLY_SAFE_TOPOLOGY_PATCH, report.safety());
        assertTrue(report.hasInvalidatedLayer(MKWorkspaceGeneratedLayer.HALLWAY_ROUTING));
        assertTrue(report.hasInvalidatedLayer(MKWorkspaceGeneratedLayer.HALLWAY_PIECES));
        assertTrue(report.hasInvalidatedLayer(MKWorkspaceGeneratedLayer.SIDECAR_BLOCKS));
        assertFalse(report.hasInvalidatedLayer(MKWorkspaceGeneratedLayer.ROOM_ENVELOPES));
        assertEquals(List.of(floorPlannerId.child("room")), report.preservedTemplateBindings());
    }

    @Test
    void linkSettingsInvalidateHallwayRoutingLayers() {
        MKWorkspaceInvalidationReport report = analyzer.analyze(
                floorPlannerId,
                settings(),
                settings().withLinksEnabled(true).withMaxLinkLength(64)
        );

        assertEquals("regenerate_hallway_routing", report.recommendedOperation());
        assertTrue(report.hasInvalidatedLayer(MKWorkspaceGeneratedLayer.HALLWAY_ROUTING));
        assertFalse(report.hasInvalidatedLayer(MKWorkspaceGeneratedLayer.PLANNER_TOPOLOGY));
    }

    @Test
    void linkRenderingSettingsOnlyInvalidateMetadataLayers() {
        MKWorkspaceInvalidationReport report = analyzer.analyze(
                floorPlannerId,
                settings(),
                settings()
                        .withLinkGenerationMode(MKFloorLinkGenerationMode.DECAYING_HALLWAY)
                        .withLinkDecay(0.5f)
                        .withEndpointIntactRadius(4)
                        .withMiddleDecayBonus(0.3f)
                        .withInsertFamily(Optional.of("crypt_link_supports"))
                        .withInsertDepth(3)
                        .withInsertSpacing(7)
                        .withInsertProbability(0.65f)
                        .withInsertMaxDecay(0.55f)
        );

        assertEquals("refresh_link_rendering", report.recommendedOperation());
        assertEquals(MKWorkspaceMutationSafety.SAFE_METADATA_UPDATE, report.safety());
        assertTrue(report.hasInvalidatedLayer(MKWorkspaceGeneratedLayer.RUNTIME_METADATA));
        assertTrue(report.hasInvalidatedLayer(MKWorkspaceGeneratedLayer.SIDECAR_BLOCKS));
        assertFalse(report.hasInvalidatedLayer(MKWorkspaceGeneratedLayer.PLANNER_TOPOLOGY));
        assertFalse(report.hasInvalidatedLayer(MKWorkspaceGeneratedLayer.HALLWAY_ROUTING));
    }

    @Test
    void pathCountChangesInvalidateFloorTopology() {
        MKWorkspaceInvalidationReport report = analyzer.analyze(
                floorPlannerId,
                settings(),
                settings().withMaxMainPathPieces(3)
        );

        assertEquals("regenerate_floor_topology", report.recommendedOperation());
        assertEquals(MKWorkspaceMutationSafety.DESTRUCTIVE_REGENERATE, report.safety());
        assertTrue(report.hasInvalidatedLayer(MKWorkspaceGeneratedLayer.PLANNER_TOPOLOGY));
        assertTrue(report.hasInvalidatedLayer(MKWorkspaceGeneratedLayer.ROOM_ENVELOPES));
        assertTrue(report.hasInvalidatedLayer(MKWorkspaceGeneratedLayer.TEMPLATE_BINDINGS));
        assertFalse(report.warnings().isEmpty());
    }

    @Test
    void layoutSeedChangesInvalidateFloorTopology() {
        MKWorkspaceInvalidationReport report = analyzer.analyze(
                floorPlannerId,
                settings(),
                settings().withLockedLayoutSeed(Optional.of(42L))
        );

        assertTrue(report.hasInvalidatedLayer(MKWorkspaceGeneratedLayer.PLANNER_TOPOLOGY));
        assertEquals(MKWorkspaceMutationSafety.DESTRUCTIVE_REGENERATE, report.safety());
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
