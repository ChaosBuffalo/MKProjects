package com.chaosbuffalo.mkcore.abilities2.runtime;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record AbilityGrantSource(
        ResourceLocation sourceType,
        ResourceLocation sourceId
) {
    public AbilityGrantSource {
        Objects.requireNonNull(sourceType, "sourceType");
        Objects.requireNonNull(sourceId, "sourceId");
    }
}
