package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import net.minecraft.resources.ResourceLocation;

public interface IMKEntityStats {

    float getDamageTypeBonus(MKDamageType damageType);

    void setMana(float value);

    float getPoise();

    float getMaxPoise();

    void setPoise(float value);

    float getMaxMana();

    float getPoiseRegenRate();

    float getManaRegenRate();

    float getHealBonus();

    float getHealEfficiency();

    float getBuffDurationModifier();

    float getHealth();

    void setHealth(float value);

    float getMaxHealth();

    int getPoiseBreakRemainingTicks();

    int getPoiseBreakCooldownTicks();

    float getPoiseBreakPercent(float partialTick);

    void breakPoise();

    boolean isPoiseBroke();

    void addMana(float value);

    boolean consumeMana(float amount);

    record BlockResult(float damageLeft, boolean poiseBroke) {
    }

    BlockResult tryPoiseBlock(float damageIn);

    float getAbilityManaCost(MKAbilityInfo abilityInfo);

    int getAbilityCooldown(MKAbilityInfo abilityInfo);

    int getAbilityCastTime(MKAbilityInfo abilityInfo);

    boolean canActivateAbility(MKAbilityInfo abilityInfo);

    void setTimer(ResourceLocation id, int cooldown);

    void setLocalTimer(ResourceLocation id, int cooldown);

    int getTimer(ResourceLocation id);

    float getTimerPercent(ResourceLocation id, float partialTick);
}
