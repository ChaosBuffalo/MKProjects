package com.chaosbuffalo.mkcore.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
                e.isPickable() &&
                entityPredicate.test(e);

        HitResult position = null;

        if (mainEntity.level != null) {
            Vec3 look = mainEntity.getLookAngle().scale(distance);
            Vec3 from = mainEntity.position().add(0, mainEntity.getEyeHeight(), 0);
            Vec3 to = from.add(look);
            position = rayTraceBlocksAndEntities(clazz, mainEntity, from, to, false, finalFilter);
        }
        return position;
    }

    public static BlockHitResult rayTraceBlocks(Entity entity, Vec3 from, Vec3 to, boolean stopOnLiquid) {
        ClipContext.Fluid mode = stopOnLiquid ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE;
        ClipContext context = new ClipContext(from, to, ClipContext.Block.COLLIDER, mode, entity);
        return entity.getCommandSenderWorld().clip(context);
    }

    public static HitResult rayTraceEntities(Level world, Vec3 from, Vec3 to, Vec3 aaExpansion, float aaGrowth,
                                             float entityExpansion, final Predicate<Entity> filter) {
        return rayTraceEntities(Entity.class, world, from, to, aaExpansion, aaGrowth, entityExpansion, filter);
    }

    public static <E extends Entity> EntityHitResult rayTraceEntities(Class<E> clazz, Level world,
                                                                      Vec3 from, Vec3 to,
                                                                      Vec3 aaExpansion,
                                                                      float aaGrowth,
                                                                      float entityExpansion,
                                                                      final Predicate<E> filter) {

        Predicate<E> predicate = input -> defaultFilter.test(input) && filter.test(input);

        Entity nearest = null;
        double distance = 0;

        AABB bb = new AABB(new BlockPos(from), new BlockPos(to))
                .expandTowards(aaExpansion.x, aaExpansion.y, aaExpansion.z)
                .inflate(aaGrowth);
        List<E> entities = world.getEntitiesOfClass(clazz, bb, predicate);
        for (Entity entity : entities) {
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
        AABB bb = new AABB(new BlockPos(from), new BlockPos(to))
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
        return new EntityCollectionRayTraceResult<>(finalEnt);
    }

    private static <E extends Entity> HitResult rayTraceBlocksAndEntities(Class<E> clazz, Entity mainEntity,
                                                                          Vec3 from, Vec3 to, boolean stopOnLiquid,
                                                                          final Predicate<E> entityFilter) {
        BlockHitResult block = rayTraceBlocks(mainEntity, from, to, stopOnLiquid);
        if (block.getType() == HitResult.Type.BLOCK)
            to = block.getLocation();

        EntityHitResult entity = rayTraceEntities(clazz, mainEntity.getCommandSenderWorld(), from, to, Vec3.ZERO, 0.5f, 0.5f, entityFilter);

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
