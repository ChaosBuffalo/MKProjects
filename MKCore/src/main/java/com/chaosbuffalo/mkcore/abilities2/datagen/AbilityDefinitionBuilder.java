package com.chaosbuffalo.mkcore.abilities2.datagen;

import com.chaosbuffalo.mkcore.abilities2.actions.AbilityAction;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityActivationDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityDefinitionData;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityDeliveryDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityParameterDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityPresentation;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityReactionDefinition;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class AbilityDefinitionBuilder {
    private ResourceLocation id;
    private AbilityPresentation presentation;
    private ResourceLocation slotFamily;
    private final Set<ResourceLocation> schools = new LinkedHashSet<>();
    private final Set<ResourceLocation> tags = new LinkedHashSet<>();
    private final Map<String, AbilityParameterDefinition> parameters = new LinkedHashMap<>();
    private final Map<String, AbilityActivationDefinition> activations = new LinkedHashMap<>();
    private final Map<String, List<AbilityAction>> entryPoints = new LinkedHashMap<>();
    private final Map<String, AbilityReactionDefinition> reactions = new LinkedHashMap<>();
    private final Map<String, AbilityDeliveryDefinition> deliveries = new LinkedHashMap<>();

    private AbilityDefinitionBuilder(ResourceLocation id, AbilityPresentation presentation, ResourceLocation slotFamily) {
        this.id = Objects.requireNonNull(id, "id");
        this.presentation = Objects.requireNonNull(presentation, "presentation");
        this.slotFamily = Objects.requireNonNull(slotFamily, "slotFamily");
    }

    public static AbilityDefinitionBuilder create(ResourceLocation id,
                                                  String name,
                                                  String description,
                                                  ResourceLocation slotFamily) {
        return new AbilityDefinitionBuilder(id, new AbilityPresentation(name, description, null, null, null,
                null, null), slotFamily);
    }

    public static AbilityDefinitionBuilder create(ResourceLocation id,
                                                  AbilityPresentation presentation,
                                                  ResourceLocation slotFamily) {
        return new AbilityDefinitionBuilder(id, presentation, slotFamily);
    }

    public static AbilityDefinitionBuilder from(AbilityDefinitionData definition) {
        AbilityDefinitionBuilder builder = new AbilityDefinitionBuilder(
                definition.id(),
                definition.presentation(),
                definition.slotFamily()
        );
        builder.schools.addAll(definition.schools());
        builder.tags.addAll(definition.tags());
        builder.parameters.putAll(definition.parameters());
        builder.activations.putAll(definition.activations());
        definition.entryPoints().forEach((id, actions) -> builder.entryPoints.put(id, new ArrayList<>(actions)));
        builder.reactions.putAll(definition.reactions());
        builder.deliveries.putAll(definition.deliveries());
        return builder;
    }

    public AbilityDefinitionBuilder id(ResourceLocation id) {
        this.id = Objects.requireNonNull(id, "id");
        return this;
    }

    public AbilityDefinitionBuilder presentation(AbilityPresentation presentation) {
        this.presentation = Objects.requireNonNull(presentation, "presentation");
        return this;
    }

    public AbilityDefinitionBuilder slotFamily(ResourceLocation slotFamily) {
        this.slotFamily = Objects.requireNonNull(slotFamily, "slotFamily");
        return this;
    }

    public AbilityDefinitionBuilder school(ResourceLocation school) {
        schools.add(Objects.requireNonNull(school, "school"));
        return this;
    }

    public AbilityDefinitionBuilder schools(Collection<ResourceLocation> schools) {
        this.schools.addAll(Objects.requireNonNull(schools, "schools"));
        return this;
    }

    public AbilityDefinitionBuilder tag(ResourceLocation tag) {
        tags.add(Objects.requireNonNull(tag, "tag"));
        return this;
    }

    public AbilityDefinitionBuilder tags(Collection<ResourceLocation> tags) {
        this.tags.addAll(Objects.requireNonNull(tags, "tags"));
        return this;
    }

    public AbilityDefinitionBuilder parameter(AbilityParameterDefinition parameter) {
        AbilityParameterDefinition resolved = Objects.requireNonNull(parameter, "parameter");
        parameters.put(resolved.id(), resolved);
        return this;
    }

    public AbilityDefinitionBuilder parameters(Collection<AbilityParameterDefinition> parameters) {
        Objects.requireNonNull(parameters, "parameters");
        parameters.forEach(this::parameter);
        return this;
    }

    public AbilityDefinitionBuilder activation(String activationId, AbilityActivationDefinition activation) {
        activations.put(requireKey(activationId, "activationId"), Objects.requireNonNull(activation, "activation"));
        return this;
    }

    public AbilityDefinitionBuilder entryPoint(String entryPointId, List<AbilityAction> actions) {
        entryPoints.put(requireKey(entryPointId, "entryPointId"), new ArrayList<>(Objects.requireNonNull(actions, "actions")));
        return this;
    }

    public AbilityDefinitionBuilder appendEntryPointActions(String entryPointId, List<AbilityAction> actions) {
        entryPoints.computeIfAbsent(requireKey(entryPointId, "entryPointId"), ignored -> new ArrayList<>())
                .addAll(Objects.requireNonNull(actions, "actions"));
        return this;
    }

    public AbilityDefinitionBuilder reaction(String reactionId, AbilityReactionDefinition reaction) {
        reactions.put(requireKey(reactionId, "reactionId"), Objects.requireNonNull(reaction, "reaction"));
        return this;
    }

    public AbilityDefinitionBuilder delivery(String deliveryId, AbilityDeliveryDefinition delivery) {
        deliveries.put(requireKey(deliveryId, "deliveryId"), Objects.requireNonNull(delivery, "delivery"));
        return this;
    }

    public AbilityDefinitionData build() {
        LinkedHashMap<String, List<AbilityAction>> builtEntryPoints = new LinkedHashMap<>();
        entryPoints.forEach((id, actions) -> builtEntryPoints.put(id, List.copyOf(actions)));
        return new AbilityDefinitionData(
                id,
                presentation,
                slotFamily,
                new LinkedHashSet<>(schools),
                new LinkedHashSet<>(tags),
                new LinkedHashMap<>(parameters),
                new LinkedHashMap<>(activations),
                builtEntryPoints,
                new LinkedHashMap<>(reactions),
                new LinkedHashMap<>(deliveries)
        );
    }

    private static String requireKey(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " must not be blank");
        }
        return value;
    }
}
