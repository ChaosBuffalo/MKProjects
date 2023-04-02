package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.core.CombatExtensionModule;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.sync.SyncInt;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class PlayerCombatExtensionModule extends CombatExtensionModule implements IPlayerSyncComponentProvider {
    private final SyncComponent sync = new SyncComponent("combatExtension");
    private final SyncInt currentProjectileHitCount = new SyncInt("projectileHits", 0);
    private final Set<String> spellTag = new HashSet<>();
    @Nullable
    private Entity pointedEntity;

    public PlayerCombatExtensionModule(IMKEntityData entityData) {
        super(entityData);
        addSyncPrivate(currentProjectileHitCount);
        pointedEntity = null;
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

    public void setPointedEntity(@Nullable Entity pointedEntity) {
        this.pointedEntity = pointedEntity;
    }


    public Optional<Entity> getPointedEntity() {
        return pointedEntity != null ? Optional.of(pointedEntity) : Optional.empty();
    }
}
