package com.chaosbuffalo.mknpc.npc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.registries.ForgeRegistries;

public class NpcAttributeEntry {
    public static final Codec<NpcAttributeEntry> CODEC = RecordCodecBuilder.<NpcAttributeEntry>mapCodec(builder -> {
        return builder.group(
                ForgeRegistries.ATTRIBUTES.getCodec().fieldOf("attribute").forGetter(NpcAttributeEntry::getAttribute),
                Codec.DOUBLE.fieldOf("value").forGetter(NpcAttributeEntry::getValue)
        ).apply(builder, NpcAttributeEntry::new);
    }).codec();

    private final Attribute attribute;
    private final double value;

    public NpcAttributeEntry(Attribute attribute, double value) {
        this.attribute = attribute;
        this.value = value;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public double getValue() {
        return value;
    }
}
