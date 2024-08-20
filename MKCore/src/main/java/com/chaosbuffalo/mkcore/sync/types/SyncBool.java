package com.chaosbuffalo.mkcore.sync.types;

import com.chaosbuffalo.mkcore.sync.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public class SyncBool implements ISyncObject {
    private final String name;
    private boolean value;
    private boolean dirty;
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

    public SyncBool(String name, boolean value) {
        this.name = name;
        set(value, false);
    }

    public void set(boolean value) {
        set(value, true);
    }

    private void set(boolean value, boolean setDirty) {
        this.value = value;
        if (setDirty) {
            this.dirty = true;
            parentNotifier.notifyUpdate(this);
        }
    }

    public boolean get() {
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
            this.value = tag.getBoolean(name);
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
        tag.putBoolean(name, value);
    }
}
