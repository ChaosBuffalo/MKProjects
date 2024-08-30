package com.chaosbuffalo.mknpc.entity.ai.goal;

import com.chaosbuffalo.mkcore.core.CombatExtensionModule;
import com.chaosbuffalo.mkcore.core.MKEntityData;
import com.chaosbuffalo.mkcore.events.PostAttackEvent;
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

        boolean strafingForwards = false;
        if (entity.distanceTo(target) >= (getAttackReach(target) / 2.0)) {
            entity.getNavigation().moveTo(target, entity.getLungeSpeed());
            strafingForwards = true;
        }

        entity.getMoveControl().strafe(strafingForwards ? 1.0F : -1.0F, 0);

        entity.lookAt(target, 30.0f, 30.0f);
//        entity.getLookControl().setLookAt(target, 30.0f, 30.0f);
        double cooldownPeriod = EntityUtils.getCooldownPeriod(entity);
        int ticksSinceSwing = entity.getTicksSinceLastSwing();
        if (ticksSinceSwing >= cooldownPeriod && isInReach(target) && entity.getSensing().hasLineOfSight(target) && EntityUtils.isInFrontOf(entity, target)) {
            performAttack(target);
        }

    }

    protected void performAttack(LivingEntity enemy) {
        entity.swing(InteractionHand.MAIN_HAND);
        boolean didAttack = entity.doHurtTarget(enemy);
        ItemStack mainHand = entity.getMainHandItem();
        if (didAttack && !mainHand.isEmpty()) {
            mainHand.getItem().hurtEnemy(mainHand, enemy, entity);
        }
        entity.resetSwing();

        MKEntityData cap = entity.getEntityDataCap();
        CombatExtensionModule combat = cap.getCombatExtension();
        combat.recordSwingHit();
        NeoForge.EVENT_BUS.post(new PostAttackEvent(cap));
        if (combat.getCurrentSwingCount() > 0 && combat.getCurrentSwingCount() % getComboCount() == 0) {
            entity.subtractFromTicksSinceLastSwing(getComboDelay());
        }
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
        return target != null && targetOpt.map((ent) -> ent.is(target) && isInMeleeRange(ent)).orElse(false);
    }
}
