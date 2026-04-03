package com.chaosbuffalo.mkfaction.entities;

import net.minecraft.world.entity.Entity;

import java.util.Optional;
import java.util.UUID;

public interface IEntityFactionIdentity {
    UUID getFactionIdentity();

    static Optional<IEntityFactionIdentity> get(Entity entity) {
        return EntityFactionIdentityManager.getIdentity(entity);
    }
}
