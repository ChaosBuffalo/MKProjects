package com.chaosbuffalo.mknpc.world.gen.workspace.model;

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
        MKWorkspaceFloorTopologySettings settings = settings();

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

    private static MKWorkspaceFloorTopologySettings settings() {
        return new MKWorkspaceFloorTopologySettings(
                "tower.primary",
                "main_01",
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
}
