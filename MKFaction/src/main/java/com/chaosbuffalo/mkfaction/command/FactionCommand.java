package com.chaosbuffalo.mkfaction.command;

import com.chaosbuffalo.mkcore.utils.ChatUtils;
import com.chaosbuffalo.mkfaction.capabilities.FactionCapabilities;
import com.chaosbuffalo.mkfaction.event.MKFactionRegistry;
import com.chaosbuffalo.mkfaction.faction.PlayerFactionEntry;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

public class FactionCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("mk")
                .then(Commands.literal("faction")
                        .then(Commands.literal("show")
                                .then(Commands.argument("faction", FactionIdArgument.factionId())
                                        .executes(FactionCommand::showFaction)
                                )
                                .executes(FactionCommand::showAllFactions)
                        )
                        .then(Commands.literal("add")
                                .then(Commands.argument("faction", FactionIdArgument.factionId())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                                .executes(FactionCommand::addFaction)
                                        )
                                )
                        )
                        .then(Commands.literal("set")
                                .then(Commands.argument("faction", FactionIdArgument.factionId())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                                .executes(FactionCommand::setFaction)
                                        )
                                )
                        )
                );

        dispatcher.register(builder);
    }

    public static void registerArgumentTypes() {
        ArgumentTypes.register("faction_id", FactionIdArgument.class, new EmptyArgumentSerializer<>(FactionIdArgument::new));
    }

    private static String describeEntry(PlayerFactionEntry entry) {
        return String.format("%s: %d (%s)", entry.getFactionName(), entry.getFactionScore(), entry.getFactionStatus());
    }

    static int addFaction(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ResourceLocation factionId = ctx.getArgument("faction", ResourceLocation.class);
        int amount = IntegerArgumentType.getInteger(ctx, "amount");

        player.getCapability(FactionCapabilities.PLAYER_FACTION_CAPABILITY).ifPresent(faction -> {
            faction.getFactionEntry(factionId).ifPresent(entry -> {
                entry.incrementFaction(amount);
                String line = describeEntry(entry);
                ChatUtils.sendMessage(player, line);
            });
        });

        return Command.SINGLE_SUCCESS;
    }

    static int setFaction(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ResourceLocation factionId = ctx.getArgument("faction", ResourceLocation.class);
        int amount = IntegerArgumentType.getInteger(ctx, "amount");

        player.getCapability(FactionCapabilities.PLAYER_FACTION_CAPABILITY).ifPresent(faction -> {
            faction.getFactionEntry(factionId).ifPresent(entry -> {
                entry.setFactionScore(amount);
                String line = describeEntry(entry);
                ChatUtils.sendMessage(player, line);
            });
        });

        return Command.SINGLE_SUCCESS;
    }

    static int showFaction(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ResourceLocation factionId = ctx.getArgument("faction", ResourceLocation.class);

        player.getCapability(FactionCapabilities.PLAYER_FACTION_CAPABILITY).ifPresent(faction -> {
            faction.getFactionEntry(factionId).ifPresent(entry -> {
                String line = describeEntry(entry);
                ChatUtils.sendMessage(player, line);
            });
        });

        return Command.SINGLE_SUCCESS;
    }


    static int showAllFactions(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        player.getCapability(FactionCapabilities.PLAYER_FACTION_CAPABILITY).ifPresent(faction -> {
            faction.getFactionMap().forEach((name, entry) -> {
                String line = describeEntry(entry);
                ChatUtils.sendMessage(player, line);
            });
        });

        return Command.SINGLE_SUCCESS;
    }

    public static class FactionIdArgument implements ArgumentType<ResourceLocation> {

        public static FactionIdArgument factionId() {
            return new FactionIdArgument();
        }

        @Override
        public ResourceLocation parse(final StringReader reader) throws CommandSyntaxException {
            return ResourceLocation.read(reader);
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
            return SharedSuggestionProvider.suggest(MKFactionRegistry.FACTION_REGISTRY.getKeys().stream().map(ResourceLocation::toString), builder);
        }
    }
}
