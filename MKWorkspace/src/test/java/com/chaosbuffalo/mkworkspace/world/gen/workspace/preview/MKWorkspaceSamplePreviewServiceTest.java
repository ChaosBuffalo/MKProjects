package com.chaosbuffalo.mkworkspace.world.gen.workspace.preview;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MKWorkspaceSamplePreviewServiceTest {
    @Test
    void anchorBiasMovesSmallPreviewTowardPositiveAnchorSide() {
        BoundingBox footprint = new BoundingBox(-128, 0, -128, 128, 40, 128);
        BoundingBox sample = new BoundingBox(-10, 0, -10, 10, 20, 10);

        BlockPos delta = MKWorkspaceSamplePreviewPlacement.anchorBiasDelta(sample, footprint,
                new BlockPos(200, 0, 200));

        assertEquals(new BlockPos(118, 0, 118), delta);
    }

    @Test
    void anchorBiasMovesSmallPreviewTowardNegativeAnchorSide() {
        BoundingBox footprint = new BoundingBox(-128, 0, -128, 128, 40, 128);
        BoundingBox sample = new BoundingBox(-10, 0, -10, 10, 20, 10);

        BlockPos delta = MKWorkspaceSamplePreviewPlacement.anchorBiasDelta(sample, footprint,
                new BlockPos(-200, 0, -200));

        assertEquals(new BlockPos(-118, 0, -118), delta);
    }

    @Test
    void anchorBiasDoesNotPullOversizedPreviewPastReservedFootprint() {
        BoundingBox footprint = new BoundingBox(-128, 0, -128, 128, 40, 128);
        BoundingBox sample = new BoundingBox(-160, 0, -160, 160, 20, 160);

        BlockPos delta = MKWorkspaceSamplePreviewPlacement.anchorBiasDelta(sample, footprint,
                new BlockPos(200, 0, 200));

        assertEquals(BlockPos.ZERO, delta);
    }
}
