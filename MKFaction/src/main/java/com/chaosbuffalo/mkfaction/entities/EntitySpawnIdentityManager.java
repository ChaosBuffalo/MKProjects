package com.chaosbuffalo.mkfaction.entities;

import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EntitySpawnIdentityManager {
    private static final List<IEntitySpawnIdentityProvider> providers = new ArrayList<>();

    private EntitySpawnIdentityManager() {
    }

    public static void registerProvider(IEntitySpawnIdentityProvider provider) {
        Objects.requireNonNull(provider);
        providers.add(provider);
    }

    public static Optional<IEntitySpawnIdentity> getSpawnIdentity(Entity entity) {
        for (IEntitySpawnIdentityProvider provider : providers) {
            Optional<IEntitySpawnIdentity> result = provider.getSpawnIdentity(entity);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }
}
