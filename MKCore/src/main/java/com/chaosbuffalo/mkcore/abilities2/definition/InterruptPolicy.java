package com.chaosbuffalo.mkcore.abilities2.definition;

public record InterruptPolicy(
        boolean onDamage,
        float minDamage,
        boolean onMove,
        double moveThresholdBlocks,
        boolean onDeath
) {
    public InterruptPolicy {
        if (minDamage < 0.0f) {
            throw new IllegalArgumentException("Interrupt minDamage must be >= 0");
        }
        if (moveThresholdBlocks < 0.0) {
            throw new IllegalArgumentException("Interrupt moveThresholdBlocks must be >= 0");
        }
    }
}
