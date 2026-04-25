package com.chaosbuffalo.mkcore.abilities2.description;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities2.AbilityTargeting;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityActivationDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityCooldownDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityCostDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityParameterDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityScalar;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityTargetRelation;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityTargetResolverDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityValue;
import com.chaosbuffalo.mkcore.abilities2.definition.ActivationBehavior;
import com.chaosbuffalo.mkcore.abilities2.definition.ActivationKind;
import com.chaosbuffalo.mkcore.abilities2.definition.CostKind;
import com.chaosbuffalo.mkcore.abilities2.definition.StateScope;
import com.chaosbuffalo.mkcore.abilities2.runtime.PatchedAbilityDefinition;
import com.chaosbuffalo.mkcore.core.player.PlayerKnownAbility;
import com.chaosbuffalo.mkcore.utils.text.IconTextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class AbilityDefinitionDescriptions {
    private AbilityDefinitionDescriptions() {
    }

    public static List<Component> collectDescription(@Nullable PlayerKnownAbility knownAbility,
                                                     PatchedAbilityDefinition definition) {
        ArrayList<Component> lines = new ArrayList<>();
        buildDescription(knownAbility, definition, lines::add);
        return List.copyOf(lines);
    }

    public static void buildDescription(@Nullable PlayerKnownAbility knownAbility,
                                        PatchedAbilityDefinition definition,
                                        Consumer<Component> consumer) {
        if (knownAbility != null && knownAbility.usesAbilityPool()) {
            consumer.accept(new IconTextComponent(MKAbility.POOL_SLOT_ICON, "mkcore.ability.description.uses_pool")
                    .withStyle(ChatFormatting.ITALIC));
        }

        String description = definition.definition().data().presentation().description();
        if (!description.isBlank()) {
            consumer.accept(Component.literal(description).withStyle(ChatFormatting.GRAY));
        }

        describePlayerFacingActivations(definition, consumer);
        describeParameters(definition, consumer);
        describeMetadata(definition, consumer);
    }

    private static void describePlayerFacingActivations(PatchedAbilityDefinition definition,
                                                        Consumer<Component> consumer) {
        definition.definition().data().activations().entrySet().stream()
                .filter(entry -> isPlayerFacing(entry.getValue().kind()))
                .sorted(Comparator
                        .comparingInt((Map.Entry<String, AbilityActivationDefinition> entry) ->
                                activationOrder(entry.getValue().kind()))
                        .thenComparing(Map.Entry::getKey))
                .forEach(entry -> {
                    AbilityActivationDefinition activation = entry.getValue();
                    consumer.accept(describeActivation(activation, definition));
                    Component targeting = describeActivationTargeting(activation);
                    if (targeting != null) {
                        consumer.accept(targeting);
                    }
                });
    }

    private static Component describeActivation(AbilityActivationDefinition activation,
                                                PatchedAbilityDefinition definition) {
        List<String> parts = new ArrayList<>();

        activation.costs().stream()
                .map(cost -> describeCost(cost, definition))
                .forEach(parts::add);
        activation.cooldowns().stream()
                .map(cooldown -> describeCooldown(cooldown, definition))
                .forEach(parts::add);

        if (activation.castTicks() > 0) {
            String castText = formatTicks(activation.castTicks()) + " cast";
            if (activation.affectedByCastSpeed()) {
                castText += " (cast speed)";
            }
            parts.add(castText);
        } else if (activation.kind() != ActivationKind.PASSIVE_SETUP) {
            parts.add("instant");
        }

        if (activation.gcdGroup() != null) {
            parts.add("shared gcd");
        }

        switch (activation.behavior()) {
            case ActivationBehavior.InstantBehavior ignored -> {
            }
            case ActivationBehavior.ChannelBehavior channel ->
                    parts.add("channels every " + formatTicks(channel.tickIntervalTicks())
                            + (channel.preserveInitialTargets() ? "" : " with retargeting"));
            case ActivationBehavior.AuraBehavior aura ->
                    parts.add("aura pulses every " + formatTicks(aura.pulseIntervalTicks())
                            + (aura.pulseOnEnable() ? ", including immediately" : ""));
        }

        if (activation.kind() == ActivationKind.PASSIVE_SETUP && parts.isEmpty()) {
            parts.add("active while slotted");
        }

        return Component.literal(displayActivationKind(activation.kind()) + ": " + String.join(", ", parts))
                .withStyle(ChatFormatting.DARK_GRAY);
    }

    private static @Nullable Component describeActivationTargeting(AbilityActivationDefinition activation) {
        String targetSummary = describeTargeting(activation.targeting());
        if (targetSummary == null) {
            return null;
        }

        return Component.literal(displayActivationKind(activation.kind()) + " Target: " + targetSummary)
                .withStyle(ChatFormatting.DARK_GRAY);
    }

    private static void describeParameters(PatchedAbilityDefinition definition,
                                           Consumer<Component> consumer) {
        for (Map.Entry<String, AbilityParameterDefinition> entry : definition.definition().data().parameters().entrySet()) {
            AbilityParameterDefinition parameter = entry.getValue();
            AbilityValue value = definition.getPatchedParameter(entry.getKey());
            String label = parameter.description() != null && !parameter.description().isBlank()
                    ? parameter.description()
                    : parameter.id();
            consumer.accept(Component.literal(label + ": " + formatValue(value))
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    private static void describeMetadata(PatchedAbilityDefinition definition,
                                         Consumer<Component> consumer) {
        if (!definition.definition().data().schools().isEmpty()) {
            consumer.accept(Component.literal("Schools: " + formatIds(definition.definition().data().schools()))
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        if (!definition.definition().data().tags().isEmpty()) {
            consumer.accept(Component.literal("Tags: " + formatIds(definition.definition().data().tags()))
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static boolean isPlayerFacing(ActivationKind kind) {
        return switch (kind) {
            case MANUAL, AI, PASSIVE_SETUP, TOGGLE_ENABLE, TOGGLE_DISABLE -> true;
            case PROC, PASSIVE_TEARDOWN -> false;
        };
    }

    private static int activationOrder(ActivationKind kind) {
        return switch (kind) {
            case MANUAL -> 0;
            case TOGGLE_ENABLE -> 1;
            case TOGGLE_DISABLE -> 2;
            case PASSIVE_SETUP -> 3;
            case AI -> 4;
            case PROC -> 5;
            case PASSIVE_TEARDOWN -> 6;
        };
    }

    private static String displayActivationKind(ActivationKind kind) {
        return switch (kind) {
            case MANUAL -> "Cast";
            case AI -> "AI";
            case PASSIVE_SETUP -> "Passive";
            case PASSIVE_TEARDOWN -> "Passive Off";
            case PROC -> "Proc";
            case TOGGLE_ENABLE -> "Toggle On";
            case TOGGLE_DISABLE -> "Toggle Off";
        };
    }

    private static @Nullable String describeTargeting(AbilityTargetResolverDefinition targeting) {
        return switch (targeting.type()) {
            case "self" -> "self";
            case "none" -> "none";
            case "resolved" -> describeResolvedTargeting(AbilityTargeting.relation(targeting));
            case "event_target" -> describeEventTargeting("event target", AbilityTargeting.relation(targeting));
            case "event_actor" -> describeEventTargeting("event actor", AbilityTargeting.relation(targeting));
            default -> null;
        };
    }

    private static String describeResolvedTargeting(AbilityTargetRelation relation) {
        return switch (relation) {
            case ALL -> "looked-at target";
            case FRIENDLY -> "looked-at ally";
            case ENEMY -> "looked-at enemy";
        };
    }

    private static String describeEventTargeting(String base, AbilityTargetRelation relation) {
        return switch (relation) {
            case ALL -> base;
            case FRIENDLY -> "friendly " + base;
            case ENEMY -> "enemy " + base;
        };
    }

    private static String describeCost(AbilityCostDefinition cost, PatchedAbilityDefinition definition) {
        String amount = formatScalar(cost.amount(), definition, false);
        return switch (cost.kind()) {
            case MANA -> amount + " mana";
            case HEALTH -> amount + " health";
            case CUSTOM_RESOURCE -> amount + " " + formatResource(cost.resourceId());
        };
    }

    private static String describeCooldown(AbilityCooldownDefinition cooldown, PatchedAbilityDefinition definition) {
        String duration = formatScalar(cooldown.duration(), definition, true);
        String scope = switch (cooldown.scope()) {
            case SELF -> "self";
            case LOADOUT_SLOT -> "slot";
            case SOURCE_ITEM -> "item";
            case SOURCE_EFFECT -> "effect";
            case ABILITY_INSTANCE -> "instance";
            case ABILITY_FAMILY -> "family";
        };
        return duration + " " + scope + " cooldown";
    }

    private static String formatScalar(AbilityScalar scalar,
                                       PatchedAbilityDefinition definition,
                                       boolean ticksToSeconds) {
        return switch (scalar) {
            case AbilityScalar.ConstantScalar constant -> ticksToSeconds
                    ? formatTicks(constant.value())
                    : formatNumber(constant.value());
            case AbilityScalar.ParameterScalar parameter -> formatParameterValue(
                    definition.getPatchedParameter(parameter.parameter()), ticksToSeconds);
            case AbilityScalar.AttributeScaledScalar attributeScaled -> formatScalar(attributeScaled.base(), definition, ticksToSeconds)
                    + " + " + formatScalar(attributeScaled.scale(), definition, ticksToSeconds)
                    + " x " + formatResource(attributeScaled.attribute());
        };
    }

    private static String formatParameterValue(AbilityValue value, boolean ticksToSeconds) {
        return switch (value) {
            case AbilityValue.FloatValue floatValue -> ticksToSeconds
                    ? formatTicks(floatValue.value())
                    : formatNumber(floatValue.value());
            case AbilityValue.IntValue intValue -> ticksToSeconds
                    ? formatTicks(intValue.value())
                    : Integer.toString(intValue.value());
            case AbilityValue.BoolValue boolValue -> boolValue.value() ? "true" : "false";
            case AbilityValue.StringValue stringValue -> stringValue.value();
            case AbilityValue.EntityRefValue entityRefValue -> entityRefValue.value().toString();
            case AbilityValue.ResourceLocationValue resourceLocationValue -> formatResource(resourceLocationValue.value());
        };
    }

    private static String formatValue(AbilityValue value) {
        return formatParameterValue(value, false);
    }

    private static String formatNumber(double value) {
        return MKAbility.NUMBER_FORMATTER.format(value);
    }

    private static String formatTicks(double ticks) {
        return formatNumber(ticks / GameConstants.TICKS_PER_SECOND) + "s";
    }

    private static String formatIds(Iterable<ResourceLocation> ids) {
        ArrayList<String> values = new ArrayList<>();
        ids.forEach(id -> values.add(id.toString()));
        return values.stream().collect(Collectors.joining(", "));
    }

    private static String formatResource(@Nullable ResourceLocation resourceId) {
        return resourceId != null ? resourceId.toString() : "unknown";
    }
}
