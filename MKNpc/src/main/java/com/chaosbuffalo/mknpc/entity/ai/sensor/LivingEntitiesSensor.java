package com.chaosbuffalo.mknpc.entity.ai.sensor;

import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.entity.ai.memory.MKMemoryModuleTypes;
import com.chaosbuffalo.targeting_api.Targeting;
import com.google.common.collect.ImmutableSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;

import java.util.*;

public class LivingEntitiesSensor extends Sensor<MKEntity> {

    public LivingEntitiesSensor() {
        super(10);
    }

    protected void doTick(ServerLevel worldIn, MKEntity entityIn) {
        List<LivingEntity> entities = worldIn.getEntitiesOfClass(LivingEntity.class,
                entityIn.getBoundingBox().inflate(16.0D, 16.0D, 16.0D),
                (entity) -> entity != entityIn && entity.isAlive());
        entities.sort(Comparator.comparingDouble(entityIn::distanceToSqr));
        Brain<?> brain = entityIn.getBrain();

        List<LivingEntity> enemies = new ArrayList<>();
        List<LivingEntity> friends = new ArrayList<>();
        for (LivingEntity other : entities) {
            Targeting.TargetRelation relation = Targeting.getTargetRelation(entityIn, other);
            if (relation == Targeting.TargetRelation.ENEMY) {
                enemies.add(other);
            } else if (relation == Targeting.TargetRelation.FRIEND) {
                friends.add(other);
            }
        }
        friends.sort(this::sortByHealth);

        List<LivingEntity> visibleEnemies = new ArrayList<>();
        for (LivingEntity enemy : enemies) {
            if (entityIn.getSensing().hasLineOfSight(enemy)) {
                visibleEnemies.add(enemy);
            }
        }

        brain.setMemory(MKMemoryModuleTypes.ENEMIES.get(), enemies);
        brain.setMemory(MKMemoryModuleTypes.ALLIES.get(), friends);
        brain.setMemory(MKMemoryModuleTypes.VISIBLE_ENEMIES.get(), visibleEnemies);
    }

    private int sortByHealth(LivingEntity friend, LivingEntity other) {
        return Float.compare(friend.getHealth() / friend.getMaxHealth(), other.getHealth() / other.getMaxHealth());
    }

    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MKMemoryModuleTypes.ENEMIES.get(),
                MKMemoryModuleTypes.ALLIES.get(),
                MKMemoryModuleTypes.VISIBLE_ENEMIES.get());
    }
}
