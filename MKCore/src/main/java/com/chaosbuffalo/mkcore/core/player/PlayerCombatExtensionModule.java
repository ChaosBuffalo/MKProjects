package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.core.CombatExtensionModule;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.sync.SyncInt;

import java.util.HashSet;
import java.util.Set;

public class PlayerCombatExtensionModule extends CombatExtensionModule implements IPlayerSyncComponentProvider {
    private final SyncComponent sync = new SyncComponent("combatExtension");
    private final SyncInt currentProjectileHitCount = new SyncInt("projectileHits", 0);
    private final Set<String> spellTag = new HashSet<>();

    public PlayerCombatExtensionModule(IMKEntityData entityData) {
        super(entityData);
        addSyncPrivate(currentProjectileHitCount);
    }

    @Override
    public SyncComponent getSyncComponent() {
        return sync;
    }

    public int getCurrentProjectileHitCount() {
        return currentProjectileHitCount.get();
    }

    @Override
    public void setCurrentProjectileHitCount(int currentProjectileHitCount) {
        this.currentProjectileHitCount.set(currentProjectileHitCount);
    }

    public void addSpellTag(String tag) {
        spellTag.add(tag);
    }

    public void removeSpellTag(String tag) {
        spellTag.remove(tag);
    }

    public boolean hasSpellTag(String tag) {
        return spellTag.contains(tag);
    }

}
