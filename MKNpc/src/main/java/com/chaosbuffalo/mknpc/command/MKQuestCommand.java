package com.chaosbuffalo.mknpc.command;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.capabilities.NpcCapabilities;
import com.chaosbuffalo.mknpc.quest.QuestChainInstance;
import com.chaosbuffalo.mknpc.quest.QuestDefinition;
import com.chaosbuffalo.mknpc.quest.QuestDefinitionManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MKQuestCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("mkquest")
                .then(Commands.literal("gen")
                        .then(Commands.argument("quest", QuestDefinitionIdArgument.definition())
                        .suggests(MKQuestCommand::suggestQuestDefinitions)
                        .executes(MKQuestCommand::generateQuest)))
                .then(Commands.literal("start")
                    .then(Commands.argument("id", StringArgumentType.string())
                    .executes(MKQuestCommand::startQuest)));
    }

    static CompletableFuture<Suggestions> suggestQuestDefinitions(final CommandContext<CommandSourceStack> context,
                                                                  final SuggestionsBuilder builder) throws CommandSyntaxException {
        return SharedSuggestionProvider.suggest(QuestDefinitionManager.DEFINITIONS.keySet().stream()
                .map(ResourceLocation::toString), builder);
    }

    static int startQuest(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String questIdStr = StringArgumentType.getString(ctx, "id");
        UUID questId = UUID.fromString(questIdStr);
        MinecraftServer server = player.getServer();
        if (server != null){
            Level world = server.getLevel(Level.OVERWORLD);
            if (world != null) {
                LazyOptional<IWorldNpcData> worldL = world.getCapability(NpcCapabilities.WORLD_NPC_DATA_CAPABILITY);
                Optional<IWorldNpcData> wrldOpt = worldL.resolve();
                if (wrldOpt.isPresent()){
                    IWorldNpcData worldData = wrldOpt.get();
                    MKNpc.getPlayerQuestData(player).ifPresent(x -> {
                        x.startQuest(worldData, questId);
                    });

                }


            }
        }

        return Command.SINGLE_SUCCESS;
    }

    static int generateQuest(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ResourceLocation definition_id = ctx.getArgument("quest", ResourceLocation.class);
        QuestDefinition definition = QuestDefinitionManager.getDefinition(definition_id);
        BlockPos pos = new BlockPos(player.position());
        if (definition != null){
            MinecraftServer server = player.getServer();
            if (server != null){
                Level world = server.getLevel(Level.OVERWORLD);
                if (world != null){
                    Optional<QuestChainInstance.QuestChainBuildResult> quest = world.getCapability(NpcCapabilities.WORLD_NPC_DATA_CAPABILITY)
                            .map(x -> x.buildQuest(definition, pos)).orElse(Optional.empty());
                    if (quest.isPresent()){
                        QuestChainInstance newQuest = quest.get().instance;
                        player.sendMessage(new TextComponent(String.format("Generated quest: %s", newQuest.getQuestId().toString())), Util.NIL_UUID);
                        return Command.SINGLE_SUCCESS;
                    }
                }
            }
            player.sendMessage(new TextComponent("Failed to generate quest"), Util.NIL_UUID);
        } else {
            player.sendMessage(new TextComponent("Definition not found."), Util.NIL_UUID);
        }
        return Command.SINGLE_SUCCESS;
    }
}
