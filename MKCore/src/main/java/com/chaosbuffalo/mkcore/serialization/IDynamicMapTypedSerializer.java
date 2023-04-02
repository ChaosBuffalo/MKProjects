package com.chaosbuffalo.mkcore.serialization;

import com.chaosbuffalo.mkcore.MKCore;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public interface IDynamicMapTypedSerializer {

    default <D> D serialize(DynamicOps<D> ops) {
        ImmutableMap.Builder<D, D> builder = ImmutableMap.builder();
        builder.put(ops.createString(getTypeEntryName()), ops.createString(getTypeName().toString()));
        writeAdditionalData(ops, builder);
        return ops.createMap(builder.build());
    }

    default <D> void deserialize(Dynamic<D> dynamic) {
        readAdditionalData(dynamic);
    }

    default <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
    }

    default <D> void readAdditionalData(Dynamic<D> dynamic) {
    }

    ResourceLocation getTypeName();

    static <D> Optional<ResourceLocation> getType(Dynamic<D> dynamic, String typeEntryName) {
        return dynamic.get(typeEntryName).asString().resultOrPartial(MKCore.LOGGER::error).map(ResourceLocation::new);
    }

    String getTypeEntryName();
}
