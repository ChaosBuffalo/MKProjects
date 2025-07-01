package com.chaosbuffalo.mknpc.npc.entries;

import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record LootOptionEntry(ResourceLocation lootTierName, LootSlot lootSlot, double weight) {
    public static final Codec<LootOptionEntry> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("loot_tier").forGetter(i -> i.lootTierName),
            LootSlot.CODEC.fieldOf("loot_slot").forGetter(i -> i.lootSlot),
            Codec.DOUBLE.optionalFieldOf("weight", 1.0).forGetter(i -> i.weight)
    ).apply(builder, LootOptionEntry::new));

}
