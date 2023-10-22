package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.sync.ISyncObject;
import com.chaosbuffalo.mkcore.sync.SyncGroup;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import com.chaosbuffalo.mkcore.sync.controllers.SyncController;

public class PlayerSyncComponent {

    private final SyncGroup publicUpdater;
    private final SyncGroup privateUpdater;

    public PlayerSyncComponent(String name) {
        publicUpdater = new SyncGroup(name);
        privateUpdater = new SyncGroup(name);
    }

    public void attach(SyncController engine) {
        engine.add(publicUpdater, SyncVisibility.Public);
        engine.add(privateUpdater, SyncVisibility.Private);
    }

    public void detach(SyncController engine) {
        engine.remove(publicUpdater, SyncVisibility.Public);
        engine.remove(privateUpdater, SyncVisibility.Private);
    }

    public void addChild(PlayerSyncComponent component) {
        addPublic(component.publicUpdater);
        addPrivate(component.privateUpdater);
    }

    public void addPublic(ISyncObject syncObject) {
        publicUpdater.add(syncObject);
    }

    public void addPrivate(ISyncObject syncObject) {
        addPrivate(syncObject, false);
    }

    public void addPrivate(ISyncObject syncObject, boolean forceUpdate) {
        privateUpdater.add(syncObject);
        if (forceUpdate) {
            privateUpdater.forceDirty();
        }
    }
}
