package com.chaosbuffalo.mkcore.core.combat;

import net.minecraft.util.Mth;

import java.util.Arrays;

public class VisualMeleeAttackSequence {
    private int[] swingStartTicks = new int[0];
    private int[] swingDurationTicks = new int[0];
    private int sequenceTick;
    private int nextSwingIndex;
    private int activeSwingIndex = -1;
    private int activeSwingStartTick = -1;
    private int localSwingVariant;
    private boolean swingStartedThisTick;

    public void start(int[] swingStartTicks, int[] swingDurationTicks) {
        int swingCount = Math.min(swingStartTicks.length, swingDurationTicks.length);
        if (swingCount <= 0) {
            clear();
            return;
        }
        this.swingStartTicks = Arrays.copyOf(swingStartTicks, swingCount);
        this.swingDurationTicks = Arrays.copyOf(swingDurationTicks, swingCount);
        this.sequenceTick = 0;
        this.nextSwingIndex = 0;
        this.activeSwingIndex = -1;
        this.activeSwingStartTick = -1;
        this.swingStartedThisTick = false;
        startDueSwings();
    }

    public void tick() {
        if (!hasSequence()) {
            return;
        }
        startDueSwings();
        sequenceTick++;
        if (nextSwingIndex >= swingStartTicks.length &&
                activeSwingStartTick >= 0 && sequenceTick > activeSwingStartTick + getActiveSwingDurationTicks()) {
            clear();
        }
    }

    private void startDueSwings() {
        while (nextSwingIndex < swingStartTicks.length && sequenceTick >= swingStartTicks[nextSwingIndex]) {
            activeSwingIndex = nextSwingIndex;
            activeSwingStartTick = swingStartTicks[nextSwingIndex];
            localSwingVariant++;
            swingStartedThisTick = true;
            nextSwingIndex++;
        }
    }

    public boolean consumeSwingStartedThisTick() {
        boolean started = swingStartedThisTick;
        swingStartedThisTick = false;
        return started;
    }

    public boolean hasSequence() {
        return swingStartTicks.length > 0 && swingDurationTicks.length > 0;
    }

    public float getAttackAnim(float partialTicks) {
        if (!hasSequence() || activeSwingStartTick < 0) {
            return 0.0F;
        }
        int duration = getActiveSwingDurationTicks();
        float swingTime = sequenceTick - activeSwingStartTick + partialTicks;
        if (swingTime <= 0.0F || swingTime >= duration) {
            return 0.0F;
        }
        return Mth.clamp(swingTime / duration, 0.0F, 1.0F);
    }

    public int getLocalSwingVariant() {
        return localSwingVariant;
    }

    public void clear() {
        swingStartTicks = new int[0];
        swingDurationTicks = new int[0];
        sequenceTick = 0;
        nextSwingIndex = 0;
        activeSwingIndex = -1;
        activeSwingStartTick = -1;
        swingStartedThisTick = false;
    }

    private int getActiveSwingDurationTicks() {
        if (activeSwingIndex < 0 || activeSwingIndex >= swingDurationTicks.length) {
            return 1;
        }
        return Math.max(1, swingDurationTicks[activeSwingIndex]);
    }
}
