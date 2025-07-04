package com.chaosbuffalo.mkcore.sync;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SyncGroup implements ISyncObject {
    protected final Map<String, ISyncObject> components = new HashMap<>();
    protected final Set<String> dirtySet = new HashSet<>();
    protected UnhandledKeyHandlerFunction unhandledKeyHandler;
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

    public interface UnhandledKeyHandlerFunction {
        ISyncObject createSyncObject(String key, Tag valueTag);
    }

    public SyncGroup() {

    }

    public void setUnhandledKeyHandler(UnhandledKeyHandlerFunction handler) {
        this.unhandledKeyHandler = handler;
    }

    public void add(String name, ISyncObject sync) {
        add(name, sync, true);
    }

    public void add(String name, ISyncObject sync, boolean setDirty) {
        components.put(name, sync);
        sync.setNotifier(s -> {
            childUpdated(name, s);
        });
        if (setDirty) {
            childUpdated(name, sync);
        }
    }

    public void remove(String name, ISyncObject syncObject) {
        components.remove(name);
        dirtySet.remove(name);
        syncObject.setNotifier(ISyncNotifier.NONE);
    }

    @Override
    public void setNotifier(ISyncNotifier notifier) {
        parentNotifier = notifier;
    }

    public void childUpdated(String name, ISyncObject syncObject) {
        dirtySet.add(name);
        scheduleUpdate();
    }

    public void scheduleUpdate() {
        parentNotifier.notifyUpdate(this);
    }

    @Override
    public boolean isDirty() {
        return !dirtySet.isEmpty();
    }

    @Override
    public void handleUpdatePayload(SyncContext context, Tag valueTag) {
        if (!(valueTag instanceof CompoundTag groupTag) || groupTag.isEmpty()) {
            return;
        }

        if (components.isEmpty() && unhandledKeyHandler == null) {
            // No registered members and no dynamic handler registered.
            return;
        }

        for (var key : groupTag.getAllKeys()) {
            ISyncObject sync = components.get(key);
            Tag tag = groupTag.get(key);
            if (sync != null) {
                sync.handleUpdatePayload(context, tag);
            } else if (unhandledKeyHandler != null) {
                ISyncObject newSync = unhandledKeyHandler.createSyncObject(key, tag);
                if (newSync != null) {
                    newSync.handleUpdatePayload(context, tag);
                    components.put(key, newSync);
                }
            }
        }
    }

    @Override
    public CompoundTag writeFullValue(SyncContext context) {
        if (components.isEmpty()) {
            return null;
        }

        CompoundTag holder = new CompoundTag();
        for (var entry : components.entrySet()) {
            String name = entry.getKey();
            ISyncObject sync = entry.getValue();
            Tag value = sync.writeFullValue(context);
            if (value != null) {
                holder.put(name, value);
            }
        }

        return holder;
    }

    @Override
    public @Nullable CompoundTag writeUpdateValue(SyncContext context) {
        if (dirtySet.isEmpty()) {
            return null;
        }

        CompoundTag holder = new CompoundTag();
        for (String name : dirtySet) {
            ISyncObject sync = components.get(name);
            Tag value = sync.writeUpdateValue(context);
            if (value != null) {
                holder.put(name, value);
            }
        }

        dirtySet.clear();
        return holder;
    }

    @Override
    public String toString() {
        return String.format("SyncGroup[components=%d, dirty=%d]", components.size(), dirtySet.size());
    }
}
