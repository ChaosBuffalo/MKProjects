package com.chaosbuffalo.mkcore.sync.types;

import com.chaosbuffalo.mkcore.sync.SyncContext;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import com.chaosbuffalo.mkcore.sync.v2.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.v2.ISyncObject;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class SyncEntity<T extends Entity> implements ISyncObject {
    private int networkId;
    private boolean dirty;
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

    public SyncEntity() {
        networkId = -1;
    }

    public void set(T value) {
        int newId = value == null ? -1 : value.getId();
        boolean isPrev = networkId == newId;
        networkId = newId;
        if (!isPrev) {
            this.dirty = true;
            parentNotifier.notifyUpdate();
        }
    }

    @Nullable
    public T get(Level level, Class<T> clazz) {
        if (networkId == -1) {
            return null;
        }
        var entity = level.getEntity(networkId);
        if (clazz.isInstance(entity)) {
            return clazz.cast(entity);
        }
        return null;
    }

    public boolean hasEntity() {
        return networkId != -1;
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
        return IntTag.valueOf(networkId);
    }

    @Override
    public @Nullable Tag writeDirtyValue(SyncContext context, SyncVisibility visibility) {
        dirty = false;
        return writeFullValue(context, visibility);
    }

    @Override
    public void handleUpdatePayload(SyncContext context, Tag valueTag, SyncVisibility visibility) {
        if (valueTag instanceof IntTag intTag) {
            networkId = intTag.getAsInt();
        }
    }
}

