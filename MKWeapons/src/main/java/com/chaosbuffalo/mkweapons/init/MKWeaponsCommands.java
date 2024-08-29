package com.chaosbuffalo.mkweapons.init;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.command.LootGenCommand;
import com.chaosbuffalo.mkweapons.command.LootSlotArgument;
import com.chaosbuffalo.mkweapons.command.LootTierArgument;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MKWeaponsCommands {

    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES =
            DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, MKWeapons.MODID);

    public static final Holder<ArgumentTypeInfo<?, ?>> LOOT_SLOT = ARGUMENT_TYPES.register("loot_slot",
            () -> ArgumentTypeInfos.registerByClass(LootSlotArgument.class,
                    SingletonArgumentInfo.contextFree(LootSlotArgument::definition)));

    public static final Holder<ArgumentTypeInfo<?, ?>> LOOT_TIER = ARGUMENT_TYPES.register("loot_tier",
            () -> ArgumentTypeInfos.registerByClass(LootTierArgument.class,
                    SingletonArgumentInfo.contextFree(LootTierArgument::definition)));


    public static void register(IEventBus modBus) {
        ARGUMENT_TYPES.register(modBus);
    }

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(LootGenCommand.register());
    }
}
