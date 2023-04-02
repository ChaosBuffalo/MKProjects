package com.chaosbuffalo.mkcore.sync;

import net.minecraft.nbt.CompoundTag;

public class SyncFloat implements ISyncObject {
    private final String name;
    private float value;
    private boolean dirty;
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

    public SyncFloat(String name, float value) {
        this.name = name;
        set(value);
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
    public void deserializeUpdate(CompoundTag tag) {
        if (tag.contains(name)) {
            this.value = tag.getFloat(name);
        }
    }

    @Override
    public void serializeUpdate(CompoundTag tag) {
        if (dirty) {
            serializeFull(tag);
            dirty = false;
        }
    }

    @Override
    public void serializeFull(CompoundTag tag) {
        tag.putFloat(name, value);
        dirty = false;
    }
}
