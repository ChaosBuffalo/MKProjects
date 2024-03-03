package com.chaosbuffalo.mkcore.sync;

import net.minecraft.nbt.CompoundTag;

import java.util.*;

public class SyncGroup implements ISyncObject {
    protected static final String FULL_FLAG = "#f";
    protected final List<ISyncObject> components = new ArrayList<>();
    protected final Set<ISyncObject> dirty = new HashSet<>();
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

    public SyncGroup() {

    }

    public void add(ISyncObject sync) {
        components.add(sync);
        sync.setNotifier(this::childUpdated);
    }

    public void remove(ISyncObject syncObject) {
        components.remove(syncObject);
        dirty.remove(syncObject);
        syncObject.setNotifier(ISyncNotifier.NONE);
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

    @Override
    public void deserializeUpdate(CompoundTag tag) {
        CompoundTag groupTag = extractGroupTag(tag);
        if (!groupTag.isEmpty()) {
            boolean fullSync = groupTag.contains(FULL_FLAG);
            beforeClientUpdate(groupTag, fullSync);
        }
        components.forEach(c -> c.deserializeUpdate(groupTag));
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
        components.forEach(c -> c.serializeFull(groupTag));
        if (!groupTag.isEmpty()) {
            insertGroupTag(tag, groupTag);
        }
    }

    @Override
    public String toString() {
        return String.format("SyncGroup[components=%d, dirty=%d]", components.size(), dirty.size());
    }
}
