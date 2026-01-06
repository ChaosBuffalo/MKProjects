package com.chaosbuffalo.mkcore.sync.controllers;

import com.chaosbuffalo.mkcore.sync.SyncContext;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import com.chaosbuffalo.mkcore.sync.v2.ISyncObject;
import com.chaosbuffalo.mkcore.sync.v2.SyncGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.EnumSet;
import java.util.Set;

public abstract class SyncController {
    private static final EnumSet<SyncVisibility> DEFAULT_VISIBILITY = EnumSet.of(SyncVisibility.Public);
    protected final SyncGroup rootGroup = createRootGroup();

    protected Set<SyncVisibility> supportedVisibilities() {
        return DEFAULT_VISIBILITY;
    }

    protected SyncGroup createRootGroup() {
        return new SyncGroup();
    }

    protected SyncGroup getRootGroup() {
        return rootGroup;
    }

    public void add(String name, ISyncObject syncObject, SyncVisibility visibility) {
        rootGroup.add(name, syncObject, visibility);
    }

    public void addGroup(String name, SyncGroup group) {
        rootGroup.addGroup(name, group);
    }

    public void remove(String name, ISyncObject syncObject, SyncVisibility visibility) {
        rootGroup.remove(name, syncObject, visibility);
    }

    public void deserializeUpdate(SyncContext context, CompoundTag updateTag, SyncVisibility visibility) {
        rootGroup.handleUpdatePayload(context, updateTag, visibility);
    }

    public abstract boolean syncUpdates();

    public abstract void sendFullSync(ServerPlayer otherPlayer);
}
