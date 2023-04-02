package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.CastInterruptReason;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKCombatFormulas;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.entity.EntityStats;
import com.chaosbuffalo.mkcore.sync.SyncFloat;
import com.chaosbuffalo.mkcore.utils.ChatUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.Objects;


public class PlayerStats extends EntityStats implements IPlayerSyncComponentProvider {
    private final SyncComponent sync = new SyncComponent("stats");
    private float manaRegenTimer;
    private final SyncFloat mana = new SyncFloat("mana", 0f);
    private final SyncFloat poise = new SyncFloat("poise", 0f);
    public static final ResourceLocation POISE_BREAK_TIMER = new ResourceLocation(MKCore.MOD_ID, "timer.poise_break");

    public PlayerStats(MKPlayerData playerData) {
        super(playerData);
        manaRegenTimer = 0f;
        addSyncPublic(mana);
        addSyncPrivate(poise);
        addSyncPrivate(abilityTracker);
    }

    @Override
    public SyncComponent getSyncComponent() {
        return sync;
    }

    private Player getPlayer() {
        return (Player) getEntity();
    }

    private MKPlayerData getPlayerData() {
        return (MKPlayerData) entityData;
    }

    // Mostly to shut up warning about null returns from getAttribute. Only use this for attrs you know will be present
    @Nonnull
    protected AttributeInstance requiredAttribute(Attribute attribute) {
        return Objects.requireNonNull(getEntity().getAttribute(attribute));
    }

    public float getMana() {
        return mana.get();
    }

    public void setMana(float value) {
        setMana(value, true);
    }

    private void setMana(float value, boolean sendUpdate) {
        // Here we're using isAddedToWorld as a proxy to know that attribute deserialization is done and max mana is available
        if (getEntity().isAddedToWorld()) {
            value = Mth.clamp(value, 0, getMaxMana());
        }
        mana.set(value, sendUpdate);
    }

    public float getPoise() {
        return poise.get();
    }

    public float getMaxPoise() {
        return (float) requiredAttribute(MKAttributes.MAX_POISE).getValue();
    }

    public void setMaxPoise(float max) {
        requiredAttribute(MKAttributes.MAX_POISE).setBaseValue(max);
        setPoise(getPoise()); // Refresh the poise to account for the updated maximum
    }

    public void setPoise(float value) {
        setPoise(value, true);
    }

    private void setPoise(float value, boolean sendUpdate) {
        if (getEntity().isAddedToWorld()) {
            value = Mth.clamp(value, 0, getMaxPoise());
        }
        poise.set(value, sendUpdate);
    }

    public float getMaxMana() {
        return (float) requiredAttribute(MKAttributes.MAX_MANA).getValue();
    }

    public void setMaxMana(float max) {
        requiredAttribute(MKAttributes.MAX_MANA).setBaseValue(max);
        setMana(getMana()); // Refresh the mana to account for the updated maximum
    }

    public float getPoiseRegenRate() {
        return (float) requiredAttribute(MKAttributes.POISE_REGEN).getValue();
    }

    public float getManaRegenRate() {
        return (float) requiredAttribute(MKAttributes.MANA_REGEN).getValue();
    }

    public void tick() {
        super.tick();
        updateMana();
        updatePoise();
    }

    public void onJoinWorld() {
//        MKCore.LOGGER.info("PlayerStats.onJoinWorld");
        if (entityData.isServerSide()) {
            setupBaseStats();
        }
    }

    private void addBaseStat(Attribute attribute, double value) {
        LivingEntity entity = getEntity();

        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        } else {
            MKCore.LOGGER.error("Cannot apply base stat mod to {} - missing attribute {}", getEntity(), attribute);
        }
    }

    private void setupBaseStats() {
        addBaseStat(MKAttributes.MAX_MANA, 20);
        addBaseStat(MKAttributes.MANA_REGEN, 1);

    }

    public int getPoiseBreakRemainingTicks() {
        return getTimer(POISE_BREAK_TIMER);
    }

    public int getPoiseBreakCooldownTicks() {
        return (int) Math.round(requiredAttribute(MKAttributes.POISE_BREAK_CD).getValue() * GameConstants.TICKS_PER_SECOND);
    }

    public float getPoiseBreakPercent(float partialTick) {
        return getTimerPercent(POISE_BREAK_TIMER, partialTick);
    }

    public void breakPoise() {
        setPoise(0);
        setTimer(POISE_BREAK_TIMER, getPoiseBreakCooldownTicks());
        getEntity().releaseUsingItem();
    }

    public boolean isPoiseBroke() {
        return getPoiseBreakRemainingTicks() > 0;
    }

    private void updatePoise() {
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

    private void updateMana() {
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

    public void addMana(float value) {
        setMana(getMana() + value);
    }

    public boolean consumeMana(float amount) {
        if (getMana() < amount) {
            return false;
        }

        setMana(getMana() - amount);
        return true;
    }

    public Tuple<Float, Boolean> handlePoiseDamage(float damageIn) {
        float blockPortion = (float) (requiredAttribute(MKAttributes.BLOCK_EFFICIENCY).getValue() * damageIn);
        float remainder = damageIn - blockPortion;
        float poise = getPoise();
        if (blockPortion >= poise) {
            breakPoise();
            return new Tuple<>(remainder + blockPortion - poise, true);
        } else {
            if (getEntity().getTicksUsingItem() < 6) {
                blockPortion *= 0.25f;
            }
            setPoise(poise - blockPortion);
            return new Tuple<>(remainder, false);
        }
    }

    public float getAbilityManaCost(MKAbility ability) {
        if (getPlayerData().getEntity().isCreative())
            return 0f;
        float manaCost = ability.getManaCost(entityData);
        return MKCombatFormulas.applyManaCostReduction(entityData, manaCost);
    }

    @Override
    public int getAbilityCooldown(MKAbility ability) {
        if (getPlayerData().getEntity().isCreative())
            return 0;
        return super.getAbilityCooldown(ability);
    }

    @Override
    public boolean canActivateAbility(MKAbility ability) {
        return getMana() >= getAbilityManaCost(ability);
    }

    public void printActiveCooldowns() {
        ChatUtils.sendMessageWithBrackets(getPlayer(), "All Active Cooldowns");
        abilityTracker.iterateActive((abilityId, current) -> {
            String name = abilityId.toString();
            int max = abilityTracker.getTimerMaxTicks(abilityId);
            ChatUtils.sendMessage(getPlayer(), "%s: %d / %d", name, current, max);
        });
    }

    public void refreshStats() {
        if (getHealth() > getMaxHealth()) {
            setHealth(Mth.clamp(getHealth(), 0, getMaxHealth()));
        }
        if (getMana() > getMaxMana()) {
            setMana(getMana());
        }
        if (getPoise() > getMaxPoise()) {
            setPoise(getPoise());
        }
    }

    public void onPersonaActivated() {
        refreshStats();
    }

    public void onPersonaDeactivated() {

    }

    @Override
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.put("cooldowns", abilityTracker.serialize());
        tag.putFloat("mana", mana.get());
        return tag;
    }

    @Override
    public void deserialize(CompoundTag tag) {
        abilityTracker.deserialize(tag.getCompound("cooldowns"));
        if (tag.contains("mana")) {
            setMana(tag.getFloat("mana"));
        }
    }
}
