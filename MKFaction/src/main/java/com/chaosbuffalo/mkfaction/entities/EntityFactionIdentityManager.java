package com.chaosbuffalo.mkfaction.entities;

import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EntityFactionIdentityManager {
    private static final List<IEntityFactionIdentityProvider> providers = new ArrayList<>();

    private EntityFactionIdentityManager() {
    }

    public static void registerProvider(IEntityFactionIdentityProvider provider) {
        Objects.requireNonNull(provider);
        providers.add(provider);
    }

    public static Optional<IEntityFactionIdentity> getIdentity(Entity entity) {
        for (IEntityFactionIdentityProvider provider : providers) {
            Optional<IEntityFactionIdentity> result = provider.getIdentity(entity);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }
}
