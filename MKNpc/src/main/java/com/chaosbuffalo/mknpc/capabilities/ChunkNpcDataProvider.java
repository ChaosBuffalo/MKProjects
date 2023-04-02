package com.chaosbuffalo.mknpc.capabilities;

import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;

public class ChunkNpcDataProvider extends NpcCapabilities.Provider<LevelChunk, IChunkNpcData> {

    public ChunkNpcDataProvider(LevelChunk chunk) {
        super(chunk);
    }

    @Override
    IChunkNpcData makeData(LevelChunk attached) {
        return new ChunkNpcDataHandler(attached);
    }

    @Override
    Capability<IChunkNpcData> getCapability() {
        return NpcCapabilities.CHUNK_NPC_DATA_CAPABILITY;
    }
}

