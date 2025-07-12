package com.chaosbuffalo.mkcore.utils.trace;

import com.chaosbuffalo.mkcore.utils.EntityCollectionRayTraceResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

public interface ITraceExtensionProvider {

    <E extends Entity> EntityCollectionRayTraceResult<E> getCustomTraces(Class<E> clazz, Level world,
                                                                         Vec3 from, Vec3 to,
                                                                         Vec3 aaExpansion,
                                                                         float aaGrowth,
                                                                         float entityExpansion,
                                                                         final Predicate<E> filter,
                                                                         boolean testPickable);
}
