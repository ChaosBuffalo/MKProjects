package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.GameConstants;

public class CombatExtensionModule {
    private static final int COMBAT_TIMEOUT = GameConstants.TICKS_PER_SECOND * 8;
    private static final int PROJECTILE_COMBO_TIMEOUT = GameConstants.TICKS_PER_SECOND * 30;
    private final IMKEntityData entityData;
    private int lastSwingHitTick;
    private int currentSwingCount;
    private int lastProjectileHitTick;
    private int currentProjectileHitCount;

    public CombatExtensionModule(IMKEntityData entityData) {
        this.entityData = entityData;
        lastSwingHitTick = 0;
        currentSwingCount = 0;
        lastProjectileHitTick = 0;
        currentProjectileHitCount = 0;
    }

    public IMKEntityData getEntityData() {
        return entityData;
    }

    public void tick() {
        if (isMidMeleeCombo() && getTicksSinceSwingHit() >= COMBAT_TIMEOUT) {
            setCurrentSwingCount(0);
        }
        if (isMidProjectileCombo() && getTicksSinceProjectileHit() >= PROJECTILE_COMBO_TIMEOUT) {
            setCurrentProjectileHitCount(0);
        }
    }

    public int getTicksSinceSwingHit() {
        return entityData.getEntity().tickCount - lastSwingHitTick;
    }

    public int getTicksSinceProjectileHit() {
        return entityData.getEntity().tickCount - lastProjectileHitTick;
    }

    public int getAttackStrengthTicks() {
        return getEntityData().getEntity().attackStrengthTicker;
    }

    public void setAttackStrengthTicks(int newTicks) {
        getEntityData().getEntity().attackStrengthTicker = newTicks;
    }

    public void increaseAttackStrengthTicks(int toAdd) {
        getEntityData().getEntity().attackStrengthTicker += toAdd;
    }

    public void recordSwingHit() {
        lastSwingHitTick = entityData.getEntity().tickCount;
        setCurrentSwingCount(getCurrentSwingCount() + 1);
    }

    public boolean isMidMeleeCombo() {
        return getCurrentSwingCount() > 0;
    }

    public int getCurrentSwingCount() {
        return currentSwingCount;
    }

    public void setCurrentSwingCount(int currentSwingCount) {
        this.currentSwingCount = currentSwingCount;
    }

    public int getCurrentProjectileHitCount() {
        return currentProjectileHitCount;
    }

    public void setCurrentProjectileHitCount(int currentProjectileHitCount) {
        this.currentProjectileHitCount = currentProjectileHitCount;
    }

    public void recordProjectileHit() {
        lastProjectileHitTick = entityData.getEntity().tickCount;
        setCurrentProjectileHitCount(getCurrentProjectileHitCount() + 1);
    }

    public void projectileMiss() {
        setCurrentProjectileHitCount(0);
    }

    public boolean isMidProjectileCombo() {
        return getCurrentProjectileHitCount() > 0;
    }
}
