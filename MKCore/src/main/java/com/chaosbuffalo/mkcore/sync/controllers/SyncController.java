package com.chaosbuffalo.mkcore.sync.controllers;

import com.chaosbuffalo.mkcore.sync.ISyncObject;
import com.chaosbuffalo.mkcore.sync.SyncGroup;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public abstract class SyncController {
    private static final EnumSet<SyncVisibility> DEFAULT_VISIBILITY = EnumSet.of(SyncVisibility.Public);
    protected final Map<SyncVisibility, SyncGroup> rootGroups = new EnumMap<>(SyncVisibility.class);

    protected Set<SyncVisibility> supportedVisibilities() {
        return DEFAULT_VISIBILITY;
    }

    protected SyncGroup createGroup(SyncVisibility visibility) {
        return new SyncGroup();
    }

    protected SyncGroup getVisibilityGroup(SyncVisibility visibility) {
        return rootGroups.computeIfAbsent(visibility, this::createGroup);
    }

    public void add(ISyncObject syncObject, SyncVisibility visibility) {
        getVisibilityGroup(visibility).add(syncObject);
    }

    public void remove(ISyncObject syncObject, SyncVisibility visibility) {
        getVisibilityGroup(visibility).remove(syncObject);
    }

    public void deserializeUpdate(CompoundTag updateTag, Set<SyncVisibility> visibility) {
        visibility.forEach(v -> getVisibilityGroup(v).deserializeUpdate(updateTag));
    }

    public abstract void syncUpdates();

    public abstract void sendFullSync(ServerPlayer otherPlayer);
}
