package com.chaosbuffalo.mkcore.utils.trace;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.entity.PartEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class VanillaPartEntityTraceProvider implements ITraceExtensionProvider {

    @Override
    public <E extends Entity> List<TraceCandidate<E>> getTraceCandidates(Class<E> clazz, Level world,
                                                                         AABB traceBounds,
                                                                         Predicate<E> filter,
                                                                         boolean testPickable) {
        if (world.getPartEntities().isEmpty()) {
            return List.of();
        }

        List<TraceCandidate<E>> results = new ArrayList<>();
        Set<E> seenParts = new HashSet<>();
        EntityTypeTest<Entity, E> typeTest = EntityTypeTest.forClass(clazz);
        for (PartEntity<?> part : world.getPartEntities()) {
            if (testPickable && !part.isPickable()) {
                continue;
            }

            E parent = typeTest.tryCast(part.getParent());
            if (parent == null || seenParts.contains(parent)) {
                continue;
            }

            AABB bounds = part.getBoundingBox();
            if (!bounds.intersects(traceBounds) || !filter.test(parent)) {
                continue;
            }

            results.add(new TraceCandidate<>(parent, bounds));
            seenParts.add(parent);
        }

        return results;
    }
}
