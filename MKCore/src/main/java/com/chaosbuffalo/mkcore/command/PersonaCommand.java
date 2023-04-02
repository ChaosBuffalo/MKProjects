package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.persona.PersonaManager;
import com.chaosbuffalo.mkcore.utils.ChatUtils;
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
import net.minecraft.server.level.ServerPlayer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class PersonaCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("persona")
                .then(Commands.literal("list")
                        .executes(PersonaCommand::listPersonas))
                .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(PersonaCommand::createPersona)))
                .then(Commands.literal("switch")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .suggests(PersonaCommand::suggestKnownPersonas)
                                .executes(PersonaCommand::switchPersona)))
                .then(Commands.literal("delete")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .suggests(PersonaCommand::suggestInactivePersonas)
                                .executes(PersonaCommand::deletePersona)))
                ;
    }

    static CompletableFuture<Suggestions> suggestKnownPersonas(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) throws CommandSyntaxException {
        return suggestPersonas(context, builder, false);
    }

    static CompletableFuture<Suggestions> suggestInactivePersonas(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) throws CommandSyntaxException {
        return suggestPersonas(context, builder, true);
    }

    static CompletableFuture<Suggestions> suggestPersonas(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder, boolean inactive) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        return SharedSuggestionProvider.suggest(MKCore.getPlayer(player).map(playerData -> {
            PersonaManager manager = playerData.getPersonaManager();
            Set<String> names = new HashSet<>(manager.getPersonaNames());
            if (inactive) {
                names.remove(manager.getActivePersona().getName());
            }
            return names;
        }).orElse(Collections.emptySet()), builder);
    }

    static int listPersonas(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        MKCore.getPlayer(player).ifPresent(playerData -> {
            PersonaManager personaManager = playerData.getPersonaManager();

            ChatUtils.sendMessageWithBrackets(player, "Personas");
            for (String name : personaManager.getPersonaNames()) {
                String pName = name;
                if (personaManager.isPersonaActive(name))
                    pName = pName + " (active)";
                ChatUtils.sendMessage(player, pName);
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    static int createPersona(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        MKCore.getPlayer(player).ifPresent(playerData -> {
            String name = StringArgumentType.getString(ctx, "name");

            PersonaManager personaManager = playerData.getPersonaManager();
            if (personaManager.hasPersona(name)) {
                ChatUtils.sendMessage(player, "Persona '%s' already exists!", name);
            } else if (personaManager.createPersona(name)) {
                ChatUtils.sendMessage(player, "Created persona '%s'", name);
            } else {
                ChatUtils.sendMessage(player, "Unable to create persona '%s'", name);
            }
        });

        return Command.SINGLE_SUCCESS;
    }


    static int switchPersona(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        MKCore.getPlayer(player).ifPresent(playerData -> {
            String name = StringArgumentType.getString(ctx, "name");

            PersonaManager personaManager = playerData.getPersonaManager();
            if (!personaManager.hasPersona(name)) {
                ChatUtils.sendMessage(player, "Persona '%s' does not exist!", name);
            } else if (personaManager.isPersonaActive(name)) {
                ChatUtils.sendMessage(player, "Persona '%s' already active", name);
            } else if (personaManager.activatePersona(name)) {
                ChatUtils.sendMessage(player, "Activated persona '%s'", name);
            } else {
                ChatUtils.sendMessage(player, "Unable to activate persona '%s'", name);
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    static int deletePersona(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        MKCore.getPlayer(player).ifPresent(playerData -> {
            String name = StringArgumentType.getString(ctx, "name");

            PersonaManager personaManager = playerData.getPersonaManager();
            if (personaManager.isPersonaActive(name)) {
                ChatUtils.sendMessage(player, "Unable to delete active persona '%s'", name);
            } else if (!personaManager.hasPersona(name)) {
                ChatUtils.sendMessage(player, "Persona '%s' does not exist!", name);
            } else if (personaManager.deletePersona(name)) {
                ChatUtils.sendMessage(player, "Deleted persona '%s'", name);
            } else {
                ChatUtils.sendMessage(player, "Failed to delete persona '%s'", name);
            }
        });

        return Command.SINGLE_SUCCESS;
    }
}
