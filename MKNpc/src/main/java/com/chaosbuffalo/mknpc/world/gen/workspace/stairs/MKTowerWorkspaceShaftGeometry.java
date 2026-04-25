package com.chaosbuffalo.mknpc.world.gen.workspace.stairs;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerStairPlacement;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MKTowerWorkspaceShaftGeometry {
    public record ShaftGeometry(BoundingBox shaftBounds, int interiorMinY, int interiorMaxY,
                                MKTowerStairPlacement placement) {
        public int width() {
            return shaftBounds.getXSpan();
        }

        public int length() {
            return shaftBounds.getZSpan();
        }
    }

    public static ShaftGeometry forPiece(MKStructureWorkspace workspace, MKWorkspacePieceDefinition piece) {
        int shellMargin = piece.shellMargin();
        int exteriorAirMargin = workspace.exteriorAirMargin();
        int interiorWidth = piece.effectiveDimensions().roomWidth();
        int interiorLength = piece.effectiveDimensions().roomLength();
        int interiorMinX = piece.worldOrigin().getX() + exteriorAirMargin + shellMargin;
        int interiorMinZ = piece.worldOrigin().getZ() + exteriorAirMargin + shellMargin;
        int interiorMinY = piece.exportBounds().minY();
        int interiorMaxY = piece.exportBounds().maxY();
        int openingWidth = piece.connectors().stream()
                .filter(connector -> connector.facing() == Direction.UP || connector.facing() == Direction.DOWN)
                .mapToInt(MKWorkspaceConnectorDefinition::openingWidth)
                .findFirst()
                .orElse(workspace.dimensions().hallwayWidth());
        int openingLength = piece.connectors().stream()
                .filter(connector -> connector.facing() == Direction.UP || connector.facing() == Direction.DOWN)
                .mapToInt(MKWorkspaceConnectorDefinition::openingHeight)
                .findFirst()
                .orElse(workspace.dimensions().hallwayWidth());
        MKTowerStairPlacement placement = MKTowerStairPlacement.fromSerializedName(
                MKWorkspaceVerticalAccessTags.placement(piece.tags(), workspace.towerStairPlacement().getSerializedName())
        );

        int centerX = getCenterX(interiorMinX, interiorWidth, openingWidth, placement);
        int centerZ = getCenterZ(interiorMinZ, interiorLength, openingLength, placement);
        int halfWidth = openingWidth / 2;
        int halfLength = openingLength / 2;
        BoundingBox shaftBounds = new BoundingBox(
                centerX - halfWidth,
                interiorMinY,
                centerZ - halfLength,
                centerX + halfWidth,
                interiorMaxY,
                centerZ + halfLength
        );
        return new ShaftGeometry(shaftBounds, interiorMinY, interiorMaxY, placement);
    }

    public static List<BlockPos> getPerimeterClockwise(ShaftGeometry geometry) {
        BoundingBox bounds = geometry.shaftBounds();
        List<BlockPos> perimeter = new ArrayList<>();
        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
            perimeter.add(new BlockPos(x, geometry.interiorMinY(), bounds.minZ()));
        }
        for (int z = bounds.minZ() + 1; z <= bounds.maxZ(); z++) {
            perimeter.add(new BlockPos(bounds.maxX(), geometry.interiorMinY(), z));
        }
        if (bounds.maxZ() > bounds.minZ()) {
            for (int x = bounds.maxX() - 1; x >= bounds.minX(); x--) {
                perimeter.add(new BlockPos(x, geometry.interiorMinY(), bounds.maxZ()));
            }
        }
        if (bounds.maxX() > bounds.minX()) {
            for (int z = bounds.maxZ() - 1; z > bounds.minZ(); z--) {
                perimeter.add(new BlockPos(bounds.minX(), geometry.interiorMinY(), z));
            }
        }
        return perimeter;
    }

    public static BlockPos getPreferredStart(ShaftGeometry geometry) {
        BoundingBox bounds = geometry.shaftBounds();
        int centerX = (bounds.minX() + bounds.maxX()) / 2;
        int centerZ = (bounds.minZ() + bounds.maxZ()) / 2;
        return switch (geometry.placement()) {
            case NORTH -> new BlockPos(centerX, geometry.interiorMinY(), bounds.maxZ());
            case SOUTH -> new BlockPos(centerX, geometry.interiorMinY(), bounds.minZ());
            case EAST -> new BlockPos(bounds.minX(), geometry.interiorMinY(), centerZ);
            case WEST -> new BlockPos(bounds.maxX(), geometry.interiorMinY(), centerZ);
            case CENTER -> new BlockPos(centerX, geometry.interiorMinY(), bounds.maxZ());
        };
    }

    public static Direction getPreferredLadderFacing(ShaftGeometry geometry) {
        return switch (geometry.placement()) {
            case NORTH -> Direction.SOUTH;
            case SOUTH -> Direction.NORTH;
            case EAST -> Direction.WEST;
            case WEST -> Direction.EAST;
            case CENTER -> Direction.NORTH;
        };
    }

    public static int findClosestIndex(List<BlockPos> positions, BlockPos target) {
        return java.util.stream.IntStream.range(0, positions.size())
                .boxed()
                .min(Comparator.comparingInt(index -> manhattan(positions.get(index), target)))
                .orElse(0);
    }

    private static int getCenterX(int interiorMinX, int interiorWidth, int openingWidth, MKTowerStairPlacement placement) {
        int interiorMaxX = interiorMinX + interiorWidth - 1;
        int halfWidth = openingWidth / 2;
        return switch (placement) {
            case WEST -> interiorMinX + halfWidth;
            case EAST -> interiorMaxX - halfWidth;
            default -> interiorMinX + (interiorWidth / 2);
        };
    }

    private static int getCenterZ(int interiorMinZ, int interiorLength, int openingLength, MKTowerStairPlacement placement) {
        int interiorMaxZ = interiorMinZ + interiorLength - 1;
        int halfLength = openingLength / 2;
        return switch (placement) {
            case NORTH -> interiorMinZ + halfLength;
            case SOUTH -> interiorMaxZ - halfLength;
            default -> interiorMinZ + (interiorLength / 2);
        };
    }

    private static int manhattan(BlockPos left, BlockPos right) {
        return Math.abs(left.getX() - right.getX()) + Math.abs(left.getY() - right.getY()) +
                Math.abs(left.getZ() - right.getZ());
    }
}
