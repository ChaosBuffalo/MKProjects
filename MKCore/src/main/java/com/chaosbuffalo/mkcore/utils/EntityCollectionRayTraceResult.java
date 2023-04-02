package com.chaosbuffalo.mkcore.utils;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class EntityCollectionRayTraceResult<E extends Entity> {

    public static class TraceEntry<E> {
        public E entity;
        public double distance;
        public Vec3 intercept;

        public TraceEntry(E entity, double distance, Vec3 intercept) {
            this.entity = entity;
            this.distance = distance;
            this.intercept = intercept;
        }
    }

    private final List<TraceEntry<E>> entities;

    public EntityCollectionRayTraceResult(List<TraceEntry<E>> entities) {
        this.entities = entities;
    }

    public List<TraceEntry<E>> getEntities() {
        return entities;
    }

}
