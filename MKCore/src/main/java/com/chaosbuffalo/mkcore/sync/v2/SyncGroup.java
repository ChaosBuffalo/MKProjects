package com.chaosbuffalo.mkcore.sync.v2;

import com.chaosbuffalo.mkcore.sync.SyncContext;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SyncGroup implements ISyncUpdatableBase {
    private final Map<String, SyncGroup> subgroups = new HashMap<>();
    private final Map<String, SyncMemberInfo> members = new HashMap<>();
    private final EnumSet<SyncVisibility> dirtySet = EnumSet.noneOf(SyncVisibility.class);
    private DynamicObjectFactoryFunction dynamicMemberFactory = null;
    private SyncGroup parentGroup;

    public interface DynamicObjectFactoryFunction {
        ISyncUpdatableBase createSyncObject(String key, Tag valueTag, SyncVisibility visibility);
    }

    public static final class SyncMemberInfo {
        public final ISyncObject object;
        public final SyncVisibility visibility;
        public boolean dirty;

        public SyncMemberInfo(ISyncObject object, SyncVisibility visibility, boolean dirty) {
            this.object = object;
            this.visibility = visibility;
            this.dirty = dirty;
        }

        public boolean matches(SyncVisibility visibility) {
            return this.visibility == visibility;
        }

        private void clearDirty() {
            object.clearDirty();
            dirty = false;
        }
    }

    public void setDynamicMemberFactory(DynamicObjectFactoryFunction dynamicMemberFactory) {
        this.dynamicMemberFactory = dynamicMemberFactory;
    }

    public void add(String name, ISyncObject sync, SyncVisibility visibility) {
        add(name, sync, visibility, false);
    }

    public void add(String name, ISyncObject sync, SyncVisibility visibility, boolean setDirty) {
        SyncMemberInfo entry = new SyncMemberInfo(sync, visibility, setDirty);

        members.put(name, entry);
        sync.setSyncUpdateNotifier(() -> {
            entry.dirty = true;
            onMemberUpdated(visibility);
        });
        if (setDirty) {
            onMemberUpdated(visibility);
        }
    }

    public void remove(String name, ISyncObject syncObject, SyncVisibility visibility) {
        SyncMemberInfo sync = members.remove(name);
        if (sync != null) {
            sync.object.setSyncUpdateNotifier(ISyncNotifier.NONE);
        }
    }

    private void setParentGroup(SyncGroup parent) {
        this.parentGroup = parent;
    }

    protected void onMemberUpdated(SyncVisibility visibility) {
        dirtySet.add(visibility);
        if (parentGroup != null) {
            parentGroup.onMemberUpdated(visibility);
        }
    }

    public void addGroup(String name, SyncGroup childGroup) {
        subgroups.put(name, childGroup);

        childGroup.setParentGroup(this);

        // If the group being added was already dirty, reflect that in ourselves
        childGroup.dirtySet.forEach(this::onMemberUpdated);
    }

    public void removeGroup(String name, SyncGroup group) {
        SyncGroup childGroup = subgroups.remove(name);
        if (childGroup == null) {
            return;
        }
        if (childGroup != group) {
            Util.logAndPauseIfInIde("sync group mismatch");
        }

        childGroup.setParentGroup(null);
    }

    public boolean isDirty(SyncVisibility visibility) {
        return dirtySet.contains(visibility);
    }

    public void clearDirty() {
        subgroups.values().forEach(SyncGroup::clearDirty);
        members.values().forEach(SyncMemberInfo::clearDirty);
        dirtySet.clear();
    }

    @Override
    public void handleUpdatePayload(SyncContext context, Tag valueTag, SyncVisibility visibility) {
        if (!(valueTag instanceof CompoundTag groupTag) || groupTag.isEmpty()) {
            return;
        }

        if (subgroups.isEmpty() && members.isEmpty() && dynamicMemberFactory == null) {
            return;
        }

        for (String key : groupTag.getAllKeys()) {
            Tag memberTag = groupTag.get(key);
            SyncGroup childGroup = subgroups.get(key);
            if (childGroup != null) {
                childGroup.handleUpdatePayload(context, memberTag, visibility);
                continue;
            }

            SyncMemberInfo existingEntry = members.get(key);
            if (existingEntry != null) {
                existingEntry.object.handleUpdatePayload(context, memberTag, visibility);
            } else if (dynamicMemberFactory != null) {
                ISyncUpdatableBase newObject = dynamicMemberFactory.createSyncObject(key, memberTag, visibility);
                if (newObject instanceof SyncGroup newGrp) {
                    newGrp.handleUpdatePayload(context, memberTag, visibility);
                    addGroup(key, newGrp);
                } else if (newObject instanceof ISyncObject newObj) {
                    newObj.handleUpdatePayload(context, memberTag, visibility);
                    add(key, newObj, visibility, false);
                }
            }
        }
    }


    @Override
    public @Nullable CompoundTag writeDirtyValue(SyncContext context, SyncVisibility visibility) {

        // Early return if no dirty elements for this visibility
        if (!dirtySet.contains(visibility)) {
            return null;
        }

        CompoundTag outputTag = new CompoundTag();
        writeDirtyGroups(context, outputTag, visibility);
        writeDirtyMembers(context, outputTag, visibility);

        dirtySet.remove(visibility);
        return outputTag.isEmpty() ? null : outputTag;
    }

    private void writeDirtyGroups(SyncContext context, CompoundTag outputTag, SyncVisibility visibility) {
        if (subgroups.isEmpty()) {
            return;
        }

        for (Map.Entry<String, SyncGroup> entry : subgroups.entrySet()) {
            SyncGroup childGroup = entry.getValue();
            if (childGroup.isDirty(visibility)) {
                Tag childTag = childGroup.writeDirtyValue(context, visibility);
                if (childTag != null) {
                    outputTag.put(entry.getKey(), childTag);
                }
            }
        }
    }

    private void writeDirtyMembers(SyncContext context, CompoundTag outputTag, SyncVisibility visibility) {
        if (members.isEmpty()) {
            return;
        }

        for (Map.Entry<String, SyncMemberInfo> entry : members.entrySet()) {
            SyncMemberInfo value = entry.getValue();
            if (value.dirty && value.matches(visibility)) {
                Tag childTag = value.object.writeDirtyValue(context, visibility);
                if (childTag != null) {
                    outputTag.put(entry.getKey(), childTag);
                }
                value.dirty = false;
            }
        }
    }

    @Override
    public @Nullable CompoundTag writeFullValue(SyncContext context, SyncVisibility visibility) {
        // Early return if no children or members
        if (subgroups.isEmpty() && members.isEmpty()) {
            return null;
        }

        CompoundTag groupTag = new CompoundTag();
        writeFullGroups(context, groupTag, visibility);
        writeFullMembers(context, groupTag, visibility);
        return groupTag.isEmpty() ? null : groupTag;
    }

    private void writeFullGroups(SyncContext context, CompoundTag groupTag, SyncVisibility visibility) {
        // Early return if no child groups
        if (subgroups.isEmpty()) {
            return;
        }

        for (Map.Entry<String, SyncGroup> entry : subgroups.entrySet()) {
            SyncGroup childGroup = entry.getValue();
            var childTag = childGroup.writeFullValue(context, visibility);
            if (childTag != null) {
                groupTag.put(entry.getKey(), childTag);
            }
        }
    }

    private void writeFullMembers(SyncContext context, CompoundTag outputTag, SyncVisibility visibility) {
        // Early return if no members
        if (members.isEmpty()) {
            return;
        }

        for (Map.Entry<String, SyncMemberInfo> entry : members.entrySet()) {
            SyncMemberInfo value = entry.getValue();
            if (value.matches(visibility)) {
                Tag childTag = value.object.writeFullValue(context, visibility);
                if (childTag != null) {
                    outputTag.put(entry.getKey(), childTag);
                }
            }
        }
    }

    @Override
    public String toString() {
        String dirtyString = Arrays.stream(SyncVisibility.values())
                .map(v -> String.format("%s=%b", v, isDirty(v)))
                .collect(Collectors.joining(","));
        return String.format("SyncGroup[children=%s, components=%s, dirty='%s']", subgroups, members, dirtyString);
    }
}
