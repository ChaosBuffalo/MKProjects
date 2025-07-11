package com.chaosbuffalo.mkcore.sync.types;

import com.chaosbuffalo.mkcore.sync.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import com.chaosbuffalo.mkcore.sync.SyncContext;
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
    public void clearDirty() {
        dirty = false;
    }

    @Override
    public @Nullable Tag writeFullValue(SyncContext context) {
        return FloatTag.valueOf(value);
    }

    @Override
    public @Nullable Tag writeUpdateValue(SyncContext context) {
        dirty = false;
        return writeFullValue(context);
    }

    @Override
    public void handleUpdatePayload(SyncContext context, Tag valueTag) {
        if (valueTag instanceof FloatTag floatTag) {
            value = floatTag.getAsFloat();
        }
    }
}
