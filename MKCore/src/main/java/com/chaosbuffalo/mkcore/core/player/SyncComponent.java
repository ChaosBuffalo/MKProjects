package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.sync.ISyncObject;
import com.chaosbuffalo.mkcore.sync.SyncGroup;
import com.chaosbuffalo.mkcore.sync.UpdateEngine;

public class SyncComponent {

    private final SyncGroup publicUpdater;
    private final SyncGroup privateUpdater;

    public SyncComponent(String name) {
        publicUpdater = new SyncGroup(name);
        privateUpdater = new SyncGroup(name);
    }

    public void attach(UpdateEngine engine) {
        engine.addPublic(publicUpdater);
        engine.addPrivate(privateUpdater);
    }

    public void detach(UpdateEngine engine) {
        engine.removePublic(publicUpdater);
        engine.removePrivate(privateUpdater);
    }

    public void addChild(SyncComponent component) {
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
