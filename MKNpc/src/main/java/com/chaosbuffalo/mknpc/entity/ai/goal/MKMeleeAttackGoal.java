package com.chaosbuffalo.mknpc.entity.ai.goal;

import com.chaosbuffalo.mkcore.core.CombatExtensionModule;
import com.chaosbuffalo.mkcore.core.MKEntityData;
import com.chaosbuffalo.mkcore.core.MultiAttackHelper;
import com.chaosbuffalo.mkcore.core.combat.MKMeleeManager;
import com.chaosbuffalo.mkcore.core.combat.MultiAttackState;
import com.chaosbuffalo.mkcore.core.combat.MeleeSequenceTimingManager;
import com.chaosbuffalo.mkcore.core.combat.MeleeSequenceTimings;
import com.chaosbuffalo.mkcore.events.PostAttackEvent;
import com.chaosbuffalo.mkcore.network.MeleeAttackSequencePacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.utils.EntityUtils;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.entity.combat.NpcMeleeAttackExecutor;
import com.chaosbuffalo.mknpc.entity.ai.memory.MKMemoryModuleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class MKMeleeAttackGoal extends Goal {
    private final MKEntity entity;
    private LivingEntity target;
    private final EnumMap<InteractionHand, LivingEntity> multiAttackTargets = new EnumMap<>(InteractionHand.class);
    private final EnumMap<InteractionHand, MultiAttackState> multiAttackStates = new EnumMap<>(InteractionHand.class);

    @Override
    public boolean canUse() {
        if (entity.getCombatMoveType() != MKEntity.CombatMoveType.MELEE) {
            return false;
        }
        Brain<?> brain = entity.getBrain();
        Optional<LivingEntity> targetOpt = brain.getMemory(MKMemoryModuleTypes.THREAT_TARGET.get());
        if (targetOpt.isPresent()) {
            LivingEntity target = targetOpt.get();
            if (target.isAlive() && entity.isInVisualMeleeWindupRange(target)) {
                this.target = target;
                return true;
            }
        }
        return false;
    }


    public MKMeleeAttackGoal(MKEntity entity) {
        this.entity = entity;
        this.target = null;
        for (InteractionHand hand : InteractionHand.values()) {
            multiAttackTargets.put(hand, null);
            multiAttackStates.put(hand, new MultiAttackState(hand));
        }
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }


    public void start() {
        this.entity.setAggressive(true);
    }

    public int getComboCount() {
        return entity.getAttackComboCount();
    }

    public int getComboDelay() {
        return entity.getAttackComboCooldown();
    }

    @Override
    public void tick() {
        tickMultiAttack(InteractionHand.MAIN_HAND);
        tickMultiAttack(InteractionHand.OFF_HAND);

        boolean strafingForwards = false;
        if (entity.distanceTo(target) >= getAttackReach(target) * entity.getMeleeApproachDistanceMultiplier()) {
            entity.getNavigation().moveTo(target, entity.getLungeSpeed());
            strafingForwards = true;
        }

        entity.getMoveControl().strafe(strafingForwards ? 1.0F : -1.0F, 0);

        entity.lookAt(target, 30.0f, 30.0f);
//        entity.getLookControl().setLookAt(target, 30.0f, 30.0f);
        if (!hasPendingMultiAttack() && isInReach(target) && entity.getSensing().hasLineOfSight(target) && EntityUtils.isInFrontOf(entity, target)) {
            List<InteractionHand> handsToAttack = selectHandsForAttack();
            for (InteractionHand hand : handsToAttack) {
                performAttack(target, hand, false);
            }
        }

    }

    protected void performAttack(LivingEntity enemy, InteractionHand hand, boolean secondaryAttack) {
        MKEntityData cap = entity.getEntityDataCap();
        CombatExtensionModule combat = cap.getCombatExtension();
        if (!secondaryAttack) {
            int attackCount = scheduleMultiAttack(enemy, hand);
            int cooldownTicks = Math.max(attackCount, (int) Math.ceil(entity.getMeleeCooldownPeriod(hand)));
            MeleeSequenceTimings timings = MeleeSequenceTimingManager.resolve(entity, hand, attackCount, 0, cooldownTicks,
                    entity.getMeleeSwingDurationTicks(hand), combat.getCurrentSwingCount());
            PacketHandler.sendToTrackingAndSelf(new MeleeAttackSequencePacket(
                    entity.getId(),
                    hand,
                    timings.swingStartTicks(),
                    timings.swingDurationTicks()), entity);
        }
        boolean didAttack = performHandAttack(enemy, hand, combat);
        if (!secondaryAttack) {
            entity.resetSwing(hand);
        }
        combat.recordSwingHit();
        NeoForge.EVENT_BUS.post(new PostAttackEvent(cap, enemy, secondaryAttack, hand));
        if (!secondaryAttack && combat.getCurrentSwingCount() > 0 && combat.getCurrentSwingCount() % getComboCount() == 0) {
            entity.subtractFromTicksSinceLastSwing(hand, getComboDelay());
        }
    }

    private int scheduleMultiAttack(LivingEntity enemy, InteractionHand hand) {
        if (isExecutingMultiAttack(hand)) {
            return 1;
        }
        int attackCount = MultiAttackHelper.rollAttackCount(entity);
        if (attackCount <= 1) {
            cancelMultiAttack(hand);
            return 1;
        }

        MultiAttackState state = getMultiAttackState(hand);
        multiAttackTargets.put(hand, enemy);
        state.setAttackCount(attackCount);
        state.setNextIndex(1);
        state.setSequenceTick(0);
        state.setCooldownTicks(Math.max(attackCount, (int) Math.ceil(entity.getMeleeCooldownPeriod(hand))));
        MeleeSequenceTimings timings = MeleeSequenceTimingManager.resolve(entity, hand, attackCount, 1,
                state.getCooldownTicks(), entity.getMeleeSwingDurationTicks(hand),
                entity.getEntityDataCap().getCombatExtension().getCurrentSwingCount());
        state.setStartTicks(timings.swingStartTicks());
        return attackCount;
    }

    private boolean hasPendingMultiAttack() {
        return hasPendingMultiAttack(InteractionHand.MAIN_HAND) || hasPendingMultiAttack(InteractionHand.OFF_HAND);
    }

    private boolean hasPendingMultiAttack(InteractionHand hand) {
        return multiAttackTargets.get(hand) != null && getMultiAttackState(hand).hasPendingAttack();
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
        LivingEntity multiAttackTarget = multiAttackTargets.get(hand);
        if (!isValidMultiAttackTarget(multiAttackTarget)) {
            cancelMultiAttack(hand);
            return;
        }
        state.setExecuting(true);
        try {
            performAttack(multiAttackTarget, hand, true);
        } finally {
            state.setExecuting(false);
        }
        state.setNextIndex(state.getNextIndex() + 1);
        if (!hasPendingMultiAttack(hand)) {
            cancelMultiAttack(hand);
        }
    }

    private boolean isValidMultiAttackTarget(LivingEntity target) {
        return target != null && target.isAlive() && isInReach(target) && entity.getSensing().hasLineOfSight(target) &&
                EntityUtils.isInFrontOf(entity, target);
    }

    private void cancelMultiAttack(InteractionHand hand) {
        multiAttackTargets.put(hand, null);
        getMultiAttackState(hand).reset();
    }

    public boolean isInMeleeRange(LivingEntity target) {
        return entity.distanceToSqr(target) <= this.getAttackReachSqr(target);
    }


    public boolean isInReach(LivingEntity target) {
        return entity.distanceToSqr(target) <= (this.getAttackReachSqr(target) * MKNpc.getDifficultyScale(target));
    }

    public void stop() {
        this.entity.setAggressive(false);
        this.target = null;
        cancelMultiAttack(InteractionHand.MAIN_HAND);
        cancelMultiAttack(InteractionHand.OFF_HAND);
    }

    protected double getAttackReach(LivingEntity target) {
        double range = entity.getEntityReach();
        return range * entity.getScale();
    }

    protected double getAttackReachSqr(LivingEntity attackTarget) {
        double range = entity.getEntityReach();
        range *= entity.getScale();
        return range * range;
    }

    private List<InteractionHand> selectHandsForAttack() {
        boolean mainHandDualWieldable = MKMeleeManager.canUseForAttack(entity, InteractionHand.MAIN_HAND);
        if (!mainHandDualWieldable) {
            double cooldownPeriod = entity.getMeleeCooldownPeriod(InteractionHand.MAIN_HAND);
            if (entity.getTicksSinceLastSwing(InteractionHand.MAIN_HAND) >= cooldownPeriod) {
                return List.of(InteractionHand.MAIN_HAND);
            }
            return List.of();
        }

        List<InteractionHand> eligible = new ArrayList<>();
        eligible.add(InteractionHand.MAIN_HAND);
        if (MKMeleeManager.canUseForAttack(entity, InteractionHand.OFF_HAND)) {
            eligible.add(InteractionHand.OFF_HAND);
        }

        List<InteractionHand> ready = new ArrayList<>();
        for (InteractionHand hand : eligible) {
            if (entity.getTicksSinceLastSwing(hand) >= entity.getMeleeCooldownPeriod(hand)) {
                ready.add(hand);
            }
        }
        if (!ready.isEmpty()) {
            ready.sort(Comparator.comparingInt(Enum::ordinal));
            return ready;
        }
        return List.of();
    }

    private boolean performHandAttack(LivingEntity enemy, InteractionHand hand, CombatExtensionModule combat) {
        return NpcMeleeAttackExecutor.executeAttack(entity, enemy, hand, combat);
    }

    @Override
    public boolean canContinueToUse() {
        if (entity.getCombatMoveType() != MKEntity.CombatMoveType.MELEE) {
            return false;
        }
        Brain<?> brain = entity.getBrain();
        Optional<LivingEntity> targetOpt = brain.getMemory(MKMemoryModuleTypes.THREAT_TARGET.get());
        return hasPendingMultiAttack() || target != null &&
                targetOpt.map(ent -> ent.is(target) && ent.isAlive() && entity.isInVisualMeleeWindupRange(ent)).orElse(false);
    }

    private boolean isExecutingMultiAttack(InteractionHand hand) {
        return getMultiAttackState(hand).isExecuting();
    }

    private MultiAttackState getMultiAttackState(InteractionHand hand) {
        return multiAttackStates.get(hand);
    }
}
