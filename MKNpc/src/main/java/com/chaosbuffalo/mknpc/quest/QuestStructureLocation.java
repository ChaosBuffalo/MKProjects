package com.chaosbuffalo.mknpc.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public class QuestStructureLocation {
    public static final Codec<QuestStructureLocation> CODEC = RecordCodecBuilder.<QuestStructureLocation>mapCodec(builder -> {
        return builder.group(
                ResourceLocation.CODEC.fieldOf("structureId").forGetter(i -> i.structId),
                Codec.INT.fieldOf("index").forGetter(i -> i.index)
        ).apply(builder, QuestStructureLocation::new);
    }).codec();

    private final ResourceLocation structId;
    private final int index;

    public QuestStructureLocation(ResourceLocation structureId, int index) {
        this.structId = structureId;
        this.index = index;
    }

    public ResourceLocation getStructureId() {
        return structId;
    }

    public int getIndex() {
        return index;
    }
}
