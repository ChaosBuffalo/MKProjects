package com.chaosbuffalo.mkchat.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

public class DimCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("dim").then(Commands.argument("msg", StringArgumentType.greedyString())
                .executes(DimCommand::handleMessage));
    }

    static int handleMessage(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String msg = StringArgumentType.getString(ctx, "msg");
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        MutableComponent compMessage = Component.literal(String.format("[Dim]<%s>: %s",
                player.getName().getString(), msg));
        compMessage.withStyle(ChatFormatting.GOLD);
        // emulate sendMessage but only to players in the dimension
        player.getLevel().players().forEach(
                playerEntity -> playerEntity.sendSystemMessage(compMessage));
        return Command.SINGLE_SUCCESS;
    }
}
