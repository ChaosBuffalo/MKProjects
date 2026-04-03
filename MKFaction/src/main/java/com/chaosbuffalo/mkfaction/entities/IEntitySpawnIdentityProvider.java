package com.chaosbuffalo.mkfaction.entities;

import net.minecraft.world.entity.Entity;

import java.util.Optional;

public interface IEntitySpawnIdentityProvider {
    Optional<IEntitySpawnIdentity> getSpawnIdentity(Entity entity);
}
