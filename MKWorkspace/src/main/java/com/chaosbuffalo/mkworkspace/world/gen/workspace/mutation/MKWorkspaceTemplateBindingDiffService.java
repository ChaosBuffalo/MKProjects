package com.chaosbuffalo.mkworkspace.world.gen.workspace.mutation;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePlannerId;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTemplateRemapSuggestion;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTemplateReuseTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKPlannedConnector;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKPlannedPiece;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.scaffold.MKWorkspaceGridLayout;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MKWorkspaceTemplateBindingDiffService {
    private static final String STACK_ID_TAG = "workspace_floor_topology_stack_id";
    private static final String FLOOR_ROLE_TAG = "workspace_floor_topology_floor_role";

    public TemplateBindingDiff floorTopologyBindings(MKStructureWorkspace existing,
                                                     List<MKPlannedPiece> requestedCanonicalPieces,
                                                     String existingStackId,
                                                     String existingFloorRole,
                                                     String requestedStackId,
                                                     String requestedFloorRole) {
        Set<String> requestedBaseNames = requestedCanonicalPieces.stream()
                .filter(piece -> belongsToFloor(piece.tags(), requestedStackId, requestedFloorRole))
                .map(MKPlannedPiece::pieceName)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        LinkedHashSet<MKWorkspacePlannerId> preserved = new LinkedHashSet<>();
        LinkedHashSet<MKWorkspacePlannerId> orphaned = new LinkedHashSet<>();
        for (MKWorkspacePieceDefinition piece : existing.pieces()) {
            if (!belongsToFloor(piece.tags(), existingStackId, existingFloorRole) ||
                    MKWorkspaceTemplateReuseTags.isDerived(piece.tags())) {
                continue;
            }
            String baseName = baseName(piece);
            if (requestedBaseNames.contains(baseName)) {
                preserved.add(piece.plannerId());
            } else {
                orphaned.add(piece.plannerId());
            }
        }

        return new TemplateBindingDiff(List.copyOf(preserved), List.copyOf(orphaned));
    }

    public List<MKWorkspaceTemplateRemapSuggestion> suggestFloorTopologyRemaps(
            MKStructureWorkspace existing,
            List<MKPlannedPiece> requestedCanonicalPieces,
            List<MKWorkspacePlannerId> orphanedPlannerIds,
            String existingStackId,
            String existingFloorRole,
            String requestedStackId,
            String requestedFloorRole) {
        Set<MKWorkspacePlannerId> orphanedIds = new HashSet<>(orphanedPlannerIds);
        List<MKWorkspacePieceDefinition> orphanedPieces = existing.pieces().stream()
                .filter(piece -> orphanedIds.contains(piece.plannerId()))
                .filter(piece -> belongsToFloor(piece.tags(), existingStackId, existingFloorRole))
                .filter(piece -> !MKWorkspaceTemplateReuseTags.isDerived(piece.tags()))
                .toList();
        List<MKPlannedPiece> requestedFloorPieces = requestedCanonicalPieces.stream()
                .filter(piece -> belongsToFloor(piece.tags(), requestedStackId, requestedFloorRole))
                .toList();

        return orphanedPieces.stream()
                .flatMap(orphaned -> requestedFloorPieces.stream()
                        .filter(target -> compatible(orphaned, target))
                        .map(target -> new MKWorkspaceTemplateRemapSuggestion(
                                orphaned.plannerId(),
                                target.plannerId(),
                                100,
                                "same floor piece kind, dimensions, and connector signature")))
                .sorted(Comparator
                        .comparing((MKWorkspaceTemplateRemapSuggestion suggestion) ->
                                suggestion.orphanedPlannerId().value())
                        .thenComparing((MKWorkspaceTemplateRemapSuggestion suggestion) -> -suggestion.score())
                        .thenComparing(suggestion -> suggestion.targetPlannerId().value()))
                .toList();
    }

    private boolean belongsToFloor(Map<String, String> tags, String stackId, String floorRole) {
        return stackId.equals(tags.get(STACK_ID_TAG)) && floorRole.equals(tags.get(FLOOR_ROLE_TAG));
    }

    private String baseName(MKWorkspacePieceDefinition piece) {
        return piece.tags().getOrDefault(MKWorkspaceGridLayout.TAG_BASE_NAME, piece.pieceName());
    }

    private boolean compatible(MKWorkspacePieceDefinition orphaned, MKPlannedPiece target) {
        return samePieceKind(orphaned, target) && sameDimensions(orphaned, target) &&
                connectorSignatures(orphaned).equals(plannedConnectorSignatures(target));
    }

    private boolean samePieceKind(MKWorkspacePieceDefinition orphaned, MKPlannedPiece target) {
        return orphaned.tags().getOrDefault(MKWorkspaceHallwayRegenerationPlanner.TOWER_PIECE_KIND_TAG, "")
                .equals(target.tags().getOrDefault(MKWorkspaceHallwayRegenerationPlanner.TOWER_PIECE_KIND_TAG, ""));
    }

    private boolean sameDimensions(MKWorkspacePieceDefinition orphaned, MKPlannedPiece target) {
        return orphaned.effectiveDimensions().roomWidth() == target.interiorWidth() &&
                orphaned.effectiveDimensions().roomLength() == target.interiorLength() &&
                orphaned.effectiveDimensions().roomHeight() == target.interiorHeight();
    }

    private List<String> connectorSignatures(MKWorkspacePieceDefinition piece) {
        return piece.connectors().stream()
                .map(this::connectorSignature)
                .sorted()
                .toList();
    }

    private List<String> plannedConnectorSignatures(MKPlannedPiece piece) {
        return piece.connectors().stream()
                .filter(MKPlannedConnector::placesJigsaw)
                .map(this::connectorSignature)
                .sorted()
                .toList();
    }

    private String connectorSignature(MKWorkspaceConnectorDefinition connector) {
        return connector.role().name() + "|" + connector.facing().getName() + "|" +
                connector.openingWidth() + "|" + connector.openingHeight();
    }

    private String connectorSignature(MKPlannedConnector connector) {
        return connector.role().name() + "|" + connector.facing().getName() + "|" +
                connector.openingWidth() + "|" + connector.openingHeight();
    }

    public record TemplateBindingDiff(
            List<MKWorkspacePlannerId> preserved,
            List<MKWorkspacePlannerId> orphaned
    ) {
        public TemplateBindingDiff {
            preserved = List.copyOf(preserved);
            orphaned = List.copyOf(orphaned);
        }
    }
}
