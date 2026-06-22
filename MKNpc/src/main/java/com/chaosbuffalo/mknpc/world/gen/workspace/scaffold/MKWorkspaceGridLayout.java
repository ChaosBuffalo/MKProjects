package com.chaosbuffalo.mknpc.world.gen.workspace.scaffold;

import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKPlannedPiece;
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
        int maxPreviewWidth = 0;
        int maxPreviewLength = 0;
        int maxPreviewHeight = 0;
        for (MKPlannedPiece piece : pieces) {
            int exportWidth = getExportWidth(piece, shellMargin, exteriorAirMargin);
            int exportLength = getExportLength(piece, shellMargin, exteriorAirMargin);
            int exportHeight = getExportHeight(piece, shellMargin);
            int previewWidth = exportWidth + (2 * previewMargin);
            int previewLength = exportLength + (2 * previewMargin);
            int previewHeight = exportHeight;
            maxPreviewWidth = Math.max(maxPreviewWidth, previewWidth);
            maxPreviewLength = Math.max(maxPreviewLength, previewLength);
            maxPreviewHeight = Math.max(maxPreviewHeight, previewHeight);
        }

        int strideX = maxPreviewWidth + cellPadding;
        int strideZ = maxPreviewLength + cellPadding;
        BlockPos start = anchor.offset(WORKSPACE_START_MARGIN, 0, WORKSPACE_START_MARGIN);
        Map<String, Integer> columnByBaseName = new LinkedHashMap<>();
        List<Placement> placements = new ArrayList<>();
        for (MKPlannedPiece piece : pieces) {
            String baseName = getBaseName(piece);
            int column = columnByBaseName.computeIfAbsent(baseName, ignored -> columnByBaseName.size());
            int row = getVariantIndex(piece);
            int previewWidth = getExportWidth(piece, shellMargin, exteriorAirMargin) + (2 * previewMargin);
            int previewLength = getExportLength(piece, shellMargin, exteriorAirMargin) + (2 * previewMargin);
            int previewHeight = getExportHeight(piece, shellMargin);
            BlockPos previewOrigin = start.offset(column * strideX, 0, row * strideZ);
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

    private int getExportWidth(MKPlannedPiece piece, int shellMargin, int exteriorAirMargin) {
        int effectiveShellMargin = getShellMargin(piece, shellMargin);
        int margin = isEmptyScaffold(piece) ? 0 : exteriorAirMargin;
        return piece.interiorWidth() + (2 * effectiveShellMargin) + (2 * margin);
    }

    private int getExportLength(MKPlannedPiece piece, int shellMargin, int exteriorAirMargin) {
        int effectiveShellMargin = getShellMargin(piece, shellMargin);
        int margin = isEmptyScaffold(piece) ? 0 : exteriorAirMargin;
        return piece.interiorLength() + (2 * effectiveShellMargin) + (2 * margin);
    }

    private int getExportHeight(MKPlannedPiece piece, int shellMargin) {
        return piece.interiorHeight() + (2 * getVerticalShellThickness(piece));
    }

    private int getShellMargin(MKPlannedPiece piece, int shellMargin) {
        return isEmptyScaffold(piece) ? 0 : shellMargin;
    }

    private int getVerticalShellThickness(MKPlannedPiece piece) {
        return isEmptyScaffold(piece) ? 0 : 1;
    }

    private boolean isEmptyScaffold(MKPlannedPiece piece) {
        return "embedded_stair".equals(piece.tags().get("tower_piece_kind")) ||
                "floor_link_insert".equals(piece.tags().get("tower_piece_kind"));
    }
}
