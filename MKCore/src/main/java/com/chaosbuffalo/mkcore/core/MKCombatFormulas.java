package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.core.healing.MKHealSource;
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.FormulaContext;
import net.minecraft.world.entity.LivingEntity;

public class MKCombatFormulas {

    public static int applyCooldownReduction(IMKEntityData entityData, int originalCooldownTicks) {
        final float MAX_COOLDOWN = 2.0f;
        float cdrValue = (float) entityData.getEntity().getAttributeValue(MKAttributes.COOLDOWN);
        float mod = MAX_COOLDOWN - cdrValue;
        float newTicks = mod * originalCooldownTicks;
        return (int) newTicks;
    }

    public static int applyCastTimeModifier(IMKEntityData entityData, int originalCastTicks) {
        final float MAX_RATE = 2.0f;
        float castSpeed = (float) entityData.getEntity().getAttributeValue(MKAttributes.CASTING_SPEED);
        float mod = MAX_RATE - castSpeed;
        float newTicks = mod * originalCastTicks;
        return (int) newTicks;
    }

    public static int secondsToTicks(float seconds) {
        return Math.round(GameConstants.TICKS_PER_SECOND * seconds);
    }

    public static float applyManaCostReduction(IMKEntityData playerData, float originalCost) {
        return originalCost;
    }

    public static float applyHealBonus(IMKEntityData entityData, float amount, float modifierScaling) {
        return applyHealBonus(entityData, amount, MKHealSource.createLegacyHealBonusFormula(modifierScaling));
    }

    public static float applyHealBonus(IMKEntityData entityData, float amount, AbilityFormula healBonusFormula) {
        FormulaContext context = FormulaContext.builder()
                .withHealBonus(entityData.getStats().getHealBonus())
                .build();
        return amount + healBonusFormula.evaluate(context);
    }

    public static float applyHealEfficiency(IMKEntityData entityData, float amount) {
        float mult = entityData.getStats().getHealEfficiency();
        return amount * mult;
    }

    public static int applyBuffDurationModifier(IMKEntityData entityData, float amount) {
        float mod = entityData.getStats().getBuffDurationModifier();
        return (int) (amount * mod);
    }

    public static boolean checkCrit(LivingEntity entity, float chance) {
        return entity.getRandom().nextFloat() >= 1.0f - chance;
    }
}
