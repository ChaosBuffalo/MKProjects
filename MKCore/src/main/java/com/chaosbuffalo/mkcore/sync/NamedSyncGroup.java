package com.chaosbuffalo.mkcore.sync;

import net.minecraft.nbt.CompoundTag;

public class NamedSyncGroup extends SyncGroup {
    private final String groupName;

    public NamedSyncGroup(String name) {
        groupName = name;
    }

    @Override
    protected CompoundTag extractGroupTag(CompoundTag parentTag) {
        return parentTag.getCompound(groupName);
    }

    @Override
    protected void insertGroupTag(CompoundTag parentTag, CompoundTag groupTag) {
        parentTag.put(groupName, groupTag);
    }

    @Override
    public String toString() {
        return String.format("NamedSyncGroup[name='%s', components=%d, dirty=%d]", groupName, components.size(), dirty.size());
    }
}
