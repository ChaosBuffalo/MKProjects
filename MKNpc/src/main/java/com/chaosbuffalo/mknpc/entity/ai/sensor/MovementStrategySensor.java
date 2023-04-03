package com.chaosbuffalo.mknpc.entity.ai.sensor;

import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.entity.ai.memory.MKMemoryModuleTypes;
import com.google.common.collect.ImmutableSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;

import java.util.Set;

public class MovementStrategySensor extends Sensor<MKEntity> {

    public MovementStrategySensor() {
        super(5);
    }

    @Override
    protected void doTick(ServerLevel worldIn, MKEntity entityIn) {
        if ((entityIn.avoidsWater() && entityIn.isInWater()) || entityIn.getBrain().getMemory(MKMemoryModuleTypes.IS_RETURNING.get()).orElse(false)) {
            return;
        }
        entityIn.getBrain().getMemory(MKMemoryModuleTypes.MOVEMENT_STRATEGY.get())
                .ifPresent(movementStrategy -> movementStrategy.update(worldIn, entityIn));
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MKMemoryModuleTypes.THREAT_TARGET.get(),
                MKMemoryModuleTypes.VISIBLE_ENEMIES.get(),
                MemoryModuleType.WALK_TARGET,
                MKMemoryModuleTypes.MOVEMENT_STRATEGY.get(),
                MKMemoryModuleTypes.IS_RETURNING.get());
    }
}
