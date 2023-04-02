package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import com.chaosbuffalo.mknpc.world.gen.StructureUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.LevelAccessor;

import java.util.Random;
import java.util.UUID;

public interface IMKStructurePiece {
    UUID getInstanceId();

    ResourceLocation getStructureName();

    default void handleMKDataMarker(String function, BlockPos pos, LevelAccessor worldIn,
                                    Random rand, BoundingBox sbb) {
        StructureUtils.handleMKDataMarker(function, pos, worldIn, rand, sbb, getStructureName(), getInstanceId());
    }
}
