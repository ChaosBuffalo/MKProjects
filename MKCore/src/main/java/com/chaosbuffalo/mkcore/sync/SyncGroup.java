package com.chaosbuffalo.mkcore.sync;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.*;

public class SyncGroup implements ISyncObject {
    protected static final String FULL_FLAG = "#f";
    protected final Set<ISyncObject> dirty = new HashSet<>();
    protected final Map<String, ISyncObject> components = new HashMap<>();
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

    public SyncGroup() {

    }

    @Override
    public String getName() {
        return "root";
    }

    public void add(ISyncObject sync) {
        components.put(sync.getName(), sync);
        sync.setNotifier(this::childUpdated);
    }

    public void remove(ISyncObject syncObject) {
        components.remove(syncObject.getName());
        dirty.remove(syncObject);
        syncObject.setNotifier(ISyncNotifier.NONE);
    }

    @Nullable
    public ISyncObject getChild(String name) {
        return components.get(name);
    }

    @Override
    public void setNotifier(ISyncNotifier notifier) {
        parentNotifier = notifier;
    }

    public void childUpdated(ISyncObject syncObject) {
        dirty.add(syncObject);
        scheduleUpdate();
    }

    public void scheduleUpdate() {
        parentNotifier.notifyUpdate(this);
    }

    @Override
    public boolean isDirty() {
        return !dirty.isEmpty();
    }

    protected CompoundTag extractGroupTag(CompoundTag tag) {
        return tag;
    }

    protected void insertGroupTag(CompoundTag tag, CompoundTag filledRoot) {

    }

    protected void beforeClientUpdate(CompoundTag groupTag, boolean fullSync) {

    }

    protected void afterClientUpdate(CompoundTag groupTag, boolean fullSync) {

    }

    @Nullable
    protected ISyncObject onUnknownKey(CompoundTag groupTag, String key) {
        MKCore.LOGGER.warn("SyncGroup {} received data for unknown tag {}", getName(), key);
        return null;
    }

    @Override
    public void deserializeUpdate(CompoundTag tag) {
        CompoundTag groupTag = extractGroupTag(tag);
        if (groupTag.isEmpty()) {
            return;
        }

        boolean fullSync = groupTag.contains(FULL_FLAG);
        beforeClientUpdate(groupTag, fullSync);
        for (String key : groupTag.getAllKeys()) {
            ISyncObject child = components.get(key);
            if (child != null) {
                child.deserializeUpdate(groupTag);
            } else {
                ISyncObject newObject = onUnknownKey(groupTag, key);
                if (newObject != null) {
                    newObject.deserializeUpdate(groupTag);
                    components.put(newObject.getName(), newObject);
                }
            }
        }
        afterClientUpdate(groupTag, fullSync);
    }

    @Override
    public void serializeUpdate(CompoundTag tag) {
        if (dirty.isEmpty())
            return;

        CompoundTag groupTag = extractGroupTag(tag);
        dirty.forEach(c -> c.serializeUpdate(groupTag));
        if (!groupTag.isEmpty()) {
            insertGroupTag(tag, groupTag);
        }
        dirty.clear();
    }

    @Override
    public void serializeFull(CompoundTag tag) {
        if (components.isEmpty())
            return;

        CompoundTag groupTag = extractGroupTag(tag);
        groupTag.putBoolean(FULL_FLAG, true);
        components.values().forEach(c -> c.serializeFull(groupTag));
        if (!groupTag.isEmpty()) {
            insertGroupTag(tag, groupTag);
        }
    }

    @Override
    public String toString() {
        return String.format("SyncGroup[components=%d, dirty=%d]", components.size(), dirty.size());
    }
}
