package com.chaosbuffalo.mkcore.core.combat;

public interface IVisualMeleeAttackEntity {
    void startVisualMeleeAttackSequence(int[] swingStartTicks, int[] swingDurationTicks);

    float getVisualMeleeAttackAnim(float partialTicks);

    boolean hasVisualMeleeAttackSequence();
}
