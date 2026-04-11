package com.chaosbuffalo.mkcore.sync;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;

// This exists to pass around anything we need to provide to sync or serialization methods
public record SyncContext(RegistryAccess registryAccess) {
    public SyncContext(HolderLookup.Provider provider) {
        this(requireRegistryAccess(provider));
    }

    private static RegistryAccess requireRegistryAccess(HolderLookup.Provider provider) {
        if (provider instanceof RegistryAccess registryAccess) {
            return registryAccess;
        }
        throw new IllegalArgumentException("SyncContext requires RegistryAccess but received: " + provider);
    }

    public HolderLookup.Provider provider() {
        return registryAccess;
    }

    public <V> Registry<V> registryOrThrow(ResourceKey<? extends Registry<V>> key) {
        return registryAccess.registryOrThrow(key);
    }
}
