package com.chaosbuffalo.mkcore.abilities2.definition;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Objects;

public record AbilityCostDefinition(
        CostKind kind,
        @Nullable ResourceLocation resourceId,
        AbilityScalar amount
) {
    public AbilityCostDefinition {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(amount, "amount");
        if (kind == CostKind.CUSTOM_RESOURCE && resourceId == null) {
            throw new IllegalArgumentException("CUSTOM_RESOURCE costs require a resourceId");
        }
        if (kind != CostKind.CUSTOM_RESOURCE && resourceId != null) {
            throw new IllegalArgumentException("%s costs must not declare a resourceId".formatted(kind));
        }
    }
}
