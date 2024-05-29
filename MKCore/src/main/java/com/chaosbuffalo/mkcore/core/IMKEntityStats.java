package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public interface IMKEntityStats {

    LivingEntity getEntity();

    default float getDamageTypeBonus(MKDamageType damageType) {
        return (float) getEntity().getAttributeValue(damageType.getDamageAttribute());
    }

    default float getHealth() {
        return getEntity().getHealth();
    }

    default void setHealth(float value) {
        getEntity().setHealth(value);
    };

    default float getMaxHealth() {
        return getEntity().getMaxHealth();
    }

    float getMana();

    void setMana(float value);

    default float getMaxMana() {
        return (float) getEntity().getAttributeValue(MKAttributes.MAX_MANA);
    }

    default float getManaRegenRate() {
        return (float) getEntity().getAttributeValue(MKAttributes.MANA_REGEN);
    }

    void addMana(float value);

    boolean consumeMana(float amount);

    float getPoise();

    void setPoise(float value);

    default float getMaxPoise() {
        return (float) getEntity().getAttributeValue(MKAttributes.MAX_POISE);
    }

    default float getPoiseRegenRate() {
        return (float) getEntity().getAttributeValue(MKAttributes.POISE_REGEN);
    }

    int getPoiseBreakRemainingTicks();

    int getPoiseBreakCooldownTicks();

    float getPoiseBreakPercent(float partialTick);

    void breakPoise();

    boolean isPoiseBroke();

    record BlockResult(float damageLeft, boolean poiseBroke) {
    }

    BlockResult tryPoiseBlock(float damageIn);

    default float getHealBonus() {
        return (float) getEntity().getAttributeValue(MKAttributes.HEAL_BONUS);
    }

    default float getHealEfficiency() {
        return (float) getEntity().getAttributeValue(MKAttributes.HEAL_EFFICIENCY);
    }

    default float getBuffDurationModifier() {
        return (float) getEntity().getAttributeValue(MKAttributes.BUFF_DURATION);
    }

    float getAbilityManaCost(MKAbilityInfo abilityInfo);

    int getAbilityCooldown(MKAbility ability);

    int getAbilityCastTime(MKAbility ability);

    boolean canActivateAbility(MKAbilityInfo abilityInfo);

    void setTimer(ResourceLocation id, int cooldown);

    void setLocalTimer(ResourceLocation id, int cooldown);

    int getTimer(ResourceLocation id);

    float getTimerPercent(ResourceLocation id, float partialTick);
}
