package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.CombatExtensionModule;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.sync.types.SyncInt;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class PlayerCombatExtensionModule extends CombatExtensionModule implements IPlayerSyncComponentProvider {
    private static final ResourceLocation blockMaxPoiseBonusId = MKCore.id("block_skill_modifier");
    private final PlayerSyncComponent sync = new PlayerSyncComponent("combatExtension");
    private final SyncInt currentProjectileHitCount = new SyncInt(0);


    public PlayerCombatExtensionModule(MKPlayerData playerData) {
        super(playerData);
        addSyncPrivate("projectileHits", currentProjectileHitCount);
    }

    @Override
    public PlayerSyncComponent getSyncComponent() {
        return sync;
    }

    private MKPlayerData getPlayerData() {
        return (MKPlayerData) getEntityData();
    }

    private static double getBlockSkillMaxPoiseBonus(IMKEntityData entityData) {
        double blockVal = entityData.getEntity().getAttributeValue(MKAttributes.BLOCK);
        return MKAbility.convertSkillToMultiplier(blockVal);
    }

    private static void updatePoiseBonus(MKPlayerData playerData) {
        AttributeInstance maxPoise = playerData.getEntity().getAttribute(MKAttributes.MAX_POISE);
        if (maxPoise != null) {
            maxPoise.removeModifier(blockMaxPoiseBonusId);
            AttributeModifier blockPoiseBonus = new AttributeModifier(blockMaxPoiseBonusId,
                    getBlockSkillMaxPoiseBonus(playerData), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            maxPoise.addTransientModifier(blockPoiseBonus);
        }
    }

    public void serverInit() {
        updatePoiseBonus(getPlayerData());
        getPlayerData().getAttributeMonitor().monitor(MKAttributes.BLOCK, PlayerCombatExtensionModule::onBlockChange);
    }

    private static void onBlockChange(MKPlayerData playerData, AttributeInstance attributeInstance) {
//        MKCore.LOGGER.info("recomputing max_poise value due to block attribute update");

        updatePoiseBonus(playerData);
    }

    public int getCurrentProjectileHitCount() {
        return currentProjectileHitCount.get();
    }

    @Override
    public void setCurrentProjectileHitCount(int currentProjectileHitCount) {
        this.currentProjectileHitCount.set(currentProjectileHitCount);
    }

}
