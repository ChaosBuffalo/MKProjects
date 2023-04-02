package com.chaosbuffalo.mkweapons.items.randomization;

import com.chaosbuffalo.mkcore.utils.SerializationUtils;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.item.ItemStack;

public class RandomizationItemEntry  {
    public ItemStack item;
    public double weight;

    public RandomizationItemEntry(ItemStack item, double weight){
        this.item = item;
        this.weight = weight;
    }

    public RandomizationItemEntry(){
        this(ItemStack.EMPTY, 1.0);
    }

    public <D> D serialize(DynamicOps<D> ops){
        return ops.createMap(ImmutableMap.of(
                ops.createString("itemStack"), SerializationUtils.serializeItemStack(ops, this.item),
                ops.createString("weight"), ops.createDouble(weight)
        ));
    }

    public <D> void deserialize(Dynamic<D> dynamic) {
        this.item = dynamic.get("itemStack").map(SerializationUtils::deserializeItemStack).result().orElse(ItemStack.EMPTY);
        this.weight = dynamic.get("weight").asDouble(1.0);
    }
}