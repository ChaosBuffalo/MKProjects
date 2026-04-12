package com.chaosbuffalo.mknpc.command;

import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcRegistries;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.neoforge.event.EventHooks;

public class MKSummonCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("mksummon")
                .then(Commands.argument("npc_definition", NpcDefinitionIdArgument.definition())
                        .then(Commands.argument("difficulty_value", DoubleArgumentType.doubleArg(0.0, 200.0))
                                .executes(MKSummonCommand::summon)));
    }

    static int summon(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ResourceKey<NpcDefinition> definitionId = NpcDefinitionIdArgument.get(ctx, "npc_definition");
        double difficulty_value = DoubleArgumentType.getDouble(ctx, "difficulty_value");
        NpcDefinition definition = ctx.getSource().registryAccess().registryOrThrow(NpcRegistries.NPC_DEFINITIONS).get(definitionId);
        if (definition != null) {
            Entity entity = definition.createEntity(player.level(), player.position(), difficulty_value);
            if (entity != null) {
                if (entity instanceof Mob mob && player.level() instanceof ServerLevel serverLevel) {
                    EventHooks.finalizeMobSpawn(mob, serverLevel,
                            serverLevel.getCurrentDifficultyAt(player.blockPosition()),
                            MobSpawnType.COMMAND, null);
                }
                player.level().addFreshEntity(entity);
            } else {
                player.sendSystemMessage(Component.literal(String.format("Failed to summon: %s", definitionId)));
            }
        } else {
            player.sendSystemMessage(Component.literal("Definition not found."));
        }
        return Command.SINGLE_SUCCESS;
    }
}
