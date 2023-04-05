package com.chaosbuffalo.mkfaction.init;

import com.chaosbuffalo.mkfaction.MKFactionMod;
import com.chaosbuffalo.mkfaction.command.FactionCommand;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class FactionCommands {

    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES =
            DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, MKFactionMod.MODID);

    public static final RegistryObject<ArgumentTypeInfo<?, ?>> FACTION_ID = ARGUMENT_TYPES.register("faction_id",
            () -> ArgumentTypeInfos.registerByClass(FactionCommand.FactionIdArgument.class,
                    SingletonArgumentInfo.contextFree(FactionCommand.FactionIdArgument::factionId)));

    public static void register(IEventBus modBus) {
        ARGUMENT_TYPES.register(modBus);
    }
}
