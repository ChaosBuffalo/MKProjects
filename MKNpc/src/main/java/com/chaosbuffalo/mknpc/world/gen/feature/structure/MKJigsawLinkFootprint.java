package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class MKJigsawLinkFootprint {
    private MKJigsawLinkFootprint() {
    }

    record Cell(int routeIndex, boolean center) {
    }

    record Footprint(Map<BlockPos, Cell> interior, Map<BlockPos, Cell> boundary) {
    }

    static Footprint build(List<BlockPos> route, int width) {
        LinkedHashMap<BlockPos, Cell> interior = new LinkedHashMap<>();
        LinkedHashMap<BlockPos, Cell> boundaryCandidates = new LinkedHashMap<>();
        if (route.isEmpty()) {
            return new Footprint(Map.of(), Map.of());
        }
        int minInterior = -((width - 1) / 2);
        int maxInterior = width / 2;
        for (int index = 0; index < route.size(); index++) {
            for (Direction.Axis axis : routeAxes(route, index)) {
                for (int across = minInterior; across <= maxInterior; across++) {
                    interior.putIfAbsent(offsetAcross(route.get(index), axis, across),
                            new Cell(index, across > minInterior && across < maxInterior));
                }
                boundaryCandidates.putIfAbsent(offsetAcross(route.get(index), axis, minInterior - 1),
                        new Cell(index, false));
                boundaryCandidates.putIfAbsent(offsetAcross(route.get(index), axis, maxInterior + 1),
                        new Cell(index, false));
            }
        }

        LinkedHashMap<BlockPos, Cell> boundary = new LinkedHashMap<>();
        for (Map.Entry<BlockPos, Cell> entry : boundaryCandidates.entrySet()) {
            if (!interior.containsKey(entry.getKey())) {
                boundary.put(entry.getKey(), entry.getValue());
            }
        }
        return new Footprint(unmodifiableCopy(interior), unmodifiableCopy(boundary));
    }

    private static Map<BlockPos, Cell> unmodifiableCopy(LinkedHashMap<BlockPos, Cell> positions) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(positions));
    }

    private static List<Direction.Axis> routeAxes(List<BlockPos> route, int index) {
        if (route.size() <= 1) {
            return List.of(Direction.Axis.X);
        }
        if (index == 0) {
            return List.of(directionBetween(route.get(0), route.get(1)).getAxis());
        }
        if (index == route.size() - 1) {
            return List.of(directionBetween(route.get(index - 1), route.get(index)).getAxis());
        }
        Direction.Axis previous = directionBetween(route.get(index - 1), route.get(index)).getAxis();
        Direction.Axis next = directionBetween(route.get(index), route.get(index + 1)).getAxis();
        return previous == next ? List.of(previous) : List.of(previous, next);
    }

    private static BlockPos offsetAcross(BlockPos center, Direction.Axis axis, int across) {
        return axis == Direction.Axis.X ? center.offset(0, 0, across) : center.offset(across, 0, 0);
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
