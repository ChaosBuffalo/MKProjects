package com.chaosbuffalo.mknpc.entity.ai.sensor;

import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.init.CoreParticles;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.entity.ai.memory.MKMemoryModuleTypes;
import com.google.common.collect.ImmutableSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.Set;

public class MovementStrategySensor extends Sensor<MKEntity> {

    public MovementStrategySensor() {
        super(5);
    }

    @Override
    protected void doTick(ServerLevel worldIn, MKEntity entityIn) {
        if ((entityIn.avoidsWater() && entityIn.isInWater()) || (!entityIn.getEntityDataCap().getPets().isPet() && entityIn.getBrain().getMemory(MKMemoryModuleTypes.IS_RETURNING.get()).orElse(false))) {
            return;
        }
        Optional<WalkTarget> walkTargetOptional = entityIn.getBrain().getMemory(MemoryModuleType.WALK_TARGET);
        if (walkTargetOptional.isPresent()) {
            Vec3 pos = walkTargetOptional.get().getTarget().currentPosition();
//            PacketHandler.sendToTrackingAndSelf(new ParticleEffectSpawnPacket(
//                    CoreParticles.INDICATOR_PARTICLE.get(),
//                    ParticleEffects.SPHERE_MOTION, 1, 1,
//                    pos.x + 0.5, pos.y + 1.0,
//                    pos.z + 0.5, 0.0, 0.0, 0.0, 0.0f,
//                    entityIn.getLookAngle()), entityIn);
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
