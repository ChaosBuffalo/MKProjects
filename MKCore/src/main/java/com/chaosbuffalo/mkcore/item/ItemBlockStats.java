package com.chaosbuffalo.mkcore.item;

import com.chaosbuffalo.mkcore.init.CoreDataMaps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public record ItemBlockStats(float maxPoise, float blockEfficiency) {

    public static final Codec<ItemBlockStats> CODEC = RecordCodecBuilder.create(b -> b.group(
            Codec.FLOAT.fieldOf("max_poise").forGetter(ItemBlockStats::maxPoise),
            Codec.FLOAT.fieldOf("block_efficiency").forGetter(ItemBlockStats::blockEfficiency)
    ).apply(b, ItemBlockStats::new));

    @Nullable
    public static ItemBlockStats get(ItemStack itemStack) {
        return itemStack.getItemHolder().getData(CoreDataMaps.ITEM_BLOCK_STATS);
    }
}
