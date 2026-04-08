package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.CombatExtensionModule;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.MultiAttackHelper;
import com.chaosbuffalo.mkcore.core.combat.DualWieldManager;
import com.chaosbuffalo.mkcore.core.combat.MultiAttackState;
import com.chaosbuffalo.mkcore.core.combat.MeleeSequenceTimingManager;
import com.chaosbuffalo.mkcore.core.combat.MeleeSequenceTimings;
import com.chaosbuffalo.mkcore.events.PostAttackEvent;
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
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;

public class PlayerCombatExtensionModule extends CombatExtensionModule implements ISyncGroupProvider {
    private static final ResourceLocation blockMaxPoiseBonusId = MKCore.id("block_skill_modifier");
    private static final int NO_TARGET_ID = -1;
    private final SyncGroup syncGroup = new SyncGroup();
    private final SyncInt currentProjectileHitCount = new SyncInt(0);
    private final EnumMap<InteractionHand, Integer> multiAttackTargetIds = new EnumMap<>(InteractionHand.class);
    private final EnumMap<InteractionHand, MultiAttackState> multiAttackStates = new EnumMap<>(InteractionHand.class);


    public PlayerCombatExtensionModule(MKPlayerData playerData) {
        super(playerData);
        syncGroup.addPrivate("projectileHits", currentProjectileHitCount);
        for (InteractionHand hand : InteractionHand.values()) {
            multiAttackTargetIds.put(hand, NO_TARGET_ID);
            multiAttackStates.put(hand, new MultiAttackState(hand));
        }
    }

    @Override
    public SyncGroup getSyncGroup() {
        return syncGroup;
    }

    @Override
    public void tick() {
        super.tick();
        setAttackStrengthTicks(InteractionHand.MAIN_HAND, getPlayerData().getEntity().attackStrengthTicker);
        getHandState(InteractionHand.OFF_HAND).tickAttackStrengthTicker();
        if (getPlayerData().getEntity().level().isClientSide) {
            for (InteractionHand hand : InteractionHand.values()) {
                var sequence = getHandState(hand).getVisualMeleeAttackSequence();
                sequence.tick();
                sequence.consumeSwingStartedThisTick();
            }
        }
        tickQueuedDualWieldAttacks();
        tickMultiAttack(InteractionHand.MAIN_HAND);
        tickMultiAttack(InteractionHand.OFF_HAND);
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
        return isExecutingMultiAttack(InteractionHand.MAIN_HAND) || isExecutingMultiAttack(InteractionHand.OFF_HAND);
    }

    public boolean isExecutingMultiAttack(InteractionHand hand) {
        return getMultiAttackState(hand).isExecuting();
    }

    public void startVisualMeleeAttackSequence(InteractionHand hand, int[] swingStartTicks, int[] swingDurationTicks) {
        getHandState(hand).getVisualMeleeAttackSequence().start(swingStartTicks, swingDurationTicks);
    }

    public void startVisualMeleeAttackSequence(int[] swingStartTicks, int[] swingDurationTicks) {
        startVisualMeleeAttackSequence(InteractionHand.MAIN_HAND, swingStartTicks, swingDurationTicks);
    }

    public float getVisualMeleeAttackAnim(InteractionHand hand, float partialTicks) {
        return getHandState(hand).getVisualMeleeAttackSequence().getAttackAnim(partialTicks);
    }

    public float getVisualMeleeAttackAnim(float partialTicks) {
        return getVisualMeleeAttackAnim(InteractionHand.MAIN_HAND, partialTicks);
    }

    public boolean hasVisualMeleeAttackSequence(InteractionHand hand) {
        return getHandState(hand).getVisualMeleeAttackSequence().hasSequence();
    }

    public boolean hasVisualMeleeAttackSequence() {
        return hasVisualMeleeAttackSequence(InteractionHand.MAIN_HAND);
    }

    public int getCurrentLocalSwingVariant(InteractionHand hand) {
        return getHandState(hand).getVisualMeleeAttackSequence().getLocalSwingVariant();
    }

