package com.chaosbuffalo.mknpc.entity.ai.goal;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.init.CoreEffects;
import com.chaosbuffalo.mkcore.utils.EntityUtils;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.entity.ai.memory.MKMemoryModuleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.neoforged.neoforge.common.ItemAbilities;

import java.util.EnumSet;
import java.util.Optional;

public class MKBlockGoal extends Goal {
    private static final int RECENT_THREAT_TICKS = 12;
    private static final float ATTACK_READY_THRESHOLD = 0.90F;
    private static final float BLOCK_WHEN_ATTACK_READY_CHANCE = 0.20F;

    private final MKEntity entity;
    private LivingEntity target;

    private int currentDelay;

    private int currentHold;

    public static final ResourceLocation BLOCK_TIMER = MKNpc.id("ai_block_cooldown");


    public MKBlockGoal(MKEntity entity) {
        this.entity = entity;
        this.target = null;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    protected double getAttackReachSqr(LivingEntity attackTarget) {
        double range = MKAttributes.getValueSafe(Attributes.ENTITY_INTERACTION_RANGE, attackTarget) * 2.5;
        range *= attackTarget.getScale();
        return range * range;
    }

    public boolean isInMeleeRange(LivingEntity target) {
        return entity.distanceToSqr(target) <= this.getAttackReachSqr(target);
    }

    @Override
    public boolean canContinueToUse() {
        Brain<?> brain = entity.getBrain();
        Optional<LivingEntity> targetOpt = brain.getMemory(MKMemoryModuleTypes.THREAT_TARGET.get());
        boolean isBlocking = entity.isBlocking();
        boolean targetPresent = target != null && targetOpt.map((ent) -> ent.is(target) && isInMeleeRange(ent)).orElse(false);
        if (!(isBlocking || currentDelay > 0) || !targetPresent || currentHold <= 0 || !EntityUtils.isInFrontOf(entity, target)) {
            return false;
        }
        if (!isTargetThreatening(target)) {
            return false;
        }
        if (entity.isBlocking() && isAttackReady() && entity.getRandom().nextFloat() > BLOCK_WHEN_ATTACK_READY_CHANCE) {
            return false;
        }
        return true;
    }

    public boolean shouldConsiderBlocking(LivingEntity target) {
        //FIXME: maybe tag our swords with the sword tag and use tags here
        return target.getMainHandItem().getItem() instanceof SwordItem && MKCore.getEntityData(target).map(
                cap -> !cap.getEffects().isEffectActive(CoreEffects.STUN.get()) && !target.isBlocking() &&
                        cap.getCombatExtension().getAttackStrengthTicks(InteractionHand.MAIN_HAND) >=
                        EntityUtils.getCooldownPeriod(target)).orElse(false);
    }

    private boolean isTargetThreatening(LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (entity.getLastHurtByMob() == target && entity.tickCount - entity.getLastHurtByMobTimestamp() <= RECENT_THREAT_TICKS) {
            return true;
        }
        if (target.isUsingItem()) {
            return true;
        }
        if (target.swinging || target.getAttackAnim(1.0F) > 0.0F) {
            return true;
        }
        if (target instanceof MKEntity mkTarget &&
                (mkTarget.getMeleeWindupProgress(InteractionHand.MAIN_HAND, 1.0F) > 0.0F ||
                        mkTarget.getMeleeWindupProgress(InteractionHand.OFF_HAND, 1.0F) > 0.0F)) {
            return true;
        }
        return shouldConsiderBlocking(target);
    }

    private boolean isAttackReady() {
        double mainCooldown = entity.getMeleeCooldownPeriod(InteractionHand.MAIN_HAND) * ATTACK_READY_THRESHOLD;
        if (entity.getEntityDataCap().getCombatExtension().getAttackStrengthTicks(InteractionHand.MAIN_HAND) >= mainCooldown) {
            return true;
        }
        if (!entity.getOffhandItem().canPerformAction(ItemAbilities.SHIELD_BLOCK)) {
            return false;
        }
        double offCooldown = entity.getMeleeCooldownPeriod(InteractionHand.OFF_HAND) * ATTACK_READY_THRESHOLD;
        return entity.getEntityDataCap().getCombatExtension().getAttackStrengthTicks(InteractionHand.OFF_HAND) >= offCooldown;
    }


    public boolean isPoiseBroke() {
        return entity.getEntityDataCap().getStats().isPoiseBroke();
    }

    public boolean isOnCooldown() {
        return entity.getEntityDataCap().getStats().getTimer(BLOCK_TIMER) > 0;
    }

    public boolean maybeEndBecauseOfConsider() {
        return !shouldConsiderBlocking(target) && entity.getRandom().nextInt(10) == 1;
    }


    @Override
    public boolean canUse() {
        Brain<?> brain = entity.getBrain();
        Optional<LivingEntity> targetOpt = brain.getMemory(MKMemoryModuleTypes.THREAT_TARGET.get());
        if (isOnCooldown()) {
            return false;
        }
        if (targetOpt.isPresent()) {
            if (isPoiseBroke()) {
                return false;
            }
            if (!(entity.getMainHandItem().canPerformAction(ItemAbilities.SHIELD_BLOCK)
                    || entity.getOffhandItem().canPerformAction(ItemAbilities.SHIELD_BLOCK))) {
                return false;
            }
            LivingEntity target = targetOpt.get();
            if (!EntityUtils.isInFrontOf(target, entity) || !isInMeleeRange(target)) {
                return false;
            }
            if (!isTargetThreatening(target)) {
                return false;
            }
            if (isAttackReady() && entity.getRandom().nextFloat() > BLOCK_WHEN_ATTACK_READY_CHANCE) {
                return false;
            }
            if (shouldConsiderBlocking(target)) {
                this.target = target;
                return true;
            }
        }
        return false;
    }

    protected InteractionHand getBlockingHand() {
        if (entity.getOffhandItem().canPerformAction(ItemAbilities.SHIELD_BLOCK) &&
                entity.getOffhandItem().getItem() instanceof ShieldItem) {
            return InteractionHand.OFF_HAND;
        } else {
            return InteractionHand.MAIN_HAND;
        }
    }

    @Override
    public void start() {
        currentDelay = entity.getRandom().nextInt(entity.getBlockDelay());
        int minHold = Math.max(4, Mth.ceil(entity.getBlockHold() * 0.35F));
        int maxHold = Math.max(minHold, entity.getBlockHold());
        currentHold = entity.getRandom().nextIntBetweenInclusive(minHold, maxHold);
    }

    @Override
    public void tick() {
        super.tick();
        if (target == null || !target.isAlive() || !isTargetThreatening(target)) {
            currentHold = 0;
            return;
        }
        currentDelay--;
        if (currentDelay == 0) {
            entity.startUsingItem(getBlockingHand());
        }
        if (currentDelay < 0) {
            currentHold--;
        }
    }


    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public void stop() {
        entity.stopUsingItem();
        target = null;
        int minCooldown = Math.max(entity.getBlockCooldown(), RECENT_THREAT_TICKS);
        int maxCooldown = Math.max(minCooldown, entity.getBlockCooldown() * 3);
        int cd = entity.getRandom().nextIntBetweenInclusive(minCooldown, maxCooldown);
        entity.getEntityDataCap().getStats().setTimer(BLOCK_TIMER, cd);
    }
}
