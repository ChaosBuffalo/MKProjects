package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import com.chaosbuffalo.mknpc.world.gen.StructureUtils;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

import java.util.Random;

public interface IMKJigsawPiece {


    boolean mkPlace(StructureManager templateManager, WorldGenLevel seedReader, StructureFeatureManager structureManager,
                    ChunkGenerator chunkGenerator, BlockPos structurePos, BlockPos blockPos, Rotation rot,
                    BoundingBox boundingBox, Random rand, boolean bool, MKPoolElementPiece parent);

    default void mkHandleDataMarker(LevelAccessor worldIn, StructureTemplate.StructureBlockInfo blockInfo, BlockPos pos, Rotation rotationIn,
                                   Random rand, BoundingBox boundingBox, MKPoolElementPiece parent) {
        StructureUtils.handleMKDataMarker(blockInfo.nbt.getString("metadata"), pos, worldIn, rand, boundingBox,
                parent.getStructureName(), parent.getInstanceId());
    }
}
