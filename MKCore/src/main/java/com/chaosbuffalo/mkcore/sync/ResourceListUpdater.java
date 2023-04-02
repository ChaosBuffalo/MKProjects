package com.chaosbuffalo.mkcore.sync;

import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Supplier;

public class ResourceListUpdater extends SyncListUpdater<ResourceLocation> {
    public ResourceListUpdater(String name, Supplier<List<ResourceLocation>> list) {
        super(name, list, ResourceListUpdater::encode, ResourceListUpdater::decode);
    }

    private static Tag encode(ResourceLocation location) {
        return StringTag.valueOf(location.toString());
    }

    private static ResourceLocation decode(Tag nbt) {
        return ResourceLocation.tryParse(nbt.getAsString());
    }
}
