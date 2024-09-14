package com.chaosbuffalo.mkfaction.init;

import com.chaosbuffalo.mkfaction.MKFactionMod;
import com.chaosbuffalo.mkfaction.command.FactionCommand;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FactionCommands {

    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES =
            DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, MKFactionMod.MODID);

    public static final Holder<ArgumentTypeInfo<?, ?>> FACTION_ID = ARGUMENT_TYPES.register("faction_id",
            () -> ArgumentTypeInfos.registerByClass(FactionCommand.FactionIdArgument.class,
                    SingletonArgumentInfo.contextAware(FactionCommand.FactionIdArgument::factionId)));

    public static void register(IEventBus modBus) {
        ARGUMENT_TYPES.register(modBus);
    }
}
