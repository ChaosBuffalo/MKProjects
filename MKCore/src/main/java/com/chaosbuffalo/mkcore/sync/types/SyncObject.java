package com.chaosbuffalo.mkcore.sync.types;

import com.chaosbuffalo.mkcore.sync.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import net.minecraft.nbt.CompoundTag;

public abstract class SyncObject<T> implements ISyncObject {

    protected final String name;
    protected T value;
    protected boolean dirty;
    protected ISyncNotifier parentNotifier = ISyncNotifier.NONE;


    public SyncObject(String name, T value) {
        this.value = value;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
        this.dirty = true;
        parentNotifier.notifyUpdate(this);
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
    public abstract void deserializeUpdate(CompoundTag tag);

    @Override
    public void serializeUpdate(CompoundTag tag) {
        if (dirty) {
            serializeFull(tag);
            dirty = false;
        }
    }

    @Override
    public abstract void serializeFull(CompoundTag tag);
}
