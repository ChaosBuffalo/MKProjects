package com.chaosbuffalo.mkweapons.init;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.command.LootSlotArgument;
import com.chaosbuffalo.mkweapons.command.LootTierArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MKWeaponsCommands {

    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES =
            DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, MKWeapons.MODID);

    public static final RegistryObject<ArgumentTypeInfo<?, ?>> LOOT_SLOT = ARGUMENT_TYPES.register("loot_slot",
            () -> ArgumentTypeInfos.registerByClass(LootSlotArgument.class,
                    SingletonArgumentInfo.contextFree(LootSlotArgument::definition)));

    public static final RegistryObject<ArgumentTypeInfo<?, ?>> LOOT_TIER = ARGUMENT_TYPES.register("loot_tier",
            () -> ArgumentTypeInfos.registerByClass(LootTierArgument.class,
                    SingletonArgumentInfo.contextFree(LootTierArgument::definition)));


    public static void register(IEventBus modBus) {
        ARGUMENT_TYPES.register(modBus);
    }
}
