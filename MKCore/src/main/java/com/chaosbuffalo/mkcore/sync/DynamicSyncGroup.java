package com.chaosbuffalo.mkcore.sync;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import java.util.function.Supplier;

public abstract class DynamicSyncGroup extends NamedSyncGroup {
    private boolean forceFull;

    public DynamicSyncGroup(String name) {
        super(name);
    }

    @Override
    public boolean isDirty() {
        return forceFull || super.isDirty();
    }

    @Override
    public void add(ISyncObject sync) {
        add(sync, true);
    }

    public void add(ISyncObject sync, boolean setDirty) {
        super.add(sync);
        if (setDirty) {
            childUpdated(sync);
        }
    }

    @Override
    public void remove(ISyncObject syncObject) {
        super.remove(syncObject);
        forceFull = true;
        scheduleUpdate();
    }

    @Override
    public void serializeUpdate(HolderLookup.Provider provider, CompoundTag tag) {
        if (forceFull) {
            serializeFull(provider, tag);
            forceFull = false;
        } else {
            super.serializeUpdate(provider, tag);
        }
    }

    protected abstract void preUpdateEntry(String key, Supplier<CompoundTag> value);

    @Override
    protected void beforeClientUpdate(CompoundTag groupTag, boolean fullSync) {
        for (String key : groupTag.getAllKeys()) {
            if (key.startsWith("#"))
                continue;
            preUpdateEntry(key, () -> groupTag.getCompound(key));
        }
    }
}
