package com.chaosbuffalo.mkcore.core.pets;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;

public class PetNonCombatBehavior {
    public enum Behavior {
        GUARD,
        FOLLOW
    }

    private final Behavior behaviorType;
    @Nullable
    private final LivingEntity entity;
    @Nullable
    private final Vec3 pos;

    public PetNonCombatBehavior(LivingEntity entity) {
        behaviorType = Behavior.FOLLOW;
        this.entity = entity;
        pos = null;
    }

    public PetNonCombatBehavior(Vec3 pos) {
        behaviorType = Behavior.GUARD;
        this.pos = pos;
        entity = null;
    }

    public Optional<Vec3> getPos() {
        return Optional.ofNullable(pos);
    }

    public Optional<LivingEntity> getEntity() {
        return Optional.ofNullable(entity);
    }

    public Behavior getBehaviorType() {
        return behaviorType;
    }
}
