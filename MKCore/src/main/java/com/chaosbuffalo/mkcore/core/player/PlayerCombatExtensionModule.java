package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.CombatExtensionModule;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.sync.types.SyncInt;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;

public class PlayerCombatExtensionModule extends CombatExtensionModule implements IPlayerSyncComponentProvider {
    private static final UUID EV_ID = UUID.fromString("fce9b2a1-c8ec-4c1d-9da4-63bdd95e2ff9");
    private static final UUID blockScalerUUID = UUID.fromString("8cabfe08-4ad3-4b8a-9b94-cb146f743c36");
    private final PlayerSyncComponent sync = new PlayerSyncComponent("combatExtension");
    private final SyncInt currentProjectileHitCount = new SyncInt("projectileHits", 0);


    public PlayerCombatExtensionModule(MKPlayerData playerData) {
        super(playerData);
        addSyncPrivate(currentProjectileHitCount);
        playerData.events().subscribe(PlayerEvents.SERVER_JOIN_WORLD, EV_ID, PlayerCombatExtensionModule::onJoinWorldServer);
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
            maxPoise.removeModifier(blockScalerUUID);
            AttributeModifier blockPoiseBonus = new AttributeModifier(blockScalerUUID, "block skill bonus",
                    getBlockSkillMaxPoiseBonus(playerData), AttributeModifier.Operation.MULTIPLY_TOTAL);
            maxPoise.addTransientModifier(blockPoiseBonus);
        }
    }

    private static void onJoinWorldServer(PlayerEvents.JoinWorldServerEvent event) {
        updatePoiseBonus(event.getPlayerData());
        event.getPlayerData().getAttributes().monitor(MKAttributes.BLOCK, PlayerCombatExtensionModule::onBlockChange);
    }

    private static void onBlockChange(MKPlayerData playerData, AttributeInstance attributeInstance) {
        MKCore.LOGGER.info("recomputing max_poise value due to block attribute update");

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
