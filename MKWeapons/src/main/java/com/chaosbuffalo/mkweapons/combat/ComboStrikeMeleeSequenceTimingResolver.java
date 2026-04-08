package com.chaosbuffalo.mkweapons.combat;

import com.chaosbuffalo.mkcore.core.MultiAttackHelper;
import com.chaosbuffalo.mkcore.core.combat.MeleeSequenceTimingResolver;
import com.chaosbuffalo.mkcore.core.combat.MeleeSequenceTimings;
import com.chaosbuffalo.mkweapons.items.effects.melee.ComboStrikeMeleeWeaponEffect;
import com.chaosbuffalo.mkweapons.items.effects.melee.IMeleeWeaponEffect;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ComboStrikeMeleeSequenceTimingResolver implements MeleeSequenceTimingResolver {
    @Override
    public @Nullable MeleeSequenceTimings resolve(LivingEntity attacker, int attackCount, int firstAttackIndex,
                                                  int baseCooldownTicks, int baseSwingDurationTicks,
                                                  int currentSwingCount) {
        ItemStack mainHand = attacker.getMainHandItem();
        if (!(mainHand.getItem() instanceof IMKMeleeWeapon weapon)) {
            return null;
        }

        List<ComboStrikeMeleeWeaponEffect> comboEffects = getComboEffects(weapon, mainHand);
        if (comboEffects.isEmpty()) {
            return null;
        }

        int clampedAttackCount = Math.max(0, attackCount);
        int[] fullStartTicks = new int[clampedAttackCount];
        int[] fullDurationTicks = new int[clampedAttackCount];
        int completedHitsBeforeSequence = Math.max(0, currentSwingCount);

        for (int i = 0; i < clampedAttackCount; i++) {
            int completedHitsBeforeAttack = completedHitsBeforeSequence + i;
            int effectiveCooldownTicks = getEffectiveCooldownTicks(comboEffects, baseCooldownTicks, completedHitsBeforeAttack);
            fullDurationTicks[i] = MultiAttackHelper.getSequenceSwingDurationTicks(
                    effectiveCooldownTicks, attackCount, baseSwingDurationTicks);
            if (i > 0) {
                fullStartTicks[i] = fullStartTicks[i - 1] + MultiAttackHelper.getAttackSpacingTicks(
                        effectiveCooldownTicks, attackCount);
            }
        }

        int startIndex = Mth.clamp(firstAttackIndex, 0, clampedAttackCount);
        int includedCount = Math.max(0, clampedAttackCount - startIndex);
        int[] swingStartTicks = new int[includedCount];
        int[] swingDurationTicks = new int[includedCount];
        System.arraycopy(fullStartTicks, startIndex, swingStartTicks, 0, includedCount);
        System.arraycopy(fullDurationTicks, startIndex, swingDurationTicks, 0, includedCount);
        return new MeleeSequenceTimings(swingStartTicks, swingDurationTicks);
    }

    private static List<ComboStrikeMeleeWeaponEffect> getComboEffects(IMKMeleeWeapon weapon, ItemStack stack) {
        List<ComboStrikeMeleeWeaponEffect> comboEffects = new ArrayList<>();
        for (IMeleeWeaponEffect effect : weapon.getWeaponEffects(stack)) {
            if (effect instanceof ComboStrikeMeleeWeaponEffect comboStrikeEffect) {
                comboEffects.add(comboStrikeEffect);
            }
        }
        return comboEffects;
    }

    private static int getEffectiveCooldownTicks(List<ComboStrikeMeleeWeaponEffect> comboEffects, int baseCooldownTicks,
                                                 int completedHits) {
        int cooldownAdjustmentTicks = 0;
        for (ComboStrikeMeleeWeaponEffect comboEffect : comboEffects) {
            cooldownAdjustmentTicks += comboEffect.getCooldownAdjustmentTicks(baseCooldownTicks, completedHits);
        }
        return Math.max(1, baseCooldownTicks - cooldownAdjustmentTicks);
    }
}
