package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKCombatFormulas;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.entity.EntityStats;
import com.chaosbuffalo.mkcore.core.player.events.EPriority;
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
        playerData.events().subscribe(PlayerEvents.PERSONA_ACTIVATE, EV_ID, this::onPersonaActivated);
        playerData.events().subscribe(PlayerEvents.SERVER_JOIN_WORLD, EV_ID, this::onJoinWorld, EPriority.PROVIDER);
    }

    private Player getPlayer() {
        return (Player) getEntity();
    }

    private MKPlayerData getPlayerData() {
        return (MKPlayerData) entityData;
    }

    private void onJoinWorld(PlayerEvents.JoinWorldServerEvent event) {
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
            setHealth(getHealth());
        }
        if (getMana() > getMaxMana()) {
            setMana(getMana());
        }
        if (getPoise() > getMaxPoise()) {
            setPoise(getPoise());
        }
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
