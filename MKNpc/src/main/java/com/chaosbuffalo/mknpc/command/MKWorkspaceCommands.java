package com.chaosbuffalo.mknpc.command;

import com.chaosbuffalo.mknpc.world.gen.workspace.MKStructureWorkspaceService;
import com.chaosbuffalo.mknpc.world.gen.workspace.capability.IMKStructureWorkspaceData;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class MKWorkspaceCommands {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("mkworkspace")
                .then(Commands.literal("list").executes(MKWorkspaceCommands::listWorkspaces))
                .then(Commands.literal("regenerate").executes(MKWorkspaceCommands::regenerateAtPlayer));
    }

    private static int listWorkspaces(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(player.serverLevel());
        if (data.getAllWorkspaces().isEmpty()) {
            player.sendSystemMessage(Component.literal("No structure workspaces in this level."));
            return Command.SINGLE_SUCCESS;
        }
        for (MKStructureWorkspace workspace : data.getAllWorkspaces()) {
            player.sendSystemMessage(Component.literal(String.format("%s:%s @ %s",
                    workspace.namespace(), workspace.structureName(), workspace.anchor())));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int regenerateAtPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        BlockPos playerPos = player.blockPosition();
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(player.serverLevel());
        MKStructureWorkspace nearest = data.getAllWorkspaces().stream()
                .min((left, right) -> Integer.compare(
                        left.anchor().distManhattan(playerPos),
                        right.anchor().distManhattan(playerPos)))
                .orElse(null);
        if (nearest == null) {
            player.sendSystemMessage(Component.literal("No structure workspaces to regenerate."));
            return Command.SINGLE_SUCCESS;
        }
        boolean regenerated = new MKStructureWorkspaceService().generateTowerWorkspace(player.serverLevel(), nearest.anchor()).isPresent();
        player.sendSystemMessage(Component.literal(regenerated ?
                "Regenerated workspace at " + nearest.anchor() :
                "Failed to regenerate workspace at " + nearest.anchor()));
        return Command.SINGLE_SUCCESS;
    }
}
