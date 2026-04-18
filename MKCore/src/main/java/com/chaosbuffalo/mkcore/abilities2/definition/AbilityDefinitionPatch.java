package com.chaosbuffalo.mkcore.abilities2.definition;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;

public record AbilityDefinitionPatch(
        ResourceLocation patchId,
        ResourceLocation abilityId,
        int priority,
        int loadOrder,
        List<AbilityPatchOperation> operations
) {
    public AbilityDefinitionPatch {
        Objects.requireNonNull(patchId, "patchId");
        Objects.requireNonNull(abilityId, "abilityId");
        operations = List.copyOf(Objects.requireNonNull(operations, "operations"));
    }
}
