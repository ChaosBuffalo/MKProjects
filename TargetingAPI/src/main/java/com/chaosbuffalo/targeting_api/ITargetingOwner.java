package com.chaosbuffalo.targeting_api;

import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

/**
 * Exposes an alternate owner {@link Entity} for targeting relationship checks.
 * <p>
 * Implement this on entities whose targeting relationship should be resolved
 * against another entity rather than the entity instance itself.
 */
public interface ITargetingOwner {

    /**
     * Returns the {@link Entity} that should be treated as this entity's
     * targeting owner.
     *
     * @return the effective owner for targeting checks, or {@code null} if this
     * entity should be evaluated directly
     */
    @Nullable
    Entity getTargetingOwner();
}
