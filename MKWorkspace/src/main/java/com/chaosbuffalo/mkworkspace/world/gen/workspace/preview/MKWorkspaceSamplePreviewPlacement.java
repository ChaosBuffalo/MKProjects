package com.chaosbuffalo.mkworkspace.world.gen.workspace.preview;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

final class MKWorkspaceSamplePreviewPlacement {
    private MKWorkspaceSamplePreviewPlacement() {
    }

    static BlockPos anchorBiasDelta(BoundingBox pieceBounds, BoundingBox previewFootprintBounds, BlockPos anchor) {
        int previewCenterX = (previewFootprintBounds.minX() + previewFootprintBounds.maxX()) / 2;
        int previewCenterZ = (previewFootprintBounds.minZ() + previewFootprintBounds.maxZ()) / 2;
        return new BlockPos(
                axisAnchorBias(pieceBounds.minX(), pieceBounds.maxX(),
                        previewFootprintBounds.minX(), previewFootprintBounds.maxX(), anchor.getX(), previewCenterX),
                0,
                axisAnchorBias(pieceBounds.minZ(), pieceBounds.maxZ(),
                        previewFootprintBounds.minZ(), previewFootprintBounds.maxZ(), anchor.getZ(), previewCenterZ)
        );
    }

    private static int axisAnchorBias(int pieceMin, int pieceMax, int footprintMin, int footprintMax,
                                      int anchorCoord, int footprintCenter) {
        if (anchorCoord > footprintCenter) {
            return Math.max(0, footprintMax - pieceMax);
        }
        if (anchorCoord < footprintCenter) {
            return Math.min(0, footprintMin - pieceMin);
        }
        return 0;
    }
}
