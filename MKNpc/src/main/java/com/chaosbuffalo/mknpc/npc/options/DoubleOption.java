package com.chaosbuffalo.mknpc.npc.options;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;

public abstract class DoubleOption extends SimpleOption<Double> {

    public DoubleOption(ResourceLocation name) {
        super(name);
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("value"), ops.createDouble(getValue()));
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        setValue(dynamic.get("value").asDouble(1.0));
    }
}
