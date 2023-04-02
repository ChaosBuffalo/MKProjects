package com.chaosbuffalo.mknpc.npc.options;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;

public abstract class ResourceLocationOption extends SimpleOption<ResourceLocation> {

    public ResourceLocationOption(ResourceLocation name) {
        super(name);
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("value"), ops.createString(getValue().toString()));
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        setValue(new ResourceLocation(dynamic.get("value").asString(
                String.format("%s.invalid_decode", getName().toString()))));
    }
}
