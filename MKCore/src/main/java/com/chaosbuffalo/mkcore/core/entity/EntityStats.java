package com.chaosbuffalo.mkcore.core.entity;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.*;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.core.player.IPlayerSyncComponentProvider;
import com.chaosbuffalo.mkcore.core.player.PlayerSyncComponent;
import com.chaosbuffalo.mkcore.sync.types.SyncFloat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

import javax.annotation.Nonnull;
import java.util.Objects;

public class EntityStats implements IMKEntityStats, IPlayerSyncComponentProvider {

    public static final ResourceLocation POISE_BREAK_TIMER = new ResourceLocation(MKCore.MOD_ID, "timer.poise_break");
    protected final IMKEntityData entityData;
    protected final AbilityTracker abilityTracker;
    protected final SyncFloat mana = new SyncFloat("mana", 0f);
    protected final SyncFloat poise = new SyncFloat("poise", 0f);
    private final PlayerSyncComponent sync = new PlayerSyncComponent("stats");
    protected float manaRegenTimer;


    public EntityStats(IMKEntityData data) {
        entityData = data;
        abilityTracker = AbilityTracker.getTracker(data.getEntity());
        manaRegenTimer = 0f;
        addSyncPublic(mana);
        addSyncPrivate(poise);
        addSyncPrivate(abilityTracker);
    }

    @Override
    public float getDamageTypeBonus(MKDamageType damageType) {
        return (float) getEntity().getAttributeValue(damageType.getDamageAttribute());
    }

    // Mostly to shut up warning about null returns from getAttribute. Only use this for attrs you know will be present
    @Nonnull
    protected AttributeInstance requiredAttribute(Attribute attribute) {
        return Objects.requireNonNull(getEntity().getAttribute(attribute));
    }

    @Override
    public float getMana() {
        return mana.get();
    }

    @Override
    public void setMana(float value) {
        setMana(value, true);
    }

    public void setMana(float value, boolean sendUpdate) {
        // Here we're using isAddedToWorld as a proxy to know that attribute deserialization is done and max mana is available
        if (getEntity().isAddedToWorld()) {
            value = Mth.clamp(value, 0, getMaxMana());
        }
        mana.set(value, sendUpdate);
    }

    @Override
    public float getPoise() {
        return poise.get();
    }

    @Override
    public float getMaxPoise() {
        return (float) getEntity().getAttributeValue(MKAttributes.MAX_POISE);
    }

    public void setMaxPoise(float max) {
        requiredAttribute(MKAttributes.MAX_POISE).setBaseValue(max);
        setPoise(getPoise()); // Refresh the poise to account for the updated maximum
    }

    @Override
    public void setPoise(float value) {
        setPoise(value, true);
    }

    public void setPoise(float value, boolean sendUpdate) {
        if (getEntity().isAddedToWorld()) {
            value = Mth.clamp(value, 0, getMaxPoise());
        }
        poise.set(value, sendUpdate);
    }

    @Override
    public float getMaxMana() {
        return (float) getEntity().getAttributeValue(MKAttributes.MAX_MANA);
    }

    public void setMaxMana(float max) {
        requiredAttribute(MKAttributes.MAX_MANA).setBaseValue(max);
        setMana(getMana()); // Refresh the mana to account for the updated maximum
    }

    @Override
    public float getPoiseRegenRate() {
        return (float) getEntity().getAttributeValue(MKAttributes.POISE_REGEN);
    }

    @Override
    public float getManaRegenRate() {
        return (float) getEntity().getAttributeValue(MKAttributes.MANA_REGEN);
    }

    @Override
    public PlayerSyncComponent getSyncComponent() {
        return sync;
    }

    public void tick() {
        abilityTracker.tick();
        updateMana();
        updatePoise();
    }

    @Override
    public float getHealBonus() {
        return (float) getEntity().getAttributeValue(MKAttributes.HEAL_BONUS);
    }

