package com.chaosbuffalo.mkcore.abilities2.definition;

import com.chaosbuffalo.mkcore.abilities2.actions.AbilityAction;

import javax.annotation.Nullable;
import java.util.*;

public final class CompiledAbilityDefinition {
    private final AbilityDefinitionData data;
    private final Map<String, AbilityActivationDefinition> activations;
    private final Map<String, List<AbilityAction>> entryPoints;
    private final Map<String, AbilityReactionDefinition> reactions;
    private final Map<String, AbilityDeliveryDefinition> deliveries;

    private CompiledAbilityDefinition(AbilityDefinitionData data,
                                      Map<String, AbilityActivationDefinition> activations,
                                      Map<String, List<AbilityAction>> entryPoints,
                                      Map<String, AbilityReactionDefinition> reactions,
                                      Map<String, AbilityDeliveryDefinition> deliveries) {
        this.data = data;
        this.activations = activations;
        this.entryPoints = entryPoints;
        this.reactions = reactions;
        this.deliveries = deliveries;
    }

    public static CompiledAbilityDefinition compile(AbilityDefinitionData data) {
        Objects.requireNonNull(data, "data");

        Map<String, AbilityActivationDefinition> activations = immutableLinkedMap(data.activations());
        Map<String, List<AbilityAction>> entryPoints = immutableActionMap(data.entryPoints());
        Map<String, AbilityReactionDefinition> reactions = immutableLinkedMap(data.reactions());
        Map<String, AbilityDeliveryDefinition> deliveries = immutableLinkedMap(data.deliveries());

        validateParameters(data);
        activations.forEach((id, activation) -> validateActivation(data, id, activation, entryPoints));
        reactions.forEach((id, reaction) -> validateReaction(data, id, reaction, activations));
        deliveries.forEach((id, delivery) -> validateDelivery(data, id, delivery, activations));

        return new CompiledAbilityDefinition(data, activations, entryPoints, reactions, deliveries);
    }

    public AbilityDefinitionData data() {
        return data;
    }

    public Map<String, AbilityActivationDefinition> activations() {
        return activations;
    }

    public Map<String, List<AbilityAction>> entryPoints() {
        return entryPoints;
    }

    public Map<String, AbilityReactionDefinition> reactions() {
        return reactions;
    }

    public Map<String, AbilityDeliveryDefinition> deliveries() {
        return deliveries;
    }

    @Nullable
    public AbilityActivationDefinition getActivation(String activationId) {
        return activations.get(activationId);
    }

    @Nullable
    public List<AbilityAction> getEntryPoint(String entryPointId) {
        return entryPoints.get(entryPointId);
    }

    @Nullable
    public AbilityReactionDefinition getReaction(String reactionId) {
        return reactions.get(reactionId);
    }

    @Nullable
    public AbilityDeliveryDefinition getDelivery(String deliveryId) {
        return deliveries.get(deliveryId);
    }

    public Map<String, AbilityValue> createDefaultParameterMap() {
        LinkedHashMap<String, AbilityValue> resolved = new LinkedHashMap<>();
        data.parameters().forEach((id, definition) -> resolved.put(id, definition.defaultValue()));
        return Collections.unmodifiableMap(resolved);
    }

    private static void validateParameters(AbilityDefinitionData data) {
        data.parameters().forEach((key, value) -> {
            if (!key.equals(value.id())) {
                throw error(data, "Parameter key '%s' does not match definition id '%s'".formatted(key, value.id()));
            }
        });
    }