    public int getCurrentLocalSwingVariant() {
        return getCurrentLocalSwingVariant(InteractionHand.MAIN_HAND);
    }

    public int getCurrentStrikePoseIndex(InteractionHand hand) {
        return getCurrentLocalSwingVariant(hand) - 1;
    }

    public int getCurrentStrikePoseIndex() {
        return getCurrentStrikePoseIndex(InteractionHand.MAIN_HAND);
    }

    public int getCurrentPrimarySwingVariant(InteractionHand hand) {
        return getHandState(hand).getLocalSwingVariant();
    }

    public int getCurrentPrimarySwingVariant() {
        return getCurrentPrimarySwingVariant(InteractionHand.MAIN_HAND);
    }

    public boolean shouldQueueAttack(InteractionHand hand) {
        return !isHandReady(hand);
    }

    public void queueAttack(Entity target, InteractionHand hand) {
        if (target == null || isExecutingMultiAttack() || hasPendingMultiAttack()) {
            getHandState(hand).clearQueuedTarget();
            return;
        }
        getHandState(hand).setQueuedTargetId(target.getId());
    }

    public void recordLocalPrimarySwing(InteractionHand hand) {
        if (!getPlayerData().getEntity().level().isClientSide) {
            return;
        }
        getHandState(hand).incrementLocalSwingVariant();
    }

    public void onLocalPrimaryAttackCommitted(Entity target, InteractionHand hand) {
        if (!getPlayerData().getEntity().level().isClientSide) {
            return;
        }
        executeWithAttackHand(hand, () -> {
            recordLocalPrimarySwing(hand);
            recordSwingHit();
            NeoForge.EVENT_BUS.post(new PostAttackEvent(getPlayerData(), target, false, hand));
        });
    }

    public boolean shouldHandleCustomMeleeInput(Entity target) {
        if (target == null || !target.isAttackable()) {
            return false;
        }
        return DualWieldManager.canUseCustomMelee(getPlayerData().getEntity(), InteractionHand.MAIN_HAND);
    }

    public boolean usesCustomMainhandMelee() {
        return DualWieldManager.canUseCustomMelee(getPlayerData().getEntity(), InteractionHand.MAIN_HAND);
    }

    public boolean isDualWieldingMeleeWeapons() {
        return DualWieldManager.canUseForAttack(getPlayerData().getEntity(), InteractionHand.MAIN_HAND) &&
                DualWieldManager.canUseForAttack(getPlayerData().getEntity(), InteractionHand.OFF_HAND);
    }

    public float getAttackStrengthScale(InteractionHand hand, float partialTicks) {
        int requiredTicks = getRequiredAttackStrengthTicks(hand);
        if (requiredTicks <= 0) {
            return 1.0F;
        }
        return Mth.clamp((getAttackStrengthTicks(hand) + partialTicks) / (float) requiredTicks, 0.0F, 1.0F);
    }

    public int getRequiredAttackStrengthTicksForHand(InteractionHand hand) {
        return getRequiredAttackStrengthTicks(hand);
    }

    public void handleLocalMeleeAttackRequest(Entity target) {
        List<InteractionHand> hands = selectHandsForAttackRequest();
        if (hands.isEmpty()) {
            return;
        }
        if (areHandsReady(hands)) {
            executeAttackHands(target, hands, true);
        } else {
            queueAttackHands(target, hands);
        }
    }

    public void handleServerMeleeAttackRequest(Entity target) {
        List<InteractionHand> hands = selectHandsForAttackRequest();
        if (hands.isEmpty()) {
            return;
        }
        if (areHandsReady(hands)) {
            executeAttackHands(target, hands, false);
        } else {
            queueAttackHands(target, hands);
        }
    }

