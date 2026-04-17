package com.chaosbuffalo.mkcore.utils;

import com.chaosbuffalo.mkcore.utils.trace.ITraceExtensionProvider;
import com.chaosbuffalo.mkcore.utils.trace.TraceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.entity.PartEntity;

import java.util.*;
import java.util.function.Predicate;

public class RayTraceUtils {

    private static final Predicate<Entity> defaultFilter = e -> EntitySelector.ENTITY_STILL_ALIVE.test(e) && EntitySelector.NO_SPECTATORS.test(e);

    public static Vec3 getPerpendicular(Vec3 vec) {
        Vec3 cVec;
        if (vec.y != 0 || vec.z != 0) {
            cVec = new Vec3(1, 0, 0);
        } else {
            cVec = new Vec3(0, 1, 0);
        }
        return vec.cross(cVec);
    }

    public static <E extends Entity> HitResult getLookingAt(Class<E> clazz, final Entity mainEntity, double distance, final Predicate<E> entityPredicate) {

        Predicate<E> finalFilter = e -> e != mainEntity &&
                defaultFilter.test(e) &&
                entityPredicate.test(e);

        HitResult position = null;

        if (mainEntity.level() != null) {
            Vec3 look = mainEntity.getLookAngle().scale(distance);
            Vec3 from = mainEntity.position().add(0, mainEntity.getEyeHeight(), 0);
            Vec3 to = from.add(look);
            position = rayTraceBlocksAndEntities(clazz, mainEntity, from, to, false, finalFilter, true);
        }
        return position;
    }

    public static BlockHitResult rayTraceBlocks(Entity entity, Vec3 from, Vec3 to, boolean stopOnLiquid) {
        ClipContext.Fluid mode = stopOnLiquid ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE;
        ClipContext context = new ClipContext(from, to, ClipContext.Block.COLLIDER, mode, entity);
        return entity.getCommandSenderWorld().clip(context);
    }

    public static HitResult rayTraceEntities(Level world, Vec3 from, Vec3 to, Vec3 aaExpansion, float aaGrowth,
                                             float entityExpansion, final Predicate<Entity> filter, boolean testPickable) {
        return rayTraceEntities(Entity.class, world, from, to, aaExpansion, aaGrowth, entityExpansion, filter, testPickable);
    }

    public static <E extends Entity> EntityHitResult rayTraceEntities(Class<E> clazz, Level world,
                                                                      Vec3 from, Vec3 to,
                                                                      Vec3 aaExpansion,
                                                                      float aaGrowth,
                                                                      float entityExpansion,
                                                                      final Predicate<E> filter,
                                                                      boolean testPickable) {

        Predicate<E> predicate = input -> defaultFilter.test(input) && filter.test(input);

        Entity nearest = null;
        double distance = 0;

        AABB bb = new AABB(from, to)
                .expandTowards(aaExpansion.x, aaExpansion.y, aaExpansion.z)
                .inflate(aaGrowth);
        List<E> entities = world.getEntitiesOfClass(clazz, bb, predicate);
        for (Entity entity : entities) {
            if (testPickable && !entity.isPickable()) {
                continue;
            }
            AABB entityBB = entity.getBoundingBox().inflate(entityExpansion);
            Optional<Vec3> intercept = entityBB.clip(from, to);
            if (intercept.isPresent()) {
                double dist = from.distanceTo(intercept.get());
                if (dist < distance || distance == 0.0D) {
                    nearest = entity;
                    distance = dist;
                }
            }
        }
        if (!world.getPartEntities().isEmpty()) {
            for (PartEntity<?> p : world.getPartEntities()) {
                if (testPickable && !p.isPickable()) {
                    continue;
                }
                EntityTypeTest<Entity, E> typeTest = EntityTypeTest.forClass(clazz);
                E t = typeTest.tryCast(p.getParent());
                AABB entityBB = p.getBoundingBox().inflate(entityExpansion);
                if (t != null && entityBB.intersects(bb) && predicate.test(t)) {
                    Optional<Vec3> intercept = entityBB.clip(from, to);
                    if (intercept.isPresent()) {
                        double dist = from.distanceTo(intercept.get());
                        if (dist < distance || distance == 0.0D) {
                            nearest = t;
                            distance = dist;
                        }
                    }
                }
            }
        }

        if (!TraceManager.getExtensionProviders().isEmpty()) {
            for (ITraceExtensionProvider provider : TraceManager.getExtensionProviders()) {
                EntityCollectionRayTraceResult<E> results = provider.getCustomTraces(clazz, world,
                        from, to, aaExpansion, aaGrowth, entityExpansion, filter, testPickable);
                for (EntityCollectionRayTraceResult.TraceEntry<E> result : results.getEntities()) {
                    if (result.distance < distance || distance == 0.0D) {
                        nearest = result.entity;
                        distance = result.distance;
                    }
                }
            }
        }

        if (nearest != null)
            return new EntityHitResult(nearest);
        return null;
    }

