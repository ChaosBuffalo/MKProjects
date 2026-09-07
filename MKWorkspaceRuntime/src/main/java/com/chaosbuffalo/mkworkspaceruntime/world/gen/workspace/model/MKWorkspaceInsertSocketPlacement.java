package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

public final class MKWorkspaceInsertSocketPlacement {
    private MKWorkspaceInsertSocketPlacement() {
    }

    public static BlockPos jigsawLocalPos(int width, int height, int depth,
                                          MKWorkspaceInsertAttachmentFace attachmentFace,
                                          int faceUOffset, int faceVOffset) {
        return switch (attachmentFace) {
            case NORTH -> new BlockPos(faceUOffset, faceVOffset, 0);
            case SOUTH -> new BlockPos(faceUOffset, faceVOffset, depth - 1);
            case WEST -> new BlockPos(0, faceVOffset, faceUOffset);
            case EAST -> new BlockPos(width - 1, faceVOffset, faceUOffset);
            case BOTTOM -> new BlockPos(faceUOffset, 0, faceVOffset);
            case TOP -> new BlockPos(faceUOffset, height - 1, faceVOffset);
        };
    }

    public static BoundingBox projectedInsertBounds(BoundingBox hostLocalBounds, BlockPos hostSocketLocalPos,
                                                    int width, int height, int depth,
                                                    MKWorkspaceInsertAttachmentFace attachmentFace,
                                                    int faceUOffset, int faceVOffset) {
        BlockPos jigsawLocalPos = jigsawLocalPos(width, height, depth, attachmentFace, faceUOffset, faceVOffset);
        int minX = hostSocketLocalPos.getX() - jigsawLocalPos.getX();
        int minY = hostSocketLocalPos.getY() - jigsawLocalPos.getY();
        int minZ = hostSocketLocalPos.getZ() - jigsawLocalPos.getZ();
        return new BoundingBox(minX, minY, minZ, minX + width - 1, minY + height - 1,
                minZ + depth - 1);
    }

    public static BoundingBox projectedOrientedInsertBounds(BlockPos hostSocketLocalPos,
                                                            int width, int height, int depth,
                                                            MKWorkspaceInsertAttachmentFace authoredAttachmentFace,
                                                            int faceUOffset, int faceVOffset,
                                                            MKWorkspaceInsertAttachmentFace placementAttachmentFace,
                                                            Direction placementTop) {
        Direction authoredFront = authoredAttachmentFace.direction();
        Direction authoredTop = defaultTopForFront(authoredFront);
        Direction placementFront = placementAttachmentFace.direction();
        Direction resolvedPlacementTop = validTopForFront(placementFront, placementTop) ?
                placementTop : defaultTopForFront(placementFront);

        BlockPos jigsawLocalPos = jigsawLocalPos(width, height, depth, authoredAttachmentFace,
                faceUOffset, faceVOffset);
        MutableBounds bounds = new MutableBounds();
        int[] xs = new int[]{0, width - 1};
        int[] ys = new int[]{0, height - 1};
        int[] zs = new int[]{0, depth - 1};
        for (int x : xs) {
            for (int y : ys) {
                for (int z : zs) {
                    BlockPos localOffset = new BlockPos(x - jigsawLocalPos.getX(), y - jigsawLocalPos.getY(),
                            z - jigsawLocalPos.getZ());
                    BlockPos projectedOffset = rotateOffset(localOffset, authoredFront, authoredTop,
                            placementFront, resolvedPlacementTop);
                    bounds.include(hostSocketLocalPos.offset(projectedOffset));
                }
            }
        }
        return bounds.toBoundingBox();
    }

    public static List<String> validateExteriorFaceOffset(int width, int height, int depth,
                                                          MKWorkspaceInsertAttachmentFace attachmentFace,
                                                          int faceUOffset, int faceVOffset) {
        ArrayList<String> errors = new ArrayList<>();
        addDimensionErrors(errors, width, height, depth);
        if (attachmentFace == null) {
            errors.add("attachment face is required");
            return errors;
        }
        if (!errors.isEmpty()) {
            return errors;
        }

        int maxU = attachmentFace == MKWorkspaceInsertAttachmentFace.WEST ||
                attachmentFace == MKWorkspaceInsertAttachmentFace.EAST ? depth - 1 : width - 1;
        int maxV = attachmentFace.isHorizontal() ? height - 1 : depth - 1;
        if (faceUOffset < 0 || faceUOffset > maxU) {
            errors.add("jigsaw face u offset must be between 0 and " + maxU);
        }
        if (faceVOffset < 0 || faceVOffset > maxV) {
            errors.add("jigsaw face v offset must be between 0 and " + maxV);
        }
        return errors;
    }

    public static List<String> validateFits(BoundingBox hostLocalBounds, BlockPos hostSocketLocalPos,
                                            int width, int height, int depth,
                                            MKWorkspaceInsertAttachmentFace attachmentFace,
                                            int faceUOffset, int faceVOffset) {
        ArrayList<String> errors = new ArrayList<>(validateExteriorFaceOffset(width, height, depth, attachmentFace,
                faceUOffset, faceVOffset));
        if (hostLocalBounds == null) {
            errors.add("host authorial piece bounds are required");
        }
        if (hostSocketLocalPos == null) {
            errors.add("host socket position is required");
        }
        if (!errors.isEmpty()) {
            return errors;
        }

        BoundingBox projectedBounds = projectedInsertBounds(hostLocalBounds, hostSocketLocalPos, width, height, depth,
                attachmentFace, faceUOffset, faceVOffset);
        if (projectedBounds.minX() < hostLocalBounds.minX() || projectedBounds.maxX() > hostLocalBounds.maxX()) {
            errors.add("too wide for this socket");
        }
        if (projectedBounds.minZ() < hostLocalBounds.minZ() || projectedBounds.maxZ() > hostLocalBounds.maxZ()) {
            errors.add("too long for this socket");
        }
        if (projectedBounds.minY() < hostLocalBounds.minY() || projectedBounds.maxY() > hostLocalBounds.maxY()) {
            errors.add("too tall for this socket");
        }
        return errors;
    }

