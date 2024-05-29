package com.chaosbuffalo.mkcore.core.entity;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.*;
import com.chaosbuffalo.mkcore.core.player.IPlayerSyncComponentProvider;
import com.chaosbuffalo.mkcore.core.player.PlayerSyncComponent;
import com.chaosbuffalo.mkcore.sync.types.SyncFloat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public abstract class EntityStats implements IMKEntityStats, IPlayerSyncComponentProvider {

    public static final ResourceLocation POISE_BREAK_TIMER = new ResourceLocation(MKCore.MOD_ID, "timer.poise_break");
    protected final IMKEntityData entityData;
    protected final AbilityTracker abilityTracker;
    protected final SyncFloat mana = new SyncFloat("mana", 0f);
    protected final SyncFloat poise = new SyncFloat("poise", 0f);
    private final PlayerSyncComponent sync = new PlayerSyncComponent("stats");

    public EntityStats(IMKEntityData data) {
        entityData = data;
        abilityTracker = AbilityTracker.getTracker(data.getEntity());
        addSyncPublic(mana);
        addSyncPrivate(poise);
        addSyncPrivate(abilityTracker);
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
    public void addMana(float value) {
        setMana(getMana() + value);
    }

    @Override
    public boolean consumeMana(float amount) {
        final float current = getMana();
        if (current < amount) {
            return false;
        }

        setMana(current - amount);
        return true;
    }

    @Override
    public float getPoise() {
        return poise.get();
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
        if (isBroken) {
            return;
        }

        final float regenRate = getPoiseRegenRate();
        if (regenRate <= 0.0f) {
            return;
        }

        final float max = getMaxPoise();
        float current = getPoise();
        if (current > max) {
            setPoise(max);
            return;
        } else if (current == max) {
            return;
        }

        doPoiseRegen(current, max, regenRate);
    }

    protected abstract void doPoiseRegen(float current, float max, float regenRate);

    protected void updateMana() {
        final float regenRate = getManaRegenRate();
        if (regenRate <= 0.0f) {
            return;
        }

        final float max = getMaxMana();
        float current = getMana();
        if (current > max) {
            setMana(max);
            return;
        } else if (current == max) {
            return;
        }

        doManaRegen(current, max, regenRate);
    }

    protected abstract void doManaRegen(float current, float max, float regenRate);

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
    public float getAbilityManaCost(MKAbilityInfo abilityInfo) {
        float manaCost = abilityInfo.getAbility().getManaCost(entityData);
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
    public boolean canActivateAbility(MKAbilityInfo abilityInfo) {
        return getMana() >= getAbilityManaCost(abilityInfo);
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

    @Override
    public LivingEntity getEntity() {
        return entityData.getEntity();
    }

}
