package com.chaosbuffalo.mkchat.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;

public class PartyCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("p").then(Commands.argument("msg", StringArgumentType.greedyString())
                .executes(PartyCommand::handleMessage));
    }

    static int handleMessage(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String msg = StringArgumentType.getString(ctx, "msg");
        TextComponent msgComp = new TextComponent(String.format("[Party]<%s>: %s",
                ctx.getSource().getPlayerOrException().getName().getString(), msg));
        msgComp.withStyle(ChatFormatting.DARK_AQUA);
        ctx.getSource().getPlayerOrException().sendMessage(msgComp, Util.NIL_UUID);
        ctx.getSource().getServer().getPlayerList().broadcastToTeam(ctx.getSource().getPlayerOrException(), msgComp);
        return Command.SINGLE_SUCCESS;
    }
}
