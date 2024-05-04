package com.chaosbuffalo.mkweapons.items.randomization.templates;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.randomization.LootItemTemplate;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class LootItemTemplateEntry {
    public static final Codec<LootItemTemplateEntry> CODEC = RecordCodecBuilder.<LootItemTemplateEntry>mapCodec(builder -> {
        return builder.group(
                LootItemTemplate.CODEC.fieldOf("template").forGetter(i -> i.template),
                Codec.DOUBLE.fieldOf("weight").forGetter(i -> i.weight)
        ).apply(builder, LootItemTemplateEntry::new);
    }).codec();

    public final LootItemTemplate template;
    public final double weight;

    public LootItemTemplateEntry(LootItemTemplate template, double weight) {
        this.weight = weight;
        this.template = template;
    }

    public <D> D serialize(DynamicOps<D> ops) {
        return CODEC.encodeStart(ops, this).getOrThrow(false, MKWeapons.LOGGER::error);
    }

    public static <D> LootItemTemplateEntry deserialize(Dynamic<D> dynamic) {
        return CODEC.parse(dynamic).getOrThrow(false, MKWeapons.LOGGER::error);
    }
}
