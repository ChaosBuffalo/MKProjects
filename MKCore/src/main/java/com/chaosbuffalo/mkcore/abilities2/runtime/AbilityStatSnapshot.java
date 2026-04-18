package com.chaosbuffalo.mkcore.abilities2.runtime;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record AbilityStatSnapshot(
        Object2DoubleMap<ResourceLocation> attributes,
        double castSpeed,
        double cooldownRate,
        double manaCostMultiplier,
        double critChance,
        double critMultiplier
) {
    public AbilityStatSnapshot {
        Objects.requireNonNull(attributes, "attributes");
        attributes = Object2DoubleMaps.unmodifiable(new Object2DoubleOpenHashMap<>(attributes));
    }
}
