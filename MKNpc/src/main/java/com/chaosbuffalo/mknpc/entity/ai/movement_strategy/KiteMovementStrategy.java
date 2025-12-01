package com.chaosbuffalo.mknpc.entity.ai.movement_strategy;

import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.init.CoreParticles;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.entity.ai.MovementUtils;
import com.chaosbuffalo.mknpc.entity.ai.memory.MKMemoryModuleTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class KiteMovementStrategy extends MovementStrategy {

    private double dist;

    public KiteMovementStrategy(double dist) {
        this.dist = dist;
    }

    @Override
    public void update(ServerLevel world, MKEntity entity) {
        Brain<?> brain = entity.getBrain();
        Optional<LivingEntity> targetOpt = brain.getMemory(MKMemoryModuleTypes.MOVEMENT_TARGET.get());
        Optional<WalkTarget> walkTargetOptional = brain.getMemory(MemoryModuleType.WALK_TARGET);
        if (walkTargetOptional.isPresent() && entity.getRandom().nextInt(40) != 0) {
            return;
        }
        if (targetOpt.isPresent()) {
            LivingEntity target = targetOpt.get();
            if (target.is(entity)) {
                brain.eraseMemory(MemoryModuleType.WALK_TARGET);
                return;
            }
            double distanceTo = entity.distanceTo(target);
            Vec3 targetPos = null;
            if (distanceTo > (1.5 * dist)) {
                targetPos = target.position();
            } else {
                for (int i = 0; i < 10; i++) {
                    targetPos = MovementUtils.findRandomTargetBlockAwayFromNoWater(
                            entity, (int) Math.round(dist), 7, target.position());
                    if (targetPos != null) {
                        break;
                    }
                }
            }
            if (targetPos != null) {
                brain.setMemory(MemoryModuleType.WALK_TARGET,
                        new WalkTarget(targetPos, 1.0f, 1));

            }
        }

    }
}