    public static <E extends Entity> EntityCollectionRayTraceResult<E> rayTraceAllEntities(Class<E> clazz, Level world,
                                                                                           Vec3 from, Vec3 to,
                                                                                           Vec3 aaExpansion,
                                                                                           float aaGrowth,
                                                                                           float entityExpansion,
                                                                                           final Predicate<E> filter) {

        Predicate<E> predicate = input -> defaultFilter.test(input) && filter.test(input);
        AABB bb = new AABB(from, to)
                .expandTowards(aaExpansion.x, aaExpansion.y, aaExpansion.z)
                .inflate(aaGrowth);
        List<E> entities = world.getEntitiesOfClass(clazz, bb, predicate);
        List<EntityCollectionRayTraceResult.TraceEntry<E>> finalEnt = new ArrayList<>();
        for (E entity : entities) {
            AABB entityBB = entity.getBoundingBox().inflate(entityExpansion);
            Optional<Vec3> intercept = entityBB.clip(from, to);
            if (intercept.isPresent()) {
                double dist = from.distanceTo(intercept.get());
                finalEnt.add(new EntityCollectionRayTraceResult.TraceEntry<>(entity, dist, intercept.get()));
            }
        }
        if (!world.getPartEntities().isEmpty()) {
            Set<E> seenParts = new HashSet<>();
            for (PartEntity<?> p : world.getPartEntities()) {
                EntityTypeTest<Entity, E> typeTest = EntityTypeTest.forClass(clazz);
                E t = typeTest.tryCast(p.getParent());
                if (seenParts.contains(t)) {
                    continue;
                }
                AABB entityBB = p.getBoundingBox().inflate(entityExpansion);
                if (t != null && entityBB.intersects(bb) && predicate.test(t)) {
                    Optional<Vec3> intercept = entityBB.clip(from, to);
                    if (intercept.isPresent()) {
                        double dist = from.distanceTo(intercept.get());
                        finalEnt.add(new EntityCollectionRayTraceResult.TraceEntry<>(t, dist, intercept.get()));
                        seenParts.add(t);
                    }
                }
            }
        }

        if (!TraceManager.getExtensionProviders().isEmpty()) {
            for (ITraceExtensionProvider provider : TraceManager.getExtensionProviders()) {
                EntityCollectionRayTraceResult<E> results = provider.getCustomTraces(clazz, world,
                        from, to, aaExpansion, aaGrowth, entityExpansion, filter, false);
                finalEnt.addAll(results.getEntities());
            }
        }


        return new EntityCollectionRayTraceResult<>(finalEnt);
    }

