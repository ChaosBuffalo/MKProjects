package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;

public interface IMKEntityStats {

    float getDamageTypeBonus(MKDamageType damageType);

    void setMana(float value, boolean sendUpdate);

    float getPoise();

    float getMaxPoise();

    void setMaxPoise(float max);

    void setPoise(float value);

    void setPoise(float value, boolean sendUpdate);

    float getMaxMana();

    void setMaxMana(float max);

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

    Tuple<Float, Boolean> handlePoiseDamage(float damageIn);

    float getAbilityManaCost(MKAbility ability);

    int getAbilityCooldown(MKAbility ability);

    int getAbilityCastTime(MKAbility ability);

    boolean canActivateAbility(MKAbility ability);

    void setTimer(ResourceLocation id, int cooldown);

    void setLocalTimer(ResourceLocation id, int cooldown);

    int getTimer(ResourceLocation id);

    float getTimerPercent(ResourceLocation id, float partialTick);

    void resetAllTimers();

    CompoundTag serialize();

    void deserialize(CompoundTag nbt);
}
