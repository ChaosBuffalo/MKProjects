package com.chaosbuffalo.mkcore.compat.iaf;

import com.chaosbuffalo.mkcore.utils.EntityCollectionRayTraceResult;
import com.chaosbuffalo.mkcore.utils.trace.ITraceExtensionProvider;
import com.iafenvoy.iceandfire.entity.EntityMultipartPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.function.Predicate;

public class IAFTraceHandler implements ITraceExtensionProvider {

    @Override
    public <E extends Entity> EntityCollectionRayTraceResult<E> getCustomTraces(Class<E> clazz, Level world,
                                                                                Vec3 from, Vec3 to, Vec3 aaExpansion,
                                                                                float aaGrowth, float entityExpansion,
                                                                                Predicate<E> filter, boolean testPickable) {
        AABB bb = new AABB(from, to)
                .expandTowards(aaExpansion.x, aaExpansion.y, aaExpansion.z)
                .inflate(aaGrowth);
        List<EntityMultipartPart> entities = world.getEntitiesOfClass(EntityMultipartPart.class, bb, (ent) -> true);
        List<EntityCollectionRayTraceResult.TraceEntry<E>> finalEnt = new ArrayList<>();
        if (!entities.isEmpty()) {
            Set<E> seenParts = new HashSet<>();
            for (EntityMultipartPart entity : entities) {
                Entity parent = world.getEntities().get(entity.getParentId());
                if (parent != null) {
                    EntityTypeTest<Entity, E> typeTest = EntityTypeTest.forClass(clazz);
                    E t = typeTest.tryCast(parent);
                    if (seenParts.contains(t)) {
                        continue;
                    }
                    AABB entityBB = entity.getBoundingBox().inflate(entityExpansion);
                    if (t != null && entityBB.intersects(bb) && filter.test(t)) {
                        Optional<Vec3> intercept = entityBB.clip(from, to);
                        if (intercept.isPresent()) {
                            double dist = from.distanceTo(intercept.get());
                            finalEnt.add(new EntityCollectionRayTraceResult.TraceEntry<>(t, dist, intercept.get()));
                            seenParts.add(t);
                        }
                    }
                }
            }
        }
        return new EntityCollectionRayTraceResult<>(finalEnt);
    }
}
