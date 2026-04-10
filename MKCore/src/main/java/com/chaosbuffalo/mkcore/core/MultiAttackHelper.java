package com.chaosbuffalo.mkcore.core;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class MultiAttackHelper {
    public static final int MAX_ATTACKS_PER_SEQUENCE = 5;

    public static int rollAttackCount(LivingEntity attacker) {
        double value = Math.max(0.0D, MKAttributes.getValueSafe(MKAttributes.MULTI_ATTACK_CHANCE, attacker));
        int extraAttacks = Mth.floor(value);
        double fractionalChance = value - extraAttacks;
        if (attacker.getRandom().nextDouble() < fractionalChance) {
            extraAttacks++;
        }
        return Mth.clamp(1 + extraAttacks, 1, MAX_ATTACKS_PER_SEQUENCE);
    }

    public static int getAttackStartTick(int cooldownTicks, int attackCount, int attackIndex) {
        if (attackCount <= 1) {
            return 0;
        }
        return Math.round((cooldownTicks * attackIndex) / (float) attackCount);
    }

    public static int[] createAttackStartTicks(int cooldownTicks, int attackCount, int firstAttackIndex) {
        int startIndex = Mth.clamp(firstAttackIndex, 0, attackCount);
        int[] starts = new int[Math.max(0, attackCount - startIndex)];
        for (int i = 0; i < starts.length; i++) {
            starts[i] = getAttackStartTick(cooldownTicks, attackCount, startIndex + i);
        }
        return starts;
    }

    public static int getSequenceSwingDurationTicks(int cooldownTicks, int attackCount, int baseSwingDurationTicks) {
        int sequenceSpacing = Math.max(2, Mth.ceil(cooldownTicks / (float) Math.max(1, attackCount)));
        return Mth.clamp(Math.min(baseSwingDurationTicks, sequenceSpacing), 2, 24);
    }
}
