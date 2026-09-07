package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.List;

final class MKJigsawLinkInsertPlacement {
    private MKJigsawLinkInsertPlacement() {
    }

    static boolean spanClearsDoglegConnectorArea(List<BlockPos> positions, int startIndex, int depth,
                                                 int shellWidth) {
        if (positions.size() < 3 || depth <= 0) {
            return true;
        }
        int endIndex = Math.min(positions.size() - 1, startIndex + depth - 1);
        int clearance = doglegConnectorClearance(shellWidth);
        for (int bendIndex = 1; bendIndex < positions.size() - 1; bendIndex++) {
            Direction previous = directionBetween(positions.get(bendIndex - 1), positions.get(bendIndex));
            Direction next = directionBetween(positions.get(bendIndex), positions.get(bendIndex + 1));
            if (previous != next &&
                    startIndex <= bendIndex + clearance &&
                    endIndex >= bendIndex - clearance) {
                return false;
            }
        }
        return true;
    }

    private static int doglegConnectorClearance(int shellWidth) {
        return Math.max(1, shellWidth / 2);
    }

    private static Direction directionBetween(BlockPos from, BlockPos to) {
        int dx = Integer.compare(to.getX(), from.getX());
        int dz = Integer.compare(to.getZ(), from.getZ());
        if (dx > 0) {
            return Direction.EAST;
        } else if (dx < 0) {
            return Direction.WEST;
        } else if (dz > 0) {
            return Direction.SOUTH;
        }
        return Direction.NORTH;
    }
}
