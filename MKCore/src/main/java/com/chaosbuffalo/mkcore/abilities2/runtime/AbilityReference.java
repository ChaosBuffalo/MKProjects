package com.chaosbuffalo.mkcore.abilities2.runtime;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public record AbilityReference(
        ResourceLocation abilityId,
        @Nullable UUID grantId
) {
    public AbilityReference {
        Objects.requireNonNull(abilityId, "abilityId");
    }

    public boolean hasGrantId() {
        return grantId != null;
    }
}
