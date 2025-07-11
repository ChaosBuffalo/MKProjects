package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.sync.ISyncObject;

public interface IPlayerSyncComponentProvider {
    PlayerSyncComponent getSyncComponent();

    default void addSyncChild(String name, IPlayerSyncComponentProvider syncComponent) {
        getSyncComponent().addChild(name, syncComponent.getSyncComponent());
    }

    default void addSyncPrivate(String name, ISyncObject component) {
        getSyncComponent().addPrivate(name, component);
    }

    default void addSyncPublic(String name, ISyncObject component) {
        getSyncComponent().addPublic(name, component);
    }
}
