package com.chaosbuffalo.mknpc.entity.ai.sensor;

import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.entity.ai.memory.MKMemoryModuleTypes;
import com.chaosbuffalo.mknpc.entity.ai.memory.ThreatMapEntry;
import com.chaosbuffalo.mknpc.init.MKNpcAttributes;
import com.google.common.collect.ImmutableSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;

import java.util.*;
import java.util.stream.Collectors;

public class ThreatSensor extends Sensor<MKEntity> {
    private static final float THREAT_FALLOFF_2 = 225.0f;
    private static final float BONUS_THREAT_FIRST_SIGHT = 750.0f;
    private static final float MAX_THREAT_FROM_DISTANCE = 250.0f;
    private static final float ADD_THREAT = 125.0f;
    private static final float REMOVE_DIST_2 = 400.0f;


    private float getAggroDistanceForEntity(LivingEntity entity) {
        double aggroDist = entity.getAttributeValue(MKNpcAttributes.AGGRO_RANGE.get());
        return (float) (aggroDist * aggroDist);
    }

    public ThreatSensor() {
        super(10);
    }

    @Override
    protected void doTick(ServerLevel worldIn, MKEntity entityIn) {
        Optional<List<LivingEntity>> enemyOpt = entityIn.getBrain().getMemory(MKMemoryModuleTypes.VISIBLE_ENEMIES.get());
        Map<LivingEntity, ThreatMapEntry> threatMap = entityIn.getBrain().getMemory(MKMemoryModuleTypes.THREAT_MAP.get())
                .orElseGet(HashMap::new);
        Optional<LivingEntity> targetOpt = entityIn.getBrain().getMemory(MKMemoryModuleTypes.THREAT_TARGET.get());
        Optional<Boolean> isReturningOpt = entityIn.getBrain().getMemory(MKMemoryModuleTypes.IS_RETURNING.get());
        if (targetOpt.isPresent() && (!targetOpt.get().isAlive() || isReturningOpt.isPresent())) {
            entityIn.getBrain().eraseMemory(MKMemoryModuleTypes.THREAT_TARGET.get());
        }
        if (enemyOpt.isPresent() && isReturningOpt.isEmpty()) {
            List<LivingEntity> enemies = enemyOpt.get();
            for (LivingEntity enemy : enemies) {
                double dist2 = entityIn.distanceToSqr(enemy);
                if (dist2 < getAggroDistanceForEntity(entityIn) && !threatMap.containsKey(enemy)) {
                    float bonusThreat = ADD_THREAT;
                    if (threatMap.isEmpty()) {
                        bonusThreat = BONUS_THREAT_FIRST_SIGHT;
                    }
                    threatMap.put(enemy, new ThreatMapEntry().addThreat(bonusThreat));
                }
            }
            Set<LivingEntity> toRemove = new HashSet<>();
            for (Map.Entry<LivingEntity, ThreatMapEntry> entry : threatMap.entrySet()) {
                float dist2 = (float) entityIn.distanceToSqr(entry.getKey());
                ThreatMapEntry threat = entry.getValue().addThreat((1.0f - dist2 / THREAT_FALLOFF_2) * MAX_THREAT_FROM_DISTANCE);
                if (threat.getCurrentThreat() < 0 || dist2 > REMOVE_DIST_2 || !entry.getKey().isAlive()) {
                    toRemove.add(entry.getKey());
                }
            }
            for (LivingEntity entity : toRemove) {
                threatMap.remove(entity);
            }
            List<Map.Entry<LivingEntity, ThreatMapEntry>> sortedThreat = threatMap.entrySet().stream()
                    .sorted(Comparator.comparingDouble(entry -> -entry.getValue().getCurrentThreat()))
                    .toList();
            entityIn.getBrain().setMemory(MKMemoryModuleTypes.THREAT_MAP.get(), threatMap);
            entityIn.getBrain().setMemory(MKMemoryModuleTypes.THREAT_LIST.get(), sortedThreat.stream()
                    .map(Map.Entry::getKey).collect(Collectors.toList()));
            if (!sortedThreat.isEmpty()) {
                Map.Entry<LivingEntity, ThreatMapEntry> ent = sortedThreat.get(0);
                if (!entityIn.getBrain().hasMemoryValue(MKMemoryModuleTypes.THREAT_TARGET.get())) {
                    entityIn.callForHelp(ent.getKey(), ent.getValue().getCurrentThreat());
                }
                entityIn.getBrain().setMemory(MKMemoryModuleTypes.THREAT_TARGET.get(), ent.getKey());
            } else {
                entityIn.getBrain().eraseMemory(MKMemoryModuleTypes.THREAT_TARGET.get());

            }
        }
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MKMemoryModuleTypes.THREAT_MAP.get(),
                MKMemoryModuleTypes.VISIBLE_ENEMIES.get(),
                MKMemoryModuleTypes.THREAT_LIST.get(),
                MKMemoryModuleTypes.IS_RETURNING.get());
    }
}
