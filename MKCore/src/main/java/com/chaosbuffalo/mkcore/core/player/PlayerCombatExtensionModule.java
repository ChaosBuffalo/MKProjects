package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.CombatExtensionModule;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.MultiAttackHelper;
import com.chaosbuffalo.mkcore.core.combat.VisualMeleeAttackSequence;
import com.chaosbuffalo.mkcore.network.MeleeAttackSequencePacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.sync.types.SyncInt;
import com.chaosbuffalo.mkcore.sync.v2.ISyncGroupProvider;
import com.chaosbuffalo.mkcore.sync.v2.SyncGroup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class PlayerCombatExtensionModule extends CombatExtensionModule implements ISyncGroupProvider {
    private static final ResourceLocation blockMaxPoiseBonusId = MKCore.id("block_skill_modifier");
    private final SyncGroup syncGroup = new SyncGroup();
    private final SyncInt currentProjectileHitCount = new SyncInt(0);
    private int multiAttackTargetId = -1;
    private int multiAttackCount = 1;
    private int multiAttackNextIndex = 1;
    private int multiAttackSequenceTick;
    private int multiAttackCooldownTicks;
    private InteractionHand multiAttackHand = InteractionHand.MAIN_HAND;
    private boolean executingMultiAttack;
    private final VisualMeleeAttackSequence visualMeleeAttackSequence = new VisualMeleeAttackSequence();


    public PlayerCombatExtensionModule(MKPlayerData playerData) {
        super(playerData);
        syncGroup.addPrivate("projectileHits", currentProjectileHitCount);
    }

    @Override
    public SyncGroup getSyncGroup() {
        return syncGroup;
    }

    @Override
    public void tick() {
        super.tick();
        if (getPlayerData().getEntity().level().isClientSide) {
            visualMeleeAttackSequence.tick();
            if (visualMeleeAttackSequence.consumeSwingStartedThisTick()) {
                restartLocalFirstPersonSwing();
            }
        }
        tickMultiAttack();
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
            AttributeModifier blockPoiseBonus = new AttributeModifier(blockMaxPoiseBonusId,
                    getBlockSkillMaxPoiseBonus(playerData), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            maxPoise.addOrUpdateTransientModifier(blockPoiseBonus);
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

    public boolean isExecutingMultiAttack() {
        return executingMultiAttack;
    }

    public void startVisualMeleeAttackSequence(int[] swingStartTicks, int swingDurationTicks) {
        visualMeleeAttackSequence.start(swingStartTicks, swingDurationTicks);
    }

    public float getVisualMeleeAttackAnim(float partialTicks) {
        return visualMeleeAttackSequence.getAttackAnim(partialTicks);
    }

    public boolean hasVisualMeleeAttackSequence() {
        return visualMeleeAttackSequence.hasSequence();
    }

    public int getCurrentLocalSwingVariant() {
        return visualMeleeAttackSequence.getLocalSwingVariant();
    }

    private void restartLocalFirstPersonSwing() {
        if (!getPlayerData().getEntity().isLocalPlayer()) {
            return;
        }
        getPlayerData().getEntity().swinging = true;
        getPlayerData().getEntity().swingingArm = InteractionHand.MAIN_HAND;
        getPlayerData().getEntity().swingTime = 0;
        getPlayerData().getEntity().oAttackAnim = 0.0F;
        getPlayerData().getEntity().attackAnim = 0.0F;
    }

    public void tryScheduleMultiAttack(Entity target) {
        if (target == null || getPlayerData().getEntity().level().isClientSide || executingMultiAttack) {
            return;
        }
        int attackCount = MultiAttackHelper.rollAttackCount(getPlayerData().getEntity());
        if (attackCount <= 1) {
            cancelMultiAttack();
            return;
        }

        multiAttackTargetId = target.getId();
        multiAttackCount = attackCount;
        multiAttackNextIndex = 1;
        multiAttackSequenceTick = 0;
        multiAttackCooldownTicks = Math.max(attackCount, Mth.ceil(getPlayerData().getEntity().getCurrentItemAttackStrengthDelay()));
        multiAttackHand = InteractionHand.MAIN_HAND;
        int[] extraSwingStarts = MultiAttackHelper.createAttackStartTicks(multiAttackCooldownTicks, multiAttackCount, multiAttackNextIndex);
        int swingDurationTicks = MultiAttackHelper.getSequenceSwingDurationTicks(multiAttackCooldownTicks, multiAttackCount, 6);
        PacketHandler.sendToTrackingAndSelf(new MeleeAttackSequencePacket(
                getPlayerData().getEntity().getId(), extraSwingStarts, swingDurationTicks), getPlayerData().getEntity());
    }

    private boolean hasPendingMultiAttack() {
        return multiAttackTargetId != -1 && multiAttackNextIndex < multiAttackCount;
    }

    private void tickMultiAttack() {
        if (!hasPendingMultiAttack()) {
            return;
        }
        multiAttackSequenceTick++;
        int attackStartTick = MultiAttackHelper.getAttackStartTick(multiAttackCooldownTicks, multiAttackCount, multiAttackNextIndex);
        if (multiAttackSequenceTick < attackStartTick) {
            return;
        }

        Entity target = null;
        if (getPlayerData().getEntity().level() instanceof ServerLevel serverLevel) {
            target = serverLevel.getEntity(multiAttackTargetId);
        }
        if (!isValidMultiAttackTarget(target)) {
            cancelMultiAttack();
            return;
        }

        performSecondaryAttack(target);
        multiAttackNextIndex++;
        if (!hasPendingMultiAttack()) {
            cancelMultiAttack();
        }
    }

    private boolean isValidMultiAttackTarget(Entity target) {
        if (target == null || target.isRemoved() || !target.isAlive() || !target.isAttackable()) {
            return false;
        }
        double reach = getPlayerData().getEntity().getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
        return getPlayerData().getEntity().distanceToSqr(target) <= Mth.square(reach);
    }

    private void performSecondaryAttack(Entity target) {
        int expectedTicker = Math.min(multiAttackSequenceTick, multiAttackCooldownTicks);
        int fullStrengthTicker = Math.max(multiAttackCooldownTicks, Mth.ceil(getPlayerData().getEntity().getCurrentItemAttackStrengthDelay()));
        executingMultiAttack = true;
        try {
            setAttackStrengthTicks(fullStrengthTicker);
            getPlayerData().getEntity().attack(target);
        } finally {
            setAttackStrengthTicks(expectedTicker);
            executingMultiAttack = false;
        }
    }

    private void cancelMultiAttack() {
        multiAttackTargetId = -1;
        multiAttackCount = 1;
        multiAttackNextIndex = 1;
        multiAttackSequenceTick = 0;
        multiAttackCooldownTicks = 0;
        multiAttackHand = InteractionHand.MAIN_HAND;
    }

}
