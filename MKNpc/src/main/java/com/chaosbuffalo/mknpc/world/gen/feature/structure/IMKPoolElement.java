package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import com.chaosbuffalo.mknpc.world.gen.StructureUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.UUID;

public interface IMKPoolElement {


    boolean mkPlace(StructureTemplateManager pStructureTemplateManager, WorldGenLevel pLevel, StructureManager pStructureManager,
                    ChunkGenerator pGenerator, BlockPos piecePosition, BlockPos firstPieceBottomCenter, Rotation pRotation,
                    BoundingBox pBox, RandomSource pRandom, boolean pKeepJigsaws, ResourceLocation name, UUID uuid);

    default void mkHandleDataMarker(LevelAccessor worldIn, StructureTemplate.StructureBlockInfo blockInfo,
                                    BlockPos structureStartPos, Rotation rotationIn,
                                    RandomSource rand, BoundingBox boundingBox, ResourceLocation structureName, UUID instanceId) {
        StructureUtils.handleMKDataMarker(blockInfo.nbt.getString("metadata"), blockInfo.pos, worldIn, rand, boundingBox,
                structureName, instanceId);
    }
}
