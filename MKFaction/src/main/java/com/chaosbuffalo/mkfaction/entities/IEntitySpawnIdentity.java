package com.chaosbuffalo.mkfaction.entities;

import net.minecraft.world.entity.Entity;

import java.util.Optional;
import java.util.UUID;

public interface IEntitySpawnIdentity {
    UUID getSpawnID();

    static Optional<IEntitySpawnIdentity> get(Entity entity) {
        return EntitySpawnIdentityManager.getSpawnIdentity(entity);
    }
}
