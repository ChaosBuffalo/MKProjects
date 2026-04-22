package com.chaosbuffalo.mknpc.entity.ai.sensor;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.abilities.MKAbilityMemories;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityDecisionContext;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityTargetingDecision;
import com.chaosbuffalo.mkcore.abilities2.AbilityTargeting;
import com.chaosbuffalo.mkcore.abilities2.runtime.AbilityReference;
import com.chaosbuffalo.mkcore.abilities2.runtime.FailureReason;
import com.chaosbuffalo.mkcore.core.MKEntityData;
import com.chaosbuffalo.mkcore.core.entity.MobKnownAbility;
import com.chaosbuffalo.mkcore.utils.TargetUtil;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.entity.ai.NpcAbilitySelection;
import com.chaosbuffalo.mknpc.entity.ai.memory.MKMemoryModuleTypes;
import com.google.common.collect.ImmutableSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class AbilityUseSensor extends Sensor<MKEntity> {

    public AbilityUseSensor() {
        super(5);
    }

    @Override
    protected void doTick(@Nonnull ServerLevel worldIn, MKEntity entityIn) {
        Optional<NpcAbilitySelection> abilityOptional = entityIn.getBrain().getMemory(MKMemoryModuleTypes.CURRENT_ABILITY.get());
        int timeOut = entityIn.getBrain().getMemory(MKMemoryModuleTypes.ABILITY_TIMEOUT.get()).orElse(0);
        boolean isCasting = abilityOptional.map(selection -> isCasting(entityIn, selection))
                .orElseGet(() -> entityIn.getEntityDataCap().getAbilityExecutor().isCasting());
        if (abilityOptional.isPresent() && !isCasting && timeOut <= 20) {
            entityIn.getBrain().setMemory(MKMemoryModuleTypes.ABILITY_TIMEOUT.get(), timeOut + 1);
            return;
        }

        MKEntityData mkEntityData = entityIn.getEntityDataCap();
        AbilityDecisionContext context = createAbilityDecisionContext(entityIn);
        for (MobKnownAbility knownAbility : mkEntityData.getAbilities().getAbilitiesPriorityOrder()) {
            PlannedSelection planned = planAbility(entityIn, context, knownAbility);
            if (planned == null) {
                continue;
            }
            NpcAbilitySelection selection = planned.selection();
            LivingEntity targetEntity = planned.target();

            if (selection.isDefinitionBacked()) {
                var forcedTargets = selection.createForcedTargets(entityIn, targetEntity);
                if (forcedTargets == null) {
                    continue;
                }
                FailureReason previewFailure = MKCore.getAbilityRuntimeService().previewAiAbility(
                        mkEntityData,
                        mkEntityData,
                        new AbilityReference(selection.abilityId(), null),
                        selection.activationId(),
                        forcedTargets
                );
                if (previewFailure != null) {
                    continue;
                }
                entityIn.getBrain().setMemory(MKMemoryModuleTypes.MOVEMENT_STRATEGY.get(),
                        entityIn.getMovementStrategy(selection.movementSuggestion()));
            } else {
                entityIn.getBrain().setMemory(MKMemoryModuleTypes.MOVEMENT_STRATEGY.get(),
                        entityIn.getMovementStrategy(new AbilityTargetingDecision(targetEntity, selection.movementSuggestion(),
                                selection.legacyAbilityInfo().getAbility())));
            }

            entityIn.getBrain().setMemory(MKAbilityMemories.ABILITY_TARGET.get(), targetEntity);
            entityIn.getBrain().setMemory(MKAbilityMemories.ABILITY_POSITION_TARGET.get(),
                    new TargetUtil.LivingOrPosition(targetEntity));
            entityIn.getBrain().setMemory(MKMemoryModuleTypes.CURRENT_ABILITY.get(), selection);
            entityIn.getBrain().setMemory(MKMemoryModuleTypes.MOVEMENT_TARGET.get(), targetEntity);
            entityIn.getBrain().setMemory(MKMemoryModuleTypes.ABILITY_TIMEOUT.get(), 0);
            return;
        }
    }

    private boolean isCasting(MKEntity entity, NpcAbilitySelection selection) {
        return selection.isDefinitionBacked()
                ? MKCore.getAbilityRuntimeService().hasPendingActivation(entity.getEntityDataCap())
                : entity.getEntityDataCap().getAbilityExecutor().isCasting();
    }

    private @Nullable PlannedSelection planAbility(MKEntity entity,
                                                   AbilityDecisionContext context,
                                                   MobKnownAbility knownAbility) {
        MKAbilityInfo abilityInfo = knownAbility.getAbilityInfo();
        if (abilityInfo != null) {
            if (!entity.getEntityDataCap().getAbilityExecutor().canActivateAbility(abilityInfo)) {
                return null;
            }

            MKAbility mkAbility = abilityInfo.getAbility();
            AbilityTargetingDecision targetSelection = mkAbility.getUseCondition().getDecision(context);
            if (targetSelection == AbilityTargetingDecision.UNDECIDED) {
                return null;
            }

            return mkAbility.isValidTarget(entity, targetSelection.getTargetEntity())
                    ? new PlannedSelection(
                    NpcAbilitySelection.legacy(abilityInfo, targetSelection.getMovementSuggestion()),
                    targetSelection.getTargetEntity())
                    : null;
        }

        var execution = MKCore.getAbilityRuntimeService().resolveAiExecution(knownAbility.getId(), knownAbility.getActivationId());
        if (execution == null) {
            return null;
        }

        AbilityTargetingDecision.MovementSuggestion movementSuggestion = switch (execution.activation().targeting().type()) {
            case "resolved" -> switch (AbilityTargeting.relation(execution.activation().targeting())) {
                case ENEMY -> switch (entity.getCombatMoveType()) {
                    case RANGE -> AbilityTargetingDecision.MovementSuggestion.KITE;
                    case MELEE -> AbilityTargetingDecision.MovementSuggestion.MELEE;
                    case STATIONARY -> AbilityTargetingDecision.MovementSuggestion.STATIONARY;
                };
                case ALL, FRIENDLY -> AbilityTargetingDecision.MovementSuggestion.STATIONARY;
            };
            case "none", "self" -> AbilityTargetingDecision.MovementSuggestion.STATIONARY;
            default -> null;
        };
        if (movementSuggestion == null) {
            return null;
        }

        LivingEntity targetEntity = resolveDefinitionTargetEntity(entity, context, execution.activation().targeting());
        if (targetEntity == null) {
            return null;
        }

        return new PlannedSelection(NpcAbilitySelection.definition(
                knownAbility.getId(),
                execution.activationId(),
                execution.activation().targeting(),
                movementSuggestion
        ), targetEntity);
    }

    private @Nullable LivingEntity resolveDefinitionTargetEntity(MKEntity entity,
                                                                 AbilityDecisionContext context,
                                                                 com.chaosbuffalo.mkcore.abilities2.definition.AbilityTargetResolverDefinition targeting) {
        return switch (targeting.type()) {
            case "none", "self" -> entity;
            case "resolved" -> switch (AbilityTargeting.relation(targeting)) {
                case ENEMY -> context.getThreatTarget() != null ? context.getThreatTarget()
                        : context.getEnemies().stream().findFirst().orElse(null);
                case ALL -> context.getThreatTarget() != null ? context.getThreatTarget() : entity;
                case FRIENDLY -> entity;
            };
            default -> null;
        };
    }

    @Nonnull
    private AbilityDecisionContext createAbilityDecisionContext(MKEntity entityIn) {
        Optional<LivingEntity> targetOptional = entityIn.getBrain().getMemory(MKMemoryModuleTypes.THREAT_TARGET.get());
        return new AbilityDecisionContext(entityIn, targetOptional.orElse(null),
                entityIn.getBrain().getMemory(MKMemoryModuleTypes.ALLIES.get()).orElse(Collections.emptyList()),
                entityIn.getBrain().getMemory(MKMemoryModuleTypes.ENEMIES.get()).orElse(Collections.emptyList()));
    }

    @Nonnull
    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MKMemoryModuleTypes.CURRENT_ABILITY.get(), MKMemoryModuleTypes.THREAT_TARGET.get(),
                MKAbilityMemories.ABILITY_TARGET.get(), MKMemoryModuleTypes.ALLIES.get(), MKMemoryModuleTypes.ENEMIES.get(),
                MKMemoryModuleTypes.MOVEMENT_STRATEGY.get(), MKMemoryModuleTypes.ABILITY_TIMEOUT.get(), MKAbilityMemories.ABILITY_POSITION_TARGET.get());
    }

    private record PlannedSelection(NpcAbilitySelection selection, LivingEntity target) {
    }
}
