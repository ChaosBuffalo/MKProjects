package com.chaosbuffalo.mkcore.abilities2.definition;

import javax.annotation.Nullable;
import java.util.Objects;

public record AbilityParameterDefinition(
        String id,
        AbilityValue defaultValue,
        AbilityValueKind kind,
        boolean patchable,
        boolean grantOverrideable,
        @Nullable String description
) {
    public AbilityParameterDefinition {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Ability parameter id must not be blank");
        }
        Objects.requireNonNull(defaultValue, "defaultValue");
        Objects.requireNonNull(kind, "kind");
        if (defaultValue.kind() != kind) {
            throw new IllegalArgumentException("Ability parameter '%s' default kind %s does not match declared kind %s"
                    .formatted(id, defaultValue.kind(), kind));
        }
    }
}
