package com.chaosbuffalo.mkcore.sync;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import java.util.*;

public class SyncGroup implements ISyncObject {
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

    @Override
    public void deserializeUpdate(HolderLookup.Provider provider, CompoundTag tag) {
        CompoundTag groupTag = extractGroupTag(tag);
        readComponentUpdates(provider, groupTag);
    }

    protected void readComponentUpdates(HolderLookup.Provider provider, CompoundTag groupTag) {
        if (groupTag.isEmpty() || components.isEmpty()) {
            return;
        }
        components.forEach(c -> c.deserializeUpdate(provider, groupTag));
    }

    @Override
    public void serializeUpdate(HolderLookup.Provider provider, CompoundTag tag) {
        if (dirty.isEmpty()) {
            return;
        }

        CompoundTag groupTag = extractGroupTag(tag);
        writeComponentUpdates(provider, groupTag, dirty);
        if (!groupTag.isEmpty()) {
            insertGroupTag(tag, groupTag);
        }
        dirty.clear();
    }

    protected void writeComponentUpdates(HolderLookup.Provider provider, CompoundTag groupTag, Collection<ISyncObject> objects) {
        for (ISyncObject object : objects) {
            object.serializeUpdate(provider, groupTag);
        }
    }

    @Override
    public void serializeFull(HolderLookup.Provider provider, CompoundTag tag) {
        if (components.isEmpty()) {
            return;
        }

        CompoundTag groupTag = extractGroupTag(tag);
        writeFullComponents(provider, groupTag, components);
        if (!groupTag.isEmpty()) {
            insertGroupTag(tag, groupTag);
        }
    }

    protected void writeFullComponents(HolderLookup.Provider provider, CompoundTag groupTag, Collection<ISyncObject> objects) {
        for (ISyncObject object : objects) {
            object.serializeFull(provider, groupTag);
        }
    }

    @Override
    public String toString() {
        return String.format("SyncGroup[components=%d, dirty=%d]", components.size(), dirty.size());
    }
}
