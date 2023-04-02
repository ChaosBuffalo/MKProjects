package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.utils.ItemUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class MKCombatFormulas {

    public static int applyCooldownReduction(IMKEntityData entityData, int originalCooldownTicks) {
        final float MAX_COOLDOWN = 2.0f; // Maximum cooldown rate improvement is 200%
        float cdrValue = (float) entityData.getEntity().getAttribute(MKAttributes.COOLDOWN).getValue();
        float mod = MAX_COOLDOWN - cdrValue;
        float newTicks = mod * originalCooldownTicks;
        return (int) newTicks;
    }

    public static int applyCastTimeModifier(IMKEntityData entityData, int originalCastTicks) {
        final float MAX_RATE = 2.0f; // Maximum rate improvement is 200%
        float castSpeed = (float) entityData.getEntity().getAttribute(MKAttributes.CASTING_SPEED).getValue();
        float mod = MAX_RATE - castSpeed;
        float newTicks = mod * originalCastTicks;
        return (int) newTicks;
    }

    public static float applyManaCostReduction(IMKEntityData playerData, float originalCost) {
        return originalCost;
    }

    public static float applyHealBonus(IMKEntityData entityData, float amount, float modifierScaling) {
        float mod = entityData.getStats().getHealBonus();
        return amount + mod * modifierScaling;
    }

    public static float applyHealEfficiency(IMKEntityData entityData, float amount) {
        float mult = entityData.getStats().getHealEfficiency();
        return amount * mult;
    }

    public static int applyBuffDurationModifier(IMKEntityData entityData, float amount) {
        float mod = entityData.getStats().getBuffDurationModifier();
        return (int) (amount * mod);
    }

    public static float getCritChanceForItem(ItemStack item) {
        return ItemUtils.getCritChanceForItem(item);
    }

    public static boolean checkCrit(LivingEntity entity, float chance) {
        return entity.getRandom().nextFloat() >= 1.0f - chance;
    }
}
