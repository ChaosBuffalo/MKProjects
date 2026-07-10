package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspacePieceTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTemplateReuseTags;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceVerticalAccessTags;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class WorkspacePieceDisplay {
    private WorkspacePieceDisplay() {
    }

    public static Map<String, List<MKWorkspacePieceDefinition>> groupPiecesByTopology(MKStructureWorkspace workspace) {
        return groupPiecesByTopology(workspace.pieces());
    }

    public static Map<String, List<MKWorkspacePieceDefinition>> groupAuthoredPiecesByTopology(
            MKStructureWorkspace workspace) {
        return groupPiecesByTopology(workspace.pieces().stream()
                .filter(WorkspacePieceDisplay::isAuthoredTemplatePiece)
                .toList());
    }

    private static Map<String, List<MKWorkspacePieceDefinition>> groupPiecesByTopology(
            List<MKWorkspacePieceDefinition> pieces) {
        Map<String, List<MKWorkspacePieceDefinition>> grouped = new LinkedHashMap<>();
        List<MKWorkspacePieceDefinition> sortedPieces = pieces.stream()
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

    public static boolean isAuthoredTemplatePiece(MKWorkspacePieceDefinition piece) {
        return !"template".equals(piece.tags().getOrDefault("workspace_piece_kind", "instance")) &&
                !MKWorkspaceTemplateReuseTags.isDerived(piece.tags());
    }

    public static String buildWorkspaceGroupKey(MKWorkspacePieceDefinition piece) {
        String linearRunFamilyId = piece.tags().get("workspace_linear_run_family_id");
        if (linearRunFamilyId != null) {
            return "linear_run:" + linearRunFamilyId + ":" +
                    piece.tags().getOrDefault("workspace_linear_run_path_kind", "branch");
        }
        String familyId = piece.tags().get("workspace_family_id");
        if (familyId != null) {
            return "room:" + piece.tags().getOrDefault("workspace_topology_slot_id",
                    piece.tags().getOrDefault("workspace_topology_group", "main")) + ":" +
                    familyId + ":" + piece.tags().getOrDefault("workspace_horizontal_exits", "none");
        }
        return "role:" + piece.roleId();
    }

    public static String buildWorkspaceGroupLabel(MKWorkspacePieceDefinition piece) {
        String linearRunFamilyId = piece.tags().get("workspace_linear_run_family_id");
        if (linearRunFamilyId != null) {
            return "Linear Run / " + linearRunFamilyId + " / " +
                    formatTopologyLabel(piece.tags().getOrDefault("workspace_linear_run_path_kind", "branch"));
        }
        String familyId = piece.tags().get("workspace_family_id");
        if (familyId != null) {
            return formatTopologyLabel(piece.tags().getOrDefault("workspace_topology_slot_id",
                    piece.tags().getOrDefault("workspace_topology_group", "main"))) +
                    " / " + familyId;
        }
        return formatTopologyLabel(piece.roleId());
    }

    public static String getBaseName(MKWorkspacePieceDefinition piece) {
        return piece.tags().getOrDefault("workspace_base_name", piece.pieceName());
    }

    public static String describePiece(MKWorkspacePieceDefinition piece) {
        String label = piece.variantIndex() == 0 ? "template" : "variant " + piece.variantIndex();
        String warning = piece.tags().get(MKWorkspacePieceTags.DISABLED_REASON);
        if (warning != null && !warning.isBlank()) {
            return label + ": " + piece.pieceName() + " | Warning: " + warning;
        }
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
        String[] parts = key.split("[._]");
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
