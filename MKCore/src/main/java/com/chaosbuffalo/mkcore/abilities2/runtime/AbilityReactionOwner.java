package com.chaosbuffalo.mkcore.abilities2.runtime;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public record AbilityReactionOwner(
        ReactionOwnerType type,
        UUID ownerId,
        @Nullable UUID stableSourceId,
        @Nullable ResourceLocation abilityId
) {
    public AbilityReactionOwner {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(ownerId, "ownerId");
    }
}
