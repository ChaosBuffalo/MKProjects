package com.chaosbuffalo.mkchat.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;

public class DimCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("dim").then(Commands.argument("msg", StringArgumentType.greedyString())
                .executes(DimCommand::handleMessage));
    }

    static int handleMessage(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String msg = StringArgumentType.getString(ctx, "msg");
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        TextComponent compMessage = new TextComponent(String.format("[Dim]<%s>: %s",
                player.getName().getString(), msg));
        compMessage.withStyle(ChatFormatting.GOLD);
        // emulate sendMessage but only to players in the dimension
        ctx.getSource().getServer().sendMessage(compMessage, Util.NIL_UUID);
        player.getLevel().players().forEach(
                playerEntity -> playerEntity.sendMessage(compMessage, Util.NIL_UUID));
        return Command.SINGLE_SUCCESS;
    }
}
