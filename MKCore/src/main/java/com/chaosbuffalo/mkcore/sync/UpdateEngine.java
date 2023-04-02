package com.chaosbuffalo.mkcore.sync;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public abstract class UpdateEngine {
    protected final SyncGroup publicUpdater = new SyncGroup();
    protected boolean readyForUpdates = false;

    public void addPublic(ISyncObject syncObject) {
        publicUpdater.add(syncObject);
        if (syncObject instanceof SyncGroup) {
            ((SyncGroup) syncObject).forceDirty();
        }
    }

    public void removePublic(ISyncObject syncObject) {
        publicUpdater.remove(syncObject);
    }

    public void addPrivate(ISyncObject syncObject) {
    }

    public void removePrivate(ISyncObject syncObject) {
    }

    public abstract void syncUpdates();

    public abstract void serializeUpdate(CompoundTag updateTag, boolean fullSync, boolean privateUpdate);

    public abstract void deserializeUpdate(CompoundTag updateTag, boolean privateUpdate);

    public abstract void sendAll(ServerPlayer otherPlayer);


}
