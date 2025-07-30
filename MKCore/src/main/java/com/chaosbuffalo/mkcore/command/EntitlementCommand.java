package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.command.arguments.EntitlementIdArgument;
import com.chaosbuffalo.mkcore.command.arguments.PlayersArgument;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.entitlements.EntitlementInstance;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.chaosbuffalo.mkcore.utils.ChatUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class EntitlementCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("entitlement")
                .then(Commands.argument("player", PlayersArgument.player())
                        .then(Commands.literal("add")
                                .then(Commands.argument("entitlementId", EntitlementIdArgument.entitlementId())
                                        .executes(EntitlementCommand::addInstance))
                        )
                        .then(Commands.literal("remove")
                                .then(Commands.argument("entitlementId", EntitlementIdArgument.entitlementId())
                                        .suggests(EntitlementCommand::suggestKnownEntitlements)
                                        .then(Commands.argument("instanceId", UuidArgument.uuid())
                                                .suggests(EntitlementCommand::suggestKnownEntitlementInstances)
                                                .executes(EntitlementCommand::removeInstance)))
                        )
                        .then(Commands.literal("dump")
                                .executes(EntitlementCommand::dump)
                        )
                )
                ;
    }

    static int dump(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer commandPlayer = context.getSource().getPlayerOrException();
        ServerPlayer player = PlayersArgument.getPlayer(context, "player");
        MKPlayerData playerData = MKCore.getPlayerOrThrow(player);

        var grouped = playerData.getEntitlements().getInstanceStream()
                .collect(Collectors.groupingBy(EntitlementInstance::entitlement));
        ChatUtils.sendMessageWithBrackets(commandPlayer, Component.literal("Entitlements - ").append(player.getName()));
        grouped.forEach((entitlement, instances) -> {
            ChatUtils.sendMessage(commandPlayer, "%s - %d instances", entitlement.getRegisteredName(), instances.size());
        });

        return Command.SINGLE_SUCCESS;
    }

    static int addInstance(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer commandPlayer = context.getSource().getPlayerOrException();
        ServerPlayer player = PlayersArgument.getPlayer(context, "player");
        ResourceKey<MKEntitlement> entId = EntitlementIdArgument.get(context, "entitlementId");

        var entitlement = player.registryAccess().registryOrThrow(MKCoreRegistry.ENTITLEMENT_REGISTRY_KEY).getHolder(entId);
        if (entitlement.isPresent()) {
            MKPlayerData playerData = MKCore.getPlayerOrThrow(player);
            UUID instanceId = UUID.randomUUID();
            EntitlementInstance newInstance = new EntitlementInstance(entitlement.get(), instanceId);
            playerData.getEntitlements().addEntitlement(newInstance);

            ChatUtils.sendMessage(commandPlayer, "Granted entitlement '%s' to %s (instance id %s)", entId.location(), player.getName().getString(), instanceId);
        } else {
            ChatUtils.sendMessage(commandPlayer, "Entitlement '%s' not found", entId.location());
        }

        return Command.SINGLE_SUCCESS;
    }

    static int removeInstance(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer commandPlayer = context.getSource().getPlayerOrException();
        ServerPlayer player = PlayersArgument.getPlayer(context, "player");
        ResourceKey<MKEntitlement> entitlementId = EntitlementIdArgument.get(context, "entitlementId");
        UUID instanceId = UuidArgument.getUuid(context, "instanceId");

        var entitlement = player.registryAccess().registryOrThrow(MKCoreRegistry.ENTITLEMENT_REGISTRY_KEY).getHolder(entitlementId);
        if (entitlement.isPresent()) {
            MKPlayerData playerData = MKCore.getPlayerOrThrow(player);
            var entitlements = playerData.getEntitlements();
            if (entitlements.hasEntitlement(entitlement.get())) {
                if (entitlements.hasEntitlementInstance(instanceId)) {
                    entitlements.removeEntitlement(instanceId);
                    ChatUtils.sendMessage(commandPlayer, "Removed Entitlement '%s' instance '%s'", entitlementId.location(), instanceId);
                } else {
                    ChatUtils.sendMessage(commandPlayer, "Entitlement '%s' instance '%s' not known", entitlementId.location(), instanceId);
                }
            } else {
                ChatUtils.sendMessage(commandPlayer, "Entitlement '%s' not known", entitlementId.location());
            }
        } else {
            ChatUtils.sendMessage(commandPlayer, "Entitlement '%s' does not exist", entitlementId.location());
        }

        return Command.SINGLE_SUCCESS;
    }

    public static CompletableFuture<Suggestions> suggestKnownEntitlements(final CommandContext<CommandSourceStack> context,
                                                                          final SuggestionsBuilder builder) throws CommandSyntaxException {
        ServerPlayer player = PlayersArgument.getPlayer(context, "player");
        MKPlayerData playerData = MKCore.getPlayerOrThrow(player);
        return SharedSuggestionProvider.suggest(playerData.getEntitlements().getInstanceStream()
                .map(EntitlementInstance::entitlement)
                .distinct()
                .map(Holder::getRegisteredName), builder);
    }

    public static CompletableFuture<Suggestions> suggestKnownEntitlementInstances(final CommandContext<CommandSourceStack> context,
                                                                                  final SuggestionsBuilder builder) throws CommandSyntaxException {
        ServerPlayer player = PlayersArgument.getPlayer(context, "player");
        ResourceKey<MKEntitlement> entitlementId = EntitlementIdArgument.get(context, "entitlementId");

        MKEntitlement entitlement = player.registryAccess().registryOrThrow(MKCoreRegistry.ENTITLEMENT_REGISTRY_KEY).get(entitlementId);
        if (entitlement != null) {
            MKPlayerData playerData = MKCore.getPlayerOrThrow(player);
            return SharedSuggestionProvider.suggest(playerData.getEntitlements().getInstanceStream()
                    .filter(i -> i.entitlement().value() == entitlement)
                    .map(i -> i.instanceId().toString()), builder);
        }

        return Suggestions.empty();
    }
}
