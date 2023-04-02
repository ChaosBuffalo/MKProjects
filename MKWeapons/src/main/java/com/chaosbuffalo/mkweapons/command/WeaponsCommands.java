package com.chaosbuffalo.mkweapons.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
import net.minecraft.commands.synchronization.ArgumentTypes;

public class WeaponsCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(LootGenCommand.register());
    }

    public static void registerArguments() {
        ArgumentTypes.register("loot_tier", LootTierArgument.class, new EmptyArgumentSerializer<>(LootTierArgument::definition));
        ArgumentTypes.register("loot_slot", LootSlotArgument.class, new EmptyArgumentSerializer<>(LootSlotArgument::definition));
    }
}
