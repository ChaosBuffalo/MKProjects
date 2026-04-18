package com.chaosbuffalo.mkcore.abilities2.runtime;

import com.chaosbuffalo.mkcore.abilities2.definition.AbilityReactionDefinition;

import java.util.Objects;

public record RegisteredReaction(
        AbilityReactionHandle handle,
        AbilityReactionOwner owner,
        AbilityReactionDefinition definition
) {
    public RegisteredReaction {
        Objects.requireNonNull(handle, "handle");
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(definition, "definition");
    }
}
