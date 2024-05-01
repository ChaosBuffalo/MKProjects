package com.chaosbuffalo.mknpc.npc.entries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public class LootOptionEntry {
    public static final Codec<LootOptionEntry> CODEC = RecordCodecBuilder.<LootOptionEntry>mapCodec(builder -> {
        return builder.group(
                ResourceLocation.CODEC.fieldOf("lootSlotName").forGetter(i -> i.lootSlotName),
                ResourceLocation.CODEC.fieldOf("lootSlotTier").forGetter(i -> i.lootTierName),
                Codec.DOUBLE.optionalFieldOf("weight", 1.0).forGetter(i -> i.weight)
        ).apply(builder, LootOptionEntry::new);
    }).codec();

    public final ResourceLocation lootSlotName;
    public final ResourceLocation lootTierName;
    public final double weight;

    public LootOptionEntry(ResourceLocation lootSlotName, ResourceLocation lootTierName, double weight) {
        this.lootSlotName = lootSlotName;
        this.lootTierName = lootTierName;
        this.weight = weight;
    }
}
