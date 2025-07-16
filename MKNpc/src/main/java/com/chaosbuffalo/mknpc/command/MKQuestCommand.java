package com.chaosbuffalo.mknpc.command;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.content.ContentDB;
import com.chaosbuffalo.mknpc.quest.QuestChainInstance;
import com.chaosbuffalo.mknpc.quest.QuestDefinition;
import com.chaosbuffalo.mknpc.quest.QuestRegistries;
import com.chaosbuffalo.mknpc.quest.generation.QuestChainBuildResult;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.UUID;

public class MKQuestCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("mkquest")
                .then(Commands.literal("gen")
                        .then(Commands.argument("quest", QuestDefinitionIdArgument.definition())
                                .executes(MKQuestCommand::generateQuest)))
                .then(Commands.literal("start")
                        .then(Commands.argument("id", UuidArgument.uuid())
                                .executes(MKQuestCommand::startQuest)));
    }

    static int startQuest(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        UUID instanceId = UuidArgument.getUuid(ctx, "id");

        IWorldNpcData worldData = ContentDB.getQuestDB();
        MKNpc.getPlayerQuestData(player).ifPresent(x -> {
            x.startQuest(worldData, instanceId);
        });

        return Command.SINGLE_SUCCESS;
    }

    static int generateQuest(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ResourceKey<QuestDefinition> questId = QuestDefinitionIdArgument.get(ctx, "quest");
        QuestDefinition definition = player.registryAccess().registryOrThrow(QuestRegistries.QUEST_DEFINITIONS).get(questId);

        if (definition != null) {
            BlockPos pos = player.blockPosition();
            IWorldNpcData questDatabase = ContentDB.getQuestDB();
            Optional<QuestChainBuildResult> quest = questDatabase.buildQuest(definition, pos);
            if (quest.isPresent()) {
                QuestChainInstance newQuest = quest.get().instance;
                player.sendSystemMessage(Component.literal("Generated quest: " + newQuest.getQuestId()));
                return Command.SINGLE_SUCCESS;
            }
            player.sendSystemMessage(Component.literal("Failed to generate quest"));
        } else {
            player.sendSystemMessage(Component.literal("Definition not found."));
        }
        return Command.SINGLE_SUCCESS;
    }
}
