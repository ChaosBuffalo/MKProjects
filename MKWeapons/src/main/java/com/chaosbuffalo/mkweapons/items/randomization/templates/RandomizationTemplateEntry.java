package com.chaosbuffalo.mkweapons.items.randomization.templates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record RandomizationTemplateEntry(RandomizationTemplate template, double weight) {
    public static final Codec<RandomizationTemplateEntry> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            RandomizationTemplate.CODEC.fieldOf("template").forGetter(i -> i.template),
            Codec.DOUBLE.optionalFieldOf("weight", 1.0).forGetter(i -> i.weight)
    ).apply(builder, RandomizationTemplateEntry::new));

}
