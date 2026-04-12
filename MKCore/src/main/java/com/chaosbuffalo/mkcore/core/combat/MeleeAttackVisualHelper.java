package com.chaosbuffalo.mkcore.core.combat;

import com.chaosbuffalo.mkcore.network.MeleeAttackSequencePacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

public final class MeleeAttackVisualHelper {
    private static final int[] DEFAULT_SWING_START_TICKS = new int[]{0};
    private static final int[] DEFAULT_SWING_DURATION_TICKS = new int[]{6};

    private MeleeAttackVisualHelper() {
    }

    public static void startVisualAttack(LivingEntity attacker, InteractionHand hand, int[] swingStartTicks, int[] swingDurationTicks) {
        if (attacker.level().isClientSide()) {
            return;
        }
        int[] startTicks = swingStartTicks.length == 0 ? DEFAULT_SWING_START_TICKS : swingStartTicks;
        int[] durationTicks = swingDurationTicks.length == 0 ? DEFAULT_SWING_DURATION_TICKS : swingDurationTicks;
        PacketHandler.sendToTrackingAndSelf(new MeleeAttackSequencePacket(attacker.getId(), hand, startTicks, durationTicks), attacker);
    }
}
