package com.chaosbuffalo.mkcore.serialization;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

public interface IDynamicMapSerializer {
    default <D> D serialize(DynamicOps<D> ops) {
        ImmutableMap.Builder<D, D> builder = ImmutableMap.builder();
        writeAdditionalData(ops, builder);
        return ops.createMap(builder.build());
    }

    <D> void deserialize(Dynamic<D> dynamic);

    <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder);
}
