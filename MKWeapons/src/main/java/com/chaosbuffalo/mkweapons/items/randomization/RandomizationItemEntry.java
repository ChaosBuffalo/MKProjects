package com.chaosbuffalo.mkweapons.items.randomization;

import com.chaosbuffalo.mkcore.utils.CommonCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

public record RandomizationItemEntry(ItemStack item, double weight) {
    public static final Codec<RandomizationItemEntry> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            CommonCodecs.ITEM_STACK.fieldOf("item").forGetter(RandomizationItemEntry::item),
            Codec.DOUBLE.optionalFieldOf("weight", 1.0).forGetter(RandomizationItemEntry::weight)
    ).apply(builder, RandomizationItemEntry::new));

}