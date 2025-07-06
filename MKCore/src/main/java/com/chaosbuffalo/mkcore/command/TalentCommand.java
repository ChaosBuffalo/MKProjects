package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.command.arguments.TalentLineIdArgument;
import com.chaosbuffalo.mkcore.command.arguments.TalentTreeIdArgument;
import com.chaosbuffalo.mkcore.core.talents.*;
import com.chaosbuffalo.mkcore.utils.ChatUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class TalentCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("talent")
                .then(Commands.literal("points")
                        .then(Commands.literal("give")
                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                        .executes(TalentCommand::givePointsToSelf)
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(TalentCommand::givePointsToPlayer))))
                        .then(Commands.literal("take")
                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                        .executes(TalentCommand::takePoints)))
                        .executes(TalentCommand::showPoints)
                )
                .then(Commands.literal("learn")
                        .then(Commands.argument("tree", TalentTreeIdArgument.talentTreeId())
                                .then(Commands.argument("line", TalentLineIdArgument.talentLine())
                                        .then(Commands.argument("index", IntegerArgumentType.integer())
                                                .executes(TalentCommand::learnTalent))))
                )
                .then(Commands.literal("unlearn")
                        .then(Commands.argument("tree", TalentTreeIdArgument.talentTreeId())
                                .then(Commands.argument("line", TalentLineIdArgument.talentLine())
                                        .then(Commands.argument("index", IntegerArgumentType.integer())
                                                .executes(TalentCommand::unlearnTalent))))
                )
                .then(Commands.literal("tree")
                        .then(Commands.literal("list")
                                .executes(TalentCommand::listTrees))
                        .then(Commands.literal("unlock")
                                .then(Commands.argument("tree", TalentTreeIdArgument.talentTreeId())
                                        .executes(TalentCommand::unlockTree)))
                )
                ;
    }

    static int takePoints(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        int amount = IntegerArgumentType.getInteger(ctx, "amount");

        MKCore.getPlayer(player).ifPresent(cap -> {
            PlayerTalentKnowledge talentKnowledge = cap.getTalents();
            if (talentKnowledge.removeTalentPoints(amount)) {
                ChatUtils.sendMessage(player, "Removed %d points", amount);
            } else {
                ChatUtils.sendMessage(player, "Failed to remove %d points", amount);
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    static int givePointsToSelf(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        return givePoints(ctx, player);
    }

    private static int givePoints(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        int amount = IntegerArgumentType.getInteger(ctx, "amount");

        var playerData = MKCore.getPlayerOrThrow(player);
        PlayerTalentKnowledge talentKnowledge = playerData.getTalents();
        if (talentKnowledge.grantTalentPoints(amount)) {
            ChatUtils.sendMessage(player, "Granted %d points", amount);
        } else {
            ChatUtils.sendMessage(player, "Failed to give %d points", amount);
        }

        return Command.SINGLE_SUCCESS;
    }

    static int givePointsToPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        return givePoints(ctx, player);
    }

    static int showPoints(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        var playerData = MKCore.getPlayerOrThrow(player);
        PlayerTalentKnowledge talentKnowledge = playerData.getTalents();
        int unspent = talentKnowledge.getUnspentTalentPoints();
        int total = talentKnowledge.getTotalTalentPoints();
        ChatUtils.sendMessage(player, "Talent Points: %d (%d unspent)", total, unspent);

        return Command.SINGLE_SUCCESS;
    }

    static int learnTalent(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ResourceKey<TalentTreeDefinition> treeId = TalentTreeIdArgument.get(ctx, "tree");
        String line = StringArgumentType.getString(ctx, "line");
        int index = IntegerArgumentType.getInteger(ctx, "index");

        var playerData = MKCore.getPlayerOrThrow(player);
        PlayerTalentKnowledge talentKnowledge = playerData.getTalents();
        if (talentKnowledge.spendTalentPoint(treeId, line, index)) {
            ChatUtils.sendMessage(player, "Spent point in (%s, %s, %d)", treeId, line, index);
        } else {
            ChatUtils.sendMessage(player, "Failed to spend point in (%s, %s, %d)", treeId, line, index);
        }

        return Command.SINGLE_SUCCESS;
    }

    static int unlearnTalent(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ResourceKey<TalentTreeDefinition> talentId = TalentTreeIdArgument.get(ctx, "tree");
        String line = StringArgumentType.getString(ctx, "line");
        int index = IntegerArgumentType.getInteger(ctx, "index");

        var playerData = MKCore.getPlayerOrThrow(player);
        PlayerTalentKnowledge talentKnowledge = playerData.getTalents();
        if (talentKnowledge.refundTalentPoint(talentId, line, index)) {
            ChatUtils.sendMessage(player, "Refund point in (%s, %s, %d)", talentId, line, index);
        } else {
            ChatUtils.sendMessage(player, "Failed to refund point in (%s, %s, %d)", talentId, line, index);
        }

        return Command.SINGLE_SUCCESS;
    }

    static int unlockTree(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ResourceKey<TalentTreeDefinition> treeId = TalentTreeIdArgument.get(ctx, "tree");

        var playerData = MKCore.getPlayerOrThrow(player);
        PlayerTalentKnowledge talentKnowledge = playerData.getTalents();
        if (talentKnowledge.knowsTree(treeId)) {
            ChatUtils.sendMessage(player, "Tree %s already known", treeId.location());
            return Command.SINGLE_SUCCESS;
        }

        if (talentKnowledge.unlockTree(treeId)) {
            ChatUtils.sendMessage(player, "Tree %s unlocked", treeId.location());
        } else {
            ChatUtils.sendMessage(player, "Failed to unlock tree %s", treeId.location());
        }

        return Command.SINGLE_SUCCESS;
    }

    static int listTrees(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        MKCore.getPlayer(player).ifPresent(cap -> {
            PlayerTalentKnowledge talents = cap.getTalents();
            Collection<ResourceLocation> knownTalents = talents.getKnownTreeNames();
            if (!knownTalents.isEmpty()) {
                ChatUtils.sendMessageWithBrackets(player, "Known Talent Trees");
                knownTalents.forEach(info -> ChatUtils.sendMessage(player, "%s", info));
            } else {
                ChatUtils.sendMessage(player, "You do not know any talent trees");
            }
        });

        return Command.SINGLE_SUCCESS;
    }
}
