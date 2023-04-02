package com.chaosbuffalo.mkcore.sync;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class SyncResourceLocation extends SyncObject<ResourceLocation> {

    static void serialize(CompoundTag tag, SyncObject<ResourceLocation> instance) {
        tag.putString(instance.name, instance.get().toString());
    }

    static void deserialize(CompoundTag tag, SyncObject<ResourceLocation> instance) {
        instance.set(new ResourceLocation(tag.getString(instance.name)));
    }

    public SyncResourceLocation(String name, ResourceLocation value) {
        super(name, value, SyncResourceLocation::serialize, SyncResourceLocation::deserialize);
    }
}
