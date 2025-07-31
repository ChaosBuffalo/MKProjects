package com.chaosbuffalo.mknpc.npc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;

public record NpcAttributeEntry(Holder<Attribute> attribute, double baseValue) {
    public static final Codec<NpcAttributeEntry> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Attribute.CODEC.fieldOf("attribute").forGetter(NpcAttributeEntry::attribute),
            Codec.DOUBLE.fieldOf("base_value").forGetter(NpcAttributeEntry::baseValue)
    ).apply(builder, NpcAttributeEntry::new));

}
