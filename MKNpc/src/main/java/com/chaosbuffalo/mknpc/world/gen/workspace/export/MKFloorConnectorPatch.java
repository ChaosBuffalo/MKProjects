package com.chaosbuffalo.mknpc.world.gen.workspace.export;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class MKFloorConnectorPatch {
    private MKFloorConnectorPatch() {
    }

    static List<BlockPos> closedConnectorPatchPositions(MKWorkspacePieceDefinition piece) {
        int count = parseInt(piece.tags().get(MKFloorMaskVariantExporter.CLOSED_CONNECTOR_COUNT_TAG), 0);
        if (count <= 0) {
            return List.of();
        }
        ArrayList<BlockPos> positions = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            String prefix = MKFloorMaskVariantExporter.CLOSED_CONNECTOR_PREFIX + index + "_";
            positions.addAll(closedConnectorPatchPositions(piece.tags(), prefix));
        }
        return List.copyOf(positions);
    }

    private static List<BlockPos> closedConnectorPatchPositions(Map<String, String> tags, String prefix) {
        Direction facing = Direction.byName(tags.getOrDefault(prefix + "facing", ""));
        if (facing == null || !facing.getAxis().isHorizontal()) {
            return List.of();
        }
        BlockPos pos = new BlockPos(
                parseInt(tags.get(prefix + "x"), 0),
                parseInt(tags.get(prefix + "y"), 0),
                parseInt(tags.get(prefix + "z"), 0)
        );
        int openingWidth = Math.max(1, parseInt(tags.get(prefix + "opening_width"), 1));
        int openingHeight = Math.max(1, parseInt(tags.get(prefix + "opening_height"), 1));
        int minMinorOffset = -((openingWidth - 1) / 2);
        int maxMinorOffset = openingWidth / 2;
        ArrayList<BlockPos> positions = new ArrayList<>(openingWidth * openingHeight);
        for (int minor = minMinorOffset; minor <= maxMinorOffset; minor++) {
            for (int dy = 0; dy < openingHeight; dy++) {
                if (facing == Direction.NORTH || facing == Direction.SOUTH) {
                    positions.add(pos.offset(minor, dy, 0));
                } else {
                    positions.add(pos.offset(0, dy, minor));
                }
            }
        }
        return List.copyOf(positions);
    }

    private static int parseInt(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
