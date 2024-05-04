package com.chaosbuffalo.mkweapons.items.randomization.templates;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class RandomizationTemplateEntry {
    public static final Codec<RandomizationTemplateEntry> CODEC = RecordCodecBuilder.<RandomizationTemplateEntry>mapCodec(builder -> {
        return builder.group(
                RandomizationTemplate.CODEC.fieldOf("template").forGetter(i -> i.template),
                Codec.DOUBLE.optionalFieldOf("weight", 1.0).forGetter(i -> i.weight)
        ).apply(builder, RandomizationTemplateEntry::new);
    }).codec();

    public final RandomizationTemplate template;
    public final double weight;

    public RandomizationTemplateEntry(RandomizationTemplate template, double weight) {
        this.template = template;
        this.weight = weight;
    }

    public <D> D serialize(DynamicOps<D> ops) {
        return CODEC.encodeStart(ops, this).getOrThrow(false, MKWeapons.LOGGER::error);
    }

    public static <D> RandomizationTemplateEntry deserialize(Dynamic<D> dynamic) {
        return CODEC.parse(dynamic).getOrThrow(false, MKWeapons.LOGGER::error);
    }
}
