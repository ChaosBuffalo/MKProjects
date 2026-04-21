package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities2.runtime.AbilityReference;
import com.chaosbuffalo.mkcore.abilities2.runtime.ActivationRequest;
import com.chaosbuffalo.mkcore.abilities2.runtime.InvocationResult;
import com.chaosbuffalo.mkcore.abilities2.runtime.PatchedAbilityDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityActivationDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.ActivationKind;
import com.chaosbuffalo.mkcore.command.arguments.PlayersArgument;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Ability2Command {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("ability2")
                .requires(MKCommand::isGM)
                .then(Commands.literal("list")
                        .executes(Ability2Command::listDefinitions))
                .then(Commands.argument("player", PlayersArgument.player())
                        .then(Commands.literal("activate")
                                .then(Commands.argument("ability", ResourceLocationArgument.id())
                                        .suggests(Ability2Command::suggestAllDefinitions)
                                        .executes(Ability2Command::activateDefault)
                                        .then(Commands.argument("activation", StringArgumentType.word())
                                                .suggests(Ability2Command::suggestExternalActivations)
                                                .executes(Ability2Command::activateExplicit))))
                        .then(Commands.literal("toggle")
                                .then(Commands.argument("ability", ResourceLocationArgument.id())
                                        .suggests(Ability2Command::suggestToggleDefinitions)
                                        .executes(Ability2Command::toggleAbility))));
    }

    private static CompletableFuture<Suggestions> suggestAllDefinitions(CommandContext<CommandSourceStack> context,
                                                                        SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(
                MKCore.getAbilityDefinitionService().getDefinitionIds().stream()
                        .sorted(Comparator.comparing(ResourceLocation::toString))
                        .map(ResourceLocation::toString),
                builder
        );
    }

    private static CompletableFuture<Suggestions> suggestExternalActivations(CommandContext<CommandSourceStack> context,
                                                                             SuggestionsBuilder builder) {
        ResourceLocation abilityId = ResourceLocationArgument.getId(context, "ability");
        PatchedAbilityDefinition definition = MKCore.getAbilityDefinitionService().getResolver().resolvePatched(abilityId);
        if (definition == null) {
            return Suggestions.empty();
        }
        List<String> activations = new ArrayList<>();
        for (Map.Entry<String, AbilityActivationDefinition> entry : definition.definition().activations().entrySet()) {
            if (isExternallyCallable(entry.getValue().kind())) {
                activations.add(entry.getKey());
            }
        }
        return SharedSuggestionProvider.suggest(activations, builder);
    }

    private static CompletableFuture<Suggestions> suggestToggleDefinitions(CommandContext<CommandSourceStack> context,
                                                                           SuggestionsBuilder builder) {
        List<String> abilityIds = new ArrayList<>();
        for (ResourceLocation abilityId : MKCore.getAbilityDefinitionService().getDefinitionIds()) {
            PatchedAbilityDefinition definition = MKCore.getAbilityDefinitionService().getResolver().resolvePatched(abilityId);
            if (definition == null) {
                continue;
            }
            if (hasActivationKind(definition, ActivationKind.TOGGLE_ENABLE)) {
                abilityIds.add(abilityId.toString());
            }
        }
        return SharedSuggestionProvider.suggest(abilityIds, builder);
    }

    private static int listDefinitions(CommandContext<CommandSourceStack> context) {
        List<ResourceLocation> definitions = MKCore.getAbilityDefinitionService().getDefinitionIds().stream()
                .sorted(Comparator.comparing(ResourceLocation::toString))
                .toList();
        if (definitions.isEmpty()) {
            sendMessage(context.getSource(), "No abilities2 definitions are loaded");
            return Command.SINGLE_SUCCESS;
        }
        sendBracketedMessage(context.getSource(), "Loaded abilities2 definitions: %d", definitions.size());
        definitions.forEach(id ->
                sendMessage(context.getSource(), Component.literal("- ").append(AbilityCommand.formatAbilityDisplay(id))));
        return Command.SINGLE_SUCCESS;
    }

    private static int activateDefault(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ResourceLocation abilityId = ResourceLocationArgument.getId(context, "ability");
        PatchedAbilityDefinition definition = MKCore.getAbilityDefinitionService().getResolver().resolvePatched(abilityId);
        if (definition == null) {
            sendMessage(context.getSource(),
                    Component.literal("Unknown abilities2 definition ").append(AbilityCommand.formatAbilityDisplay(abilityId)));
            return Command.SINGLE_SUCCESS;
        }

        String activationId;
        try {
            activationId = resolveSingleActivationId(definition, ActivationKind.MANUAL);
        } catch (IllegalStateException e) {
            sendMessage(context.getSource(),
                    Component.literal("Ability ")
                            .append(AbilityCommand.formatAbilityDisplay(abilityId))
                            .append(" has multiple manual activations; specify one explicitly"));
            return Command.SINGLE_SUCCESS;
        }
        if (activationId == null) {
            sendMessage(context.getSource(),
                    Component.literal("Ability ")
                            .append(AbilityCommand.formatAbilityDisplay(abilityId))
                            .append(" has no manual activation"));
            return Command.SINGLE_SUCCESS;
        }
        return activate(context, abilityId, activationId);
    }

    private static int activateExplicit(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return activate(
                context,
                ResourceLocationArgument.getId(context, "ability"),
                StringArgumentType.getString(context, "activation")
        );
    }

    private static int activate(CommandContext<CommandSourceStack> context,
                                ResourceLocation abilityId,
                                String activationId) throws CommandSyntaxException {
        ServerPlayer player = PlayersArgument.getPlayer(context, "player");
        MKPlayerData playerData = MKCore.getPlayerOrThrow(player);

        InvocationResult result = MKCore.getAbilityRuntimeService().getEngine().activate(new ActivationRequest(
                playerData,
                playerData,
                new AbilityReference(abilityId, null),
                activationId,
                null,
                null,
                null,
                false,
                false
        ));
        if (!result.started()) {
            sendMessage(context.getSource(),
                    Component.literal("abilities2 activation ")
                            .append(AbilityCommand.formatAbilityDisplay(abilityId))
                            .append(Component.literal(":" + activationId + " failed for "))
                            .append(player.getName())
                            .append(Component.literal(": " + result.failureReason())));
            return Command.SINGLE_SUCCESS;
        }

        sendMessage(context.getSource(),
                Component.literal("Started abilities2 activation ")
                        .append(AbilityCommand.formatAbilityDisplay(abilityId))
                        .append(Component.literal(":" + activationId + " for "))
                        .append(player.getName())
                        .append(Component.literal(" (" + result.invocationId() + ")")));
        return Command.SINGLE_SUCCESS;
    }

    private static int toggleAbility(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = PlayersArgument.getPlayer(context, "player");
        MKPlayerData playerData = MKCore.getPlayerOrThrow(player);
        ResourceLocation abilityId = ResourceLocationArgument.getId(context, "ability");

        InvocationResult result = MKCore.getAbilityRuntimeService().requestToggle(
                playerData,
                playerData,
                new AbilityReference(abilityId, null),
                null
        );
        if (!result.started()) {
            sendMessage(context.getSource(),
                    Component.literal("abilities2 toggle ")
                            .append(AbilityCommand.formatAbilityDisplay(abilityId))
                            .append(Component.literal(" failed for "))
                            .append(player.getName())
                            .append(Component.literal(": " + result.failureReason())));
            return Command.SINGLE_SUCCESS;
        }

        sendMessage(context.getSource(),
                Component.literal("Started abilities2 toggle transition ")
                        .append(AbilityCommand.formatAbilityDisplay(abilityId))
                        .append(Component.literal(" for "))
                        .append(player.getName())
                        .append(Component.literal(" (" + result.invocationId() + ")")));
        return Command.SINGLE_SUCCESS;
    }

    private static boolean hasActivationKind(PatchedAbilityDefinition definition, ActivationKind kind) {
        return resolveSingleActivationId(definition, kind) != null;
    }

    private static boolean isExternallyCallable(ActivationKind kind) {
        return kind == ActivationKind.MANUAL || kind == ActivationKind.AI;
    }

    private static @Nullable String resolveSingleActivationId(PatchedAbilityDefinition definition, ActivationKind kind) {
        String match = null;
        for (Map.Entry<String, AbilityActivationDefinition> entry : definition.definition().activations().entrySet()) {
            if (entry.getValue().kind() != kind) {
                continue;
            }
            if (match != null) {
                throw new IllegalStateException("multiple activations with kind " + kind);
            }
            match = entry.getKey();
        }
        return match;
    }

    private static void sendMessage(CommandSourceStack source, String format, Object... args) {
        source.sendSuccess(() -> Component.literal(String.format(format, args)), false);
    }

    private static void sendMessage(CommandSourceStack source, Component message) {
        source.sendSuccess(() -> message, false);
    }

    private static void sendBracketedMessage(CommandSourceStack source, String format, Object... args) {
        source.sendSuccess(() -> Component.literal("[ " + String.format(format, args) + " ]"), false);
    }
}
