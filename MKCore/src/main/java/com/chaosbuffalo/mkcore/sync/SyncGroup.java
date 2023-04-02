package com.chaosbuffalo.mkcore.sync;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SyncGroup implements ISyncObject, ISyncNotifier {
    private final List<ISyncObject> components = new ArrayList<>();
    private final Set<ISyncObject> dirty = new HashSet<>();
    private String nestedName = null;
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;
    private boolean forceFull = false;

    public SyncGroup() {

    }

    public SyncGroup(String name) {
        nestedName = name;
    }

    public String getTagName() {
        return nestedName;
    }

    public void add(ISyncObject sync) {
        components.add(sync);
        sync.setNotifier(this);
    }

    public void addAll(ISyncObject... objects) {
        for (ISyncObject object : objects) {
            add(object);
        }
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

    @Override
    public void notifyUpdate(ISyncObject syncObject) {
        dirty.add(syncObject);
        parentNotifier.notifyUpdate(this);
    }

    public void forceDirty() {
        forceFull = true;
        parentNotifier.notifyUpdate(this);
    }

    @Override
    public boolean isDirty() {
        return forceFull || !dirty.isEmpty();
    }

    private CompoundTag getUpdateRootTag(CompoundTag tag) {
        return nestedName != null ? tag.getCompound(nestedName) : tag;
    }

    private void writeUpdateRootTag(CompoundTag tag, CompoundTag filledRoot) {
        if (nestedName != null && filledRoot.size() > 0) {
            tag.put(nestedName, filledRoot);
        }
    }

    @Override
    public void deserializeUpdate(CompoundTag tag) {
        CompoundTag root = getUpdateRootTag(tag);
        components.forEach(c -> c.deserializeUpdate(root));
    }

    @Override
    public void serializeUpdate(CompoundTag tag) {
        if (forceFull) {
            MKCore.LOGGER.debug("SyncGroup.serializeUpdate({}) forced full", nestedName);
            serializeFull(tag);
        } else {
            CompoundTag root = getUpdateRootTag(tag);
            dirty.stream()
                    .filter(ISyncObject::isDirty)
                    .forEach(c -> c.serializeUpdate(root));
            if (root.size() > 0) {
                writeUpdateRootTag(tag, root);
            }
            dirty.clear();
        }
    }

    @Override
    public void serializeFull(CompoundTag tag) {
        CompoundTag root = getUpdateRootTag(tag);
        components.forEach(c -> c.serializeFull(root));
        writeUpdateRootTag(tag, root);
        dirty.clear();
        forceFull = false;
    }

    @Override
    public String toString() {
        return String.format("SyncGroup[name='%s', components=%d, dirty=%d]", nestedName, components.size(), dirty.size());
    }
}
