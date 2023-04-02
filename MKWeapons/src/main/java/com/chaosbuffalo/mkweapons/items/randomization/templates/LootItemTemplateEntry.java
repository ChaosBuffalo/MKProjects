package com.chaosbuffalo.mkweapons.items.randomization.templates;

import com.chaosbuffalo.mkcore.serialization.IDynamicMapSerializer;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.randomization.LootItemTemplate;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

public class LootItemTemplateEntry implements IDynamicMapSerializer {
    public LootItemTemplate template;
    public double weight;

    public LootItemTemplateEntry() {
        this(null, 1.0);
    }

    public LootItemTemplateEntry(LootItemTemplate template, double weight) {
        this.weight = weight;
        this.template = template;
    }

    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {
        weight = dynamic.get("weight").asDouble(1.0);
        template = dynamic.get("template").map(x -> {
            LootItemTemplate template = new LootItemTemplate();
            template.deserialize(x);
            return template;
        }).getOrThrow(false, x -> MKWeapons.LOGGER.error("Failed to deserialize LootItemTemplateEntry {}", x));
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> dynamicOps, ImmutableMap.Builder<D, D> builder) {
        builder.put(dynamicOps.createString("weight"), dynamicOps.createDouble(weight));
        builder.put(dynamicOps.createString("template"), template.serialize(dynamicOps));
    }
}
