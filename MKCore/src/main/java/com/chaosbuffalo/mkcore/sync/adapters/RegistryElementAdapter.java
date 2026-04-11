package com.chaosbuffalo.mkcore.sync.adapters;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public interface RegistryElementAdapter<K, V> {
    @Nullable
    V toRegistryValue(Registry<V> registry, K element);

    @Nullable
    K fromRegistryValue(Registry<V> registry, V value);

    static <V> RegistryElementAdapter<ResourceKey<V>, V> resourceKeys() {
        return new RegistryElementAdapter<>() {
            @Override
            public @Nullable V toRegistryValue(Registry<V> registry, ResourceKey<V> element) {
                return registry.get(element);
            }

            @Override
            public @Nullable ResourceKey<V> fromRegistryValue(Registry<V> registry, V value) {
                return registry.getResourceKey(value).orElse(null);
            }
        };
    }

    static <V> RegistryElementAdapter<ResourceLocation, V> resourceLocations() {
        return new RegistryElementAdapter<>() {
            @Override
            public @Nullable V toRegistryValue(Registry<V> registry, ResourceLocation element) {
                return registry.get(element);
            }

            @Override
            public @Nullable ResourceLocation fromRegistryValue(Registry<V> registry, V value) {
                return registry.getKey(value);
            }
        };
    }
}
