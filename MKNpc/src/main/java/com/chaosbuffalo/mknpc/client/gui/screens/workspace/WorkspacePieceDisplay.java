package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessTags;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class WorkspacePieceDisplay {
    private WorkspacePieceDisplay() {
    }

    public static Map<String, List<MKWorkspacePieceDefinition>> groupPiecesByTopology(MKStructureWorkspace workspace) {
        Map<String, List<MKWorkspacePieceDefinition>> grouped = new LinkedHashMap<>();
        List<MKWorkspacePieceDefinition> sortedPieces = workspace.pieces().stream()
                .sorted(Comparator
                        .comparing(WorkspacePieceDisplay::buildWorkspaceGroupLabel)
                        .thenComparingInt(MKWorkspacePieceDefinition::variantIndex))
                .toList();
        for (MKWorkspacePieceDefinition piece : sortedPieces) {
            String topologyKey = buildWorkspaceGroupKey(piece);
            grouped.computeIfAbsent(topologyKey, ignored -> new java.util.ArrayList<>()).add(piece);
        }
        return grouped;
    }

    public static String buildWorkspaceGroupKey(MKWorkspacePieceDefinition piece) {
        String hallwayFamilyId = piece.tags().get("workspace_hallway_family_id");
        if (hallwayFamilyId != null) {
            return "hallway:" + hallwayFamilyId + ":" +
                    piece.tags().getOrDefault("workspace_hallway_path_kind", "branch");
        }
        String familyId = piece.tags().get("workspace_family_id");
        if (familyId != null) {
            return "room:" + piece.tags().getOrDefault("workspace_category", "main") + ":" +
                    familyId + ":" + piece.tags().getOrDefault("workspace_horizontal_exits", "none");
        }
        return "role:" + piece.role().getSerializedName();
    }

    public static String buildWorkspaceGroupLabel(MKWorkspacePieceDefinition piece) {
        String hallwayFamilyId = piece.tags().get("workspace_hallway_family_id");
        if (hallwayFamilyId != null) {
            return "Hallway / " + hallwayFamilyId + " / " +
                    formatTopologyLabel(piece.tags().getOrDefault("workspace_hallway_path_kind", "branch"));
        }
        String familyId = piece.tags().get("workspace_family_id");
        if (familyId != null) {
            return formatTopologyLabel(piece.tags().getOrDefault("workspace_category", "main")) +
                    " / " + familyId +
                    " / exits " + piece.tags().getOrDefault("workspace_horizontal_exits", "none");
        }
        return formatTopologyLabel(piece.role().getSerializedName());
    }

    public static String getBaseName(MKWorkspacePieceDefinition piece) {
        return piece.tags().getOrDefault("workspace_base_name", piece.pieceName());
    }

    public static String describePiece(MKWorkspacePieceDefinition piece) {
        String label = piece.variantIndex() == 0 ? "template" : "variant " + piece.variantIndex();
        return label + ": " + piece.pieceName();
    }

    public static int countVariants(List<MKWorkspacePieceDefinition> pieces) {
        return (int) pieces.stream().filter(piece -> piece.variantIndex() > 0).count();
    }

    public static boolean supportsStairGeneration(List<MKWorkspacePieceDefinition> pieces) {
        return pieces.stream().anyMatch(WorkspacePieceDisplay::supportsStairGeneration);
    }

    public static boolean supportsStairGeneration(MKWorkspacePieceDefinition piece) {
        return MKWorkspaceVerticalAccessTags.supportsVerticalAccess(piece.tags());
    }

    public static boolean hasGeneratedStairs(MKWorkspacePieceDefinition piece) {
        return !piece.generatedStairPositions().isEmpty() &&
                !"none".equals(piece.tags().getOrDefault("generated_stair_mode", "none"));
    }

    public static String formatTopologyLabel(String key) {
        String[] parts = key.split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            if (!part.isEmpty()) {
                builder.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    builder.append(part.substring(1));
                }
            }
        }
        return builder.toString();
    }
}
