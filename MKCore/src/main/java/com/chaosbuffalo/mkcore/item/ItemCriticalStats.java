package com.chaosbuffalo.mkcore.item;

import com.chaosbuffalo.mkcore.init.CoreDataMaps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public record ItemCriticalStats(float critChance, float critMultiplier) {

    public static final Codec<ItemCriticalStats> CODEC = RecordCodecBuilder.create(b -> b.group(
            Codec.FLOAT.fieldOf("crit_chance").forGetter(ItemCriticalStats::critChance),
            Codec.FLOAT.fieldOf("crit_multiplier").forGetter(ItemCriticalStats::critMultiplier)
    ).apply(b, ItemCriticalStats::new));

    @Nullable
    public static ItemCriticalStats get(ItemStack itemStack) {
        return itemStack.getItemHolder().getData(CoreDataMaps.ITEM_CRITICAL_STATS);
    }
}