    public void tryScheduleMultiAttack(Entity target, InteractionHand hand) {
        if (target == null || getPlayerData().getEntity().level().isClientSide || isExecutingMultiAttack(hand)) {
            return;
        }
        int attackCount = MultiAttackHelper.rollAttackCount(getPlayerData().getEntity());
        if (attackCount <= 1) {
            cancelMultiAttack(hand);
            return;
        }

        MultiAttackState state = getMultiAttackState(hand);
        multiAttackTargetIds.put(hand, target.getId());
        state.setAttackCount(attackCount);
        state.setNextIndex(1);
        state.setSequenceTick(0);
        state.setCooldownTicks(Math.max(attackCount, getRequiredAttackStrengthTicks(hand)));
        int comboSwingCountBeforeSequence = Math.max(0, getCurrentSwingCount() - 1);
        MeleeSequenceTimings visualTimings = MeleeSequenceTimingManager.resolve(getPlayerData().getEntity(), attackCount,
                0, state.getCooldownTicks(), getSwingDurationTicks(hand), comboSwingCountBeforeSequence);
        MeleeSequenceTimings pendingTimings = MeleeSequenceTimingManager.resolve(getPlayerData().getEntity(), attackCount,
                state.getNextIndex(), state.getCooldownTicks(), getSwingDurationTicks(hand), comboSwingCountBeforeSequence);
        state.setStartTicks(pendingTimings.swingStartTicks());
        PacketHandler.sendToTrackingAndSelf(new MeleeAttackSequencePacket(
                getPlayerData().getEntity().getId(), hand,
                visualTimings.swingStartTicks(), visualTimings.swingDurationTicks()), getPlayerData().getEntity());
    }

    private boolean hasPendingMultiAttack() {
        return hasPendingMultiAttack(InteractionHand.MAIN_HAND) || hasPendingMultiAttack(InteractionHand.OFF_HAND);
    }

    private boolean hasPendingMultiAttack(InteractionHand hand) {
        return multiAttackTargetIds.get(hand) != NO_TARGET_ID && getMultiAttackState(hand).hasPendingAttack();
    }

    private void tickQueuedDualWieldAttacks() {
        List<InteractionHand> readyHands = new ArrayList<>();
        for (InteractionHand hand : InteractionHand.values()) {
            if (getHandState(hand).getQueuedTargetId() != NO_TARGET_ID && isHandReady(hand)) {
                readyHands.add(hand);
            }
        }
        if (readyHands.isEmpty()) {
            return;
        }
        readyHands.sort(Comparator.comparingInt(Enum::ordinal));
        Entity target = getPlayerData().getEntity().level().getEntity(getHandState(readyHands.get(0)).getQueuedTargetId());
        if (!isValidMultiAttackTarget(target)) {
            clearQueuedDualWieldAttacks();
            return;
        }
        executeAttackHands(target, readyHands, getPlayerData().getEntity().level().isClientSide);
    }

    private List<InteractionHand> selectHandsForAttackRequest() {
        if (!usesCustomMainhandMelee()) {
            return List.of();
        }
        List<InteractionHand> eligible = new ArrayList<>();
        eligible.add(InteractionHand.MAIN_HAND);
        if (DualWieldManager.canUseForAttack(getPlayerData().getEntity(), InteractionHand.MAIN_HAND) &&
                DualWieldManager.canUseForAttack(getPlayerData().getEntity(), InteractionHand.OFF_HAND)) {
            eligible.add(InteractionHand.OFF_HAND);
        }
        if (eligible.isEmpty()) {
            return List.of();
        }

        List<InteractionHand> ready = new ArrayList<>();
        for (InteractionHand hand : eligible) {
            if (isHandReady(hand)) {
                ready.add(hand);
            }
        }
        if (!ready.isEmpty()) {
            return ready;
        }

        int earliestTicks = Integer.MAX_VALUE;
        for (InteractionHand hand : eligible) {
            earliestTicks = Math.min(earliestTicks, getRemainingCooldownTicks(hand));
        }
        List<InteractionHand> queued = new ArrayList<>();
        for (InteractionHand hand : eligible) {
            if (getRemainingCooldownTicks(hand) == earliestTicks) {
                queued.add(hand);
            }
        }
        return queued;
    }

