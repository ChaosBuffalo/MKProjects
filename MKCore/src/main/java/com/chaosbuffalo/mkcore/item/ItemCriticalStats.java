package com.chaosbuffalo.mkcore.item;

import com.chaosbuffalo.mkcore.init.CoreDataMaps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record ItemCriticalStats(float critChance, float critMultiplier) {
    private static final float DEFAULT_CRIT_RATE = .0f;
    private static final float DEFAULT_CRIT_MULTIPLIER = 1.5f;
    private static final ItemCriticalStats DEFAULT_CRIT = new ItemCriticalStats(DEFAULT_CRIT_RATE, DEFAULT_CRIT_MULTIPLIER);

    public static final Codec<ItemCriticalStats> CODEC = RecordCodecBuilder.create(b -> b.group(
            Codec.FLOAT.fieldOf("crit_chance").forGetter(ItemCriticalStats::critChance),
            Codec.FLOAT.fieldOf("crit_multiplier").forGetter(ItemCriticalStats::critMultiplier)
    ).apply(b, ItemCriticalStats::new));

    @Nullable
    public static ItemCriticalStats get(ItemStack itemStack) {
        return itemStack.getItemHolder().getData(CoreDataMaps.ITEM_CRITICAL_STATS);
    }

    @Nonnull
    public static ItemCriticalStats getOrDefault(ItemStack itemStack) {
        var stats = get(itemStack);
        return stats == null ? DEFAULT_CRIT : stats;
    }
}
