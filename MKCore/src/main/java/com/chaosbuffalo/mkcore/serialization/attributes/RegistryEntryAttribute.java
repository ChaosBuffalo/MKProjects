package com.chaosbuffalo.mkcore.serialization.attributes;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Optional;

public class RegistryEntryAttribute<T extends IForgeRegistryEntry<T>> extends ResourceLocationAttribute {
    private final IForgeRegistry<T> registry;

    public RegistryEntryAttribute(String name, IForgeRegistry<T> registry, ResourceLocation defaultValue) {
        super(name, defaultValue);
        this.registry = registry;
    }

    public Optional<T> resolve() {
        return Optional.ofNullable(registry.getValue(getValue()));
    }
}
