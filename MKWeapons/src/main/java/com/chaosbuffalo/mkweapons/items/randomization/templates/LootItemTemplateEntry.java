package com.chaosbuffalo.mkweapons.items.randomization.templates;

import com.chaosbuffalo.mkweapons.items.randomization.LootItemTemplate;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record LootItemTemplateEntry(LootItemTemplate template, double weight) {
    public static final Codec<LootItemTemplateEntry> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            LootItemTemplate.CODEC.fieldOf("template").forGetter(LootItemTemplateEntry::template),
            Codec.DOUBLE.fieldOf("weight").forGetter(LootItemTemplateEntry::weight)
    ).apply(builder, LootItemTemplateEntry::new));

}
