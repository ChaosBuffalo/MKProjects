package com.chaosbuffalo.mkcore.abilities2.codec;

import com.chaosbuffalo.mkcore.abilities2.actions.AbilityAction;
import com.chaosbuffalo.mkcore.abilities2.actions.AbilityActionCodecs;
import com.chaosbuffalo.mkcore.abilities2.actions.AbilityConditionDefinition;
import com.chaosbuffalo.mkcore.abilities2.actions.AbilityEventFilter;
import com.chaosbuffalo.mkcore.abilities2.definition.*;
import com.chaosbuffalo.mkcore.abilities2.runtime.AbilityEventType;
import com.chaosbuffalo.mkcore.abilities2.runtime.StatCapturePolicy;
import com.chaosbuffalo.mkcore.utils.CommonCodecs;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class AbilityCodecs {
    private AbilityCodecs() {
    }

    public static final Codec<JsonElement> JSON_ELEMENT_CODEC = Codec.PASSTHROUGH.xmap(
            dynamic -> dynamic.convert(JsonOps.INSTANCE).getValue(),
            json -> new Dynamic<>(JsonOps.INSTANCE, json)
    );

    public static final Codec<Map<String, JsonElement>> JSON_OBJECT_CODEC = Codec.unboundedMap(Codec.STRING, JSON_ELEMENT_CODEC)
            .xmap(map -> Collections.unmodifiableMap(new LinkedHashMap<>(map)), LinkedHashMap::new);

    public static <E extends Enum<E>> Codec<E> enumCodec(Class<E> enumClass) {
        Map<String, E> byName = new HashMap<>();
        for (E value : enumClass.getEnumConstants()) {
            byName.put(serializedName(value), value);
        }
        return Codec.STRING.comapFlatMap(name -> {
                    E value = byName.get(name);
                    if (value == null) {
                        return DataResult.error(() -> "Unknown " + enumClass.getSimpleName() + " value '" + name + "'");
                    }
                    return DataResult.success(value);
                },
                AbilityCodecs::serializedName);
    }

    public static <T> Codec<T> typedJsonObjectCodec(String discriminatorField,
                                                    Function<T, String> discriminatorGetter,
                                                    Function<T, Map<String, JsonElement>> dataGetter,
                                                    BiFunction<String, Map<String, JsonElement>, T> factory,
                                                    String objectName) {
        return JSON_OBJECT_CODEC.comapFlatMap(map -> {
                    JsonElement discriminator = map.get(discriminatorField);
                    if (discriminator == null || !discriminator.isJsonPrimitive() || !discriminator.getAsJsonPrimitive().isString()) {
                        return DataResult.error(() -> "%s is missing string field '%s'".formatted(objectName, discriminatorField));
                    }
                    LinkedHashMap<String, JsonElement> data = new LinkedHashMap<>(map);
                    data.remove(discriminatorField);
                    try {
                        return DataResult.success(factory.apply(discriminator.getAsString(), data));
                    } catch (Exception e) {
                        return DataResult.error(() -> "Failed to decode %s: %s".formatted(objectName, e.getMessage()));
                    }
                },
                value -> {
                    LinkedHashMap<String, JsonElement> encoded = new LinkedHashMap<>(dataGetter.apply(value));
                    encoded.put(discriminatorField, new JsonPrimitive(discriminatorGetter.apply(value)));
                    return encoded;
                });
    }

    public static final Codec<AbilityValueKind> ABILITY_VALUE_KIND_CODEC = enumCodec(AbilityValueKind.class);
    public static final Codec<ActivationKind> ACTIVATION_KIND_CODEC = enumCodec(ActivationKind.class);
    public static final Codec<InterruptRefundPolicy> INTERRUPT_REFUND_POLICY_CODEC = enumCodec(InterruptRefundPolicy.class);
    public static final Codec<CostKind> COST_KIND_CODEC = enumCodec(CostKind.class);
    public static final Codec<StateScope> STATE_SCOPE_CODEC = enumCodec(StateScope.class);
    public static final Codec<DeliveryKind> DELIVERY_KIND_CODEC = enumCodec(DeliveryKind.class);
    public static final Codec<StatCapturePolicy> STAT_CAPTURE_POLICY_CODEC = enumCodec(StatCapturePolicy.class);
    public static final Codec<AbilityEventType> ABILITY_EVENT_TYPE_CODEC = enumCodec(AbilityEventType.class);
    public static final Codec<AbilityEventFilter.EventParticipant> EVENT_PARTICIPANT_CODEC =
            enumCodec(AbilityEventFilter.EventParticipant.class);
    public static final Codec<AbilityEventFilter.ParticipantRelation> PARTICIPANT_RELATION_CODEC =
            enumCodec(AbilityEventFilter.ParticipantRelation.class);
    public static final Codec<AbilityEventFilter.ComparisonOp> COMPARISON_OP_CODEC =
            enumCodec(AbilityEventFilter.ComparisonOp.class);

    public static final Codec<AbilityTargetResolverDefinition> ABILITY_TARGET_RESOLVER_CODEC =
            typedJsonObjectCodec("type", AbilityTargetResolverDefinition::type, AbilityTargetResolverDefinition::data,
                    AbilityTargetResolverDefinition::new, "ability target resolver");
    public static final Codec<AbilityConditionDefinition> ABILITY_CONDITION_CODEC =
            typedJsonObjectCodec("type", AbilityConditionDefinition::type, AbilityConditionDefinition::data,
                    AbilityConditionDefinition::new, "ability condition");

    public static final Codec<AbilityValue> ABILITY_VALUE_CODEC = JSON_OBJECT_CODEC.comapFlatMap(
            AbilityCodecs::decodeAbilityValue,
            AbilityCodecs::encodeAbilityValue
    );

    public static final Codec<AbilityScalar> ABILITY_SCALAR_CODEC = JSON_OBJECT_CODEC.comapFlatMap(
            AbilityCodecs::decodeAbilityScalar,
            AbilityCodecs::encodeAbilityScalar
    );

    public static final Codec<ActivationBehavior> ACTIVATION_BEHAVIOR_CODEC = JSON_OBJECT_CODEC.comapFlatMap(
            AbilityCodecs::decodeActivationBehavior,
            AbilityCodecs::encodeActivationBehavior
    );

    public static final Codec<InterruptPolicy> INTERRUPT_POLICY_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.BOOL.optionalFieldOf("on_damage", false).forGetter(InterruptPolicy::onDamage),
            Codec.FLOAT.optionalFieldOf("min_damage", 0.0f).forGetter(InterruptPolicy::minDamage),
            Codec.BOOL.optionalFieldOf("on_move", false).forGetter(InterruptPolicy::onMove),
            Codec.DOUBLE.optionalFieldOf("move_threshold_blocks", 0.1).forGetter(InterruptPolicy::moveThresholdBlocks),
            Codec.BOOL.optionalFieldOf("on_death", false).forGetter(InterruptPolicy::onDeath)
    ).apply(builder, InterruptPolicy::new));

    public static final Codec<AbilityPresentation> ABILITY_PRESENTATION_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.STRING.fieldOf("name").forGetter(AbilityPresentation::name),
            Codec.STRING.fieldOf("description").forGetter(AbilityPresentation::description),
            ResourceLocation.CODEC.optionalFieldOf("icon").forGetter(value -> Optional.ofNullable(value.icon())),
            ResourceLocation.CODEC.optionalFieldOf("casting_particles").forGetter(value -> Optional.ofNullable(value.castingParticles())),
            ResourceLocation.CODEC.optionalFieldOf("complete_particles").forGetter(value -> Optional.ofNullable(value.completeParticles())),
            ResourceLocation.CODEC.optionalFieldOf("casting_sound").forGetter(value -> Optional.ofNullable(value.castingSound())),
            ResourceLocation.CODEC.optionalFieldOf("complete_sound").forGetter(value -> Optional.ofNullable(value.completeSound()))
    ).apply(builder, (name, description, icon, castingParticles, completeParticles, castingSound, completeSound) ->
            new AbilityPresentation(name, description, icon.orElse(null), castingParticles.orElse(null),
                    completeParticles.orElse(null), castingSound.orElse(null), completeSound.orElse(null))));

    public static final Codec<AbilityParameterDefinition> ABILITY_PARAMETER_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.STRING.fieldOf("id").forGetter(AbilityParameterDefinition::id),
            ABILITY_VALUE_CODEC.fieldOf("default_value").forGetter(AbilityParameterDefinition::defaultValue),
            ABILITY_VALUE_KIND_CODEC.fieldOf("kind").forGetter(AbilityParameterDefinition::kind),
            Codec.BOOL.optionalFieldOf("patchable", false).forGetter(AbilityParameterDefinition::patchable),
            Codec.BOOL.optionalFieldOf("grant_overrideable", false).forGetter(AbilityParameterDefinition::grantOverrideable),
            Codec.STRING.optionalFieldOf("description").forGetter(value -> Optional.ofNullable(value.description()))
    ).apply(builder, (id, defaultValue, kind, patchable, grantOverrideable, description) ->
            new AbilityParameterDefinition(id, defaultValue, kind, patchable, grantOverrideable, description.orElse(null))));

    public static final Codec<AbilityCostDefinition> ABILITY_COST_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            COST_KIND_CODEC.fieldOf("kind").forGetter(AbilityCostDefinition::kind),
            ResourceLocation.CODEC.optionalFieldOf("resource_id").forGetter(value -> Optional.ofNullable(value.resourceId())),
            ABILITY_SCALAR_CODEC.fieldOf("amount").forGetter(AbilityCostDefinition::amount)
    ).apply(builder, (kind, resourceId, amount) -> new AbilityCostDefinition(kind, resourceId.orElse(null), amount)));

    public static final Codec<AbilityCooldownDefinition> ABILITY_COOLDOWN_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            STATE_SCOPE_CODEC.fieldOf("scope").forGetter(AbilityCooldownDefinition::scope),
            Codec.STRING.fieldOf("key").forGetter(AbilityCooldownDefinition::key),
            ABILITY_SCALAR_CODEC.fieldOf("duration").forGetter(AbilityCooldownDefinition::duration)
    ).apply(builder, AbilityCooldownDefinition::new));

    public static final Codec<AbilityActivationDefinition> ABILITY_ACTIVATION_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ACTIVATION_KIND_CODEC.fieldOf("kind").forGetter(AbilityActivationDefinition::kind),
            Codec.STRING.fieldOf("entry_point").forGetter(AbilityActivationDefinition::entryPoint),
            ABILITY_TARGET_RESOLVER_CODEC.fieldOf("targeting").forGetter(AbilityActivationDefinition::targeting),
            ABILITY_COST_CODEC.listOf().optionalFieldOf("costs", List.of()).forGetter(AbilityActivationDefinition::costs),
            ABILITY_COOLDOWN_CODEC.listOf().optionalFieldOf("cooldowns", List.of()).forGetter(AbilityActivationDefinition::cooldowns),
            ResourceLocation.CODEC.optionalFieldOf("gcd_group").forGetter(value -> Optional.ofNullable(value.gcdGroup())),
            Codec.INT.optionalFieldOf("cast_ticks", 0).forGetter(AbilityActivationDefinition::castTicks),
            Codec.BOOL.optionalFieldOf("affected_by_cast_speed", false).forGetter(AbilityActivationDefinition::affectedByCastSpeed),
            INTERRUPT_POLICY_CODEC.fieldOf("interrupt_policy").forGetter(AbilityActivationDefinition::interruptPolicy),
            INTERRUPT_REFUND_POLICY_CODEC.optionalFieldOf("refund_policy", InterruptRefundPolicy.NONE).forGetter(AbilityActivationDefinition::refundPolicy),
            ACTIVATION_BEHAVIOR_CODEC.fieldOf("behavior").forGetter(AbilityActivationDefinition::behavior)
    ).apply(builder, (kind, entryPoint, targeting, costs, cooldowns, gcdGroup, castTicks, affectedByCastSpeed,
                      interruptPolicy, refundPolicy, behavior) ->
            new AbilityActivationDefinition(kind, entryPoint, targeting, costs, cooldowns, gcdGroup.orElse(null), castTicks,
                    affectedByCastSpeed, interruptPolicy, refundPolicy, behavior)));

    private static final MapCodec<AbilityEventFilter.EventParticipantFilter> EVENT_PARTICIPANT_FILTER_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            EVENT_PARTICIPANT_CODEC.fieldOf("participant").forGetter(AbilityEventFilter.EventParticipantFilter::participant),
            PARTICIPANT_RELATION_CODEC.fieldOf("relation").forGetter(AbilityEventFilter.EventParticipantFilter::relation)
    ).apply(builder, AbilityEventFilter.EventParticipantFilter::new));

    private static final MapCodec<AbilityEventFilter.EventSourceTagFilter> EVENT_SOURCE_TAG_FILTER_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("tag").forGetter(AbilityEventFilter.EventSourceTagFilter::tag)
    ).apply(builder, AbilityEventFilter.EventSourceTagFilter::new));

    private static final MapCodec<AbilityEventFilter.EventPayloadComparisonFilter> EVENT_PAYLOAD_COMPARISON_FILTER_CODEC =
            RecordCodecBuilder.mapCodec(builder -> builder.group(
                    Codec.STRING.fieldOf("key").forGetter(AbilityEventFilter.EventPayloadComparisonFilter::key),
                    COMPARISON_OP_CODEC.fieldOf("op").forGetter(AbilityEventFilter.EventPayloadComparisonFilter::op),
                    ABILITY_VALUE_CODEC.fieldOf("value").forGetter(AbilityEventFilter.EventPayloadComparisonFilter::value)
            ).apply(builder, AbilityEventFilter.EventPayloadComparisonFilter::new));

    private static final MapCodec<AbilityEventFilter.EventPayloadTagFilter> EVENT_PAYLOAD_TAG_FILTER_CODEC =
            RecordCodecBuilder.mapCodec(builder -> builder.group(
                    Codec.STRING.fieldOf("key").forGetter(AbilityEventFilter.EventPayloadTagFilter::key),
                    ResourceLocation.CODEC.fieldOf("tag").forGetter(AbilityEventFilter.EventPayloadTagFilter::tag)
            ).apply(builder, AbilityEventFilter.EventPayloadTagFilter::new));

    public static final Codec<AbilityEventFilter> ABILITY_EVENT_FILTER_CODEC = Codec.STRING.dispatch(
            AbilityCodecs::eventFilterType,
            type -> switch (type) {
                case "event_participant" -> EVENT_PARTICIPANT_FILTER_CODEC;
                case "event_source_tag" -> EVENT_SOURCE_TAG_FILTER_CODEC;
                case "event_payload_comparison" -> EVENT_PAYLOAD_COMPARISON_FILTER_CODEC;
                case "event_payload_tag" -> EVENT_PAYLOAD_TAG_FILTER_CODEC;
                default -> throw new IllegalStateException("Unknown ability event filter type " + type);
            });

    public static final Codec<AbilityReactionDefinition> ABILITY_REACTION_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ABILITY_EVENT_TYPE_CODEC.fieldOf("event_type").forGetter(AbilityReactionDefinition::eventType),
            ABILITY_EVENT_FILTER_CODEC.listOf().optionalFieldOf("filters", List.of()).forGetter(AbilityReactionDefinition::filters),
            Codec.FLOAT.optionalFieldOf("chance", 1.0f).forGetter(AbilityReactionDefinition::chance),
            Codec.INT.optionalFieldOf("internal_cooldown_ticks", 0).forGetter(AbilityReactionDefinition::internalCooldownTicks),
            Codec.BOOL.optionalFieldOf("once_per_root", false).forGetter(AbilityReactionDefinition::oncePerRoot),
            Codec.INT.optionalFieldOf("max_chain_depth", Integer.MAX_VALUE).forGetter(AbilityReactionDefinition::maxChainDepth),
            Codec.STRING.fieldOf("activation_id").forGetter(AbilityReactionDefinition::activationId)
    ).apply(builder, AbilityReactionDefinition::new));

    public static final Codec<AbilityDeliveryDefinition> ABILITY_DELIVERY_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            DELIVERY_KIND_CODEC.fieldOf("kind").forGetter(AbilityDeliveryDefinition::kind),
            ResourceLocation.CODEC.optionalFieldOf("entity_type").forGetter(value -> Optional.ofNullable(value.entityType())),
            ResourceLocation.CODEC.optionalFieldOf("render_item").forGetter(value -> Optional.ofNullable(value.renderItem())),
            AbilityActionCodecs.ACTION_CODEC.listOf().optionalFieldOf("on_spawn", List.of()).forGetter(AbilityDeliveryDefinition::onSpawn),
            ABILITY_SCALAR_CODEC.optionalFieldOf("delay_ticks").forGetter(value -> Optional.ofNullable(value.delayTicks())),
            ABILITY_SCALAR_CODEC.optionalFieldOf("duration_ticks").forGetter(value -> Optional.ofNullable(value.durationTicks())),
            ABILITY_SCALAR_CODEC.optionalFieldOf("tick_interval_ticks").forGetter(value -> Optional.ofNullable(value.tickIntervalTicks())),
            ABILITY_SCALAR_CODEC.optionalFieldOf("radius").forGetter(value -> Optional.ofNullable(value.radius())),
            Codec.STRING.optionalFieldOf("on_impact_activation_id").forGetter(value -> Optional.ofNullable(value.onImpactActivationId())),
            Codec.STRING.optionalFieldOf("on_air_tick_activation_id").forGetter(value -> Optional.ofNullable(value.onAirTickActivationId())),
            Codec.STRING.optionalFieldOf("on_ground_tick_activation_id").forGetter(value -> Optional.ofNullable(value.onGroundTickActivationId()))
    ).apply(builder, (kind, entityType, renderItem, onSpawn, delayTicks, durationTicks, tickIntervalTicks, radius,
                      onImpactActivationId, onAirTickActivationId, onGroundTickActivationId) ->
            new AbilityDeliveryDefinition(kind, entityType.orElse(null), renderItem.orElse(null), onSpawn,
                    delayTicks.orElse(null), durationTicks.orElse(null), tickIntervalTicks.orElse(null),
                    radius.orElse(null),
                    onImpactActivationId.orElse(null), onAirTickActivationId.orElse(null),
                    onGroundTickActivationId.orElse(null))));

    private static final MapCodec<AbilityPatchOperation.SetParameterPatchOperation> SET_PARAMETER_PATCH_CODEC =
            RecordCodecBuilder.mapCodec(builder -> builder.group(
                    Codec.STRING.fieldOf("parameter_id").forGetter(AbilityPatchOperation.SetParameterPatchOperation::parameterId),
                    ABILITY_VALUE_CODEC.fieldOf("value").forGetter(AbilityPatchOperation.SetParameterPatchOperation::value)
            ).apply(builder, AbilityPatchOperation.SetParameterPatchOperation::new));

    private static final MapCodec<AbilityPatchOperation.ScaleParameterPatchOperation> SCALE_PARAMETER_PATCH_CODEC =
            RecordCodecBuilder.mapCodec(builder -> builder.group(
                    Codec.STRING.fieldOf("parameter_id").forGetter(AbilityPatchOperation.ScaleParameterPatchOperation::parameterId),
                    Codec.DOUBLE.fieldOf("scale").forGetter(AbilityPatchOperation.ScaleParameterPatchOperation::scale)
            ).apply(builder, AbilityPatchOperation.ScaleParameterPatchOperation::new));

    public static final Codec<AbilityPatchOperation> ABILITY_PATCH_OPERATION_CODEC = Codec.STRING.dispatch(
            AbilityCodecs::patchOperationType,
            type -> switch (type) {
                case "set_parameter" -> SET_PARAMETER_PATCH_CODEC;
                case "scale_parameter" -> SCALE_PARAMETER_PATCH_CODEC;
                default -> throw new IllegalStateException("Unknown ability patch operation type " + type);
            });

    public static final Codec<AbilityDefinitionPatch> ABILITY_DEFINITION_PATCH_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("patch_id").forGetter(AbilityDefinitionPatch::patchId),
            ResourceLocation.CODEC.fieldOf("ability_id").forGetter(AbilityDefinitionPatch::abilityId),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(AbilityDefinitionPatch::priority),
            Codec.INT.optionalFieldOf("load_order", 0).forGetter(AbilityDefinitionPatch::loadOrder),
            ABILITY_PATCH_OPERATION_CODEC.listOf().fieldOf("operations").forGetter(AbilityDefinitionPatch::operations)
    ).apply(builder, AbilityDefinitionPatch::new));

    public static final Codec<AbilityDefinitionData> ABILITY_DEFINITION_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(AbilityDefinitionData::id),
            ABILITY_PRESENTATION_CODEC.fieldOf("presentation").forGetter(AbilityDefinitionData::presentation),
            ResourceLocation.CODEC.fieldOf("slot_family").forGetter(AbilityDefinitionData::slotFamily),
            CommonCodecs.sortedSet(ResourceLocation.CODEC, Comparator.comparing(ResourceLocation::toString))
                    .optionalFieldOf("schools", Set.of()).forGetter(AbilityDefinitionData::schools),
            CommonCodecs.sortedSet(ResourceLocation.CODEC, Comparator.comparing(ResourceLocation::toString))
                    .optionalFieldOf("tags", Set.of()).forGetter(AbilityDefinitionData::tags),
            Codec.unboundedMap(Codec.STRING, ABILITY_PARAMETER_CODEC).optionalFieldOf("parameters", Map.of()).forGetter(AbilityDefinitionData::parameters),
            Codec.unboundedMap(Codec.STRING, ABILITY_ACTIVATION_CODEC).fieldOf("activations").forGetter(AbilityDefinitionData::activations),
            Codec.unboundedMap(Codec.STRING, AbilityActionCodecs.ACTION_CODEC.listOf()).fieldOf("entry_points").forGetter(AbilityDefinitionData::entryPoints),
            Codec.unboundedMap(Codec.STRING, ABILITY_REACTION_CODEC).optionalFieldOf("reactions", Map.of()).forGetter(AbilityDefinitionData::reactions),
            Codec.unboundedMap(Codec.STRING, ABILITY_DELIVERY_CODEC).optionalFieldOf("deliveries", Map.of()).forGetter(AbilityDefinitionData::deliveries)
    ).apply(builder, AbilityDefinitionData::new));

    private static String serializedName(Enum<?> value) {
        return value.name().toLowerCase(Locale.ROOT);
    }

    private static String eventFilterType(AbilityEventFilter filter) {
        return switch (filter) {
            case AbilityEventFilter.EventParticipantFilter ignored -> "event_participant";
            case AbilityEventFilter.EventSourceTagFilter ignored -> "event_source_tag";
            case AbilityEventFilter.EventPayloadComparisonFilter ignored -> "event_payload_comparison";
            case AbilityEventFilter.EventPayloadTagFilter ignored -> "event_payload_tag";
        };
    }

    private static String patchOperationType(AbilityPatchOperation operation) {
        return switch (operation) {
            case AbilityPatchOperation.SetParameterPatchOperation ignored -> "set_parameter";
            case AbilityPatchOperation.ScaleParameterPatchOperation ignored -> "scale_parameter";
        };
    }

    private static DataResult<AbilityValue> decodeAbilityValue(Map<String, JsonElement> data) {
        return getRequiredString(data, "kind", "ability value").flatMap(kind -> switch (kind) {
            case "float" -> parseField(data, "value", Codec.FLOAT).map(AbilityValue.FloatValue::new);
            case "int" -> parseField(data, "value", Codec.INT).map(AbilityValue.IntValue::new);
            case "bool" -> parseField(data, "value", Codec.BOOL).map(AbilityValue.BoolValue::new);
            case "string" -> parseField(data, "value", Codec.STRING).map(AbilityValue.StringValue::new);
            case "entity_ref" -> parseField(data, "value", UUIDUtil.STRING_CODEC).map(AbilityValue.EntityRefValue::new);
            case "resource_location" -> parseField(data, "value", ResourceLocation.CODEC).map(AbilityValue.ResourceLocationValue::new);
            default -> DataResult.error(() -> "Unknown ability value kind '" + kind + "'");
        });
    }

    private static Map<String, JsonElement> encodeAbilityValue(AbilityValue value) {
        LinkedHashMap<String, JsonElement> data = new LinkedHashMap<>();
        data.put("kind", new JsonPrimitive(serializedName(value.kind())));
        switch (value) {
            case AbilityValue.FloatValue floatValue ->
                    data.put("value", encodeField(floatValue.value(), Codec.FLOAT, "ability float value"));
            case AbilityValue.IntValue intValue ->
                    data.put("value", encodeField(intValue.value(), Codec.INT, "ability int value"));
            case AbilityValue.BoolValue boolValue ->
                    data.put("value", encodeField(boolValue.value(), Codec.BOOL, "ability bool value"));
            case AbilityValue.StringValue stringValue ->
                    data.put("value", encodeField(stringValue.value(), Codec.STRING, "ability string value"));
            case AbilityValue.EntityRefValue entityRefValue ->
                    data.put("value", encodeField(entityRefValue.value(), UUIDUtil.STRING_CODEC, "ability entity_ref value"));
            case AbilityValue.ResourceLocationValue resourceLocationValue ->
                    data.put("value", encodeField(resourceLocationValue.value(), ResourceLocation.CODEC, "ability resource_location value"));
        }
        return data;
    }

    private static DataResult<AbilityScalar> decodeAbilityScalar(Map<String, JsonElement> data) {
        return getRequiredString(data, "kind", "ability scalar").flatMap(kind -> switch (kind) {
            case "constant" -> parseField(data, "value", Codec.DOUBLE).map(AbilityScalar.ConstantScalar::new);
            case "parameter" -> parseField(data, "parameter", Codec.STRING).map(AbilityScalar.ParameterScalar::new);
            case "attribute_scaled" -> parseField(data, "base", ABILITY_SCALAR_CODEC).flatMap(base ->
                    parseField(data, "scale", ABILITY_SCALAR_CODEC).flatMap(scale ->
                            parseField(data, "attribute", ResourceLocation.CODEC).flatMap(attribute ->
                                    parseField(data, "capture_policy", STAT_CAPTURE_POLICY_CODEC)
                                            .map(policy -> new AbilityScalar.AttributeScaledScalar(base, scale, attribute, policy)))));
            default -> DataResult.error(() -> "Unknown ability scalar kind '" + kind + "'");
        });
    }

    private static Map<String, JsonElement> encodeAbilityScalar(AbilityScalar scalar) {
        LinkedHashMap<String, JsonElement> data = new LinkedHashMap<>();
        switch (scalar) {
            case AbilityScalar.ConstantScalar constant -> {
                data.put("kind", new JsonPrimitive("constant"));
                data.put("value", encodeField(constant.value(), Codec.DOUBLE, "ability constant scalar"));
            }
            case AbilityScalar.ParameterScalar parameter -> {
                data.put("kind", new JsonPrimitive("parameter"));
                data.put("parameter", encodeField(parameter.parameter(), Codec.STRING, "ability parameter scalar"));
            }
            case AbilityScalar.AttributeScaledScalar attributeScaled -> {
                data.put("kind", new JsonPrimitive("attribute_scaled"));
                data.put("base", encodeField(attributeScaled.base(), ABILITY_SCALAR_CODEC, "ability scalar base"));
                data.put("scale", encodeField(attributeScaled.scale(), ABILITY_SCALAR_CODEC, "ability scalar scale"));
                data.put("attribute", encodeField(attributeScaled.attribute(), ResourceLocation.CODEC, "ability scalar attribute"));
                data.put("capture_policy", encodeField(attributeScaled.capturePolicy(), STAT_CAPTURE_POLICY_CODEC, "ability scalar capture policy"));
            }
        }
        return data;
    }

    private static DataResult<ActivationBehavior> decodeActivationBehavior(Map<String, JsonElement> data) {
        return getRequiredString(data, "kind", "activation behavior").flatMap(kind -> switch (kind) {
            case "instant" -> DataResult.success(new ActivationBehavior.InstantBehavior());
            case "channel" -> parseField(data, "tick_interval_ticks", Codec.INT).flatMap(interval ->
                    parseField(data, "tick_entry_point", Codec.STRING).flatMap(entryPoint ->
                            parseOptionalField(data, "preserve_initial_targets", Codec.BOOL, false).flatMap(preserve ->
                                    parseOptionalField(data, "tick_targeting", ABILITY_TARGET_RESOLVER_CODEC, null)
                                            .map(tickTargeting -> new ActivationBehavior.ChannelBehavior(interval, entryPoint, preserve, tickTargeting)))));
            case "aura" -> parseField(data, "pulse_interval_ticks", Codec.INT).flatMap(interval ->
                    parseField(data, "pulse_entry_point", Codec.STRING).flatMap(entryPoint ->
                            parseField(data, "pulse_targeting", ABILITY_TARGET_RESOLVER_CODEC).flatMap(targeting ->
                                    parseOptionalField(data, "pulse_on_enable", Codec.BOOL, false)
                                            .map(pulseOnEnable -> new ActivationBehavior.AuraBehavior(interval, entryPoint, targeting, pulseOnEnable)))));
            default -> DataResult.error(() -> "Unknown activation behavior kind '" + kind + "'");
        });
    }

    private static Map<String, JsonElement> encodeActivationBehavior(ActivationBehavior behavior) {
        LinkedHashMap<String, JsonElement> data = new LinkedHashMap<>();
        switch (behavior) {
            case ActivationBehavior.InstantBehavior ignored -> data.put("kind", new JsonPrimitive("instant"));
            case ActivationBehavior.ChannelBehavior channel -> {
                data.put("kind", new JsonPrimitive("channel"));
                data.put("tick_interval_ticks", encodeField(channel.tickIntervalTicks(), Codec.INT, "channel tick interval"));
                data.put("tick_entry_point", encodeField(channel.tickEntryPoint(), Codec.STRING, "channel tick entry point"));
                data.put("preserve_initial_targets", encodeField(channel.preserveInitialTargets(), Codec.BOOL, "channel preserve initial targets"));
                if (channel.tickTargeting() != null) {
                    data.put("tick_targeting", encodeField(channel.tickTargeting(), ABILITY_TARGET_RESOLVER_CODEC, "channel tick targeting"));
                }
            }
            case ActivationBehavior.AuraBehavior aura -> {
                data.put("kind", new JsonPrimitive("aura"));
                data.put("pulse_interval_ticks", encodeField(aura.pulseIntervalTicks(), Codec.INT, "aura pulse interval"));
                data.put("pulse_entry_point", encodeField(aura.pulseEntryPoint(), Codec.STRING, "aura pulse entry point"));
                data.put("pulse_targeting", encodeField(aura.pulseTargeting(), ABILITY_TARGET_RESOLVER_CODEC, "aura pulse targeting"));
                data.put("pulse_on_enable", encodeField(aura.pulseOnEnable(), Codec.BOOL, "aura pulse on enable"));
            }
        }
        return data;
    }

    private static DataResult<String> getRequiredString(Map<String, JsonElement> data, String fieldName, String context) {
        JsonElement element = data.get(fieldName);
        if (element == null) {
            return DataResult.error(() -> "%s is missing required field '%s'".formatted(context, fieldName));
        }
        return Codec.STRING.parse(JsonOps.INSTANCE, element)
                .mapError(error -> "%s field '%s': %s".formatted(context, fieldName, error));
    }

    private static <T> DataResult<T> parseField(Map<String, JsonElement> data, String fieldName, Codec<T> codec) {
        JsonElement element = data.get(fieldName);
        if (element == null) {
            return DataResult.error(() -> "Missing required field '" + fieldName + "'");
        }
        return codec.parse(JsonOps.INSTANCE, element)
                .mapError(error -> "Field '%s': %s".formatted(fieldName, error));
    }

    private static <T> DataResult<T> parseOptionalField(Map<String, JsonElement> data,
                                                        String fieldName,
                                                        Codec<T> codec,
                                                        @Nullable T defaultValue) {
        JsonElement element = data.get(fieldName);
        if (element == null) {
            return DataResult.success(defaultValue);
        }
        return codec.parse(JsonOps.INSTANCE, element)
                .mapError(error -> "Field '%s': %s".formatted(fieldName, error));
    }

    private static <T> JsonElement encodeField(T value, Codec<T> codec, String fieldName) {
        return codec.encodeStart(JsonOps.INSTANCE, value)
                .getOrThrow(error -> new IllegalStateException("Failed to encode %s: %s".formatted(fieldName, error)));
    }
}
