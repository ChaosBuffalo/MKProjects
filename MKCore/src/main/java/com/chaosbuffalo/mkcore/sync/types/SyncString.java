package com.chaosbuffalo.mkcore.sync.types;

import com.chaosbuffalo.mkcore.sync.SyncContext;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import com.chaosbuffalo.mkcore.sync.v2.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.v2.ISyncObject;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class SyncString implements ISyncObject {
    private String value;
    private boolean dirty;
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;
    @Nullable
    private Consumer<String> onSetCallback;

    public SyncString(String value) {
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
            parentNotifier.notifyUpdate();
        }
    }

    public String get() {
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
        return StringTag.valueOf(value);
    }

    @Override
    public @Nullable Tag writeDirtyValue(SyncContext context, SyncVisibility visibility) {
        dirty = false;
        return writeFullValue(context, visibility);
    }

    @Override
    public void handleUpdatePayload(SyncContext context, Tag valueTag, SyncVisibility visibility) {
        if (valueTag instanceof StringTag stringTag) {
            value = stringTag.getAsString();
            if (onSetCallback != null) {
                onSetCallback.accept(value);
            }
        }
    }
}
