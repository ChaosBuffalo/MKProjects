package com.chaosbuffalo.mknpc.entity.ai.goal;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.MKAbilityMemories;
import com.chaosbuffalo.mkcore.abilities.ai.BrainAbilityContext;
import com.chaosbuffalo.mkcore.abilities2.runtime.AbilityReference;
import com.chaosbuffalo.mkcore.abilities2.runtime.FailureReason;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.entity.ai.NpcAbilitySelection;
import com.chaosbuffalo.mknpc.entity.ai.memory.MKMemoryModuleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.Optional;

public class UseAbilityGoal extends Goal {
    public static final int CAN_SEE_TIMEOUT = 30;
    private final MKEntity entity;
    private NpcAbilitySelection currentAbility;
    private LivingEntity target;
    private int ticksSinceSeenTarget;

    public UseAbilityGoal(MKEntity entity, boolean canMove) {
        this.entity = entity;

        this.setFlags(!canMove ? EnumSet.of(Flag.MOVE, Flag.LOOK) : EnumSet.of(Flag.LOOK));
        ticksSinceSeenTarget = 0;
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public boolean canUse() {
        Optional<NpcAbilitySelection> abilityOptional = entity.getBrain().getMemory(MKMemoryModuleTypes.CURRENT_ABILITY.get());
        Optional<LivingEntity> target = entity.getBrain().getMemory(MKAbilityMemories.ABILITY_TARGET.get());
        if (abilityOptional.isPresent() && target.isPresent()) {
            currentAbility = abilityOptional.get();
            LivingEntity targetEntity = target.get();

            if (!canActivate(targetEntity))
                return false;

            if (entity != targetEntity) {
                if (!isInRange(currentAbility, targetEntity))
                    return false;
                if (requiresLineOfSightToStart(currentAbility, targetEntity) &&
                        !entity.getSensing().hasLineOfSight(targetEntity))
                    return false;
            }

            // Now we know we can actually start the cast
            this.target = targetEntity;
            return true;
        } else {
            return false;
        }
    }

    protected boolean isInRange(NpcAbilitySelection abilitySelection, LivingEntity target) {
        if (abilitySelection.isDefinitionBacked()) {
            return true;
        }
        float range = abilitySelection.legacyAbilityInfo().getAbility().getDistance(entity);
        return target.distanceToSqr(entity) <= range * range;
    }

    public boolean canActivate(LivingEntity targetEntity) {
        if (!currentAbility.isDefinitionBacked()) {
            return entity.getEntityDataCap().getAbilityExecutor().canActivateAbility(currentAbility.legacyAbilityInfo());
        }
        var forcedTargets = currentAbility.createForcedTargets(entity, targetEntity);
        if (forcedTargets == null) {
            return false;
        }
        return MKCore.getAbilityRuntimeService().previewAiAbility(
                entity.getEntityDataCap(),
                entity.getEntityDataCap(),
                new AbilityReference(currentAbility.abilityId(), null),
                currentAbility.activationId(),
                forcedTargets
        ) == null;
    }

    public boolean canContinueToUse() {
        boolean sightOk = currentAbility != null &&
                (!currentAbility.usesExternalTarget() || canMaintainWithoutSight(currentAbility) || ticksSinceSeenTarget < CAN_SEE_TIMEOUT);
        return sightOk &&
                isCastingCurrentAbility() &&
                entity.getBrain().getMemory(MKAbilityMemories.ABILITY_TARGET.get())
                        .map(tar -> tar.isAlive() && tar.is(target))
                        .orElse(false) &&
                entity.getBrain().getMemory(MKMemoryModuleTypes.CURRENT_ABILITY.get())
                        .map(mkAbility -> mkAbility.equals(currentAbility))
                        .orElse(false);
    }

    @Override
    public void start() {
        if (!target.is(entity)) {
            entity.lookAt(target, 360.0f, 90.0f);
            entity.getLookControl().setLookAt(target, 360.0f, 90.0f);
        }
        entity.onAIAbilityCastStart();
        if (!currentAbility.isDefinitionBacked()) {
            AbilityContext context = new BrainAbilityContext(entity.getEntityDataCap(), currentAbility.legacyAbilityInfo());
            entity.getEntityDataCap().getAbilityExecutor().executeAbilityInfoWithContext(currentAbility.legacyAbilityInfo(), context);
            return;
        }

        var forcedTargets = currentAbility.createForcedTargets(entity, target);
        if (forcedTargets == null) {
            stop();
            return;
        }
        var result = MKCore.getAbilityRuntimeService().activateAiAbility(
                entity.getEntityDataCap(),
                entity.getEntityDataCap(),
                new AbilityReference(currentAbility.abilityId(), null),
                currentAbility.activationId(),
                forcedTargets
        );
        if (!result.started()) {
            entity.getBrain().setMemory(MKMemoryModuleTypes.ABILITY_TIMEOUT.get(),
                    result.failureReason() == FailureReason.BUSY ? CAN_SEE_TIMEOUT : 1);
            stop();
        }
    }

    @Override
    public void tick() {
        entity.onAIAbilityCastTick();
        if (!target.is(entity)) {
            entity.lookAt(target, 90.0f, 50.0f);
            entity.getLookControl().setLookAt(target, 90.0f, 50.0f);
            if (entity.getSensing().hasLineOfSight(target)) {
                ticksSinceSeenTarget = 0;
            } else {
                ticksSinceSeenTarget++;
            }
        }
    }

    @Override
    public void stop() {
        super.stop();
        entity.onAIAbilityCastStop();
        currentAbility = null;
        target = null;
        entity.getBrain().eraseMemory(MKMemoryModuleTypes.CURRENT_ABILITY.get());
        entity.getBrain().eraseMemory(MKAbilityMemories.ABILITY_TARGET.get());
        entity.getBrain().eraseMemory(MKAbilityMemories.ABILITY_POSITION_TARGET.get());
        entity.returnToDefaultMovementState();
        ticksSinceSeenTarget = 0;
    }

    private boolean requiresLineOfSightToStart(NpcAbilitySelection abilitySelection, LivingEntity target) {
        if (abilitySelection.isDefinitionBacked()) {
            return abilitySelection.usesExternalTarget() && !target.is(entity);
        }
        return abilitySelection.legacyAbilityInfo().getAbility().requiresLineOfSightToStart(entity.getEntityDataCap(), target);
    }

    private boolean canMaintainWithoutSight(NpcAbilitySelection abilitySelection) {
        if (abilitySelection.isDefinitionBacked()) {
            return !abilitySelection.usesExternalTarget();
        }
        return abilitySelection.legacyAbilityInfo().getAbility().maintainCastWithoutLineOfSight(entity.getEntityDataCap());
    }

    private boolean isCastingCurrentAbility() {
        if (currentAbility == null) {
            return false;
        }
        return currentAbility.isDefinitionBacked()
                ? MKCore.getAbilityRuntimeService().hasPendingActivation(entity.getEntityDataCap())
                : entity.getEntityDataCap().getAbilityExecutor().isCasting();
    }
}
