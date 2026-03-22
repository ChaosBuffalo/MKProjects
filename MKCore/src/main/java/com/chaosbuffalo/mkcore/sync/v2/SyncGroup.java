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
    private final Map<String, ISyncUpdatableBase> members = new HashMap<>();
    private final EnumMap<SyncVisibility, Map<String, ISyncUpdatableBase>> dirtyMembers = new EnumMap<>(SyncVisibility.class);
    private DynamicObjectFactoryFunction dynamicMemberFactory = null;
    private Consumer<SyncVisibility> parentNotifier = vis -> {};

    public interface DynamicObjectFactoryFunction {
        ISyncUpdatableBase createSyncObject(String key, Tag valueTag, SyncVisibility visibility);
    }

    private static final class SyncMemberInfo implements ISyncUpdatableBase {
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
            return object.writeFullValue(context, visibility);
        }

        @Override
        public @Nullable Tag writeDirtyValue(SyncContext context, SyncVisibility visibility) {
            var tag = object.writeDirtyValue(context, visibility);
            dirty = false;
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
        SyncMemberInfo entry = new SyncMemberInfo(sync, visibility, setDirty);

        members.put(name, entry);
        sync.setSyncUpdateNotifier(() -> {
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

    private void markMemberDirty(String name, ISyncUpdatableBase member, SyncVisibility visibility) {
        dirtyMembers.computeIfAbsent(visibility, k -> new HashMap<>()).put(name, member);
        onMemberUpdated(visibility);
    }

    protected void onMemberUpdated(SyncVisibility visibility) {
        parentNotifier.accept(visibility);
    }

    public void addChild(String name, SyncGroup childGroup) {
        members.put(name, childGroup);

        childGroup.parentNotifier = vis -> markMemberDirty(name, childGroup, vis);

        // If the group being added was already dirty, reflect that in ourselves
        childGroup.dirtyMembers.forEach((vis, dirty) -> {
            if (!dirty.isEmpty()) {
                markMemberDirty(name, childGroup, vis);
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

            ISyncUpdatableBase existingMember = members.get(key);

            if (existingMember != null) {
                existingMember.handleUpdatePayload(context, memberTag, visibility);
            } else if (dynamicMemberFactory != null) {
                ISyncUpdatableBase newObject = dynamicMemberFactory.createSyncObject(key, memberTag, visibility);
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
        Map<String, ISyncUpdatableBase> dirty = dirtyMembers.get(visibility);
        if (dirty == null || dirty.isEmpty()) {
            return null;
        }

        CompoundTag outputTag = new CompoundTag();
        for (var entry : dirty.entrySet()) {
            Tag childTag = switch (entry.getValue()) {
                case SyncMemberInfo smi when smi.dirty && smi.matches(visibility) ->
                        smi.writeDirtyValue(context, visibility);
                case SyncGroup childGroup when childGroup.isDirty(visibility) ->
                        childGroup.writeDirtyValue(context, visibility);
                default -> null;
            };
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
        for (Map.Entry<String, ISyncUpdatableBase> entry : members.entrySet()) {
            Tag childTag = switch (entry.getValue()) {
                case SyncMemberInfo smi when smi.matches(visibility) ->
                        smi.writeFullValue(context, visibility);
                case SyncGroup childGroup ->
                        childGroup.writeFullValue(context, visibility);
                default -> null;
            };
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
