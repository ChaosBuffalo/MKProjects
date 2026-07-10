package com.chaosbuffalo.mkworkspace.world.gen.workspace.mutation;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePlannerId;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTemplateReuseTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKPlannedPiece;
import com.chaosbuffalo.mknpc.world.gen.workspace.scaffold.MKWorkspaceGridLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MKWorkspaceHallwayRegenerationPlanner {
    public static final String TOWER_PIECE_KIND_TAG = "tower_piece_kind";
    public static final String FLOOR_PLAN_LINEAR_RUN_KIND = "floor_plan_linear_run";
    public static final String WORKSPACE_PIECE_KIND_TAG = "workspace_piece_kind";
    public static final String TEMPLATE_PIECE_KIND = "template";
    public static final String INSTANCE_PIECE_KIND = "instance";

    public RegenerationPlan plan(MKStructureWorkspace workspace, List<MKPlannedPiece> canonicalPieces) {
        Map<String, MKPlannedPiece> canonicalByBaseName = canonicalPieces.stream()
                .collect(Collectors.toMap(MKPlannedPiece::pieceName, piece -> piece, (left, right) -> left));
        List<MKPlannedPiece> layoutPieces = layoutPieces(workspace, canonicalPieces, canonicalByBaseName);
        List<MKPlannedPiece> hallwayPieces = layoutPieces.stream()
                .filter(piece -> isFloorPlanHallway(piece.tags()))
                .toList();
        List<MKWorkspacePieceDefinition> existingHallwayPieces = workspace.pieces().stream()
                .filter(piece -> isFloorPlanHallway(piece.tags()))
                .toList();
        return new RegenerationPlan(layoutPieces, hallwayPieces, existingHallwayPieces);
    }

    public List<MKWorkspacePieceDefinition> mergeGeneratedHallways(MKStructureWorkspace existing,
                                                                    List<MKWorkspacePieceDefinition> generatedHallways) {
        Map<String, MKWorkspacePieceDefinition> generatedByName = generatedHallways.stream()
                .collect(Collectors.toMap(MKWorkspacePieceDefinition::pieceName, piece -> piece,
                        (left, right) -> left, LinkedHashMap::new));
        Set<String> insertedNames = new HashSet<>();
        ArrayList<MKWorkspacePieceDefinition> merged = new ArrayList<>();
        for (MKWorkspacePieceDefinition existingPiece : existing.pieces()) {
            if (!isFloorPlanHallway(existingPiece.tags())) {
                merged.add(existingPiece);
                continue;
            }
            MKWorkspacePieceDefinition replacement = generatedByName.get(existingPiece.pieceName());
            if (replacement != null) {
                merged.add(replacement);
                insertedNames.add(replacement.pieceName());
            }
        }
        for (MKWorkspacePieceDefinition generated : generatedHallways) {
            if (!insertedNames.contains(generated.pieceName())) {
                merged.add(generated);
            }
        }
        return List.copyOf(merged);
    }

    public boolean isFloorPlanHallway(Map<String, String> tags) {
        return FLOOR_PLAN_LINEAR_RUN_KIND.equals(tags.get(TOWER_PIECE_KIND_TAG));
    }

    private List<MKPlannedPiece> layoutPieces(MKStructureWorkspace workspace,
                                              List<MKPlannedPiece> canonicalPieces,
                                              Map<String, MKPlannedPiece> canonicalByBaseName) {
        ArrayList<MKPlannedPiece> layoutPieces = canonicalPieces.stream()
                .filter(this::usesPhysicalWorkspaceCell)
                .map(this::toTemplatePiece)
                .collect(Collectors.toCollection(ArrayList::new));
        layoutPieces.addAll(workspace.pieces().stream()
                .filter(piece -> piece.variantIndex() > 0)
                .filter(piece -> usesPhysicalWorkspaceCell(piece.tags()))
                .map(piece -> toExistingVariantPiece(piece, canonicalByBaseName))
                .toList());
        return List.copyOf(layoutPieces);
    }

    private MKPlannedPiece toTemplatePiece(MKPlannedPiece basePiece) {
        return new MKPlannedPiece(
                basePiece.roleId(),
                basePiece.pieceName() + "_template",
                basePiece.interiorWidth(),
                basePiece.interiorLength(),
                basePiece.interiorHeight(),
                basePiece.connectors(),
                withWorkspaceTags(basePiece, TEMPLATE_PIECE_KIND, 0),
                basePiece.plannerId()
        );
    }

    private MKPlannedPiece toExistingVariantPiece(MKWorkspacePieceDefinition piece,
                                                  Map<String, MKPlannedPiece> canonicalByBaseName) {
        String baseName = getBaseName(piece);
        MKPlannedPiece basePiece = canonicalByBaseName.get(baseName);
        if (basePiece == null) {
            throw new IllegalStateException("missing canonical piece for base name " + baseName);
        }
        return new MKPlannedPiece(
                basePiece.roleId(),
                piece.pieceName(),
                basePiece.interiorWidth(),
                basePiece.interiorLength(),
                basePiece.interiorHeight(),
                basePiece.connectors(),
                withWorkspaceTags(basePiece, INSTANCE_PIECE_KIND, piece.variantIndex()),
                variantPlannerId(basePiece.plannerId(), piece)
        );
    }

    private MKWorkspacePlannerId variantPlannerId(MKWorkspacePlannerId basePlannerId,
                                                  MKWorkspacePieceDefinition piece) {
        if (!piece.plannerId().equals(MKWorkspacePlannerId.of(piece.roleId()).child(piece.pieceName()))) {
            return piece.plannerId();
        }
        return basePlannerId.child("variant_" + piece.variantIndex());
    }

    private boolean usesPhysicalWorkspaceCell(MKPlannedPiece piece) {
        return usesPhysicalWorkspaceCell(piece.tags());
    }

    private boolean usesPhysicalWorkspaceCell(Map<String, String> tags) {
        return !MKWorkspaceTemplateReuseTags.isDerived(tags);
    }

    private Map<String, String> withWorkspaceTags(MKPlannedPiece basePiece, String pieceKind, int variantIndex) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>(basePiece.tags());
        tags.put(MKWorkspaceGridLayout.TAG_BASE_NAME, basePiece.pieceName());
        tags.put(MKWorkspaceGridLayout.TAG_VARIANT_INDEX, Integer.toString(variantIndex));
        tags.put(WORKSPACE_PIECE_KIND_TAG, pieceKind);
        return tags;
    }

    private String getBaseName(MKWorkspacePieceDefinition piece) {
        return piece.tags().getOrDefault(MKWorkspaceGridLayout.TAG_BASE_NAME, piece.pieceName());
    }

    public record RegenerationPlan(
            List<MKPlannedPiece> layoutPieces,
            List<MKPlannedPiece> hallwayPieces,
            List<MKWorkspacePieceDefinition> existingHallwayPieces
    ) {
        public RegenerationPlan {
            layoutPieces = List.copyOf(layoutPieces);
            hallwayPieces = List.copyOf(hallwayPieces);
            existingHallwayPieces = List.copyOf(existingHallwayPieces);
        }

        public boolean hasWork() {
            return !hallwayPieces.isEmpty() || !existingHallwayPieces.isEmpty();
        }
    }
}
