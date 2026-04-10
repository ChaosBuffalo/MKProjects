package com.chaosbuffalo.mkcore.core.combat;

import net.minecraft.util.Mth;

import java.util.Arrays;

public class VisualMeleeAttackSequence {
    private int[] swingStartTicks = new int[0];
    private int swingDurationTicks;
    private int sequenceTick;
    private int nextSwingIndex;
    private int activeSwingStartTick = -1;
    private int localSwingVariant;
    private boolean swingStartedThisTick;

    public void start(int[] swingStartTicks, int swingDurationTicks) {
        this.swingStartTicks = Arrays.copyOf(swingStartTicks, swingStartTicks.length);
        this.swingDurationTicks = Math.max(1, swingDurationTicks);
        this.sequenceTick = 0;
        this.nextSwingIndex = 0;
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
                activeSwingStartTick >= 0 && sequenceTick > activeSwingStartTick + swingDurationTicks) {
            clear();
        }
    }

    private void startDueSwings() {
        while (nextSwingIndex < swingStartTicks.length && sequenceTick >= swingStartTicks[nextSwingIndex]) {
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
        return swingStartTicks.length > 0;
    }

    public float getAttackAnim(float partialTicks) {
        if (!hasSequence() || activeSwingStartTick < 0) {
            return 0.0F;
        }
        float swingTime = sequenceTick - activeSwingStartTick + partialTicks;
        if (swingTime <= 0.0F || swingTime >= swingDurationTicks) {
            return 0.0F;
        }
        return Mth.clamp(swingTime / swingDurationTicks, 0.0F, 1.0F);
    }

    public int getLocalSwingVariant() {
        return localSwingVariant;
    }

    public void clear() {
        swingStartTicks = new int[0];
        swingDurationTicks = 0;
        sequenceTick = 0;
        nextSwingIndex = 0;
        activeSwingStartTick = -1;
        swingStartedThisTick = false;
    }
}
