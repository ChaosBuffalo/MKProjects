package com.chaosbuffalo.mkchat.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

public class ChatCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(OOCCommand.register());
        dispatcher.register(DimCommand.register());
        dispatcher.register(PartyCommand.register());
        dispatcher.register(CleanHistoryCommand.register());
    }
}
