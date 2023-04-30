package com.chaosbuffalo.mknpc.entity.ai.sensor;

import com.chaosbuffalo.mkcore.CoreCapabilities;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.abilities.MKAbilityMemories;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityDecisionContext;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityTargetingDecision;
import com.chaosbuffalo.mkcore.utils.TargetUtil;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.entity.ai.memory.MKMemoryModuleTypes;
import com.google.common.collect.ImmutableSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class AbilityUseSensor extends Sensor<MKEntity> {

    public AbilityUseSensor() {
        super(5);
    }

    @Override
    protected void doTick(@Nonnull ServerLevel worldIn, MKEntity entityIn) {
        Optional<MKAbilityInfo> abilityOptional = entityIn.getBrain().getMemory(MKMemoryModuleTypes.CURRENT_ABILITY.get());
        int timeOut = entityIn.getBrain().getMemory(MKMemoryModuleTypes.ABILITY_TIMEOUT.get()).orElse(0);
        boolean isCasting = MKCore.getEntityData(entityIn).map(data -> data.getAbilityExecutor().isCasting()).orElse(false);
        if (abilityOptional.isPresent() && !isCasting && timeOut <= 20) {
            entityIn.getBrain().setMemory(MKMemoryModuleTypes.ABILITY_TIMEOUT.get(), timeOut + 1);
            return;
        }
        entityIn.getCapability(CoreCapabilities.ENTITY_CAPABILITY).ifPresent(mkEntityData -> {
            AbilityDecisionContext context = createAbilityDecisionContext(entityIn);
            for (MKAbilityInfo ability : mkEntityData.getAbilities().getAbilitiesPriorityOrder()) {
                MKAbility mkAbility = ability.getAbility();
                if (!mkEntityData.getAbilityExecutor().canActivateAbility(ability))
                    continue;

                AbilityTargetingDecision targetSelection = mkAbility.getUseCondition().getDecision(ability, context);
                if (targetSelection == AbilityTargetingDecision.UNDECIDED)
                    continue;

                if (mkAbility.isValidTarget(entityIn, targetSelection.getTargetEntity())) {
                    entityIn.getBrain().setMemory(MKAbilityMemories.ABILITY_TARGET.get(), targetSelection.getTargetEntity());
                    entityIn.getBrain().setMemory(MKAbilityMemories.ABILITY_POSITION_TARGET.get(), new TargetUtil.LivingOrPosition(targetSelection.getTargetEntity()));
                    entityIn.getBrain().setMemory(MKMemoryModuleTypes.CURRENT_ABILITY.get(), ability);
                    entityIn.getBrain().setMemory(MKMemoryModuleTypes.MOVEMENT_STRATEGY.get(),
                            entityIn.getMovementStrategy(targetSelection));
                    entityIn.getBrain().setMemory(MKMemoryModuleTypes.MOVEMENT_TARGET.get(), targetSelection.getTargetEntity());
                    entityIn.getBrain().setMemory(MKMemoryModuleTypes.ABILITY_TIMEOUT.get(), 0);
                    return;
                }
            }
        });
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
}
