package com.chaosbuffalo.mknpc.entity.ai.sensor;

import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.entity.ai.memory.MKMemoryModuleTypes;
import com.google.common.collect.ImmutableSet;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.server.level.ServerLevel;

import java.util.Set;

public class MovementStrategySensor extends Sensor<MKEntity> {


    @Override
    protected void doTick(ServerLevel worldIn, MKEntity entityIn) {
        if ((entityIn.avoidsWater() && entityIn.isInWater()) || entityIn.getBrain().getMemory(MKMemoryModuleTypes.IS_RETURNING).orElse(false)) {
            return;
        }
        entityIn.getBrain().getMemory(MKMemoryModuleTypes.MOVEMENT_STRATEGY).ifPresent(
                movementStrategy -> movementStrategy.update(worldIn, entityIn));

    }

    public MovementStrategySensor(){
        super(5);
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MKMemoryModuleTypes.THREAT_TARGET, MKMemoryModuleTypes.VISIBLE_ENEMIES,
                MemoryModuleType.WALK_TARGET, MKMemoryModuleTypes.MOVEMENT_STRATEGY, MKMemoryModuleTypes.IS_RETURNING);
    }
}
