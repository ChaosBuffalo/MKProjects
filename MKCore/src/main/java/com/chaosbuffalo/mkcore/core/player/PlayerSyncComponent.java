package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.sync.ISyncObject;
import com.chaosbuffalo.mkcore.sync.SyncGroup;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import com.chaosbuffalo.mkcore.sync.controllers.SyncController;
import net.minecraft.nbt.Tag;

public class PlayerSyncComponent {

    private final SyncGroup publicUpdater;
    private final SyncGroup privateUpdater;

    public interface UnhandledChildFunction {
        PlayerSyncComponent handle(String key, Tag valueTag, SyncVisibility visibility);
    }

    public PlayerSyncComponent() {
        publicUpdater = new SyncGroup();
        privateUpdater = new SyncGroup();
    }

    public void setHandlerFunction(UnhandledChildFunction function) {
        privateUpdater.setUnhandledKeyHandler((childName, valueTag) -> {
            var sync = function.handle(childName, valueTag, SyncVisibility.Private);
            return sync != null ? sync.privateUpdater : null;
        });
        publicUpdater.setUnhandledKeyHandler((childName, valueTag) -> {
            var sync = function.handle(childName, valueTag, SyncVisibility.Public);
            return sync != null ? sync.publicUpdater : null;
        });
    }

    public void attach(String name, SyncController engine) {
        engine.add(name, publicUpdater, SyncVisibility.Public);
        engine.add(name, privateUpdater, SyncVisibility.Private);
    }

//    public void detach(SyncController engine) {
//        engine.remove(name, publicUpdater, SyncVisibility.Public);
//        engine.remove(name, privateUpdater, SyncVisibility.Private);
//    }

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
