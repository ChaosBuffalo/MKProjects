package com.chaosbuffalo.mkcore.abilities2.runtime;

import com.chaosbuffalo.mkcore.abilities2.definition.AbilityParameterDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityValue;
import com.chaosbuffalo.mkcore.abilities2.definition.CompiledAbilityDefinition;

import java.util.*;

public record PatchedAbilityDefinition(
        CompiledAbilityDefinition definition,
        Map<String, AbilityValue> patchedParameters
) {
    public PatchedAbilityDefinition {
        Objects.requireNonNull(definition, "definition");
        patchedParameters = immutableAndValidated(definition, patchedParameters);
    }

    private static Map<String, AbilityValue> immutableAndValidated(CompiledAbilityDefinition definition,
                                                                   Map<String, AbilityValue> source) {
        Objects.requireNonNull(source, "patchedParameters");
        LinkedHashMap<String, AbilityValue> copy = new LinkedHashMap<>(source);
        Set<String> remaining = new LinkedHashSet<>(copy.keySet());
        for (Map.Entry<String, AbilityParameterDefinition> entry : definition.data().parameters().entrySet()) {
            String id = entry.getKey();
            AbilityValue value = copy.get(id);
            if (value == null) {
                throw new IllegalArgumentException("Patched definition %s is missing parameter '%s'"
                        .formatted(definition.data().id(), id));
            }
            if (value.kind() != entry.getValue().kind()) {
                throw new IllegalArgumentException("Patched definition %s parameter '%s' has kind %s but expected %s"
                        .formatted(definition.data().id(), id, value.kind(), entry.getValue().kind()));
            }
            remaining.remove(id);
        }
        if (!remaining.isEmpty()) {
            throw new IllegalArgumentException("Patched definition %s contains unknown parameters %s"
                    .formatted(definition.data().id(), remaining));
        }
        return Collections.unmodifiableMap(copy);
    }
}
