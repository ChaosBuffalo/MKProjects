package com.chaosbuffalo.mknpc.entity.ai.goal;

import com.chaosbuffalo.mkcore.core.CombatExtensionModule;
import com.chaosbuffalo.mkcore.core.MKEntityData;
import com.chaosbuffalo.mkcore.core.MultiAttackHelper;
import com.chaosbuffalo.mkcore.core.combat.MeleeSequenceTimingManager;
import com.chaosbuffalo.mkcore.core.combat.MeleeSequenceTimings;
import com.chaosbuffalo.mkcore.events.PostAttackEvent;
import com.chaosbuffalo.mkcore.network.MeleeAttackSequencePacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.utils.EntityUtils;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.entity.ai.memory.MKMemoryModuleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

import java.util.EnumSet;
import java.util.Optional;

public class MKMeleeAttackGoal extends Goal {
    private final MKEntity entity;
    private LivingEntity target;
    private LivingEntity multiAttackTarget;
    private int multiAttackCount;
    private int multiAttackNextIndex;
    private int multiAttackSequenceTick;
    private int multiAttackCooldownTicks;
    private int[] multiAttackStartTicks = new int[0];
    private boolean executingMultiAttack;

    @Override
    public boolean canUse() {
        Brain<?> brain = entity.getBrain();
        Optional<LivingEntity> targetOpt = brain.getMemory(MKMemoryModuleTypes.THREAT_TARGET.get());
        if (targetOpt.isPresent()) {
            LivingEntity target = targetOpt.get();
            if (isInMeleeRange(target)) {
                this.target = target;
                return true;
            }
        }
        return false;
    }


    public MKMeleeAttackGoal(MKEntity entity) {
        this.entity = entity;
        this.target = null;
        this.multiAttackTarget = null;
        this.multiAttackCount = 1;
        this.multiAttackNextIndex = 1;
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
        tickMultiAttack();

        boolean strafingForwards = false;
        if (entity.distanceTo(target) >= getAttackReach(target) * entity.getMeleeApproachDistanceMultiplier()) {
            entity.getNavigation().moveTo(target, entity.getLungeSpeed());
            strafingForwards = true;
        }

        entity.getMoveControl().strafe(strafingForwards ? 1.0F : -1.0F, 0);

        entity.lookAt(target, 30.0f, 30.0f);
//        entity.getLookControl().setLookAt(target, 30.0f, 30.0f);
        double cooldownPeriod = entity.getMeleeCooldownPeriod();
        int ticksSinceSwing = entity.getTicksSinceLastSwing();
        if (!hasPendingMultiAttack() && ticksSinceSwing >= cooldownPeriod && isInReach(target) && entity.getSensing().hasLineOfSight(target) && EntityUtils.isInFrontOf(entity, target)) {
            performAttack(target);
        }

    }

    protected void performAttack(LivingEntity enemy) {
        performAttack(enemy, false);
    }

    protected void performAttack(LivingEntity enemy, boolean secondaryAttack) {
        MKEntityData cap = entity.getEntityDataCap();
        CombatExtensionModule combat = cap.getCombatExtension();
        if (!secondaryAttack) {
            int attackCount = scheduleMultiAttack(enemy);
            int cooldownTicks = Math.max(attackCount, (int) Math.ceil(entity.getMeleeCooldownPeriod()));
            MeleeSequenceTimings timings = MeleeSequenceTimingManager.resolve(entity, attackCount, 0, cooldownTicks,
                    entity.getMeleeSwingDurationTicks(), combat.getCurrentSwingCount());
            PacketHandler.sendToTrackingAndSelf(new MeleeAttackSequencePacket(
                    entity.getId(),
                    timings.swingStartTicks(),
                    timings.swingDurationTicks()), entity);
        }
        boolean didAttack = entity.doHurtTarget(enemy);
        ItemStack mainHand = entity.getMainHandItem();
        if (didAttack && !mainHand.isEmpty()) {
            mainHand.getItem().hurtEnemy(mainHand, enemy, entity);
        }
        if (!secondaryAttack) {
            entity.resetSwing();
        }
        combat.recordSwingHit();
        NeoForge.EVENT_BUS.post(new PostAttackEvent(cap, enemy, secondaryAttack));
        if (!secondaryAttack && combat.getCurrentSwingCount() > 0 && combat.getCurrentSwingCount() % getComboCount() == 0) {
            entity.subtractFromTicksSinceLastSwing(getComboDelay());
        }
    }

    private int scheduleMultiAttack(LivingEntity enemy) {
        if (executingMultiAttack) {
            return 1;
        }
        int attackCount = MultiAttackHelper.rollAttackCount(entity);
        if (attackCount <= 1) {
            cancelMultiAttack();
            return 1;
        }

        multiAttackTarget = enemy;
        multiAttackCount = attackCount;
        multiAttackNextIndex = 1;
        multiAttackSequenceTick = 0;
        multiAttackCooldownTicks = Math.max(attackCount, (int) Math.ceil(entity.getMeleeCooldownPeriod()));
        MeleeSequenceTimings timings = MeleeSequenceTimingManager.resolve(entity, multiAttackCount, 1,
                multiAttackCooldownTicks, entity.getMeleeSwingDurationTicks(),
                entity.getEntityDataCap().getCombatExtension().getCurrentSwingCount());
        multiAttackStartTicks = timings.swingStartTicks();
        return attackCount;
    }

    private boolean hasPendingMultiAttack() {
        return multiAttackTarget != null && multiAttackNextIndex < multiAttackCount;
    }

    private void tickMultiAttack() {
        if (!hasPendingMultiAttack()) {
            return;
        }
        multiAttackSequenceTick++;
        int attackStartTick = multiAttackStartTicks[multiAttackNextIndex - 1];
        if (multiAttackSequenceTick < attackStartTick) {
            return;
        }
        if (!isValidMultiAttackTarget(multiAttackTarget)) {
            cancelMultiAttack();
            return;
        }
        executingMultiAttack = true;
        try {
            performAttack(multiAttackTarget, true);
        } finally {
            executingMultiAttack = false;
        }
        multiAttackNextIndex++;
        if (!hasPendingMultiAttack()) {
            cancelMultiAttack();
        }
    }

    private boolean isValidMultiAttackTarget(LivingEntity target) {
        return target != null && target.isAlive() && isInReach(target) && entity.getSensing().hasLineOfSight(target) &&
                EntityUtils.isInFrontOf(entity, target);
    }

    private void cancelMultiAttack() {
        multiAttackTarget = null;
        multiAttackCount = 1;
        multiAttackNextIndex = 1;
        multiAttackSequenceTick = 0;
        multiAttackCooldownTicks = 0;
        multiAttackStartTicks = new int[0];
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
        cancelMultiAttack();
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

    @Override
    public boolean canContinueToUse() {
        Brain<?> brain = entity.getBrain();
        Optional<LivingEntity> targetOpt = brain.getMemory(MKMemoryModuleTypes.THREAT_TARGET.get());
        return hasPendingMultiAttack() || target != null && targetOpt.map((ent) -> ent.is(target) && isInMeleeRange(ent)).orElse(false);
    }
}
