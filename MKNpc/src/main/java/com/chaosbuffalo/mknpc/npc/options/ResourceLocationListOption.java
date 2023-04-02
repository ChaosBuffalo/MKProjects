package com.chaosbuffalo.mknpc.npc.options;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public abstract class ResourceLocationListOption extends SimpleOption<List<ResourceLocation>> {
    public ResourceLocationListOption(ResourceLocation name) {
        super(name);
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("value"), ops.createList(getValue().stream().map(
                x -> ops.createString(x.toString()))));
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        List<ResourceLocation> val = new ArrayList<>();
        List<DataResult<String>> decoded = dynamic.get("value").asList(Dynamic::asString);
        for (DataResult<String> data : decoded) {
            data.result().ifPresent(s -> val.add(new ResourceLocation(s)));
        }
        setValue(val);
    }
}