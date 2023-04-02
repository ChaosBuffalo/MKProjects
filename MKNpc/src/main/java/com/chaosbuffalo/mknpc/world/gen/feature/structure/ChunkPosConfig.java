package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class ChunkPosConfig implements FeatureConfiguration {
    public final int xChunk;
    public final int zChunk;

    public static final Codec<ChunkPosConfig> CODEC = RecordCodecBuilder.create((builder) -> builder.group(
            Codec.intRange(Integer.MIN_VALUE, Integer.MAX_VALUE).fieldOf("x_chunk")
                    .forGetter((config) -> config.xChunk),
            Codec.intRange(Integer.MIN_VALUE, Integer.MAX_VALUE).fieldOf("z_chunk")
                    .forGetter((config) -> config.zChunk))
            .apply(builder, ChunkPosConfig::new));

    public ChunkPosConfig(int xChunk, int zChunk){
        this.xChunk = xChunk;
        this.zChunk = zChunk;
    }
}
