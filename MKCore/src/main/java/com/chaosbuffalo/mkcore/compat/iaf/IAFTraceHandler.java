package com.chaosbuffalo.mkcore.compat.iaf;

import com.chaosbuffalo.mkcore.utils.trace.ITraceExtensionProvider;
import com.iafenvoy.iceandfire.entity.EntityDragonBase;
import com.iafenvoy.iceandfire.entity.EntityMultipartPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

import java.util.*;
import java.util.function.Predicate;

public class IAFTraceHandler implements ITraceExtensionProvider {

    @Override
    public <E extends Entity> List<TraceCandidate<E>> getTraceCandidates(Class<E> clazz, Level world,
                                                                         AABB traceBounds,
                                                                         Predicate<E> filter,
                                                                         boolean testPickable) {
        List<EntityMultipartPart> entities = world.getEntitiesOfClass(EntityMultipartPart.class, traceBounds, (ent) -> true);
        if (entities.isEmpty()) {
            return List.of();
        }
        List<TraceCandidate<E>> finalEnt = new ArrayList<>();
        Set<E> seenParts = new HashSet<>();
        for (EntityMultipartPart entity : entities) {
            if (testPickable && !entity.isPickable()) {
                continue;
            }
            Entity parent = world.getEntities().get(entity.getParentId());
            if (parent != null) {
                EntityTypeTest<Entity, E> typeTest = EntityTypeTest.forClass(clazz);
                E t = typeTest.tryCast(parent);
                if (seenParts.contains(t)) {
                    continue;
                }
                if (t != null && entity.getBoundingBox().intersects(traceBounds) && filter.test(t)) {
                    finalEnt.add(new TraceCandidate<>(t, entity.getBoundingBox()));
                    seenParts.add(t);
                }
            }
        }
        return finalEnt;
    }
}
