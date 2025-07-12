package com.chaosbuffalo.mkfaction.command;

import com.chaosbuffalo.mkcore.command.arguments.PlayersArgument;
import com.chaosbuffalo.mkcore.utils.ChatUtils;
import com.chaosbuffalo.mkfaction.capabilities.IPlayerFaction;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import com.chaosbuffalo.mkfaction.faction.MKFactionRegistry;
import com.chaosbuffalo.mkfaction.faction.PlayerFactionEntry;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class FactionCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("mk")
                .then(Commands.literal("faction")
                        .then(Commands.literal("show")
                                .then(Commands.argument("faction", FactionIdArgument.factionId(context))
                                        .executes(FactionCommand::showFaction)
                                        .then(Commands.argument("player", PlayersArgument.player())
                                                .executes(FactionCommand::showOtherFaction))
                                )
                                .executes(FactionCommand::showAllFactions)
                                .then(Commands.argument("player", PlayersArgument.player())
                                        .executes(FactionCommand::showAllFactionsOther))
                        )
                        .then(Commands.literal("add")
                                .then(Commands.argument("faction", FactionIdArgument.factionId(context))
                                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                                .executes(FactionCommand::addFaction)
                                                .then(Commands.argument("player", PlayersArgument.player())
                                                        .executes(FactionCommand::addFactionOther))
                                        )
                                )
                        )
                        .then(Commands.literal("set")
                                .then(Commands.argument("faction", FactionIdArgument.factionId(context))
                                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                                .executes(FactionCommand::setFaction)
                                                .then(Commands.argument("player", PlayersArgument.player())
                                                        .executes(FactionCommand::setFactionOther))
                                        )
                                )
                        )
                );

        dispatcher.register(builder);
    }


    private static Component describeEntry(PlayerFactionEntry entry) {
        return Component.empty()
                .append(entry.getDisplayName())
                .append(String.format(": %d (%s)", entry.getFactionScore(), entry.getFactionStatus()));
    }

    static int addFaction(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        return doAddFaction(ctx, player);
    }

    static int addFactionOther(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = PlayersArgument.getPlayer(ctx, "player");
        return doAddFaction(ctx, player);
    }

    private static int doAddFaction(CommandContext<CommandSourceStack> ctx, ServerPlayer player) throws CommandSyntaxException {
        Holder<MKFaction> faction = FactionIdArgument.getFaction(ctx, "faction");
        int amount = IntegerArgumentType.getInteger(ctx, "amount");

        IPlayerFaction playerFaction = IPlayerFaction.getOrThrow(player);
        playerFaction.getFactionEntry(faction).ifPresent(entry -> {
            entry.incrementFaction(amount);
            Component line = describeEntry(entry);
            ChatUtils.sendMessage(player, line);
        });

        return Command.SINGLE_SUCCESS;
    }

    static int setFaction(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        return doSetFaction(ctx, player);
    }

    static int setFactionOther(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = PlayersArgument.getPlayer(ctx, "player");
        return doSetFaction(ctx, player);
    }

    private static int doSetFaction(CommandContext<CommandSourceStack> ctx, ServerPlayer player) throws CommandSyntaxException {
        Holder<MKFaction> faction = FactionIdArgument.getFaction(ctx, "faction");
        int amount = IntegerArgumentType.getInteger(ctx, "amount");

        IPlayerFaction playerFaction = IPlayerFaction.getOrThrow(player);
        playerFaction.getFactionEntry(faction).ifPresent(entry -> {
            entry.setFactionScore(amount);
            Component line = describeEntry(entry);
            ChatUtils.sendMessage(player, line);
        });

        return Command.SINGLE_SUCCESS;
    }

    static int showFaction(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        return doShowFaction(ctx, player);
    }

    static int showOtherFaction(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = PlayersArgument.getPlayer(ctx, "player");
        return doShowFaction(ctx, player);
    }

    static int doShowFaction(CommandContext<CommandSourceStack> ctx, ServerPlayer player) throws CommandSyntaxException {
        Holder<MKFaction> faction = FactionIdArgument.getFaction(ctx, "faction");

        IPlayerFaction playerFaction = IPlayerFaction.getOrThrow(player);
        playerFaction.getFactionEntry(faction).ifPresent(entry -> {
            Component line = describeEntry(entry);
            ChatUtils.sendMessage(player, line);
        });

        return Command.SINGLE_SUCCESS;
    }


    static int showAllFactions(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        return doShowAllFactions(player);
    }

    static int showAllFactionsOther(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = PlayersArgument.getPlayer(ctx, "player");
        return doShowAllFactions(player);
    }

    private static int doShowAllFactions(ServerPlayer player) {
        IPlayerFaction playerFaction = IPlayerFaction.getOrThrow(player);
        playerFaction.getFactionMap().forEach((name, entry) -> {
            Component line = describeEntry(entry);
            ChatUtils.sendMessage(player, line);
        });

        return Command.SINGLE_SUCCESS;
    }

    public static class FactionIdArgument extends ResourceArgument<MKFaction> {

        public static FactionIdArgument factionId(CommandBuildContext context) {
            return new FactionIdArgument(context);
        }

        public FactionIdArgument(CommandBuildContext context) {
            super(context, MKFactionRegistry.FACTION_REGISTRY_KEY);
        }

        public static Holder.Reference<MKFaction> getFaction(CommandContext<CommandSourceStack> context, String argument)
                throws CommandSyntaxException {
            return getResource(context, argument, MKFactionRegistry.FACTION_REGISTRY_KEY);
        }
    }
}
