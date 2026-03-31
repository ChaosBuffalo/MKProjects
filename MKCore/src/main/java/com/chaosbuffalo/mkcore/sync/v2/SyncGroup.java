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

    private static final class MemberEntry implements ISyncUpdatableBase {
        public final @Nullable ISyncObject object;
        public final @Nullable SyncGroup childGroup;
        public final @Nullable SyncVisibility visibility;
        public boolean dirty;

        private MemberEntry(@Nullable ISyncObject object, @Nullable SyncGroup childGroup,
                            @Nullable SyncVisibility visibility, boolean dirty) {
            this.object = object;
            this.childGroup = childGroup;
            this.visibility = visibility;
            this.dirty = dirty;
        }

        public static MemberEntry syncObject(ISyncObject object, SyncVisibility visibility, boolean dirty) {
            return new MemberEntry(object, null, visibility, dirty);
        }

        public static MemberEntry childGroup(SyncGroup childGroup) {
            return new MemberEntry(null, childGroup, null, false);
        }

        public boolean isObjectVisible(SyncVisibility visibility) {
            return object != null && this.visibility == visibility;
        }

        @Override
        public void clearDirty() {
            if (object != null) {
                object.clearDirty();
                dirty = false;
            } else if (childGroup != null) {
                childGroup.clearDirty();
            }
        }

        @Override
        public void handleUpdatePayload(SyncContext context, Tag valueTag, SyncVisibility visibility) {
            if (object != null) {
                object.handleUpdatePayload(context, valueTag, visibility);
            } else if (childGroup != null) {
                childGroup.handleUpdatePayload(context, valueTag, visibility);
            }
        }

        @Override
        public @Nullable Tag writeFullValue(SyncContext context, SyncVisibility visibility) {
            if (object != null) {
                return object.writeFullValue(context, visibility);
            } else if (childGroup != null) {
                return childGroup.writeFullValue(context, visibility);
            }
            return null;
        }

        @Override
        public @Nullable Tag writeDirtyValue(SyncContext context, SyncVisibility visibility) {
            if (object != null) {
                Tag tag = object.writeDirtyValue(context, visibility);
                dirty = false;
                return tag;
            } else if (childGroup != null) {
                return childGroup.writeDirtyValue(context, visibility);
            }
            return null;
        }
    }

    public void setDynamicMemberFactory(DynamicObjectFactoryFunction dynamicMemberFactory) {
        this.dynamicMemberFactory = dynamicMemberFactory;
    }

    public void add(String name, ISyncObject sync, SyncVisibility visibility) {
        add(name, sync, visibility, false);
    }

    public void add(String name, ISyncObject sync, SyncVisibility visibility, boolean setDirty) {
        MemberEntry entry = MemberEntry.syncObject(sync, visibility, setDirty);

        members.put(name, entry);
        sync.setSyncUpdateNotifier(() -> {
            if (entry.dirty) {
                return;
            }
            entry.dirty = true;
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
        MemberEntry entry = MemberEntry.childGroup(childGroup);
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
            Tag childTag = null;
            if (member.object != null) {
                if (member.dirty && member.isObjectVisible(visibility)) {
                    childTag = member.writeDirtyValue(context, visibility);
                }
            } else if (member.childGroup != null && member.childGroup.isDirty(visibility)) {
                childTag = member.childGroup.writeDirtyValue(context, visibility);
            }
            if (childTag != null) {
                outputTag.put(entry.getKey(), childTag);
            }
        }

        dirty.clear();
        return outputTag.isEmpty() ? null : outputTag;
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
            Tag childTag = null;
            if (member.object != null) {
                if (member.isObjectVisible(visibility)) {
                    childTag = member.writeFullValue(context, visibility);
                }
            } else if (member.childGroup != null) {
                childTag = member.childGroup.writeFullValue(context, visibility);
            }
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
