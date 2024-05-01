package com.chaosbuffalo.mkweapons.items.randomization;

import com.chaosbuffalo.mkcore.utils.CommonCodecs;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

public class RandomizationItemEntry {
    public static final Codec<RandomizationItemEntry> CODEC = RecordCodecBuilder.<RandomizationItemEntry>mapCodec(builder -> {
        return builder.group(
                CommonCodecs.ITEM_STACK.fieldOf("item").forGetter(i -> i.item),
                Codec.DOUBLE.optionalFieldOf("weight", 1.0).forGetter(i -> i.weight)
        ).apply(builder, RandomizationItemEntry::new);
    }).codec();

    public final ItemStack item;
    public final double weight;

    public RandomizationItemEntry(ItemStack item, double weight) {
        this.item = item;
        this.weight = weight;
    }

    public <D> D serialize(DynamicOps<D> ops) {
        return CODEC.encodeStart(ops, this).getOrThrow(false, MKWeapons.LOGGER::error);
    }

    public static <D> RandomizationItemEntry deserialize(Dynamic<D> dynamic) {
        return CODEC.parse(dynamic).getOrThrow(false, MKWeapons.LOGGER::error);
    }
}