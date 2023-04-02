package com.chaosbuffalo.mkcore.core.entity;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.*;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class EntityStats implements IMKEntityStats {

    protected final IMKEntityData entityData;
    protected final AbilityTracker abilityTracker;

    public EntityStats(IMKEntityData data) {
        entityData = data;
        abilityTracker = AbilityTracker.getTracker(data.getEntity());
    }

    @Override
    public float getDamageTypeBonus(MKDamageType damageType) {
        return (float) getEntity().getAttribute(damageType.getDamageAttribute()).getValue();
    }

    public void tick() {
        abilityTracker.tick();
    }

    @Override
    public float getHealBonus() {
        return (float) getEntity().getAttribute(MKAttributes.HEAL_BONUS).getValue();
    }

    @Override
    public float getHealEfficiency() {
        return (float) getEntity().getAttribute(MKAttributes.HEAL_EFFICIENCY).getValue();
    }

    @Override
    public float getBuffDurationModifier() {
        return (float) getEntity().getAttribute(MKAttributes.BUFF_DURATION).getValue();
    }

    @Override
    public float getHealth() {
        return getEntity().getHealth();
    }

    @Override
    public void setHealth(float value) {
        getEntity().setHealth(value);
    }

    @Override
    public float getMaxHealth() {
        return getEntity().getMaxHealth();
    }

    @Override
    public int getAbilityCooldown(MKAbility ability) {
        int ticks = ability.getCooldown(entityData);
        return MKCombatFormulas.applyCooldownReduction(entityData, ticks);
    }

    @Override
    public int getAbilityCastTime(MKAbility ability) {
        int ticks = ability.getCastTime(entityData);
        return ability.canApplyCastingSpeedModifier() ?
                MKCombatFormulas.applyCastTimeModifier(entityData, ticks) :
                ticks;
    }

    @Override
    public boolean canActivateAbility(MKAbility ability) {
        return true;
    }

    @Override
    public void setTimer(ResourceLocation id, int cooldown) {
        abilityTracker.setTimer(id, cooldown);
    }

    @Override
    public void setLocalTimer(ResourceLocation id, int cooldown) {
        abilityTracker.setLocalTimer(id, cooldown);
    }

    @Override
    public int getTimer(ResourceLocation id) {
        return abilityTracker.getTimerTicksRemaining(id);
    }

    @Override
    public float getTimerPercent(ResourceLocation timerId, float partialTick) {
        return abilityTracker.getTimerProgressPercent(timerId, partialTick);
    }

    @Override
    public void resetAllTimers() {
        abilityTracker.removeAll();
    }

    @Override
    public CompoundTag serialize() {
        return new CompoundTag();
    }

    @Override
    public void deserialize(CompoundTag nbt) {

    }

    public LivingEntity getEntity() {
        return entityData.getEntity();
    }

}