    private boolean areHandsReady(List<InteractionHand> hands) {
        for (InteractionHand hand : hands) {
            if (!isHandReady(hand)) {
                return false;
            }
        }
        return true;
    }

    private boolean isHandReady(InteractionHand hand) {
        return getAttackStrengthTicks(hand) >= getRequiredAttackStrengthTicks(hand);
    }

    private int getRemainingCooldownTicks(InteractionHand hand) {
        return Math.max(0, getRequiredAttackStrengthTicks(hand) - getAttackStrengthTicks(hand));
    }

    private void queueAttackHands(Entity target, List<InteractionHand> hands) {
        clearQueuedDualWieldAttacks();
        for (InteractionHand hand : hands) {
            getHandState(hand).setQueuedTargetId(target.getId());
        }
    }

    private void clearQueuedDualWieldAttacks() {
        for (InteractionHand hand : InteractionHand.values()) {
            getHandState(hand).clearQueuedTarget();
        }
    }

    private void executeAttackHands(Entity target, List<InteractionHand> hands, boolean clientSide) {
        for (InteractionHand hand : hands) {
            if (clientSide) {
                performLocalHandAttack(target, hand);
            } else {
                performServerHandAttack(target, hand);
            }
            getHandState(hand).clearQueuedTarget();
        }
    }

    private void performLocalHandAttack(Entity target, InteractionHand hand) {
        setAttackStrengthTicks(hand, 0);
        startVisualMeleeAttackSequence(hand, new int[]{0}, new int[]{getSwingDurationTicks(hand)});
        onLocalPrimaryAttackCommitted(target, hand);
    }

    private void performServerHandAttack(Entity target, InteractionHand hand) {
        startVisualMeleeAttackSequence(hand, new int[]{0}, new int[]{getSwingDurationTicks(hand)});
        PacketHandler.sendToTracking(new MeleeAttackSequencePacket(getPlayerData().getEntity().getId(), hand,
                new int[]{0}, new int[]{getSwingDurationTicks(hand)}), getPlayerData().getEntity());
        if (hand == InteractionHand.MAIN_HAND) {
            executeWithAttackHand(hand, () -> {
                setAttackStrengthTicks(hand, getRequiredAttackStrengthTicks(hand));
                getPlayerData().getEntity().attack(target);
            });
            return;
        }
        performProjectedOffhandAttack(target);
    }

    private void performProjectedOffhandAttack(Entity target) {
        var player = getPlayerData().getEntity();
        var inventory = player.getInventory();
        int selectedSlot = inventory.selected;
        var mainHand = inventory.items.get(selectedSlot).copy();
        var offHand = inventory.offhand.get(0).copy();
        int preservedMainhandTicker = getAttackStrengthTicks(InteractionHand.MAIN_HAND);
        int offhandRequiredTicks = getRequiredAttackStrengthTicks(InteractionHand.OFF_HAND);
        executeWithAttackHand(InteractionHand.OFF_HAND, () -> {
            try {
                setAttackStrengthTicks(InteractionHand.OFF_HAND, offhandRequiredTicks);
                inventory.items.set(selectedSlot, offHand);
                inventory.offhand.set(0, mainHand);
                setAttackStrengthTicks(InteractionHand.MAIN_HAND, offhandRequiredTicks);
                player.attack(target);
            } finally {
                int offhandPostAttackTicks = getAttackStrengthTicks(InteractionHand.OFF_HAND);
                ItemStack projectedMainHand = inventory.items.get(selectedSlot).copy();
                inventory.items.set(selectedSlot, mainHand);
                inventory.offhand.set(0, projectedMainHand);
                inventory.setChanged();
                player.inventoryMenu.broadcastChanges();
                setAttackStrengthTicks(InteractionHand.OFF_HAND, Math.max(0, offhandPostAttackTicks - offhandRequiredTicks));
                setAttackStrengthTicks(InteractionHand.MAIN_HAND, preservedMainhandTicker);
            }
        });
    }

    private int getSwingDurationTicks(InteractionHand hand) {
        return 6;
    }

