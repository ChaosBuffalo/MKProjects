package com.chaosbuffalo.mkcore.sync.v2;

import com.chaosbuffalo.mkcore.sync.SyncContext;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SyncGroup implements ISyncUpdatableBase {
    private final Map<String, ISyncUpdatableBase> members = new HashMap<>();
    private final EnumSet<SyncVisibility> dirtySet = EnumSet.noneOf(SyncVisibility.class);
    private DynamicObjectFactoryFunction dynamicMemberFactory = null;
    private SyncGroup parentGroup;

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
            onMemberUpdated(visibility);
        });
        if (setDirty) {
            onMemberUpdated(visibility);
        }
    }

    public void addPublic(String name, ISyncObject sync) {
        add(name, sync, SyncVisibility.Public);
    }

    public void addPrivate(String name, ISyncObject sync) {
        add(name, sync, SyncVisibility.Private);
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

    public void addChild(String name, SyncGroup childGroup) {
        members.put(name, childGroup);

        childGroup.setParentGroup(this);

        // If the group being added was already dirty, reflect that in ourselves
        childGroup.dirtySet.forEach(this::onMemberUpdated);
    }

    public void addChild(String name, ISyncGroupProvider provider) {
        addChild(name, provider.getSyncGroup());
    }

    public boolean isDirty(SyncVisibility visibility) {
        return dirtySet.contains(visibility);
    }

    @Override
    public void clearDirty() {
        members.values().forEach(ISyncUpdatableBase::clearDirty);
        dirtySet.clear();
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


    private interface MemberVisitor {
        void visitMember(String key, SyncMemberInfo member);

        void visitGroup(String key, SyncGroup member);
    }

    private void visitMembers(MemberVisitor visitor) {
        for (Map.Entry<String, ISyncUpdatableBase> entry : members.entrySet()) {
            ISyncUpdatableBase member = entry.getValue();
            switch (member) {
                case SyncMemberInfo smi -> {
                    visitor.visitMember(entry.getKey(), smi);
                }
                case SyncGroup childGroup -> {
                    visitor.visitGroup(entry.getKey(), childGroup);
                }
                default -> {
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
        visitMembers(new MemberVisitor() {

            @Override
            public void visitMember(String key, SyncMemberInfo member) {
                if (member.dirty && member.matches(visibility)) {
                    Tag childTag = member.writeDirtyValue(context, visibility);
                    if (childTag != null) {
                        outputTag.put(key, childTag);
                    }
                }
            }

            @Override
            public void visitGroup(String key, SyncGroup childGroup) {
                if (childGroup.isDirty(visibility)) {
                    Tag childTag = childGroup.writeDirtyValue(context, visibility);
                    if (childTag != null) {
                        outputTag.put(key, childTag);
                    }
                }
            }
        });

        dirtySet.remove(visibility);
        return outputTag.isEmpty() ? null : outputTag;
    }

    @Override
    public @Nullable CompoundTag writeFullValue(SyncContext context, SyncVisibility visibility) {
        // Early return if no children or members
        if (members.isEmpty()) {
            return null;
        }

        CompoundTag groupTag = new CompoundTag();
        visitMembers(new MemberVisitor() {

            @Override
            public void visitMember(String key, SyncMemberInfo member) {
                if (member.matches(visibility)) {
                    Tag childTag = member.writeFullValue(context, visibility);
                    if (childTag != null) {
                        groupTag.put(key, childTag);
                    }
                }
            }

            @Override
            public void visitGroup(String key, SyncGroup childGroup) {
                Tag childTag = childGroup.writeFullValue(context, visibility);
                if (childTag != null) {
                    groupTag.put(key, childTag);
                }
            }
        });

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
