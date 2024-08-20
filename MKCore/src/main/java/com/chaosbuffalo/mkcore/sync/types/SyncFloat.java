package com.chaosbuffalo.mkcore.sync.types;

import com.chaosbuffalo.mkcore.sync.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public class SyncFloat implements ISyncObject {
    private final String name;
    private float value;
    private boolean dirty;
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

    public SyncFloat(String name, float value) {
        this.name = name;
        set(value, false);
    }

    public void set(float value) {
        set(value, true);
    }

    public void set(float value, boolean setDirty) {
        this.value = value;
        if (setDirty) {
            this.dirty = true;
            parentNotifier.notifyUpdate(this);
        }
    }

    public void add(float value) {
        set(get() + value);
    }

    public float get() {
        return value;
    }

    @Override
    public void setNotifier(ISyncNotifier notifier) {
        parentNotifier = notifier;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void deserializeUpdate(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains(name)) {
            this.value = tag.getFloat(name);
        }
    }

    @Override
    public void serializeUpdate(HolderLookup.Provider provider, CompoundTag tag) {
        if (dirty) {
            serializeFull(provider, tag);
            dirty = false;
        }
    }

    @Override
    public void serializeFull(HolderLookup.Provider provider, CompoundTag tag) {
        tag.putFloat(name, value);
    }
}