    private void tickMultiAttack(InteractionHand hand) {
        if (!hasPendingMultiAttack(hand)) {
            return;
        }
        MultiAttackState state = getMultiAttackState(hand);
        state.incrementSequenceTick();
        int attackStartTick = state.getStartTicks()[state.getNextIndex() - 1];
        if (state.getSequenceTick() < attackStartTick) {
            return;
        }

        Entity target = null;
        if (getPlayerData().getEntity().level() instanceof ServerLevel serverLevel) {
            target = serverLevel.getEntity(multiAttackTargetIds.get(hand));
        }
        if (!isValidMultiAttackTarget(target)) {
            cancelMultiAttack(hand);
            return;
        }

        performSecondaryAttack(target, hand);
        state.setNextIndex(state.getNextIndex() + 1);
        if (!hasPendingMultiAttack(hand)) {
            cancelMultiAttack(hand);
        }
    }

    private boolean isValidMultiAttackTarget(Entity target) {
        if (target == null || target.isRemoved() || !target.isAlive() || !target.isAttackable()) {
            return false;
        }
        double reach = getPlayerData().getEntity().getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
        return getPlayerData().getEntity().distanceToSqr(target) <= Mth.square(reach);
    }

    private void performSecondaryAttack(Entity target, InteractionHand hand) {
        MultiAttackState state = getMultiAttackState(hand);
        int expectedTicker = Math.min(state.getSequenceTick(), state.getCooldownTicks());
        int fullStrengthTicker = Math.max(state.getCooldownTicks(), getRequiredAttackStrengthTicks(hand));
        state.setExecuting(true);
        try {
            setAttackStrengthTicks(hand, fullStrengthTicker);
            if (hand == InteractionHand.MAIN_HAND) {
                executeWithAttackHand(hand, () -> getPlayerData().getEntity().attack(target));
            } else {
                performProjectedOffhandAttack(target);
            }
        } finally {
            setAttackStrengthTicks(hand, expectedTicker);
            state.setExecuting(false);
        }
    }

    private void cancelMultiAttack(InteractionHand hand) {
        multiAttackTargetIds.put(hand, NO_TARGET_ID);
        getMultiAttackState(hand).reset();
    }

    @Override
    public int getRequiredAttackStrengthTicks(InteractionHand hand) {
        return Math.max(1, Mth.ceil(resolveAttackSpeedDelay(hand)));
    }

    private float resolveAttackSpeedDelay(InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            double attackSpeed = getPlayerData().getEntity().getAttributeValue(Attributes.ATTACK_SPEED);
            return (float) (20.0D / Math.max(attackSpeed, 0.001D));
        }
        double currentAttackSpeed = getPlayerData().getEntity().getAttributeValue(Attributes.ATTACK_SPEED);
        double mainHandAttackSpeed = getItemAddValueModifier(getPlayerData().getEntity().getMainHandItem(), Attributes.ATTACK_SPEED);
        double selectedHandAttackSpeed = getItemAddValueModifier(getPlayerData().getEntity().getItemInHand(hand), Attributes.ATTACK_SPEED);
        double effectiveAttackSpeed = currentAttackSpeed - mainHandAttackSpeed + selectedHandAttackSpeed;
        return (float) (20.0D / Math.max(effectiveAttackSpeed, 0.001D));
    }

    private static double getItemAddValueModifier(net.minecraft.world.item.ItemStack stack,
                                                  net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute) {
        final double[] total = {0.0D};
        stack.getAttributeModifiers().forEach(net.minecraft.world.entity.EquipmentSlot.MAINHAND, (holder, modifier) -> {
            if (holder.equals(attribute) && modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
                total[0] += modifier.amount();
            }
        });
        return total[0];
    }

    private int getRequiredAttackStrengthTicks() {
        return getRequiredAttackStrengthTicks(InteractionHand.MAIN_HAND);
    }

    private MultiAttackState getMultiAttackState(InteractionHand hand) {
        return multiAttackStates.get(hand);
    }

}
