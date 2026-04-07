package com.chaosbuffalo.mkcore.core.combat;

public interface VisualMeleeAttackEntity {
    void startVisualMeleeAttackSequence(int[] swingStartTicks, int swingDurationTicks);

    float getVisualMeleeAttackAnim(float partialTicks);

    boolean hasVisualMeleeAttackSequence();
}
