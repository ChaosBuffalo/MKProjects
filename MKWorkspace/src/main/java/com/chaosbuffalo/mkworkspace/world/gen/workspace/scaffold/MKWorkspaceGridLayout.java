package com.chaosbuffalo.mkworkspace.world.gen.workspace.scaffold;

import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKPlannedPiece;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceContentSelectionTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MKWorkspaceGridLayout {
    public static final String TAG_BASE_NAME = "workspace_base_name";
    public static final String TAG_VARIANT_INDEX = "workspace_variant_index";
    public static final int WORKSPACE_START_MARGIN = 8;

    public record Placement(BlockPos previewOrigin, BoundingBox previewBounds) {
    }

    public List<Placement> assignPlacements(BlockPos anchor, List<MKPlannedPiece> pieces, int shellMargin,
                                            int exteriorAirMargin, int previewMargin, int columns, int cellPadding) {
        return assignPlacements(anchor, pieces, shellMargin, 1, exteriorAirMargin, previewMargin, columns,
                cellPadding);
    }

    public List<Placement> assignPlacements(BlockPos anchor, List<MKPlannedPiece> pieces, int shellMargin,
                                            int verticalShellMargin, int exteriorAirMargin, int previewMargin,
                                            int columns, int cellPadding) {
        Map<String, Integer> columnWidths = new LinkedHashMap<>();
        Map<String, String> columnGroupKeys = new LinkedHashMap<>();
        int maxPreviewLength = 0;
        int maxPreviewHeight = 0;
        for (MKPlannedPiece piece : pieces) {
            int exportLength = getExportLength(piece, shellMargin, exteriorAirMargin);
            int exportHeight = getExportHeight(piece, verticalShellMargin);
            int previewWidth = getExportWidth(piece, shellMargin, exteriorAirMargin) + (2 * previewMargin);
            int previewLength = exportLength + (2 * previewMargin);
            int previewHeight = exportHeight;
            String baseName = getBaseName(piece);
            columnWidths.merge(baseName, previewWidth, Math::max);
            columnGroupKeys.putIfAbsent(baseName, getColumnGroupKey(piece, baseName));
            maxPreviewLength = Math.max(maxPreviewLength, previewLength);
            maxPreviewHeight = Math.max(maxPreviewHeight, previewHeight);
        }

        Map<String, Integer> columnOrigins = new LinkedHashMap<>();
        int nextColumnX = 0;
        Map<String, List<String>> columnsByGroup = new LinkedHashMap<>();
        for (String baseName : columnWidths.keySet()) {
            columnsByGroup.computeIfAbsent(columnGroupKeys.get(baseName), ignored -> new ArrayList<>())
                    .add(baseName);
        }
        for (List<String> groupColumns : columnsByGroup.values()) {
            for (String baseName : groupColumns) {
                columnOrigins.put(baseName, nextColumnX);
                nextColumnX += columnWidths.get(baseName) + cellPadding;
            }
        }
        int strideZ = maxPreviewLength + cellPadding;
        BlockPos start = anchor.offset(WORKSPACE_START_MARGIN, 0, WORKSPACE_START_MARGIN);
        List<Placement> placements = new ArrayList<>();
        for (MKPlannedPiece piece : pieces) {
            String baseName = getBaseName(piece);
            int row = getVariantIndex(piece);
            int previewWidth = getExportWidth(piece, shellMargin, exteriorAirMargin) + (2 * previewMargin);
            int previewLength = getExportLength(piece, shellMargin, exteriorAirMargin) + (2 * previewMargin);
            int previewHeight = getExportHeight(piece, verticalShellMargin);
            BlockPos previewOrigin = start.offset(columnOrigins.getOrDefault(baseName, 0), 0, row * strideZ);
            BoundingBox previewBounds = new BoundingBox(
                    previewOrigin.getX(),
                    previewOrigin.getY(),
                    previewOrigin.getZ(),
                    previewOrigin.getX() + previewWidth - 1,
                    previewOrigin.getY() + previewHeight - 1,
                    previewOrigin.getZ() + previewLength - 1
            );
            placements.add(new Placement(previewOrigin, previewBounds));
        }
        return placements;
    }

    private String getBaseName(MKPlannedPiece piece) {
        return piece.tags().getOrDefault(TAG_BASE_NAME, piece.pieceName());
    }

    private int getVariantIndex(MKPlannedPiece piece) {
        try {
            return Integer.parseInt(piece.tags().getOrDefault(TAG_VARIANT_INDEX, "0"));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private String getColumnGroupKey(MKPlannedPiece piece, String baseName) {
        String topologySlotId = MKWorkspaceContentSelectionTags.topologySlotId(piece.roleId(), piece.tags());
        return topologySlotId.isBlank() ? "column:" + baseName : "slot:" + topologySlotId;
    }

    private int getExportWidth(MKPlannedPiece piece, int shellMargin, int exteriorAirMargin) {
        int effectiveShellMargin = getShellMargin(piece, shellMargin);
        int margin = isExactBoundsScaffold(piece) ? 0 : exteriorAirMargin;
        return piece.interiorWidth() + (2 * effectiveShellMargin) + (2 * margin);
    }

    private int getExportLength(MKPlannedPiece piece, int shellMargin, int exteriorAirMargin) {
        int effectiveShellMargin = getShellMargin(piece, shellMargin);
        int margin = isExactBoundsScaffold(piece) ? 0 : exteriorAirMargin;
        return piece.interiorLength() + (2 * effectiveShellMargin) + (2 * margin);
    }

    private int getExportHeight(MKPlannedPiece piece, int verticalShellMargin) {
        return piece.interiorHeight() + (2 * getVerticalShellMargin(piece, verticalShellMargin));
    }

    private int getShellMargin(MKPlannedPiece piece, int shellMargin) {
        return isExactBoundsScaffold(piece) ? 0 : shellMargin;
    }

    private int getVerticalShellMargin(MKPlannedPiece piece, int verticalShellMargin) {
        return isExactBoundsScaffold(piece) ? 0 : verticalShellMargin;
    }

    private boolean isExactBoundsScaffold(MKPlannedPiece piece) {
        return "embedded_stair".equals(piece.tags().get("tower_piece_kind")) ||
                "floor_link_insert".equals(piece.tags().get("tower_piece_kind"));
    }
}
