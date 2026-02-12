package com.chaosbuffalo.mkcore.sync.controllers;

import com.chaosbuffalo.mkcore.sync.SyncContext;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import com.chaosbuffalo.mkcore.sync.v2.ISyncGroupProvider;
import com.chaosbuffalo.mkcore.sync.v2.ISyncObject;
import com.chaosbuffalo.mkcore.sync.v2.SyncGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.EnumSet;
import java.util.Set;

public interface SyncController {
    EnumSet<SyncVisibility> DEFAULT_VISIBILITY = EnumSet.of(SyncVisibility.Public);

    default Set<SyncVisibility> supportedVisibilities() {
        return DEFAULT_VISIBILITY;
    }

    void add(String name, ISyncObject syncObject, SyncVisibility visibility);

    void addChild(String name, SyncGroup group);

    default void addChild(String name, ISyncGroupProvider provider) {
        addChild(name, provider.getSyncGroup());
    }

    void applyRemoteUpdate(SyncContext context, CompoundTag updateTag, SyncVisibility visibility);

    boolean syncUpdates();

    void sendFullSync(ServerPlayer otherPlayer);
}
