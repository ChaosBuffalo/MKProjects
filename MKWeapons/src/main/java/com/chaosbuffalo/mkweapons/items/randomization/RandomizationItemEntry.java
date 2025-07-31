package com.chaosbuffalo.mkweapons.items.randomization;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

public record RandomizationItemEntry(ItemStack item, double weight) {
    public static final Codec<RandomizationItemEntry> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ItemStack.CODEC.fieldOf("item").forGetter(RandomizationItemEntry::item),
            Codec.DOUBLE.optionalFieldOf("weight", 1.0).forGetter(RandomizationItemEntry::weight)
    ).apply(builder, RandomizationItemEntry::new));

}