package com.chaosbuffalo.mknpc.world.gen.workspace.mutation;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePlannerId;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTemplateReuseTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKPlannedPiece;
import com.chaosbuffalo.mknpc.world.gen.workspace.scaffold.MKWorkspaceGridLayout;

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

    private boolean belongsToFloor(Map<String, String> tags, String stackId, String floorRole) {
        return stackId.equals(tags.get(STACK_ID_TAG)) && floorRole.equals(tags.get(FLOOR_ROLE_TAG));
    }

    private String baseName(MKWorkspacePieceDefinition piece) {
        return piece.tags().getOrDefault(MKWorkspaceGridLayout.TAG_BASE_NAME, piece.pieceName());
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
