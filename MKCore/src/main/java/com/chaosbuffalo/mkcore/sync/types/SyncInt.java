package com.chaosbuffalo.mkcore.sync.types;

import com.chaosbuffalo.mkcore.sync.SyncContext;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import com.chaosbuffalo.mkcore.sync.v2.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.v2.ISyncObject;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;

public class SyncInt implements ISyncObject {
    private int value;
    private boolean dirty;
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

    public SyncInt(int value) {
        set(value, false);
    }

    public void set(int value) {
        set(value, true);
    }

    public void set(int value, boolean setDirty) {
        this.value = value;
        if (setDirty) {
            this.dirty = true;
            parentNotifier.notifyUpdate();
        }
    }

    public void add(int value) {
        set(get() + value);
    }

    public int get() {
        return value;
    }

    @Override
    public void setSyncUpdateNotifier(ISyncNotifier notifier) {
        parentNotifier = notifier;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void clearDirty() {
        dirty = false;
    }

    @Override
    public void handleUpdatePayload(SyncContext context, Tag valueTag, SyncVisibility visibility) {
        if (valueTag instanceof IntTag intTag) {
            value = intTag.getAsInt();
        }
    }

    @Override
    public @Nullable Tag writeFullValue(SyncContext context, SyncVisibility visibility) {
        return IntTag.valueOf(value);
    }

    @Override
    public @Nullable Tag writeDirtyValue(SyncContext context, SyncVisibility visibility) {
        dirty = false;
        return writeFullValue(context, visibility);
    }
}