    private static void validateActivation(AbilityDefinitionData data,
                                           String activationId,
                                           AbilityActivationDefinition activation,
                                           Map<String, List<AbilityAction>> entryPoints) {
        if (!entryPoints.containsKey(activation.entryPoint())) {
            throw error(data, "Activation '%s' references unknown entry point '%s'"
                    .formatted(activationId, activation.entryPoint()));
        }

        ActivationKind kind = activation.kind();
        ActivationBehavior behavior = activation.behavior();
        if (behavior instanceof ActivationBehavior.ChannelBehavior channel) {
            if (!entryPoints.containsKey(channel.tickEntryPoint())) {
                throw error(data, "Activation '%s' channel tickEntryPoint '%s' is missing"
                        .formatted(activationId, channel.tickEntryPoint()));
            }
        } else if (behavior instanceof ActivationBehavior.AuraBehavior aura) {
            if (!entryPoints.containsKey(aura.pulseEntryPoint())) {
                throw error(data, "Activation '%s' aura pulseEntryPoint '%s' is missing"
                        .formatted(activationId, aura.pulseEntryPoint()));
            }
        }

        switch (kind) {
            case MANUAL, AI -> {
                if (!(behavior instanceof ActivationBehavior.InstantBehavior
                        || behavior instanceof ActivationBehavior.ChannelBehavior)) {
                    throw error(data, "Activation '%s' kind %s only supports instant or channel behavior"
                            .formatted(activationId, kind));
                }
            }
            case TOGGLE_ENABLE -> {
                if (!(behavior instanceof ActivationBehavior.InstantBehavior
                        || behavior instanceof ActivationBehavior.AuraBehavior)) {
                    throw error(data, "Activation '%s' kind %s only supports instant or aura behavior"
                            .formatted(activationId, kind));
                }
            }
            case PROC, PASSIVE_SETUP, PASSIVE_TEARDOWN, TOGGLE_DISABLE -> {
                if (!(behavior instanceof ActivationBehavior.InstantBehavior)) {
                    throw error(data, "Activation '%s' kind %s must use instant behavior"
                            .formatted(activationId, kind));
                }
            }
        }

        if (!(kind == ActivationKind.MANUAL || kind == ActivationKind.AI)) {
            if (activation.castTicks() != 0) {
                throw error(data, "Activation '%s' kind %s must have castTicks = 0"
                        .formatted(activationId, kind));
            }
            if (activation.affectedByCastSpeed()) {
                throw error(data, "Activation '%s' kind %s must not be affectedByCastSpeed"
                        .formatted(activationId, kind));
            }
            if (activation.refundPolicy() != InterruptRefundPolicy.NONE) {
                throw error(data, "Activation '%s' kind %s must use refundPolicy NONE"
                        .formatted(activationId, kind));
            }
        }
    }

    private static void validateReaction(AbilityDefinitionData data,
                                         String reactionId,
                                         AbilityReactionDefinition reaction,
                                         Map<String, AbilityActivationDefinition> activations) {
        if (!activations.containsKey(reaction.activationId())) {
            throw error(data, "Reaction '%s' references unknown activation '%s'"
                    .formatted(reactionId, reaction.activationId()));
        }
    }

    private static void validateDelivery(AbilityDefinitionData data,
                                         String deliveryId,
                                         AbilityDeliveryDefinition delivery,
                                         Map<String, AbilityActivationDefinition> activations) {
        if (delivery.kind() == DeliveryKind.PROJECTILE && delivery.entityType() == null) {
            throw error(data, "Delivery '%s' kind PROJECTILE requires an entityType".formatted(deliveryId));
        }
        validateActivationReference(data, deliveryId, "onImpactActivationId", delivery.onImpactActivationId(), activations);
        validateActivationReference(data, deliveryId, "onAirTickActivationId", delivery.onAirTickActivationId(), activations);
        validateActivationReference(data, deliveryId, "onGroundTickActivationId", delivery.onGroundTickActivationId(), activations);
    }

    private static void validateActivationReference(AbilityDefinitionData data,
                                                    String deliveryId,
                                                    String fieldName,
                                                    @Nullable String activationId,
                                                    Map<String, AbilityActivationDefinition> activations) {
        if (activationId != null && !activations.containsKey(activationId)) {
            throw error(data, "Delivery '%s' field %s references unknown activation '%s'"
                    .formatted(deliveryId, fieldName, activationId));
        }
    }

    private static IllegalArgumentException error(AbilityDefinitionData data, String message) {
        return new IllegalArgumentException("Invalid ability definition %s: %s".formatted(data.id(), message));
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
