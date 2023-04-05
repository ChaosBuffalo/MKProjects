package com.chaosbuffalo.mkweapons.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

public class WeaponsCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(LootGenCommand.register());
    }

    public static void registerArguments() {

    }
}
