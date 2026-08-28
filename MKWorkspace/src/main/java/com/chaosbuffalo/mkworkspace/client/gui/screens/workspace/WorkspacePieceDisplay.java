package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceContentSelectionTags;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTemplatePurpose;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspacePieceTags;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTemplateReuseTags;
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
        return MKWorkspaceContentSelectionTags.purpose(piece).placeable() &&
                !MKWorkspaceTemplateReuseTags.isDerived(piece.tags());
    }

    public static String buildWorkspaceGroupKey(MKWorkspacePieceDefinition piece) {
        String explicitFamilyId = piece.tags().get(MKWorkspaceContentSelectionTags.FAMILY_ID);
        if (explicitFamilyId != null && !explicitFamilyId.isBlank()) {
            return "slot_family:" + MKWorkspaceContentSelectionTags.topologySlotId(piece) + ":" + explicitFamilyId;
        }
        String linearRunFamilyId = piece.tags().get("workspace_linear_run_family_id");
        if (linearRunFamilyId != null) {
            return "linear_run:" + linearRunFamilyId + ":" +
                    piece.tags().getOrDefault("workspace_linear_run_path_kind", "branch");
        }
        String insertFamilyId = piece.tags().get(MKWorkspaceInsertFamilyDefinition.TAG_INSERT_FAMILY_ID);
        if (insertFamilyId != null) {
            return "insert_family:" + insertFamilyId;
        }
        String familyId = piece.tags().get("workspace_family_id");
        if (familyId != null) {
            return "room:" + piece.tags().getOrDefault("workspace_topology_slot_id",
                    piece.tags().getOrDefault("workspace_topology_group", "main")) + ":" +
                    familyId + ":" + piece.tags().getOrDefault("workspace_horizontal_exits", "none");
        }
        String baseName = getBaseName(piece);
        if (!baseName.isBlank()) {
            return "base:" + baseName;
        }
        String topologySlotId = piece.tags().get("workspace_topology_slot_id");
        if (topologySlotId != null && !topologySlotId.isBlank()) {
            return "slot:" + topologySlotId;
        }
        return "role:" + piece.roleId();
    }

    public static String buildWorkspaceGroupLabel(MKWorkspacePieceDefinition piece) {
        String explicitFamilyId = piece.tags().get(MKWorkspaceContentSelectionTags.FAMILY_ID);
        if (explicitFamilyId != null && !explicitFamilyId.isBlank()) {
            return "Slot / " + formatTopologyLabel(MKWorkspaceContentSelectionTags.topologySlotId(piece)) +
                    " / Family / " + formatTopologyLabel(explicitFamilyId);
        }
        String linearRunFamilyId = piece.tags().get("workspace_linear_run_family_id");
        if (linearRunFamilyId != null) {
            return "Linear Run / " + linearRunFamilyId + " / " +
                    formatTopologyLabel(piece.tags().getOrDefault("workspace_linear_run_path_kind", "branch"));
        }
        String insertFamilyId = piece.tags().get(MKWorkspaceInsertFamilyDefinition.TAG_INSERT_FAMILY_ID);
        if (insertFamilyId != null) {
            return "Insert Family / " + insertFamilyId;
        }
        String familyId = piece.tags().get("workspace_family_id");
        if (familyId != null) {
            return formatTopologyLabel(piece.tags().getOrDefault("workspace_topology_slot_id",
                    piece.tags().getOrDefault("workspace_topology_group", "main"))) +
                    " / " + familyId;
        }
        String baseName = getBaseName(piece);
        if (!baseName.isBlank()) {
            return formatTopologyLabel(baseName);
        }
        String topologySlotId = piece.tags().get("workspace_topology_slot_id");
        if (topologySlotId != null && !topologySlotId.isBlank()) {
            return formatTopologyLabel(topologySlotId);
        }
        return formatTopologyLabel(piece.roleId());
    }

    public static String getBaseName(MKWorkspacePieceDefinition piece) {
        return piece.tags().getOrDefault("workspace_base_name", piece.pieceName());
    }

    public static String describePiece(MKWorkspacePieceDefinition piece) {
        String label = switch (MKWorkspaceContentSelectionTags.purpose(piece)) {
            case FAMILY_CANONICAL -> "canonical";
            case FAMILY_VARIANT -> "variant " + MKWorkspaceContentSelectionTags.variantId(piece);
            case SLOT_SCAFFOLD -> "slot scaffold";
            case DERIVED_DATA_ONLY -> "data-only template";
        };
        String warning = piece.tags().get(MKWorkspacePieceTags.DISABLED_REASON);
        if (warning != null && !warning.isBlank()) {
            return label + ": " + piece.pieceName() + " | Warning: " + warning;
        }
        return label + ": " + piece.pieceName();
    }

    public static int countVariants(List<MKWorkspacePieceDefinition> pieces) {
        return (int) pieces.stream().filter(piece ->
                MKWorkspaceContentSelectionTags.purpose(piece) == MKWorkspaceTemplatePurpose.FAMILY_VARIANT).count();
    }

    public static boolean supportsStairGeneration(List<MKWorkspacePieceDefinition> pieces) {
        return pieces.stream().anyMatch(WorkspacePieceDisplay::supportsStairGeneration);
    }

    public static boolean supportsStairGeneration(MKWorkspacePieceDefinition piece) {
        return MKWorkspaceVerticalAccessTags.supportsVerticalAccess(piece.tags()) &&
                !MKWorkspaceTemplateReuseTags.isDerived(piece.tags());
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
