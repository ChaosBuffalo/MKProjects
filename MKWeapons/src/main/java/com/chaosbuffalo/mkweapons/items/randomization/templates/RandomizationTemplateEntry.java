package com.chaosbuffalo.mkweapons.items.randomization.templates;

import com.chaosbuffalo.mkcore.serialization.IDynamicMapSerializer;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

public class RandomizationTemplateEntry implements IDynamicMapSerializer {
    public RandomizationTemplate template;
    public double weight;

    public RandomizationTemplateEntry(RandomizationTemplate template, double weight){
        this.template = template;
        this.weight = weight;
    }

    public RandomizationTemplateEntry() {
        this.template = null;
        this.weight = 1.0;
    }

    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {
        weight = dynamic.get("weight").asDouble(1.0);
        template = dynamic.get("template").map(RandomizationTemplate::deserializeTemplate).getOrThrow(false,
                x -> MKWeapons.LOGGER.error("Failed to deserialize RandomizedTemplate"));
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> dynamicOps, ImmutableMap.Builder<D, D> builder) {
        builder.put(dynamicOps.createString("template"), template.serialize(dynamicOps));
        builder.put(dynamicOps.createString("weight"), dynamicOps.createDouble(weight));
    }
}
