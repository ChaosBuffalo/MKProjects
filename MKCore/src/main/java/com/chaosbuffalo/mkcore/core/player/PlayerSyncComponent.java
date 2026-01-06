package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import com.chaosbuffalo.mkcore.sync.controllers.SyncController;
import com.chaosbuffalo.mkcore.sync.v2.ISyncObject;
import com.chaosbuffalo.mkcore.sync.v2.SyncGroup;
import net.minecraft.nbt.Tag;

public class PlayerSyncComponent {
    private final SyncGroup syncGroup;

    public interface DynamicComponentFactory {
        PlayerSyncComponent createComponent(String key, Tag valueTag, SyncVisibility visibility);
    }

    public PlayerSyncComponent() {
        syncGroup = new SyncGroup();
    }

    public void setDynamicMemberFactory(DynamicComponentFactory factory) {
        syncGroup.setDynamicMemberFactory((key, tag, v) -> {
            var component = factory.createComponent(key, tag, v);
            return component != null ? component.syncGroup : null;
        });
    }

    public void attach(String name, SyncController engine) {
        engine.addGroup(name, syncGroup);
    }

    public void addChild(String name, PlayerSyncComponent component) {
        syncGroup.addGroup(name, component.syncGroup);
    }

    public void addChild(String name, SyncGroup component) {
        syncGroup.addGroup(name, component);
    }

    public void addPublic(String name, ISyncObject syncObject) {
        syncGroup.add(name, syncObject, SyncVisibility.Public);
    }

    public void addPrivate(String name, ISyncObject syncObject) {
        syncGroup.add(name, syncObject, SyncVisibility.Private);
    }

    public void addMember(String name, ISyncObject syncObject, SyncVisibility visibility) {
        syncGroup.add(name, syncObject, visibility);
    }
}
