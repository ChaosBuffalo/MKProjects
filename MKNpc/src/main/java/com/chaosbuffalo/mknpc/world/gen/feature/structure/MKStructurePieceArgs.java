package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

import java.util.List;
import java.util.UUID;

public class MKStructurePieceArgs {
    public final StructureFeature<?> structure;
    public final StructureManager templateManager;
    public final BlockPos blockPos;
    public final Rotation rotation;
    public final WorldgenRandom random;
    public final UUID structureId;
    public final List<StructurePiece> componentsOut;
    public final ChunkGenerator generator;

    public MKStructurePieceArgs(ChunkGenerator generator, StructureFeature<?> structure, StructureManager templateManager,
                                BlockPos blockPos, Rotation rotation, WorldgenRandom random,
                                UUID structureId, List<StructurePiece> componentsOut){
        this.generator = generator;
        this.structure = structure;
        this.templateManager = templateManager;
        this.blockPos = blockPos;
        this.rotation = rotation;
        this.random = random;
        this.structureId = structureId;
        this.componentsOut = componentsOut;
    }
}
