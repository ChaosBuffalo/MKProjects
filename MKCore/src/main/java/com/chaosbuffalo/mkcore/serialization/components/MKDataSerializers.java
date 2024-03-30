package com.chaosbuffalo.mkcore.serialization.components;

import com.chaosbuffalo.mkcore.MKCore;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceLocation;

public class MKDataSerializers {

    public static final MKDataSerializer<Integer> INT = new MKDataSerializer<>(EntityDataSerializers.INT) {
        @Override
        public <D> D serialize(DynamicOps<D> ops, Integer value) {
            return ops.createInt(value);
        }

        @Override
        public <D> Integer deserialize(Dynamic<D> dynamic, Integer defaultValue) {
            return dynamic.asInt(defaultValue);
        }
    };

    public static final MKDataSerializer<Float> FLOAT = new MKDataSerializer<>(EntityDataSerializers.FLOAT) {
        @Override
        public <D> D serialize(DynamicOps<D> ops, Float value) {
            return ops.createFloat(value);
        }

        @Override
        public <D> Float deserialize(Dynamic<D> dynamic, Float defaultValue) {
            return dynamic.asFloat(defaultValue);
        }
    };

    public static EntityDataSerializer<ResourceLocation> RESOURCE_LOCATION_EDS = new EntityDataSerializer<>() {
        @Override
        public void write(FriendlyByteBuf friendlyByteBuf, ResourceLocation resourceLocation) {
            friendlyByteBuf.writeResourceLocation(resourceLocation);
        }

        @Override
        public ResourceLocation read(FriendlyByteBuf friendlyByteBuf) {
            return friendlyByteBuf.readResourceLocation();
        }

        @Override
        public ResourceLocation copy(ResourceLocation resourceLocation) {
            return resourceLocation;
        }
    };

    public static final MKDataSerializer<ResourceLocation> RESOURCE_LOCATION = new MKDataSerializer<>(RESOURCE_LOCATION_EDS) {
        @Override
        public <D> D serialize(DynamicOps<D> ops, ResourceLocation value) {
            return ResourceLocation.CODEC.encodeStart(ops, value).getOrThrow(false, MKCore.LOGGER::error);
        }

        @Override
        public <D> ResourceLocation deserialize(Dynamic<D> dynamic, ResourceLocation defaultValue) {
            return ResourceLocation.CODEC.parse(dynamic).result().orElse(defaultValue);
        }
    };
}
