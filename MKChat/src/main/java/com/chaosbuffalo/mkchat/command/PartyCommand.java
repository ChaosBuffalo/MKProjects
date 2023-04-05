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

public class PartyCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("p").then(Commands.argument("msg", StringArgumentType.greedyString())
                .executes(PartyCommand::handleMessage));
    }

    static int handleMessage(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String msg = StringArgumentType.getString(ctx, "msg");
        MutableComponent msgComp = Component.literal(String.format("[Party]<%s>: %s",
                ctx.getSource().getPlayerOrException().getName().getString(), msg));
        msgComp.withStyle(ChatFormatting.DARK_AQUA);
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        player.sendSystemMessage(msgComp);
        player.getServer().getPlayerList().broadcastSystemToTeam(player, msgComp);
        return Command.SINGLE_SUCCESS;
    }
}
