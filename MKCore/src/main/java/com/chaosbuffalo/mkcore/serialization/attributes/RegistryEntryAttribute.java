package com.chaosbuffalo.mkcore.serialization.attributes;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public class RegistryEntryAttribute<T> extends ResourceLocationAttribute {
    private final Registry<T> registry;

    public RegistryEntryAttribute(String name, Registry<T> registry, ResourceLocation defaultValue) {
        super(name, defaultValue);
        this.registry = registry;
    }

    public Optional<T> resolve() {
        return Optional.ofNullable(registry.get(getValue()));
    }
}
