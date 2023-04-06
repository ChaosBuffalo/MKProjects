package com.chaosbuffalo.mknpc.command;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.command.arguments.AbilityIdArgument;
import com.chaosbuffalo.mknpc.MKNpc;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class NpcCommands {

    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES =
            DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, MKNpc.MODID);

    public static final RegistryObject<ArgumentTypeInfo<?, ?>> NPC_DEFINITION_ID = ARGUMENT_TYPES.register("npc_definition_id",
            () -> ArgumentTypeInfos.registerByClass(NpcDefinitionIdArgument.class,
                    SingletonArgumentInfo.contextFree(NpcDefinitionIdArgument::definition)));

    public static final RegistryObject<ArgumentTypeInfo<?, ?>> QUEST_DEFINITION_ID = ARGUMENT_TYPES.register("quest_definition_id",
            () -> ArgumentTypeInfos.registerByClass(QuestDefinitionIdArgument.class,
                    SingletonArgumentInfo.contextFree(QuestDefinitionIdArgument::definition)));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(MKSummonCommand.register());
        dispatcher.register(MKQuestCommand.register());
        dispatcher.register(MKStructureCommands.register());
    }

    public static void register(IEventBus modBus) {
        ARGUMENT_TYPES.register(modBus);
    }
}
