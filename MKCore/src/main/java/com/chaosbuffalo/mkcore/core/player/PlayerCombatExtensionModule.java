package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.attributes.AttributeInstanceExtension;
import com.chaosbuffalo.mkcore.core.CombatExtensionModule;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.sync.SyncInt;
import com.chaosbuffalo.mkcore.utils.DynamicAttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerCombatExtensionModule extends CombatExtensionModule implements IPlayerSyncComponentProvider {
    private static final UUID blockScalerUUID = UUID.fromString("8cabfe08-4ad3-4b8a-9b94-cb146f743c36");
    private final PlayerSyncComponent sync = new PlayerSyncComponent("combatExtension");
    private final SyncInt currentProjectileHitCount = new SyncInt("projectileHits", 0);
    private final Set<String> spellTag = new HashSet<>();
    private final DynamicAttributeModifier blockPoiseBonus;

    public PlayerCombatExtensionModule(MKPlayerData entityData) {
        super(entityData);
        addSyncPrivate(currentProjectileHitCount);
        blockPoiseBonus = new DynamicAttributeModifier(blockScalerUUID, "block skill bonus",
                this::getBlockSkillMaxPoiseBonus, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public PlayerSyncComponent getSyncComponent() {
        return sync;
    }

    private MKPlayerData getPlayerData() {
        return (MKPlayerData) getEntityData();
    }

    private double getBlockSkillMaxPoiseBonus() {
        double blockVal = getEntityData().getEntity().getAttributeValue(MKAttributes.BLOCK);
        return MKAbility.convertSkillToMultiplier(blockVal);
    }

    public void onJoinWorld() {
        if (getEntityData().isClientSide())
            return;

        AttributeInstance maxPoise = getEntityData().getEntity().getAttribute(MKAttributes.MAX_POISE);
        if (maxPoise != null) {
            maxPoise.removeModifier(blockScalerUUID);
            maxPoise.addTransientModifier(blockPoiseBonus);
        }
        getPlayerData().getAttributes().subscribe(MKAttributes.BLOCK, PlayerCombatExtensionModule::onBlockChange);
    }

    private static void onBlockChange(MKPlayerData playerData, AttributeInstance attributeInstance) {
        MKCore.LOGGER.info("recomputing max_poise value due to block attribute update");
        AttributeInstance maxPoise = playerData.getEntity().getAttribute(MKAttributes.MAX_POISE);
        if (maxPoise != null) {
            AttributeInstanceExtension.recomputeValue(maxPoise);
        }
    }

    public int getCurrentProjectileHitCount() {
        return currentProjectileHitCount.get();
    }

    @Override
    public void setCurrentProjectileHitCount(int currentProjectileHitCount) {
        this.currentProjectileHitCount.set(currentProjectileHitCount);
    }

    public void addSpellTag(String tag) {
        spellTag.add(tag);
    }

    public void removeSpellTag(String tag) {
        spellTag.remove(tag);
    }

    public boolean hasSpellTag(String tag) {
        return spellTag.contains(tag);
    }

}
