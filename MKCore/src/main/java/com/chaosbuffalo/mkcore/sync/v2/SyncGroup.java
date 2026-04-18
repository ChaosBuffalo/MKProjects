package com.chaosbuffalo.mkcore.sync.v2;

import com.chaosbuffalo.mkcore.sync.SyncContext;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SyncGroup implements ISyncUpdatableBase {
    private final Map<String, MemberEntry> members = new HashMap<>();
    private final EnumMap<SyncVisibility, Map<String, MemberEntry>> dirtyMembers = new EnumMap<>(SyncVisibility.class);
    private DynamicObjectFactoryFunction dynamicMemberFactory = null;
    private Consumer<SyncVisibility> parentNotifier = vis -> {};

    public interface DynamicObjectFactoryFunction {
        @Nullable
        ISyncUpdatableBase createSyncObject(String key, Tag valueTag, SyncVisibility visibility);
    }

    private abstract static sealed class MemberEntry implements ISyncUpdatableBase
            permits ObjectMemberEntry, ChildGroupMemberEntry {
        public abstract boolean isDirtyFor(SyncVisibility visibility);
    }

    private static final class ObjectMemberEntry extends MemberEntry {
        private final ISyncObject object;
        private final SyncVisibility visibility;
        private boolean dirty;

        private ObjectMemberEntry(ISyncObject object, SyncVisibility visibility, boolean dirty) {
            this.object = object;
            this.visibility = visibility;
            this.dirty = dirty;
        }

        public boolean markDirty() {
            if (dirty) {
                return false;
            }
            dirty = true;
            return true;
        }

        @Override
        public boolean isDirtyFor(SyncVisibility visibility) {
            return dirty && this.visibility == visibility;
        }

        @Override
        public void clearDirty() {
            object.clearDirty();
            dirty = false;
        }

        @Override
        public void handleUpdatePayload(SyncContext context, Tag valueTag, SyncVisibility visibility) {
            object.handleUpdatePayload(context, valueTag, visibility);
        }

        @Override
        public @Nullable Tag writeFullValue(SyncContext context, SyncVisibility visibility) {
            if (this.visibility != visibility) {
                return null;
            }
            return object.writeFullValue(context, visibility);
        }

        @Override
        public @Nullable Tag writeDirtyValue(SyncContext context, SyncVisibility visibility) {
            if (!isDirtyFor(visibility)) {
                throw new IllegalStateException(
                        "Sync object '%s' was asked to write while not dirty for visibility %s"
                                .formatted(object, visibility));
            }

            Tag tag = object.writeDirtyValue(context, visibility);
            if (tag == null) {
                throw new IllegalStateException(
                        "Dirty sync object '%s' returned null for visibility %s".formatted(object, visibility));
            }
            dirty = false;
            return tag;
        }
    }

    private static final class ChildGroupMemberEntry extends MemberEntry {
        private final SyncGroup childGroup;

        private ChildGroupMemberEntry(SyncGroup childGroup) {
            this.childGroup = childGroup;
        }

        @Override
        public boolean isDirtyFor(SyncVisibility visibility) {
            return childGroup.isDirty(visibility);
        }

        @Override
        public void clearDirty() {
            childGroup.clearDirty();
        }

        @Override
        public void handleUpdatePayload(SyncContext context, Tag valueTag, SyncVisibility visibility) {
            childGroup.handleUpdatePayload(context, valueTag, visibility);
        }

        @Override
        public @Nullable Tag writeFullValue(SyncContext context, SyncVisibility visibility) {
            return childGroup.writeFullValue(context, visibility);
        }

        @Override
        public @Nullable Tag writeDirtyValue(SyncContext context, SyncVisibility visibility) {
            if (!childGroup.isDirty(visibility)) {
                throw new IllegalStateException(
                        "Child sync group was asked to write while not dirty for visibility %s"
                                .formatted(visibility));
            }

            Tag tag = childGroup.writeDirtyValue(context, visibility);
            if (tag == null) {
                throw new IllegalStateException(
                        "Dirty child sync group returned null for visibility %s".formatted(visibility));
            }
            return tag;
        }
    }

    public void setDynamicMemberFactory(DynamicObjectFactoryFunction dynamicMemberFactory) {
        this.dynamicMemberFactory = dynamicMemberFactory;
    }

    public void add(String name, ISyncObject sync, SyncVisibility visibility) {
        add(name, sync, visibility, false);
    }

    public void add(String name, ISyncObject sync, SyncVisibility visibility, boolean setDirty) {
        ObjectMemberEntry entry = new ObjectMemberEntry(sync, visibility, setDirty);

        members.put(name, entry);
        sync.setSyncUpdateNotifier(() -> {
            if (!entry.markDirty()) {
                return;
            }
            markMemberDirty(name, entry, visibility);
        });
        if (setDirty) {
            markMemberDirty(name, entry, visibility);
        }
    }

    public void addPublic(String name, ISyncObject sync) {
        add(name, sync, SyncVisibility.Public);
    }

    public void addPrivate(String name, ISyncObject sync) {
        add(name, sync, SyncVisibility.Private);
    }

    private void markMemberDirty(String name, MemberEntry member, SyncVisibility visibility) {
        Map<String, MemberEntry> dirtyByVisibility =
                dirtyMembers.computeIfAbsent(visibility, k -> new HashMap<>());
        if (dirtyByVisibility.putIfAbsent(name, member) == null) {
            onMemberUpdated(visibility);
        }
    }

    protected void onMemberUpdated(SyncVisibility visibility) {
        parentNotifier.accept(visibility);
    }

    public void addChild(String name, SyncGroup childGroup) {
        ChildGroupMemberEntry entry = new ChildGroupMemberEntry(childGroup);
        members.put(name, entry);

        childGroup.parentNotifier = vis -> markMemberDirty(name, entry, vis);

        // If the group being added was already dirty, reflect that in ourselves
        childGroup.dirtyMembers.forEach((vis, dirty) -> {
            if (!dirty.isEmpty()) {
                markMemberDirty(name, entry, vis);
            }
        });
    }

    public void addChild(String name, ISyncGroupProvider provider) {
        addChild(name, provider.getSyncGroup());
    }

    public boolean isDirty(SyncVisibility visibility) {
        var dirty = dirtyMembers.get(visibility);
        return dirty != null && !dirty.isEmpty();
    }

    @Override
    public void clearDirty() {
        members.values().forEach(ISyncUpdatableBase::clearDirty);
        dirtyMembers.values().forEach(Map::clear);
    }

    @Override
    public void handleUpdatePayload(SyncContext context, Tag valueTag, SyncVisibility visibility) {
        if (!(valueTag instanceof CompoundTag groupTag) || groupTag.isEmpty()) {
            return;
        }

        if (members.isEmpty() && dynamicMemberFactory == null) {
            return;
        }

        for (String key : groupTag.getAllKeys()) {
            Tag memberTag = groupTag.get(key);

            MemberEntry existingMember = members.get(key);

            if (existingMember != null) {
                existingMember.handleUpdatePayload(context, memberTag, visibility);
            } else if (dynamicMemberFactory != null) {
                ISyncUpdatableBase newObject = dynamicMemberFactory.createSyncObject(key, memberTag, visibility);
                if (newObject == null) {
                    continue;
                }
                newObject.handleUpdatePayload(context, memberTag, visibility);
                if (newObject instanceof SyncGroup newGrp) {
                    addChild(key, newGrp);
                } else if (newObject instanceof ISyncObject newObj) {
                    add(key, newObj, visibility, false);
                }
            }
        }
    }


    @Override
    public @Nullable CompoundTag writeDirtyValue(SyncContext context, SyncVisibility visibility) {
        Map<String, MemberEntry> dirty = dirtyMembers.get(visibility);
        if (dirty == null || dirty.isEmpty()) {
            return null;
        }

        CompoundTag outputTag = new CompoundTag();
        for (var entry : dirty.entrySet()) {
            MemberEntry member = entry.getValue();
            if (member.isDirtyFor(visibility)) {
                Tag memberTag = member.writeDirtyValue(context, visibility);
                if (memberTag == null) {
                    throw new IllegalStateException(
                            "Dirty sync member '%s' returned null for visibility %s"
                                    .formatted(entry.getKey(), visibility));
                }
                outputTag.put(entry.getKey(), memberTag);
            }
        }
        dirty.clear();
        if (outputTag.isEmpty()) {
            throw new IllegalStateException(
                    "Dirty sync group produced no payload for visibility %s".formatted(visibility));
        }
        return outputTag;
    }

    @Override
    public @Nullable CompoundTag writeFullValue(SyncContext context, SyncVisibility visibility) {
        // Early return if no children or members
        if (members.isEmpty()) {
            return null;
        }

        CompoundTag groupTag = new CompoundTag();
        for (Map.Entry<String, MemberEntry> entry : members.entrySet()) {
            MemberEntry member = entry.getValue();
            Tag childTag = member.writeFullValue(context, visibility);
            if (childTag != null) {
                groupTag.put(entry.getKey(), childTag);
            }
        }

        return groupTag.isEmpty() ? null : groupTag;
    }

    @Override
    public String toString() {
        String dirtyString = Arrays.stream(SyncVisibility.values())
                .map(v -> String.format("%s=%b", v, isDirty(v)))
                .collect(Collectors.joining(","));
        return String.format("SyncGroup[components=%s, dirty='%s']", members, dirtyString);
    }
}
