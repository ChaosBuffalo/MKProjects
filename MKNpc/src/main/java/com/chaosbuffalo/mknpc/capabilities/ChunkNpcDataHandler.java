package com.chaosbuffalo.mknpc.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.chunk.LevelChunk;

public class ChunkNpcDataHandler implements IChunkNpcData {
    private final LevelChunk chunk;

    public ChunkNpcDataHandler(LevelChunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public CompoundTag serializeNBT() {
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {

    }
}
