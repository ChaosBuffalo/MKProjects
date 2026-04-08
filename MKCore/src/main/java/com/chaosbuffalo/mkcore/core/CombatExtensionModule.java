package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.core.combat.MeleeHandState;
import com.chaosbuffalo.mkcore.utils.EntityUtils;
import net.minecraft.world.InteractionHand;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class CombatExtensionModule {
    private static final int COMBAT_TIMEOUT = GameConstants.TICKS_PER_SECOND * 8;
    private static final int PROJECTILE_COMBO_TIMEOUT = GameConstants.TICKS_PER_SECOND * 30;
    private final IMKEntityData entityData;
    private final Set<String> spellTag = new HashSet<>();
    private final EnumMap<InteractionHand, MeleeHandState> handStates = new EnumMap<>(InteractionHand.class);
    private int lastSwingHitTick;
    private int currentSwingCount;
    private int lastProjectileHitTick;
    private int currentProjectileHitCount;
    private InteractionHand activeAttackHand = InteractionHand.MAIN_HAND;

    public CombatExtensionModule(IMKEntityData entityData) {
        this.entityData = entityData;
        handStates.put(InteractionHand.MAIN_HAND, new MeleeHandState(InteractionHand.MAIN_HAND));
        handStates.put(InteractionHand.OFF_HAND, new MeleeHandState(InteractionHand.OFF_HAND));
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
        return getAttackStrengthTicks(activeAttackHand);
    }

    public void setAttackStrengthTicks(int newTicks) {
        setAttackStrengthTicks(activeAttackHand, newTicks);
    }

    public void increaseAttackStrengthTicks(int toAdd) {
        increaseAttackStrengthTicks(activeAttackHand, toAdd);
    }

    public int getAttackStrengthTicks(InteractionHand hand) {
        return getHandState(hand).getAttackStrengthTicker();
    }

    public int getRequiredAttackStrengthTicks(InteractionHand hand) {
        return (int) Math.round(EntityUtils.getCooldownPeriod(getEntityData().getEntity()));
    }

    public void setAttackStrengthTicks(InteractionHand hand, int newTicks) {
        getHandState(hand).setAttackStrengthTicker(newTicks);
        if (hand == InteractionHand.MAIN_HAND) {
            getEntityData().getEntity().attackStrengthTicker = newTicks;
        }
    }

    public void increaseAttackStrengthTicks(InteractionHand hand, int toAdd) {
        getHandState(hand).increaseAttackStrengthTicker(toAdd);
        if (hand == InteractionHand.MAIN_HAND) {
            getEntityData().getEntity().attackStrengthTicker += toAdd;
        }
    }

    public void tickAttackStrengthTicks() {
        for (MeleeHandState handState : handStates.values()) {
            handState.tickAttackStrengthTicker();
        }
    }

    public MeleeHandState getHandState(InteractionHand hand) {
        return handStates.get(hand);
    }

    public InteractionHand getActiveAttackHand() {
        return activeAttackHand;
    }

    public void setActiveAttackHand(InteractionHand activeAttackHand) {
        this.activeAttackHand = activeAttackHand;
    }

    public <T> T executeWithAttackHand(InteractionHand hand, Supplier<T> action) {
        InteractionHand previousHand = activeAttackHand;
        activeAttackHand = hand;
        try {
            return action.get();
        } finally {
            activeAttackHand = previousHand;
        }
    }

    public void executeWithAttackHand(InteractionHand hand, Runnable action) {
        executeWithAttackHand(hand, () -> {
            action.run();
            return null;
        });
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
