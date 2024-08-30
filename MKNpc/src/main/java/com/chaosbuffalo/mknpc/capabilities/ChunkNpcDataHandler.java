package com.chaosbuffalo.mknpc.capabilities;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.chunk.LevelChunk;

public class ChunkNpcDataHandler implements IChunkNpcData {
    private final LevelChunk chunk;

    public ChunkNpcDataHandler(LevelChunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {

    }
}
