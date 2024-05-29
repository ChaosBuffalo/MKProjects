package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKCombatFormulas;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.entity.EntityStats;
import com.chaosbuffalo.mkcore.core.player.events.EventPriorities;
import com.chaosbuffalo.mkcore.utils.ChatUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;


public class PlayerStats extends EntityStats {
    private static final UUID EV_ID = UUID.fromString("77c81f2b-4edc-4341-9926-28983fc0e4c3");

    public PlayerStats(MKPlayerData playerData) {
        super(playerData);
        playerData.events().subscribe(PlayerEvents.PERSONA_ACTIVATE, EV_ID, this::onPersonaActivated, EventPriorities.CONSUMER_PERSONA);
    }

    @Override
    public Player getEntity() {
        return (Player) super.getEntity();
    }

    private MKPlayerData getPlayerData() {
        return (MKPlayerData) entityData;
    }

    public void onJoinLevel() {
        // This needs to be done on both sides so the client has the correct base value before first tick
        setupBaseStats();
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

    @Override
    public float getAbilityManaCost(MKAbilityInfo abilityInfo) {
        if (getPlayerData().getEntity().isCreative())
            return 0f;
        float manaCost = abilityInfo.getAbility().getManaCost(entityData);
        return MKCombatFormulas.applyManaCostReduction(entityData, manaCost);
    }

    @Override
    public int getAbilityCooldown(MKAbility ability) {
        if (getPlayerData().getEntity().isCreative())
            return 0;
        return super.getAbilityCooldown(ability);
    }

    public void printActiveCooldowns() {
        ChatUtils.sendMessageWithBrackets(getEntity(), "All Active Cooldowns");
        abilityTracker.iterateActive((abilityId, current) -> {
            String name = abilityId.toString();
            int max = abilityTracker.getTimerMaxTicks(abilityId);
            ChatUtils.sendMessage(getEntity(), "%s: %d / %d", name, current, max);
        });
    }

    public void refreshStats() {
        if (getHealth() > getMaxHealth()) {
            setHealth(getHealth());
        }
        if (getMana() > getMaxMana()) {
            setMana(getMana());
        }
        if (getPoise() > getMaxPoise()) {
            setPoise(getPoise());
        }
    }

    // For now, this is the same formula as MobStats.doManaRegen but rises smoother for a better visual
    @Override
    protected void doManaRegen(float current, float max, float regenRate) {
        // if getManaRegenRate == 1, this is 1 mana per 3 seconds
        final float manaTickPeriod = 3.0f;
        final float manaPerTick = (regenRate / manaTickPeriod / GameConstants.TICKS_PER_SECOND);

        float newMana = Math.min(current + manaPerTick, max);
        setMana(newMana, newMana >= max);
    }

    @Override
    protected void doPoiseRegen(float current, float max, float regenRate) {
        // if getPoiseRegenRate == 1, this is 1 poise per 1 seconds
        final float poiseTickPeriod = 1.0f;
        final float poisePerTick = (regenRate / poiseTickPeriod / GameConstants.TICKS_PER_SECOND);

        float newPoise = Math.min(current + poisePerTick, max);
        setPoise(newPoise, newPoise >= max);
    }

    private void onPersonaActivated(PlayerEvents.PersonaEvent event) {
        refreshStats();
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.put("cooldowns", abilityTracker.serialize());
        tag.putFloat("mana", mana.get());
        return tag;
    }

    public void deserialize(CompoundTag tag) {
        abilityTracker.deserialize(tag.getCompound("cooldowns"));
        if (tag.contains("mana")) {
            setMana(tag.getFloat("mana"), false);
        }
    }
}
