package com.chaosbuffalo.mkcore.core.combat;

import net.minecraft.world.InteractionHand;

public interface IVisualMeleeAttackEntity {
    default void startVisualMeleeAttackSequence(int[] swingStartTicks, int[] swingDurationTicks) {
        startVisualMeleeAttackSequence(InteractionHand.MAIN_HAND, swingStartTicks, swingDurationTicks);
    }

    void startVisualMeleeAttackSequence(InteractionHand hand, int[] swingStartTicks, int[] swingDurationTicks);

    default float getVisualMeleeAttackAnim(float partialTicks) {
        return getVisualMeleeAttackAnim(InteractionHand.MAIN_HAND, partialTicks);
    }

    float getVisualMeleeAttackAnim(InteractionHand hand, float partialTicks);

    default boolean hasVisualMeleeAttackSequence() {
        return hasVisualMeleeAttackSequence(InteractionHand.MAIN_HAND);
    }

    boolean hasVisualMeleeAttackSequence(InteractionHand hand);
}
