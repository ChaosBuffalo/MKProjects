package com.chaosbuffalo.mknpc.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
import net.minecraft.commands.synchronization.ArgumentTypes;

public class NpcCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(MKSummonCommand.register());
        dispatcher.register(MKQuestCommand.register());
        dispatcher.register(MKStructureCommands.register());
    }

    public static void registerArguments() {
        ArgumentTypes.register("npc_definition_id", NpcDefinitionIdArgument.class, new EmptyArgumentSerializer<>(NpcDefinitionIdArgument::definition));
        ArgumentTypes.register("quest_definition_id", QuestDefinitionIdArgument.class, new EmptyArgumentSerializer<>(QuestDefinitionIdArgument::definition));
    }
}
