package com.chaosbuffalo.mkcore.sync.types;

import com.chaosbuffalo.mkcore.sync.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import net.minecraft.nbt.CompoundTag;

public class SyncInt implements ISyncObject {
    private final String name;
    private int value;
    private boolean dirty;
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

    public SyncInt(String name, int value) {
        this.name = name;
        set(value, false);
    }

    public void set(int value) {
        set(value, true);
    }

    public void set(int value, boolean setDirty) {
        this.value = value;
        if (setDirty) {
            this.dirty = true;
            parentNotifier.notifyUpdate(this);
        }
    }

    public void add(int value) {
        set(get() + value);
    }

    public int get() {
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
            this.value = tag.getInt(name);
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
        tag.putInt(name, value);
    }
}
