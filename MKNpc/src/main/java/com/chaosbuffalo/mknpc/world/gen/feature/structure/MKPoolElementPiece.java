package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import com.chaosbuffalo.mknpc.init.MKNpcWorldGen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

import java.util.Random;
import java.util.UUID;

public class MKPoolElementPiece extends PoolElementStructurePiece implements IMKStructurePiece {
    private final ResourceLocation structureName;
    private UUID instanceId;

    public MKPoolElementPiece(StructureManager templateManager, StructurePoolElement jigsawPiece, BlockPos blockPos,
                              int groundLevelDelta, Rotation rotation, BoundingBox boundingBox,
                              ResourceLocation structureName) {
        super(templateManager, jigsawPiece, blockPos, groundLevelDelta, rotation, boundingBox);
        this.type = MKNpcWorldGen.MK_JIGSAW_PIECE_TYPE.get();
        this.structureName = structureName;
        this.instanceId = UUID.randomUUID();

    }

    public void setInstanceId(UUID instanceId) {
        this.instanceId = instanceId;
    }

    public MKPoolElementPiece(StructurePieceSerializationContext context, CompoundTag compoundNBT) {
        super(context, compoundNBT);
        this.type = MKNpcWorldGen.MK_JIGSAW_PIECE_TYPE.get();
        structureName = new ResourceLocation(compoundNBT.getString("structureName"));
        instanceId = compoundNBT.getUUID("instanceId");

    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
        tag.putUUID("instanceId", instanceId);
        tag.putString("structureName", structureName.toString());
    }



    @Override
    public void place(WorldGenLevel seedReader, StructureFeatureManager structureManager,
                         ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox,
                         BlockPos blockPos, boolean bool) {

        if (element instanceof IMKJigsawPiece){
           ((IMKJigsawPiece) element).mkPlace(this.structureManager, seedReader, structureManager, chunkGenerator,
                    this.position, blockPos, this.rotation, boundingBox, random, bool, this);
        } else {
            super.place(seedReader, structureManager, chunkGenerator, random,
                    boundingBox, blockPos, bool);
        }
    }

    @Override
    public ResourceLocation getStructureName() {
        return structureName;
    }

    @Override
    public UUID getInstanceId() {
        return instanceId;
    }
}
