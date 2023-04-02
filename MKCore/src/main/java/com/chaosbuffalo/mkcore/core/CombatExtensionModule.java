package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.GameConstants;

public class CombatExtensionModule {
    private static final int COMBAT_TIMEOUT = GameConstants.TICKS_PER_SECOND * 8;
    private static final int PROJECTILE_COMBO_TIMEOUT = GameConstants.TICKS_PER_SECOND * 30;

    private int ticksSinceSwing;
    private int currentSwingCount;
    private int currentProjectileHitCount;
    private int ticksSinceProjectileHit;
    private boolean midCombo;
    private boolean midProjectileCombo;
    private final IMKEntityData entityData;

    public CombatExtensionModule(IMKEntityData entityData) {
        this.entityData = entityData;
        ticksSinceSwing = 0;
        currentSwingCount = 0;
        midCombo = false;
        midProjectileCombo = false;
        currentProjectileHitCount = 0;
        ticksSinceProjectileHit = 0;
    }

    public IMKEntityData getEntityData() {
        return entityData;
    }

    public void tick() {
        incrementTicksSinceSwing();
        incrementTicksSinceProjectileHit();
        if (midCombo && getTicksSinceSwing() >= COMBAT_TIMEOUT) {
            setCurrentSwingCount(0);
            midCombo = false;
        }
        if (midProjectileCombo && ticksSinceProjectileHit >= PROJECTILE_COMBO_TIMEOUT) {
            setCurrentProjectileHitCount(0);
            midProjectileCombo = false;
        }
    }

    public void setCurrentProjectileHitCount(int currentProjectileHitCount) {
        this.currentProjectileHitCount = currentProjectileHitCount;
    }

    public void setCurrentSwingCount(int currentSwingCount) {
        this.currentSwingCount = currentSwingCount;
    }

    protected void incrementTicksSinceSwing() {
        ticksSinceSwing++;
    }

    public int getTicksSinceSwing() {
        return ticksSinceSwing;
    }

    public int getTicksSinceProjectileHit() {
        return ticksSinceProjectileHit;
    }

    protected void incrementTicksSinceProjectileHit() {
        ticksSinceProjectileHit++;
    }

    public void setEntityTicksSinceLastSwing(int newTicks) {
        getEntityData().getEntity().attackStrengthTicker = newTicks;
    }

    public int getEntityTicksSinceLastSwing() {
        return getEntityData().getEntity().attackStrengthTicker;
    }

    public void addEntityTicksSinceLastSwing(int toAdd) {
        getEntityData().getEntity().attackStrengthTicker += toAdd;
    }

    public void recordSwing() {
        ticksSinceSwing = 0;
        setCurrentSwingCount(getCurrentSwingCount() + 1);
        midCombo = true;
    }

    public void recordProjectileHit() {
        midProjectileCombo = true;
        setCurrentProjectileHitCount(getCurrentProjectileHitCount() + 1);
        ticksSinceProjectileHit = 0;
    }

    public void projectileMiss() {
        midProjectileCombo = false;
        setCurrentProjectileHitCount(0);
    }

    public boolean isMidCombo() {
        return midCombo;
    }

    public int getCurrentSwingCount() {
        return currentSwingCount;
    }

    public int getCurrentProjectileHitCount() {
        return currentProjectileHitCount;
    }

    public boolean isMidProjectileCombo() {
        return midProjectileCombo;
    }
}
