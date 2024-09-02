package com.chaosbuffalo.mkultra.world.gen.feature.structure;

import com.chaosbuffalo.mkultra.init.MKUWorldGen;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

import java.util.Optional;

public class StaticPlacement extends StructurePlacement {
    public static final MapCodec<StaticPlacement> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.INT.fieldOf("chunkX").forGetter(s -> s.chunkPos.x),
            Codec.INT.fieldOf("chunkZ").forGetter(s -> s.chunkPos.z)
    ).apply(builder, StaticPlacement::new));

    private final BlockPos blockPos;
    private final ChunkPos chunkPos;

    public StaticPlacement(int chunkX, int chunkZ) {
        super(Vec3i.ZERO, FrequencyReductionMethod.DEFAULT, 1f, 0, Optional.empty());
        this.chunkPos = new ChunkPos(chunkX, chunkZ);
        blockPos = new BlockPos(SectionPos.sectionToBlockCoord(chunkX), 0, SectionPos.sectionToBlockCoord(chunkZ));
    }

    @Override
    public StructurePlacementType<?> type() {
        return MKUWorldGen.STATIC_PLACEMENT.get();
    }

    @Override
    public BlockPos getLocatePos(ChunkPos chunkPos) {
        return blockPos;
    }

    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState pStructureState, int pX, int pZ) {
        return pX == chunkPos.x && pZ == chunkPos.z;
    }
}
