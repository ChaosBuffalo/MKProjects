package com.chaosbuffalo.mkchat.command;

import com.chaosbuffalo.mkchat.capabilities.IPlayerDialogue;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CleanHistoryCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("chat").then(Commands.literal("history")
                .then(Commands.literal("clean").executes(CleanHistoryCommand::handleMessage)));
    }

    static int handleMessage(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        IPlayerDialogue.get(ctx.getSource().getPlayerOrException()).ifPresent(IPlayerDialogue::cleanHistory);
        return Command.SINGLE_SUCCESS;
    }
}
