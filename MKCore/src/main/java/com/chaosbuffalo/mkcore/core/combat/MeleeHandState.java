package com.chaosbuffalo.mkcore.core.combat;

import net.minecraft.world.InteractionHand;

public class MeleeHandState {
    private final InteractionHand hand;
    private int attackStrengthTicker;
    private int localSwingVariant;
    private int queuedTargetId = -1;
    private final VisualMeleeAttackSequence visualMeleeAttackSequence = new VisualMeleeAttackSequence();

    public MeleeHandState(InteractionHand hand) {
        this.hand = hand;
    }

    public InteractionHand getHand() {
        return hand;
    }

    public int getAttackStrengthTicker() {
        return attackStrengthTicker;
    }

    public void setAttackStrengthTicker(int attackStrengthTicker) {
        this.attackStrengthTicker = attackStrengthTicker;
    }

    public void increaseAttackStrengthTicker(int toAdd) {
        attackStrengthTicker += toAdd;
    }

    public void tickAttackStrengthTicker() {
        attackStrengthTicker++;
    }

    public int getLocalSwingVariant() {
        return localSwingVariant;
    }

    public void incrementLocalSwingVariant() {
        localSwingVariant++;
    }

    public int getQueuedTargetId() {
        return queuedTargetId;
    }

    public void setQueuedTargetId(int queuedTargetId) {
        this.queuedTargetId = queuedTargetId;
    }

    public void clearQueuedTarget() {
        queuedTargetId = -1;
    }

    public VisualMeleeAttackSequence getVisualMeleeAttackSequence() {
        return visualMeleeAttackSequence;
    }
}
