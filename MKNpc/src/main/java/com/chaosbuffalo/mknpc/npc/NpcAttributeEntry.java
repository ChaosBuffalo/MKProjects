package com.chaosbuffalo.mknpc.npc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;

public class NpcAttributeEntry {
    public static final Codec<NpcAttributeEntry> CODEC = RecordCodecBuilder.<NpcAttributeEntry>create(builder -> builder.group(
            Attribute.CODEC.fieldOf("attribute").forGetter(NpcAttributeEntry::getAttribute),
            Codec.DOUBLE.fieldOf("base_value").forGetter(NpcAttributeEntry::getBaseValue)
    ).apply(builder, NpcAttributeEntry::new));

    private final Holder<Attribute> attribute;
    private final double baseValue;

    public NpcAttributeEntry(Holder<Attribute> attribute, double baseValue) {
        this.attribute = attribute;
        this.baseValue = baseValue;
    }

    public Holder<Attribute> getAttribute() {
        return attribute;
    }

    public double getBaseValue() {
        return baseValue;
    }
}
