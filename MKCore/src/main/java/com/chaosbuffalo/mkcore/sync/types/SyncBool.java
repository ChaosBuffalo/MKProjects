package com.chaosbuffalo.mkcore.sync.types;

import com.chaosbuffalo.mkcore.sync.SyncContext;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import com.chaosbuffalo.mkcore.sync.v2.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.v2.ISyncObject;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;

public class SyncBool implements ISyncObject {
    private boolean value;
    private boolean dirty;
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

    public SyncBool(boolean value) {
        set(value, false);
    }

    public void set(boolean value) {
        set(value, value != this.value);
    }

    private void set(boolean value, boolean setDirty) {
        this.value = value;
        if (setDirty) {
            this.dirty = true;
            parentNotifier.notifyUpdate();
        }
    }

    public boolean get() {
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
    public @Nullable Tag writeFullValue(SyncContext context, SyncVisibility visibility) {
        return ByteTag.valueOf(value);
    }

    @Override
    public @Nullable Tag writeDirtyValue(SyncContext context, SyncVisibility visibility) {
        dirty = false;
        return writeFullValue(context, visibility);
    }

    @Override
    public void handleUpdatePayload(SyncContext context, Tag valueTag, SyncVisibility visibility) {
        if (valueTag instanceof ByteTag byteTag) {
            value = byteTag.getAsByte() != 0;
        }
    }
}
