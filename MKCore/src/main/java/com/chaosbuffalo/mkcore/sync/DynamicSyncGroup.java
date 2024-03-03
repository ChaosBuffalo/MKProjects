package com.chaosbuffalo.mkcore.sync;

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
    public void serializeUpdate(CompoundTag tag) {
        if (forceFull) {
            serializeFull(tag);
            forceFull = false;
        } else {
            super.serializeUpdate(tag);
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
