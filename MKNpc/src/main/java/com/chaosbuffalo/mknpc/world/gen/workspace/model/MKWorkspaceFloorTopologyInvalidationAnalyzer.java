package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MKWorkspaceFloorTopologyInvalidationAnalyzer {
    private static final String FLOOR_TOPOLOGY_OPERATION = "regenerate_floor_topology";
    private static final String HALLWAY_ROUTING_OPERATION = "regenerate_hallway_routing";

    public MKWorkspaceInvalidationReport analyze(MKWorkspacePlannerId floorPlannerId,
                                                 MKWorkspaceFloorTopologySettings previous,
                                                 MKWorkspaceFloorTopologySettings updated) {
        if (previous.equals(updated)) {
            return MKWorkspaceInvalidationReport.noChanges("Floor topology settings are unchanged.");
        }

        if (identityChanged(previous, updated) || topologyChanged(previous, updated)) {
            return broadTopologyReport(floorPlannerId);
        }

        if (hallwayRoutingChanged(previous, updated)) {
            return hallwayRoutingReport(floorPlannerId);
        }

        return broadTopologyReport(floorPlannerId);
    }

    private MKWorkspaceInvalidationReport hallwayRoutingReport(MKWorkspacePlannerId floorPlannerId) {
        return new MKWorkspaceInvalidationReport(
                List.of(
                        MKWorkspaceGeneratedLayer.HALLWAY_ROUTING,
                        MKWorkspaceGeneratedLayer.HALLWAY_PIECES,
                        MKWorkspaceGeneratedLayer.SIDECAR_BLOCKS,
                        MKWorkspaceGeneratedLayer.RUNTIME_METADATA
                ),
                List.of(floorPlannerId),
                List.of(floorPlannerId.child("room")),
                List.of(),
                MKWorkspaceMutationSafety.CONDITIONALLY_SAFE_TOPOLOGY_PATCH,
                "Floor hallway routing can be regenerated in-place while preserving room template bindings.",
                HALLWAY_ROUTING_OPERATION,
                List.of()
        );
    }

    private MKWorkspaceInvalidationReport broadTopologyReport(MKWorkspacePlannerId floorPlannerId) {
        return new MKWorkspaceInvalidationReport(
                List.of(
                        MKWorkspaceGeneratedLayer.PLANNER_TOPOLOGY,
                        MKWorkspaceGeneratedLayer.ROOM_ENVELOPES,
                        MKWorkspaceGeneratedLayer.CONNECTOR_GRAPH,
                        MKWorkspaceGeneratedLayer.HALLWAY_ROUTING,
                        MKWorkspaceGeneratedLayer.HALLWAY_PIECES,
                        MKWorkspaceGeneratedLayer.TEMPLATE_BINDINGS,
                        MKWorkspaceGeneratedLayer.SIDECAR_BLOCKS,
                        MKWorkspaceGeneratedLayer.RUNTIME_METADATA
                ),
                List.of(floorPlannerId),
                List.of(),
                List.of(),
                MKWorkspaceMutationSafety.DESTRUCTIVE_REGENERATE,
                "Floor topology changes require regenerating room envelopes, connector graph, and hallway routing.",
                FLOOR_TOPOLOGY_OPERATION,
                List.of("Existing authored room bindings may need remapping after topology regeneration.")
        );
    }

    private boolean identityChanged(MKWorkspaceFloorTopologySettings previous,
                                    MKWorkspaceFloorTopologySettings updated) {
        return !Objects.equals(previous.stackId(), updated.stackId())
                || !Objects.equals(previous.floorRole(), updated.floorRole());
    }

    private boolean hallwayRoutingChanged(MKWorkspaceFloorTopologySettings previous,
                                          MKWorkspaceFloorTopologySettings updated) {
        return previous.hallwayLeadInMode() != updated.hallwayLeadInMode()
                || previous.manualHallwayLeadInPieces() != updated.manualHallwayLeadInPieces()
                || previous.linksEnabled() != updated.linksEnabled()
                || Float.compare(previous.linkDensity(), updated.linkDensity()) != 0
                || previous.maxLinksPerFloor() != updated.maxLinksPerFloor()
                || previous.maxLinksPerRoom() != updated.maxLinksPerRoom()
                || previous.maxLinkLength() != updated.maxLinkLength();
    }

    private boolean topologyChanged(MKWorkspaceFloorTopologySettings previous,
                                    MKWorkspaceFloorTopologySettings updated) {
        List<Boolean> checks = new ArrayList<>();
        checks.add(previous.minMainPathPieces() != updated.minMainPathPieces());
        checks.add(previous.maxMainPathPieces() != updated.maxMainPathPieces());
        checks.add(previous.maxBranchPiecesBeforeCap() != updated.maxBranchPiecesBeforeCap());
        checks.add(previous.mainHallwaysEnabled() != updated.mainHallwaysEnabled());
        checks.add(previous.branchHallwaysEnabled() != updated.branchHallwaysEnabled());
        checks.add(previous.mainCapApproachEnabled() != updated.mainCapApproachEnabled());
        checks.add(Float.compare(previous.sprawl(), updated.sprawl()) != 0);
        checks.add(!Objects.equals(previous.lockedLayoutSeed(), updated.lockedLayoutSeed()));
        checks.add(!Objects.equals(previous.mainRoomProfiles(), updated.mainRoomProfiles()));
        checks.add(!Objects.equals(previous.branchRoomProfiles(), updated.branchRoomProfiles()));
        checks.add(!Objects.equals(previous.branchCapProfiles(), updated.branchCapProfiles()));
        checks.add(!Objects.equals(previous.mainCapApproachProfiles(), updated.mainCapApproachProfiles()));
        checks.add(!Objects.equals(previous.mainCapProfiles(), updated.mainCapProfiles()));
        return checks.stream().anyMatch(Boolean::booleanValue);
    }
}
