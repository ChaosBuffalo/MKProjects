package com.chaosbuffalo.mkultra.world.gen.feature.structure;

import com.chaosbuffalo.mkultra.init.MKUWorldGen;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

import javax.annotation.Nullable;
import java.util.Optional;

public class StaticPlacement extends StructurePlacement {
    public static final Codec<StaticPlacement> CODEC = RecordCodecBuilder.<StaticPlacement>mapCodec(builder ->
            placementCodec(builder).and(builder.group(
                            Codec.INT.fieldOf("chunkX").forGetter(s -> s.chunkPos.x),
                            Codec.INT.fieldOf("chunkZ").forGetter(s -> s.chunkPos.z)
                    ))
                    .apply(builder, StaticPlacement::new)).codec();

    private final ChunkPos chunkPos;

    public StaticPlacement(Vec3i pLocateOffset, StructurePlacement.FrequencyReductionMethod pFrequencyReductionMethod, float pFrequency, int pSalt, Optional<StructurePlacement.ExclusionZone> pExclusionZone, int chunkX, int chunkZ) {
        super(pLocateOffset, pFrequencyReductionMethod, pFrequency, pSalt, pExclusionZone);
        this.chunkPos = new ChunkPos(chunkX, chunkZ);
    }

    public StaticPlacement(int chunkX, int chunkZ) {
        this(chunkX, chunkZ, null);
    }

    public StaticPlacement(int chunkX, int chunkZ, @Nullable ExclusionZone zone) {
        super(Vec3i.ZERO, FrequencyReductionMethod.DEFAULT, 1f, 0, Optional.ofNullable(zone));
        this.chunkPos = new ChunkPos(chunkX, chunkZ);
    }

    @Override
    public StructurePlacementType<?> type() {
        return MKUWorldGen.STATIC_PLACEMENT.get();
    }

    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState pStructureState, int pX, int pZ) {
        return pX == chunkPos.x && pZ == chunkPos.z;
    }
}
