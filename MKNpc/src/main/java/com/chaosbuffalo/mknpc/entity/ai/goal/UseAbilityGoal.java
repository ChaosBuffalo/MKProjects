package com.chaosbuffalo.mknpc.entity.ai.goal;

import com.chaosbuffalo.mkcore.CoreCapabilities;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityMemories;
import com.chaosbuffalo.mkcore.abilities.ai.BrainAbilityContext;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.entity.ai.memory.MKMemoryModuleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.Optional;

import net.minecraft.world.entity.ai.goal.Goal.Flag;

public class UseAbilityGoal extends Goal {
    public static final int CAN_SEE_TIMEOUT = 30;
    private final MKEntity entity;
    private MKAbility currentAbility;
    private LivingEntity target;
    private int ticksSinceSeenTarget;

    public UseAbilityGoal(MKEntity entity) {
        this.entity = entity;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        ticksSinceSeenTarget = 0;
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public boolean canUse() {
        Optional<MKAbility> abilityOptional = entity.getBrain().getMemory(MKMemoryModuleTypes.CURRENT_ABILITY);
        Optional<LivingEntity> target = entity.getBrain().getMemory(MKAbilityMemories.ABILITY_TARGET.get());
        if (abilityOptional.isPresent() && target.isPresent()) {
            currentAbility = abilityOptional.get();
            LivingEntity targetEntity = target.get();

            if (!canActivate())
                return false;

            if (entity != targetEntity) {
                if (!isInRange(currentAbility, targetEntity))
                    return false;
                if (!entity.getSensing().hasLineOfSight(targetEntity))
                    return false;
            }

            // Now we know we can actually start the cast
            this.target = targetEntity;
            return true;
        } else {
            return false;
        }
    }

    protected boolean isInRange(MKAbility ability, LivingEntity target) {
        float range = ability.getDistance(entity);
        return target.distanceToSqr(entity) <= range * range;
    }

    public boolean canActivate() {
        return entity.getCapability(CoreCapabilities.ENTITY_CAPABILITY).map((entityData) ->
                entityData.getAbilityExecutor().canActivateAbility(currentAbility))
                .orElse(false);
    }

    public boolean canContinueToUse() {
        return ticksSinceSeenTarget < CAN_SEE_TIMEOUT && entity.getCapability(CoreCapabilities.ENTITY_CAPABILITY).map(
                (entityData) -> entityData.getAbilityExecutor().isCasting()).orElse(false) && entity.getBrain()
                .getMemory(MKAbilityMemories.ABILITY_TARGET.get()).map(tar -> tar.isAlive()
                        && tar.is(target)).orElse(false) && entity.getBrain().getMemory(MKMemoryModuleTypes.CURRENT_ABILITY)
                .map(mkAbility -> mkAbility.equals(currentAbility)).orElse(false);
    }

    @Override
    public void start() {
        if (!target.is(entity)) {
            entity.lookAt(target, 360.0f, 360.0f);
            entity.getLookControl().setLookAt(target, 50.0f, 50.0f);
        }
        AbilityContext context = new BrainAbilityContext(entity);
        MKNpc.LOGGER.debug("ai {} casting {} on {}", entity, currentAbility.getAbilityId(), target);
        entity.getCapability(CoreCapabilities.ENTITY_CAPABILITY).ifPresent(
                (entityData) -> entityData.getAbilityExecutor().executeAbilityWithContext(currentAbility.getAbilityId(), context));
    }

    @Override
    public void tick() {
        if (!target.is(entity)){
            entity.lookAt(target, 50.0f, 50.0f);
            entity.getLookControl().setLookAt(target, 50.0f, 50.0f);
            if (entity.getSensing().hasLineOfSight(target)){
                ticksSinceSeenTarget = 0;
            } else {
                ticksSinceSeenTarget++;
            }
        }
    }

    @Override
    public void stop() {
        super.stop();
        currentAbility = null;
        target = null;
        entity.getBrain().eraseMemory(MKMemoryModuleTypes.CURRENT_ABILITY);
        entity.getBrain().eraseMemory(MKAbilityMemories.ABILITY_TARGET.get());
        entity.getBrain().eraseMemory(MKAbilityMemories.ABILITY_POSITION_TARGET.get());
        entity.returnToDefaultMovementState();
        ticksSinceSeenTarget = 0;

    }
}
