package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.sync.ISyncObject;
import com.chaosbuffalo.mkcore.sync.SyncGroup;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import com.chaosbuffalo.mkcore.sync.controllers.SyncController;

public class PlayerSyncComponent {

    private final SyncGroup publicUpdater;
    private final SyncGroup privateUpdater;
    private final String name;

    public PlayerSyncComponent(String name) {
        this.name = name;
        publicUpdater = new SyncGroup();
        privateUpdater = new SyncGroup();
    }

    public void attach(SyncController engine) {
        engine.add(name, publicUpdater, SyncVisibility.Public);
        engine.add(name, privateUpdater, SyncVisibility.Private);
    }

    public void detach(SyncController engine) {
        engine.remove(name, publicUpdater, SyncVisibility.Public);
        engine.remove(name, privateUpdater, SyncVisibility.Private);
    }

    public void addChild(String name, PlayerSyncComponent component) {
        addPublic(name, component.publicUpdater);
        addPrivate(name, component.privateUpdater);
    }

    public void addPublic(String name, ISyncObject syncObject) {
        publicUpdater.add(name, syncObject);
    }

    public void addPrivate(String name, ISyncObject syncObject) {
        privateUpdater.add(name, syncObject);
    }
}
