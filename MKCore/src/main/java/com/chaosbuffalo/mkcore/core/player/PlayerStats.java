package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKCombatFormulas;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.entity.EntityStats;
import com.chaosbuffalo.mkcore.utils.ChatUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;


public class PlayerStats extends EntityStats {

    public PlayerStats(MKPlayerData playerData) {
        super(playerData);

    }
    private Player getPlayer() {
        return (Player) getEntity();
    }

    private MKPlayerData getPlayerData() {
        return (MKPlayerData) entityData;
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

    @Override
    public float getAbilityManaCost(MKAbilityInfo abilityInfo) {
        if (getPlayerData().getEntity().isCreative())
            return 0f;
        float manaCost = abilityInfo.getAbility().getManaCost(entityData);
        return MKCombatFormulas.applyManaCostReduction(entityData, manaCost);
    }

    @Override
    public int getAbilityCooldown(MKAbilityInfo abilityInfo) {
        if (getPlayerData().getEntity().isCreative())
            return 0;
        return super.getAbilityCooldown(abilityInfo);
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

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.put("cooldowns", abilityTracker.serialize());
        tag.putFloat("mana", mana.get());
        return tag;
    }

    public void deserialize(CompoundTag tag) {
        abilityTracker.deserialize(tag.getCompound("cooldowns"));
        if (tag.contains("mana")) {
            setMana(tag.getFloat("mana"));
        }
    }
}
