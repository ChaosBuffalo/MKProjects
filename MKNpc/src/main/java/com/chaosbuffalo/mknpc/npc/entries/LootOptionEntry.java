package com.chaosbuffalo.mknpc.npc.entries;

import com.chaosbuffalo.mkweapons.items.randomization.LootTierManager;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlotManager;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;

public class LootOptionEntry {

    public ResourceLocation lootSlotName;
    public ResourceLocation lootTierName;
    public double weight;

    public LootOptionEntry(ResourceLocation lootSlotName, ResourceLocation lootTierName, double weight){
        this.lootSlotName = lootSlotName;
        this.lootTierName = lootTierName;
        this.weight = weight;
    }

    public LootOptionEntry(){
        this(LootSlotManager.INVALID_LOOT_SLOT, LootTierManager.INVALID_LOOT_TIER, 1.0);
    }

    public <D> void deserialize(Dynamic<D> dynamic){
        lootSlotName = dynamic.get("lootSlotName").asString().result().map(ResourceLocation::new)
                .orElse(LootSlotManager.INVALID_LOOT_SLOT);
        lootTierName = dynamic.get("lootSlotTier").asString().result().map(ResourceLocation::new)
                .orElse(LootTierManager.INVALID_LOOT_TIER);
        weight = dynamic.get("weight").asDouble(1.0);
    }

    public boolean isValidConfiguration(){
        return !lootSlotName.equals(LootSlotManager.INVALID_LOOT_SLOT) &&
                !lootTierName.equals(LootTierManager.INVALID_LOOT_TIER);
    }


    public <D> D serialize(DynamicOps<D> ops) {
        ImmutableMap.Builder<D, D> builder = ImmutableMap.builder();
        builder.put(ops.createString("lootSlotName"), ops.createString(lootSlotName.toString()));
        builder.put(ops.createString("lootSlotTier"), ops.createString(lootTierName.toString()));
        builder.put(ops.createString("weight"), ops.createDouble(weight));
        return ops.createMap(builder.build());
    }

}
