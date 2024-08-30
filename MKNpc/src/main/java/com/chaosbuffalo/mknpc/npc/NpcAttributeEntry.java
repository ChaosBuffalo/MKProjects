package com.chaosbuffalo.mknpc.npc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;

public class NpcAttributeEntry {
    public static final Codec<NpcAttributeEntry> CODEC = RecordCodecBuilder.<NpcAttributeEntry>mapCodec(builder -> {
        return builder.group(
                Attribute.CODEC.fieldOf("attribute").forGetter(NpcAttributeEntry::getAttribute),
                Codec.DOUBLE.fieldOf("value").forGetter(NpcAttributeEntry::getValue)
        ).apply(builder, NpcAttributeEntry::new);
    }).codec();

    private final Holder<Attribute> attribute;
    private final double value;

    public NpcAttributeEntry(Holder<Attribute> attribute, double value) {
        this.attribute = attribute;
        this.value = value;
    }

    public Holder<Attribute> getAttribute() {
        return attribute;
    }

    public double getValue() {
        return value;
    }
}
