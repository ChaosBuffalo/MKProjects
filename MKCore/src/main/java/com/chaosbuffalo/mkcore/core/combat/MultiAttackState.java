package com.chaosbuffalo.mkcore.core.combat;

import net.minecraft.world.InteractionHand;

public class MultiAttackState {
    private final InteractionHand hand;
    private int attackCount = 1;
    private int nextIndex = 1;
    private int sequenceTick;
    private int cooldownTicks;
    private int[] startTicks = new int[0];
    private boolean executing;

    public MultiAttackState(InteractionHand hand) {
        this.hand = hand;
    }

    public InteractionHand getHand() {
        return hand;
    }

    public int getAttackCount() {
        return attackCount;
    }

    public void setAttackCount(int attackCount) {
        this.attackCount = attackCount;
    }

    public int getNextIndex() {
        return nextIndex;
    }

    public void setNextIndex(int nextIndex) {
        this.nextIndex = nextIndex;
    }

    public int getSequenceTick() {
        return sequenceTick;
    }

    public void setSequenceTick(int sequenceTick) {
        this.sequenceTick = sequenceTick;
    }

    public void incrementSequenceTick() {
        sequenceTick++;
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    public void setCooldownTicks(int cooldownTicks) {
        this.cooldownTicks = cooldownTicks;
    }

    public int[] getStartTicks() {
        return startTicks;
    }

    public void setStartTicks(int[] startTicks) {
        this.startTicks = startTicks;
    }

    public boolean isExecuting() {
        return executing;
    }

    public void setExecuting(boolean executing) {
        this.executing = executing;
    }

    public boolean hasPendingAttack() {
        return nextIndex < attackCount;
    }

    public void reset() {
        attackCount = 1;
        nextIndex = 1;
        sequenceTick = 0;
        cooldownTicks = 0;
        startTicks = new int[0];
        executing = false;
    }
}
