package com.chaosbuffalo.mknpc.entity.ai.goal;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.utils.EntityUtils;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.entity.ai.memory.MKMemoryModuleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.common.ToolActions;

import java.util.EnumSet;
import java.util.Optional;

public class MKBlockGoal extends Goal {

    private final MKEntity entity;
    private LivingEntity target;

    private int currentDelay;

    private int currentHold;

    public static final ResourceLocation BLOCK_TIMER = new ResourceLocation(MKNpc.MODID, "ai_block_cooldown");


    public MKBlockGoal(MKEntity entity) {
        this.entity = entity;
        this.target = null;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    protected double getAttackReachSqr(LivingEntity attackTarget) {
        double range = attackTarget.getAttribute(MKAttributes.ATTACK_REACH).getValue() * 2.5;
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
        return (isBlocking || currentDelay > 0) && targetPresent && currentHold > 0 && EntityUtils.isInFrontOf(entity, target)
                && !maybeEndBecauseOfConsider();
    }

    public boolean shouldConsiderBlocking(LivingEntity target) {
        //FIXME: maybe tag our swords with the sword tag and use tags here
        return target.getMainHandItem().getItem() instanceof SwordItem && MKCore.getEntityData(target).map(
                cap -> !target.isBlocking() && cap.getCombatExtension().getEntityTicksSinceLastSwing() >=
                        EntityUtils.getCooldownPeriod(target)).orElse(false);
    }


    public boolean isPoiseBroke() {
        return MKCore.getEntityData(entity).map(cap -> cap.getStats().isPoiseBroke()).orElse(false);
    }

    public boolean isOnCooldown() {
        return MKCore.getEntityData(entity).map(cap -> cap.getStats().getTimer(BLOCK_TIMER) > 0).orElse(true);
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
            if (!(entity.getMainHandItem().canPerformAction(ToolActions.SHIELD_BLOCK)
                    || entity.getOffhandItem().canPerformAction(ToolActions.SHIELD_BLOCK))) {
                return false;
            }
            LivingEntity target = targetOpt.get();
            if (EntityUtils.isInFrontOf(target, entity) && isInMeleeRange(target) && shouldConsiderBlocking(target)) {
                this.target = target;
                return true;
            }
        }
        return false;
    }

    protected InteractionHand getBlockingHand() {
        if (entity.getOffhandItem().canPerformAction(ToolActions.SHIELD_BLOCK) &&
                entity.getOffhandItem().getItem() instanceof ShieldItem) {
            return InteractionHand.OFF_HAND;
        } else {
            return InteractionHand.MAIN_HAND;
        }
    }

    @Override
    public void start() {
        currentDelay = entity.getRandom().nextInt(entity.getBlockDelay());
        currentHold = entity.getRandom().nextIntBetweenInclusive(entity.getBlockHold() / 2, entity.getBlockHold() * 2);
    }

    @Override
    public void tick() {
        super.tick();
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
        MKCore.getEntityData(entity).ifPresent(x -> x.getStats().setTimer(BLOCK_TIMER,
                entity.getRandom().nextIntBetweenInclusive(entity.getBlockCooldown() / 2, entity.getBlockCooldown() * 3)));
    }
}
