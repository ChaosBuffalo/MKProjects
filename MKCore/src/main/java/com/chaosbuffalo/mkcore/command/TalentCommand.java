package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.command.arguments.PlayersArgument;
import com.chaosbuffalo.mkcore.command.arguments.TalentTreeIdArgument;
import com.chaosbuffalo.mkcore.core.talents.*;
import com.chaosbuffalo.mkcore.utils.ChatUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class TalentCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("talent")
                .then(Commands.argument("player", PlayersArgument.player())
                        .then(Commands.literal("points")
                                .requires(MKCommand::isGM)
                                .then(Commands.literal("give")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                                .executes(TalentCommand::givePoints)))
                                .then(Commands.literal("take")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                                .executes(TalentCommand::takePoints)))
                                .executes(TalentCommand::showPoints)
                        )
                        .then(Commands.literal("tree")
                                .then(Commands.literal("list")
                                        .executes(TalentCommand::listTrees))
                                .then(Commands.literal("unlock")
                                        .requires(MKCommand::isGM)
                                        .then(Commands.argument("tree", TalentTreeIdArgument.talentTreeId())
                                                .executes(TalentCommand::unlockTree)))
                        )
                )
                ;
    }

    static int takePoints(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer commandPlayer = context.getSource().getPlayerOrException();
        ServerPlayer player = PlayersArgument.getPlayer(context, "player");
        int amount = IntegerArgumentType.getInteger(context, "amount");

        var playerData = MKCore.getPlayerOrThrow(player);
        PlayerTalentKnowledge talentKnowledge = playerData.getTalents();
        if (talentKnowledge.removeTalentPoints(amount)) {
            ChatUtils.sendMessage(commandPlayer, "Removed %d points", amount);
        } else {
            ChatUtils.sendMessage(commandPlayer, "Failed to remove %d points", amount);
        }

        return Command.SINGLE_SUCCESS;
    }

    static int givePoints(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer commandPlayer = context.getSource().getPlayerOrException();
        ServerPlayer player = PlayersArgument.getPlayer(context, "player");
        int amount = IntegerArgumentType.getInteger(context, "amount");

        var playerData = MKCore.getPlayerOrThrow(player);
        PlayerTalentKnowledge talentKnowledge = playerData.getTalents();
        if (talentKnowledge.grantTalentPoints(amount)) {
            ChatUtils.sendMessage(commandPlayer, "Granted %d points", amount);
        } else {
            ChatUtils.sendMessage(commandPlayer, "Failed to give %d points", amount);
        }

        return Command.SINGLE_SUCCESS;
    }

    static int showPoints(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer commandPlayer = context.getSource().getPlayerOrException();
        ServerPlayer player = PlayersArgument.getPlayer(context, "player");

        if (commandPlayer != player && !context.getSource().hasPermission(Commands.LEVEL_GAMEMASTERS)) {
            ChatUtils.sendMessage(commandPlayer, "Error - Can only query yourself");
            return Command.SINGLE_SUCCESS;
        }

        var playerData = MKCore.getPlayerOrThrow(player);
        PlayerTalentKnowledge talentKnowledge = playerData.getTalents();
        int unspent = talentKnowledge.getUnspentTalentPoints();
        int total = talentKnowledge.getTotalTalentPoints();
        ChatUtils.sendMessage(commandPlayer, "Talent Points: %d (%d unspent)", total, unspent);

        return Command.SINGLE_SUCCESS;
    }

    static int unlockTree(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer commandPlayer = context.getSource().getPlayerOrException();
        ServerPlayer player = PlayersArgument.getPlayer(context, "player");
        ResourceKey<TalentTreeDefinition> treeId = TalentTreeIdArgument.get(context, "tree");

        var playerData = MKCore.getPlayerOrThrow(player);
        PlayerTalentKnowledge talentKnowledge = playerData.getTalents();
        if (talentKnowledge.knowsTree(treeId)) {
            ChatUtils.sendMessage(commandPlayer, "Tree %s already known", treeId.location());
            return Command.SINGLE_SUCCESS;
        }

        if (talentKnowledge.unlockTree(treeId)) {
            ChatUtils.sendMessage(commandPlayer, "Tree %s unlocked", treeId.location());
        } else {
            ChatUtils.sendMessage(commandPlayer, "Failed to unlock tree %s", treeId.location());
        }

        return Command.SINGLE_SUCCESS;
    }

    static int listTrees(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer commandPlayer = context.getSource().getPlayerOrException();
        ServerPlayer player = PlayersArgument.getPlayer(context, "player");

        if (commandPlayer != player && !context.getSource().hasPermission(Commands.LEVEL_GAMEMASTERS)) {
            ChatUtils.sendMessage(commandPlayer, "Error - Can only query yourself");
            return Command.SINGLE_SUCCESS;
        }

        var playerData = MKCore.getPlayerOrThrow(player);
        Collection<ResourceLocation> knownTalents = playerData.getTalents().getKnownTreeNames();
        if (!knownTalents.isEmpty()) {
            ChatUtils.sendMessageWithBrackets(commandPlayer, Component.literal("Known Talent Trees - ").append(player.getName()));
            knownTalents.forEach(info -> ChatUtils.sendMessage(commandPlayer, "%s", info));
        } else {
            ChatUtils.sendMessage(commandPlayer, "You do not know any talent trees");
        }

        return Command.SINGLE_SUCCESS;
    }
}
