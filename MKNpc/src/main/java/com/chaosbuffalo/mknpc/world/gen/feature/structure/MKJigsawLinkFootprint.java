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
            }
        }
        addBendInterior(route, minInterior, maxInterior, interior);

        LinkedHashMap<BlockPos, Cell> boundary = new LinkedHashMap<>();
        Map<BlockPos, Cell> openEnds = openEndCells(route, minInterior, maxInterior);
        for (Map.Entry<BlockPos, Cell> entry : interior.entrySet()) {
            BlockPos pos = entry.getKey();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos neighbor = pos.relative(direction);
                if (!interior.containsKey(neighbor) && !openEnds.containsKey(neighbor)) {
                    boundary.putIfAbsent(neighbor, new Cell(entry.getValue().routeIndex(), false));
                }
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

    private static void addBendInterior(List<BlockPos> route, int minInterior, int maxInterior,
                                        LinkedHashMap<BlockPos, Cell> interior) {
        for (int index = 1; index < route.size() - 1; index++) {
            Direction.Axis previous = directionBetween(route.get(index - 1), route.get(index)).getAxis();
            Direction.Axis next = directionBetween(route.get(index), route.get(index + 1)).getAxis();
            if (previous == next) {
                continue;
            }
            BlockPos bend = route.get(index);
            int minX = bend.getX();
            int maxX = bend.getX();
            int minZ = bend.getZ();
            int maxZ = bend.getZ();
            for (int across = minInterior; across <= maxInterior; across++) {
                BlockPos previousOffset = offsetAcross(bend, previous, across);
                BlockPos nextOffset = offsetAcross(bend, next, across);
                minX = Math.min(minX, Math.min(previousOffset.getX(), nextOffset.getX()));
                maxX = Math.max(maxX, Math.max(previousOffset.getX(), nextOffset.getX()));
                minZ = Math.min(minZ, Math.min(previousOffset.getZ(), nextOffset.getZ()));
                maxZ = Math.max(maxZ, Math.max(previousOffset.getZ(), nextOffset.getZ()));
            }
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    interior.putIfAbsent(new BlockPos(x, bend.getY(), z), new Cell(index, false));
                }
            }
        }
    }

    private static Map<BlockPos, Cell> openEndCells(List<BlockPos> route, int minInterior, int maxInterior) {
        LinkedHashMap<BlockPos, Cell> openEnds = new LinkedHashMap<>();
        if (route.size() <= 1) {
            return openEnds;
        }
        Direction firstDirection = directionBetween(route.get(0), route.get(1));
        addOpenEnd(openEnds, route.get(0).relative(firstDirection.getOpposite()), firstDirection.getAxis(),
                minInterior, maxInterior);
        Direction lastDirection = directionBetween(route.get(route.size() - 2), route.getLast());
        addOpenEnd(openEnds, route.getLast().relative(lastDirection), lastDirection.getAxis(), minInterior,
                maxInterior);
        return openEnds;
    }

    private static void addOpenEnd(Map<BlockPos, Cell> openEnds, BlockPos center, Direction.Axis axis,
                                   int minInterior, int maxInterior) {
        for (int across = minInterior; across <= maxInterior; across++) {
            openEnds.put(offsetAcross(center, axis, across), new Cell(0, false));
        }
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
