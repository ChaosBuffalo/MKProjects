package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.sync.ISyncObject;

public interface IPlayerSyncComponentProvider {
    SyncComponent getSyncComponent();

    default void addSyncChild(IPlayerSyncComponentProvider syncComponent) {
        getSyncComponent().addChild(syncComponent.getSyncComponent());
    }

    default void addSyncPrivate(ISyncObject component) {
        getSyncComponent().addPrivate(component);
    }

    default void addSyncPublic(ISyncObject component) {
        getSyncComponent().addPublic(component);
    }
}
