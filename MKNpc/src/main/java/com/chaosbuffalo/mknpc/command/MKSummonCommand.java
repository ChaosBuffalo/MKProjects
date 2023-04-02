package com.chaosbuffalo.mknpc.command;

import com.chaosbuffalo.mknpc.npc.NpcDefinitionManager;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;

import java.util.concurrent.CompletableFuture;

public class MKSummonCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("mksummon")
                .then(Commands.argument("npc_definition", NpcDefinitionIdArgument.definition())
                        .suggests(MKSummonCommand::suggestNpcDefinitions)
                        .then(Commands.argument("difficulty_value", DoubleArgumentType.doubleArg(0.0, 200.0))
                        .executes(MKSummonCommand::summon)));
    }

    static CompletableFuture<Suggestions> suggestNpcDefinitions(final CommandContext<CommandSourceStack> context,
                                                                final SuggestionsBuilder builder) throws CommandSyntaxException {
        return SharedSuggestionProvider.suggest(NpcDefinitionManager.DEFINITIONS.keySet().stream()
                .map(ResourceLocation::toString), builder);
    }

    static int summon(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ResourceLocation definition_id = ctx.getArgument("npc_definition", ResourceLocation.class);
        double difficulty_value = DoubleArgumentType.getDouble(ctx, "difficulty_value");
        NpcDefinition definition = NpcDefinitionManager.getDefinition(definition_id);
        if (definition != null){
            Entity entity = definition.createEntity(player.getLevel(), player.position(), difficulty_value);
            if (entity != null){
                player.getLevel().addFreshEntity(entity);
                if (entity instanceof Mob){
                    ((Mob) entity).finalizeSpawn(player.getLevel(), player.getLevel().getCurrentDifficultyAt(
                            new BlockPos(entity.position())), MobSpawnType.COMMAND, null, null);
                }
            } else {
                player.sendMessage(new TextComponent(String.format("Failed to summon: %s",
                        definition_id.toString())), Util.NIL_UUID);
            }
        } else {
            player.sendMessage(new TextComponent("Definition not found."), Util.NIL_UUID);
        }
        return Command.SINGLE_SUCCESS;
    }
}
