package com.chaosbuffalo.mkcore.utils;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class TargetUtil {

    private static final Predicate<Entity> defaultFilter = e -> EntitySelector.ENTITY_STILL_ALIVE.test(e) && EntitySelector.NO_SPECTATORS.test(e);

    public static <E extends Entity> List<E> getEntitiesInLine(Class<E> clazz, final Entity mainEntity,
                                                               Vec3 from, Vec3 to, Vec3 expansion,
                                                               float growth, final Predicate<E> filter) {
        Predicate<E> predicate = e -> defaultFilter.test(e) && filter.test(e);
        AABB bb = new AABB(from, to)
                .expandTowards(expansion.x, expansion.y, expansion.z)
                .inflate(growth);
        return mainEntity.getCommandSenderWorld().getEntitiesOfClass(clazz, bb, predicate);
    }

    public static LivingEntity getSingleLivingTarget(LivingEntity caster, float distance,
                                                     BiPredicate<LivingEntity, LivingEntity> validTargetChecker) {
        return getSingleLivingTarget(LivingEntity.class, caster, distance, validTargetChecker);
    }

    public static class LivingOrPosition {
        @Nullable
        private final LivingEntity entity;
        @Nullable
        private final Vec3 position;

        public LivingOrPosition(Vec3 loc) {
            position = loc;
            entity = null;
        }

        public LivingOrPosition(LivingEntity entity) {
            this.entity = entity;
            this.position = null;
        }

        public Optional<LivingEntity> getEntity() {
            return Optional.ofNullable(entity);
        }

        public Optional<Vec3> getPosition() {
            if (entity != null) {
                return Optional.of(entity.position());
            }
            return Optional.ofNullable(position);
        }

    }

    @Nullable
    public static LivingOrPosition getPositionTarget(LivingEntity caster, float distance, BiPredicate<LivingEntity, LivingEntity> validTargetChecker) {
        HitResult lookingAt = RayTraceUtils.getLookingAt(LivingEntity.class, caster, distance,
                e -> validTargetChecker == null || (e != null && validTargetChecker.test(caster, e)));

        if (lookingAt != null && lookingAt.getType() == HitResult.Type.ENTITY) {
            EntityHitResult traceResult = (EntityHitResult) lookingAt;
            Entity entityHit = traceResult.getEntity();
            if (entityHit instanceof LivingEntity) {
                if (validTargetChecker != null && !validTargetChecker.test(caster, (LivingEntity) entityHit)) {
                    return null;
                }
                return new LivingOrPosition((LivingEntity) entityHit);
            }
        } else if (lookingAt != null && lookingAt.getType() == HitResult.Type.BLOCK) {
            return new LivingOrPosition(lookingAt.getLocation());
        }
        return null;
    }

    public static <E extends LivingEntity> E getSingleLivingTarget(Class<E> clazz, LivingEntity caster, float distance,
                                                                   BiPredicate<LivingEntity, LivingEntity> validTargetChecker) {
        HitResult lookingAt = RayTraceUtils.getLookingAt(clazz, caster, distance,
                e -> validTargetChecker == null || (e != null && validTargetChecker.test(caster, e)));

        if (lookingAt != null && lookingAt.getType() == HitResult.Type.ENTITY) {

            EntityHitResult traceResult = (EntityHitResult) lookingAt;
            Entity entityHit = traceResult.getEntity();
            if (entityHit instanceof LivingEntity) {

                if (validTargetChecker != null && !validTargetChecker.test(caster, (LivingEntity) entityHit)) {
                    return null;
                }

                //noinspection unchecked
                return (E) entityHit;
            }
        }

        return null;
    }

    @Nonnull
    public static LivingEntity getSingleLivingTargetOrSelf(LivingEntity caster, float distance,
                                                           BiPredicate<LivingEntity, LivingEntity> validTargetChecker) {
        LivingEntity target = getSingleLivingTarget(caster, distance, validTargetChecker);
        return target != null ? target : caster;
    }

    public static List<LivingEntity> getTargetsInLine(LivingEntity caster, Vec3 from, Vec3 to, float growth,
                                                      BiPredicate<LivingEntity, LivingEntity> validTargetChecker) {
        EntityCollectionRayTraceResult<LivingEntity> traceResult = RayTraceUtils.rayTraceAllEntities(
                LivingEntity.class,
                caster.level(),
                from,
                to,
                Vec3.ZERO,
                growth,
                0.0f,
                e -> validTargetChecker == null || validTargetChecker.test(caster, e)
        );
        List<EntityCollectionRayTraceResult.TraceEntry<LivingEntity>> entries = new ArrayList<>(traceResult.getEntities());
        entries.sort(Comparator.comparingDouble(entry -> entry.distance));
        Set<LivingEntity> orderedTargets = new LinkedHashSet<>();
        for (EntityCollectionRayTraceResult.TraceEntry<LivingEntity> entry : entries) {
            orderedTargets.add(entry.entity);
        }
        return List.copyOf(orderedTargets);
    }
}