    @Override
    public float getHealEfficiency() {
        return (float) getEntity().getAttributeValue(MKAttributes.HEAL_EFFICIENCY);
    }

    @Override
    public float getBuffDurationModifier() {
        return (float) getEntity().getAttributeValue(MKAttributes.BUFF_DURATION);
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
    public int getPoiseBreakRemainingTicks() {
        return getTimer(POISE_BREAK_TIMER);
    }

    @Override
    public int getPoiseBreakCooldownTicks() {
        return (int) Math.round(getEntity().getAttributeValue(MKAttributes.POISE_BREAK_CD) * GameConstants.TICKS_PER_SECOND);
    }

    @Override
    public float getPoiseBreakPercent(float partialTick) {
        return getTimerPercent(POISE_BREAK_TIMER, partialTick);
    }

    @Override
    public void breakPoise() {
        setPoise(0);
        setTimer(POISE_BREAK_TIMER, getPoiseBreakCooldownTicks());
        getEntity().releaseUsingItem();
    }

    @Override
    public boolean isPoiseBroke() {
        return getPoiseBreakRemainingTicks() > 0;
    }

    protected void updatePoise() {
        boolean isBroken = isPoiseBroke();
        if (getEntity().isBlocking()) {
            if (entityData.getAbilityExecutor().isCasting()) {
                entityData.getAbilityExecutor().interruptCast(CastInterruptReason.StartedBlocking);
            }
            if (isBroken) {
                getEntity().releaseUsingItem();
            }
            return;
        }
        if (isBroken || getPoiseRegenRate() <= 0f) {
            return;
        }

        float max = getMaxPoise();
        if (getPoise() > max) {
            setPoise(max);
        }

        if (getPoise() == max) {
            return;
        }

        // if getPoiseRegenRate == 1, this is 1 poise per 1 seconds
        float newPoise = Math.min(getPoise() + (getPoiseRegenRate() / GameConstants.TICKS_PER_SECOND), max);
        setPoise(newPoise, newPoise == max);
    }

    protected void updateMana() {
        if (getManaRegenRate() <= 0.0f) {
            return;
        }

        float max = getMaxMana();
        if (getMana() > max)
            setMana(max);

        if (getMana() == max)
            return;

        manaRegenTimer += 1f / GameConstants.TICKS_PER_SECOND;

        // if getManaRegenRate == 1, this is 1 mana per 3 seconds
        float i_regen = 3.0f / getManaRegenRate();
        while (manaRegenTimer >= i_regen) {
            float current = getMana();
            if (current < max) {
                float newValue = current + 1;
                setMana(newValue, newValue == max);
            }
            manaRegenTimer -= i_regen;
        }
    }

    @Override
    public void addMana(float value) {
        setMana(getMana() + value);
    }

    @Override
    public boolean consumeMana(float amount) {
        if (getMana() < amount) {
            return false;
        }

        setMana(getMana() - amount);
        return true;
    }

    @Override
    public BlockResult tryPoiseBlock(float damageIn) {
        float blockPortion = (float) (getEntity().getAttributeValue(MKAttributes.BLOCK_EFFICIENCY) * damageIn);
        float remainder = damageIn - blockPortion;
        float poise = getPoise();
        if (blockPortion >= poise) {
            breakPoise();
            return new BlockResult(remainder + blockPortion - poise, true);
        } else {
            if (getEntity().getTicksUsingItem() < 6) {
                blockPortion *= 0.25f;
            }
            setPoise(poise - blockPortion);
            return new BlockResult(remainder, false);
        }
    }

    @Override
    public float getAbilityManaCost(MKAbility ability) {
        float manaCost = ability.getManaCost(entityData);
        return MKCombatFormulas.applyManaCostReduction(entityData, manaCost);
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
        return getMana() >= getAbilityManaCost(ability);
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

    public void resetAllTimers() {
        abilityTracker.removeAll();
    }

    public LivingEntity getEntity() {
        return entityData.getEntity();
    }

}
