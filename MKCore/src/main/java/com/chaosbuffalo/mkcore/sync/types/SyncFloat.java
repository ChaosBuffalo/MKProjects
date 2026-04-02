package com.chaosbuffalo.mkcore.sync.types;

import com.chaosbuffalo.mkcore.sync.SyncContext;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import com.chaosbuffalo.mkcore.sync.v2.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.v2.ISyncObject;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;

public class SyncFloat implements ISyncObject {
    private float value;
    private boolean dirty;
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

    public SyncFloat(float value) {
        set(value, false);
    }

    public void set(float value) {
        set(value, value != this.value);
    }

    public void set(float value, boolean setDirty) {
        this.value = value;
        if (setDirty) {
            this.dirty = true;
            parentNotifier.notifyUpdate();
        }
    }

    public void add(float value) {
        set(get() + value);
    }

    public float get() {
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
        if (valueTag instanceof FloatTag floatTag) {
            value = floatTag.getAsFloat();
        }
    }

    @Override
    public @Nullable Tag writeFullValue(SyncContext context, SyncVisibility visibility) {
        return FloatTag.valueOf(value);
    }

    @Override
    public @Nullable Tag writeDirtyValue(SyncContext context, SyncVisibility visibility) {
        dirty = false;
        return writeFullValue(context, visibility);
    }
}
