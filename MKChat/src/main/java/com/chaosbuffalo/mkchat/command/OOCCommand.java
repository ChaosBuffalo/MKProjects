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

public class OOCCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("ooc").then(Commands.argument("msg", StringArgumentType.greedyString())
                .executes(OOCCommand::handleMessage));
    }

    static int handleMessage(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String msg = StringArgumentType.getString(ctx, "msg");
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        MutableComponent oocMessage = Component.literal(String.format("[OOC]<%s>: %s",
                ctx.getSource().getPlayerOrException().getName().getString(), msg));
        oocMessage.withStyle(ChatFormatting.DARK_GREEN);
        player.getLevel().players().forEach(
                playerEntity -> playerEntity.sendSystemMessage(oocMessage));
        return Command.SINGLE_SUCCESS;
    }

}
