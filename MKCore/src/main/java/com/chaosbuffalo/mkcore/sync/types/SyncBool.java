package com.chaosbuffalo.mkcore.sync.types;

import com.chaosbuffalo.mkcore.sync.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import com.chaosbuffalo.mkcore.sync.SyncContext;
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
    public void clearDirty() {
        dirty = false;
    }

    @Override
    public @Nullable Tag writeFullValue(SyncContext context) {
        return ByteTag.valueOf(value);
    }

    @Override
    public @Nullable Tag writeUpdateValue(SyncContext context) {
        dirty = false;
        return writeFullValue(context);
    }

    @Override
    public void handleUpdatePayload(SyncContext context, Tag valueTag) {
        if (valueTag instanceof ByteTag byteTag) {
            value = byteTag.getAsByte() != 0;
        }
    }
}