    public static List<String> validateOrientedFits(BoundingBox hostLocalBounds, BlockPos hostSocketLocalPos,
                                                    int width, int height, int depth,
                                                    MKWorkspaceInsertAttachmentFace authoredAttachmentFace,
                                                    int faceUOffset, int faceVOffset,
                                                    MKWorkspaceInsertAttachmentFace placementAttachmentFace,
                                                    Direction placementTop) {
        ArrayList<String> errors = new ArrayList<>(validateExteriorFaceOffset(width, height, depth,
                authoredAttachmentFace, faceUOffset, faceVOffset));
        if (hostLocalBounds == null) {
            errors.add("host authorial piece bounds are required");
        }
        if (hostSocketLocalPos == null) {
            errors.add("host socket position is required");
        }
        if (placementAttachmentFace == null) {
            errors.add("placement attachment face is required");
        }
        if (!errors.isEmpty()) {
            return errors;
        }

        BoundingBox projectedBounds = projectedOrientedInsertBounds(hostSocketLocalPos, width, height, depth,
                authoredAttachmentFace, faceUOffset, faceVOffset, placementAttachmentFace, placementTop);
        if (projectedBounds.minX() < hostLocalBounds.minX() || projectedBounds.maxX() > hostLocalBounds.maxX()) {
            errors.add("too wide for this socket");
        }
        if (projectedBounds.minZ() < hostLocalBounds.minZ() || projectedBounds.maxZ() > hostLocalBounds.maxZ()) {
            errors.add("too long for this socket");
        }
        if (projectedBounds.minY() < hostLocalBounds.minY() || projectedBounds.maxY() > hostLocalBounds.maxY()) {
            errors.add("too tall for this socket");
        }
        return errors;
    }

    public static List<String> validateInsertDimensions(int width, int height, int depth) {
        ArrayList<String> errors = new ArrayList<>();
        addDimensionErrors(errors, width, height, depth);
        return errors;
    }

    public static boolean intersects(BoundingBox first, BoundingBox second) {
        return first.minX() <= second.maxX() && first.maxX() >= second.minX() &&
                first.minY() <= second.maxY() && first.maxY() >= second.minY() &&
                first.minZ() <= second.maxZ() && first.maxZ() >= second.minZ();
    }

    private static void addDimensionErrors(List<String> errors, int width, int height, int depth) {
        if (width < 1) {
            errors.add("insert width must be at least 1");
        }
        if (height < 1) {
            errors.add("insert height must be at least 1");
        }
        if (depth < 1) {
            errors.add("insert length must be at least 1");
        }
        if (width > 0 && (width & 1) == 0) {
            errors.add("insert width must be odd");
        }
        if (depth > 0 && (depth & 1) == 0) {
            errors.add("insert length must be odd");
        }
    }

    private static Direction defaultTopForFront(Direction front) {
        if (front == Direction.UP || front == Direction.DOWN) {
            return Direction.NORTH;
        }
        return Direction.UP;
    }

    private static boolean validTopForFront(Direction front, Direction top) {
        return top != null && front.getAxis() != top.getAxis();
    }

    private static BlockPos rotateOffset(BlockPos offset, Direction authoredFront, Direction authoredTop,
                                         Direction placementFront, Direction placementTop) {
        Vec authoredSide = cross(Vec.of(authoredFront), Vec.of(authoredTop));
        Vec placementSide = cross(Vec.of(placementFront), Vec.of(placementTop));
        Vec source = Vec.of(offset);
        int side = dot(source, authoredSide);
        int top = dot(source, Vec.of(authoredTop));
        int front = dot(source, Vec.of(authoredFront));
        Vec projected = placementSide.scale(side)
                .add(Vec.of(placementTop).scale(top))
                .add(Vec.of(placementFront).scale(front));
        return projected.toBlockPos();
    }

    private static int dot(Vec first, Vec second) {
        return first.x * second.x + first.y * second.y + first.z * second.z;
    }

    private static Vec cross(Vec first, Vec second) {
        return new Vec(
                first.y * second.z - first.z * second.y,
                first.z * second.x - first.x * second.z,
                first.x * second.y - first.y * second.x
        );
    }

    private record Vec(int x, int y, int z) {
        private static Vec of(Direction direction) {
            return new Vec(direction.getStepX(), direction.getStepY(), direction.getStepZ());
        }

        private static Vec of(BlockPos pos) {
            return new Vec(pos.getX(), pos.getY(), pos.getZ());
        }

        private Vec scale(int value) {
            return new Vec(x * value, y * value, z * value);
        }

        private Vec add(Vec other) {
            return new Vec(x + other.x, y + other.y, z + other.z);
        }

        private BlockPos toBlockPos() {
            return new BlockPos(x, y, z);
        }
    }

    private static final class MutableBounds {
        private int minX = Integer.MAX_VALUE;
        private int minY = Integer.MAX_VALUE;
        private int minZ = Integer.MAX_VALUE;
        private int maxX = Integer.MIN_VALUE;
        private int maxY = Integer.MIN_VALUE;
        private int maxZ = Integer.MIN_VALUE;

        private void include(BlockPos pos) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        private BoundingBox toBoundingBox() {
            return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
        }
    }
}
