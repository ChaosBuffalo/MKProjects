package com.chaosbuffalo.mknpc.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record QuestStructureLocation(ResourceLocation structId, String name) {
    public static final Codec<QuestStructureLocation> CODEC = RecordCodecBuilder.<QuestStructureLocation>mapCodec(builder -> {
        return builder.group(
                ResourceLocation.CODEC.fieldOf("structureId").forGetter(i -> i.structId),
                Codec.STRING.fieldOf("name").forGetter(i -> i.name)
        ).apply(builder, QuestStructureLocation::new);
    }).codec();

    public ResourceLocation getStructureId() {
        return structId;
    }


    public String getName() {
        return name;
    }
}
