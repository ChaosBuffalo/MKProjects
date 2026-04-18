package com.chaosbuffalo.mkcore.abilities2.actions;

import com.google.gson.JsonElement;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record AbilityConditionDefinition(String type, Map<String, JsonElement> data) {
    public AbilityConditionDefinition(String type) {
        this(type, Map.of());
    }

    public AbilityConditionDefinition {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Ability condition type must not be blank");
        }
        data = Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(data, "data")));
    }

    @Nullable
    public JsonElement get(String key) {
        return data.get(key);
    }
}