    public static <E extends Entity> EntityCollectionRayTraceResult<E> traceAllEntitiesInCapsule(Class<E> clazz, Level world,
                                                                                                  Vec3 from, Vec3 to,
                                                                                                  double radius,
                                                                                                  float entityExpansion,
                                                                                                  final Predicate<E> filter) {
        Predicate<E> predicate = input -> defaultFilter.test(input) && filter.test(input);
        AABB bb = new AABB(from, to).inflate(radius);
        List<E> entities = world.getEntitiesOfClass(clazz, bb, predicate);
        List<EntityCollectionRayTraceResult.TraceEntry<E>> finalEnt = new ArrayList<>();
        for (E entity : entities) {
            addCapsuleTraceHit(finalEnt, entity, entity.getBoundingBox().inflate(entityExpansion), from, to, radius);
        }
        if (!world.getPartEntities().isEmpty()) {
            Set<E> seenParts = new HashSet<>();
            for (PartEntity<?> p : world.getPartEntities()) {
                EntityTypeTest<Entity, E> typeTest = EntityTypeTest.forClass(clazz);
                E t = typeTest.tryCast(p.getParent());
                if (t == null || seenParts.contains(t)) {
                    continue;
                }
                AABB entityBB = p.getBoundingBox().inflate(entityExpansion);
                if (entityBB.intersects(bb) && predicate.test(t) && addCapsuleTraceHit(finalEnt, t, entityBB, from, to, radius)) {
                    seenParts.add(t);
                }
            }
        }
        return new EntityCollectionRayTraceResult<>(finalEnt);
    }

    private static <E extends Entity> boolean addCapsuleTraceHit(List<EntityCollectionRayTraceResult.TraceEntry<E>> finalEnt, E entity,
                                                                 AABB entityBB, Vec3 from, Vec3 to, double radius) {
        ClosestSegmentPointResult result = closestPointOnSegmentToAABB(from, to, entityBB);
        if (result.distanceSqr <= radius * radius) {
            finalEnt.add(new EntityCollectionRayTraceResult.TraceEntry<>(entity, from.distanceTo(result.closestPoint), result.closestPoint));
            return true;
        }
        return false;
    }

    private static ClosestSegmentPointResult closestPointOnSegmentToAABB(Vec3 from, Vec3 to, AABB box) {
        double low = 0.0;
        double high = 1.0;
        for (int i = 0; i < 32; i++) {
            double left = (2.0 * low + high) / 3.0;
            double right = (low + 2.0 * high) / 3.0;
            double leftDist = distanceSqrPointToAABB(from.lerp(to, left), box);
            double rightDist = distanceSqrPointToAABB(from.lerp(to, right), box);
            if (leftDist <= rightDist) {
                high = right;
            } else {
                low = left;
            }
        }
        double t = (low + high) * 0.5;
        Vec3 point = from.lerp(to, t);
        return new ClosestSegmentPointResult(point, distanceSqrPointToAABB(point, box));
    }

    private static double distanceSqrPointToAABB(Vec3 point, AABB box) {
        double dx = axisDistance(point.x, box.minX, box.maxX);
        double dy = axisDistance(point.y, box.minY, box.maxY);
        double dz = axisDistance(point.z, box.minZ, box.maxZ);
        return dx * dx + dy * dy + dz * dz;
    }

    private static double axisDistance(double value, double min, double max) {
        if (value < min) {
            return min - value;
        }
        if (value > max) {
            return value - max;
        }
        return 0.0;
    }

    private record ClosestSegmentPointResult(Vec3 closestPoint, double distanceSqr) {
    }

    private static <E extends Entity> HitResult rayTraceBlocksAndEntities(Class<E> clazz, Entity mainEntity,
                                                                          Vec3 from, Vec3 to, boolean stopOnLiquid,
                                                                          final Predicate<E> entityFilter, boolean testPickable) {
        BlockHitResult block = rayTraceBlocks(mainEntity, from, to, stopOnLiquid);
        if (block.getType() == HitResult.Type.BLOCK)
            to = block.getLocation();

        EntityHitResult entity = rayTraceEntities(clazz, mainEntity.getCommandSenderWorld(), from, to, Vec3.ZERO, 0.5f, 0.5f, entityFilter, testPickable);

        if (block.getType() == HitResult.Type.MISS) {
            return entity;
        } else {
            if (entity == null) {
                return block;
            } else {
                double blockDist = block.getLocation().distanceTo(from);
                double entityDist = entity.getLocation().distanceTo(from);
                if (blockDist < entityDist) {
                    return block;
                } else {
                    return entity;
                }
            }
        }
    }
}
