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

    public AbilityParameterDefinition getParameterDefinition(String id) {
        AbilityParameterDefinition parameter = definition.data().parameters().get(id);
        if (parameter == null) {
            throw new IllegalArgumentException("Unknown parameter '%s' on ability %s"
                    .formatted(id, definition.data().id()));
        }
        return parameter;
    }

    public AbilityValue getPatchedParameter(String id) {
        AbilityValue value = patchedParameters.get(id);
        if (value == null) {
            throw new IllegalArgumentException("Patched parameter '%s' not found on ability %s"
                    .formatted(id, definition.data().id()));
        }
        return value;
    }

    public AbilityValue resolveParameter(String id, Map<String, AbilityValue> grantOverrides) {
        AbilityParameterDefinition parameter = getParameterDefinition(id);
        AbilityValue override = grantOverrides.get(id);
        if (override != null) {
            if (!parameter.grantOverrideable()) {
                throw new IllegalArgumentException("Parameter '%s' on ability %s is not grantOverrideable"
                        .formatted(id, definition.data().id()));
            }
            if (override.kind() != parameter.kind()) {
                throw new IllegalArgumentException("Grant override '%s' on ability %s has kind %s but expected %s"
                        .formatted(id, definition.data().id(), override.kind(), parameter.kind()));
            }
            return override;
        }
        return getPatchedParameter(id);
    }

    public Map<String, AbilityValue> validateGrantParameterOverrides(Map<String, AbilityValue> overrides) {
        Objects.requireNonNull(overrides, "overrides");
        LinkedHashMap<String, AbilityValue> copy = new LinkedHashMap<>();
        for (Map.Entry<String, AbilityValue> entry : overrides.entrySet()) {
            AbilityParameterDefinition parameter = getParameterDefinition(entry.getKey());
            if (!parameter.grantOverrideable()) {
                throw new IllegalArgumentException("Grant override '%s' on ability %s is not allowed"
                        .formatted(entry.getKey(), definition.data().id()));
            }
            if (entry.getValue().kind() != parameter.kind()) {
                throw new IllegalArgumentException("Grant override '%s' on ability %s has kind %s but expected %s"
                        .formatted(entry.getKey(), definition.data().id(), entry.getValue().kind(), parameter.kind()));
            }
            copy.put(entry.getKey(), entry.getValue());
        }
        return Collections.unmodifiableMap(copy);
    }
}
