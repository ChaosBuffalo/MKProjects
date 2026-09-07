package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.export;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MKFloorConnectorPatch {
    private MKFloorConnectorPatch() {
    }

    public static List<BlockPos> closedConnectorPatchPositions(MKWorkspacePieceDefinition piece) {
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
        int closureDepth = Math.max(1, parseInt(tags.get(prefix + "closure_depth"), 1));
        int minMinorOffset = -((openingWidth - 1) / 2);
        int maxMinorOffset = openingWidth / 2;
        ArrayList<BlockPos> positions = new ArrayList<>(openingWidth * openingHeight * closureDepth);
        for (int depth = 0; depth < closureDepth; depth++) {
            BlockPos basePos = depth == 0 ? pos : pos.relative(facing.getOpposite(), depth);
            for (int minor = minMinorOffset; minor <= maxMinorOffset; minor++) {
                for (int dy = 0; dy < openingHeight; dy++) {
                    if (facing == Direction.NORTH || facing == Direction.SOUTH) {
                        positions.add(basePos.offset(minor, dy, 0));
                    } else {
                        positions.add(basePos.offset(0, dy, minor));
                    }
                }
            }
        }
        return List.copyOf(positions);
    }

    public static int closureDepth(MKWorkspacePieceDefinition piece, MKWorkspaceConnectorDefinition connector) {
        Direction facing = connector.facing();
        if (facing == null || !facing.getAxis().isHorizontal()) {
            return 1;
        }
        int span = facing.getAxis() == Direction.Axis.X ?
                piece.exportBounds().getXSpan() :
                piece.exportBounds().getZSpan();
        int roomSpan = facing.getAxis() == Direction.Axis.X ?
                piece.effectiveDimensions().roomWidth() :
                piece.effectiveDimensions().roomLength();
        if (span <= 0 || roomSpan <= 0) {
            return 1;
        }
        int horizontalPadding = Math.max(0, (span - roomSpan) / 2);
        int innerShellPlane = switch (facing) {
            case WEST, NORTH -> Math.max(0, horizontalPadding - 1);
            case EAST, SOUTH -> span - horizontalPadding;
            default -> 0;
        };
        int connectorCoord = facing.getAxis() == Direction.Axis.X ?
                connector.relativePos().getX() :
                connector.relativePos().getZ();
        if (connectorCoord < 0 || connectorCoord >= span) {
            return 1;
        }
        return switch (facing) {
            case WEST, NORTH -> connectorCoord < innerShellPlane ? innerShellPlane - connectorCoord + 1 : 1;
            case EAST, SOUTH -> connectorCoord > innerShellPlane ? connectorCoord - innerShellPlane + 1 : 1;
            default -> 1;
        };
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
