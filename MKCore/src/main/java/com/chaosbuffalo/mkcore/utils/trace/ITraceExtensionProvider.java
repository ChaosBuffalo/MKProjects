package com.chaosbuffalo.mkcore.utils.trace;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.function.Predicate;

public interface ITraceExtensionProvider {

    <E extends Entity> List<TraceCandidate<E>> getTraceCandidates(Class<E> clazz, Level world,
                                                                  AABB traceBounds,
                                                                  final Predicate<E> filter,
                                                                  boolean testPickable);

    record TraceCandidate<E extends Entity>(E entity, AABB bounds) {
    }
}
