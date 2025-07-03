package com.chaosbuffalo.mkcore.sync.types;

import com.chaosbuffalo.mkcore.sync.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class SyncString implements ISyncObject {
    private final String name;
    private String value;
    private boolean dirty;
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;
    @Nullable
    private Consumer<String> onSetCallback;

    public SyncString(String name, String value) {
        this.name = name;
        set(value, false);
    }

    public void setCallback(Consumer<String> onSetCallback) {
        this.onSetCallback = onSetCallback;
    }

    public void set(String value) {
        set(value, true);
    }

    private void set(String value, boolean setDirty) {
        this.value = value;
        if (setDirty) {
            this.dirty = true;
            parentNotifier.notifyUpdate(this);
        }
    }

    public String get() {
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
            this.value = tag.getString(name);
            if (onSetCallback != null) {
                onSetCallback.accept(value);
            }
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
        tag.putString(name, value);
    }
}
