package com.chaosbuffalo.mkcore.abilities2.definition;

import com.chaosbuffalo.mkcore.abilities2.actions.AbilityAction;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public record AbilityDefinitionData(
        ResourceLocation id,
        AbilityPresentation presentation,
        ResourceLocation slotFamily,
        Set<ResourceLocation> schools,
        Set<ResourceLocation> tags,
        Map<String, AbilityParameterDefinition> parameters,
        Map<String, AbilityActivationDefinition> activations,
        Map<String, List<AbilityAction>> entryPoints,
        Map<String, AbilityReactionDefinition> reactions,
        Map<String, AbilityDeliveryDefinition> deliveries
) {
    public AbilityDefinitionData {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(presentation, "presentation");
        Objects.requireNonNull(slotFamily, "slotFamily");
        schools = immutableLinkedSet(schools);
        tags = immutableLinkedSet(tags);
        parameters = immutableLinkedMap(parameters);
        activations = immutableLinkedMap(activations);
        entryPoints = immutableActionMap(entryPoints);
        reactions = immutableLinkedMap(reactions);
        deliveries = immutableLinkedMap(deliveries);
    }

    private static <T> Set<T> immutableLinkedSet(Set<T> source) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(Objects.requireNonNull(source, "source")));
    }

    private static <K, V> Map<K, V> immutableLinkedMap(Map<K, V> source) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(source, "source")));
    }

    private static Map<String, List<AbilityAction>> immutableActionMap(Map<String, List<AbilityAction>> source) {
        Objects.requireNonNull(source, "source");
        LinkedHashMap<String, List<AbilityAction>> copy = new LinkedHashMap<>();
        source.forEach((key, value) -> copy.put(key, List.copyOf(Objects.requireNonNull(value, key))));
        return Collections.unmodifiableMap(copy);
    }
}
