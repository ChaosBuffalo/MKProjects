package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.command.arguments.PlayersArgument;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.PartyInvitePacket;
import com.chaosbuffalo.mkcore.party.PartyManager;
import com.chaosbuffalo.mkcore.utils.ChatUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;


public class PartyCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("party")
                .then(Commands.literal("invite")
                        .then(Commands.argument("player", PlayersArgument.player())
                                .executes(PartyCommand::partyInvite)
                        )
                ).then(Commands.literal("leave")
                        .executes(PartyCommand::partyLeave)
                ).then(Commands.literal("info")
                        .executes(PartyCommand::partyInfo)
                );

    }

    private static int partyLeave(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        MinecraftServer server = player.getServer();
        if (server != null) {
            PartyManager.removePlayerFromParty(server, player);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int partyInfo(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        PlayerTeam team = player.getTeam();
        if (team != null) {
            ChatUtils.sendMessage(player, Component.translatable("mk.core.party.info.name", team.getDisplayName()));
            StringBuilder builder = new StringBuilder();
            int i = 0;
            for (String name : team.getPlayers()) {
                builder.append(name);
                if (i < team.getPlayers().size() - 1) {
                    builder.append(", ");
                }
                i++;
            }
            ChatUtils.sendMessage(player, Component.translatable("mk.core.party.info.members", builder.toString()));
        } else {
            ChatUtils.sendMessage(player, Component.translatable("mk.core.party.info.none"));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int partyInvite(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        MinecraftServer server = player.getServer();
        if (server != null) {
            ServerPlayer invited = EntityArgument.getPlayer(ctx, "player");
            MKCore.LOGGER.info("{} invited {}", player.getName(), invited.getName());
            if (player.equals(invited)) {
                ChatUtils.sendMessage(player, Component.translatable("mk.core.party.invite_self"));
            } else {
                PacketHandler.sendMessage(new PartyInvitePacket(player), invited);
                ChatUtils.sendMessage(player, Component.translatable("mk.core.party.inviter.text", invited.getDisplayName()));
                ChatUtils.sendMessage(invited, Component.translatable("mk.core.party.invitee.text", player.getDisplayName()));
            }
        }
        return Command.SINGLE_SUCCESS;
    }
}
