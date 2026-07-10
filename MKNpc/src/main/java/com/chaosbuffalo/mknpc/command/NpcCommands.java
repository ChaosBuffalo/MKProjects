package com.chaosbuffalo.mknpc.command;

import com.chaosbuffalo.mknpc.MKNpc;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;

public class NpcCommands {

    public static final net.neoforged.neoforge.registries.DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES =
            net.neoforged.neoforge.registries.DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, MKNpc.MODID);

    public static final Holder<ArgumentTypeInfo<?, ?>> NPC_DEFINITION_ID = ARGUMENT_TYPES.register("npc_definition_id",
            () -> ArgumentTypeInfos.registerByClass(NpcDefinitionIdArgument.class,
                    SingletonArgumentInfo.contextFree(NpcDefinitionIdArgument::definition)));

    public static final Holder<ArgumentTypeInfo<?, ?>> QUEST_DEFINITION_ID = ARGUMENT_TYPES.register("quest_definition_id",
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
