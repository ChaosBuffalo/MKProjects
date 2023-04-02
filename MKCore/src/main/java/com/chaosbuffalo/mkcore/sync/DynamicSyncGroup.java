package com.chaosbuffalo.mkcore.sync;

import net.minecraft.nbt.CompoundTag;

public abstract class DynamicSyncGroup extends SyncGroup {

    protected abstract void onKey(String key);

    @Override
    public void deserializeUpdate(CompoundTag tag) {
        tag.getAllKeys().forEach(this::onKey);
        super.deserializeUpdate(tag);
    }
}
