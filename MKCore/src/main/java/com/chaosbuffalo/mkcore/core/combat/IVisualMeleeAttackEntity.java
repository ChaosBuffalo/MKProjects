package com.chaosbuffalo.mkcore.core.combat;

import net.minecraft.world.InteractionHand;

public interface IVisualMeleeAttackEntity {
    void startVisualMeleeAttackSequence(InteractionHand hand, int[] swingStartTicks, int[] swingDurationTicks);

    float getVisualMeleeAttackAnim(InteractionHand hand, float partialTicks);

    boolean hasVisualMeleeAttackSequence(InteractionHand hand);

    boolean hasActiveVisualMeleeAttack(InteractionHand hand, float partialTicks);
}
