package com.chaosbuffalo.mkcore.sync;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;

// This exists to pass around anything we need to provide to sync or serialization methods
public record SyncContext(RegistryAccess registryAccess) {
    public HolderLookup.Provider provider() {
        return registryAccess;
    }

    public <V> Registry<V> registryOrThrow(ResourceKey<? extends Registry<V>> key) {
        return registryAccess.registryOrThrow(key);
    }
}
