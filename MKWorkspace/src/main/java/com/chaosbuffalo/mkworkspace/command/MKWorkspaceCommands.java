package com.chaosbuffalo.mkworkspace.command;

import com.chaosbuffalo.mkworkspace.world.gen.workspace.MKStructureWorkspaceService;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.capability.IMKStructureWorkspaceData;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.export.MKWorkspaceBackupManifestDiscovery;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.export.MKWorkspaceBackupRestoreService;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.mutation.MKWorkspacePieceRelayoutService;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.mutation.MKStructureWorkspaceMutationService;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.util.Map;

public class MKWorkspaceCommands {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("mkworkspace")
                .then(Commands.literal("list").executes(MKWorkspaceCommands::listWorkspaces))
                .then(Commands.literal("open").executes(MKWorkspaceCommands::openWorkspaceAtPlayer))
                .then(Commands.literal("backups")
                        .then(Commands.literal("list").executes(MKWorkspaceCommands::listBackupsAtNearestWorkspace))
                        .then(Commands.literal("restorelatest")
                                .executes(MKWorkspaceCommands::restoreLatestBackupAtNearestWorkspace))
                        .then(Commands.literal("restore")
                                .then(Commands.argument("fileName", StringArgumentType.word())
                                        .executes(MKWorkspaceCommands::restoreSelectedBackupAtNearestWorkspace))))
                .then(Commands.literal("regenerate").executes(MKWorkspaceCommands::regenerateAtPlayer))
                .then(Commands.literal("setpreviewmargin")
                        .then(Commands.argument("value", IntegerArgumentType.integer(2))
                                .executes(MKWorkspaceCommands::setPreviewMarginAtNearestWorkspace)))
                .then(Commands.literal("swapblock")
                        .then(Commands.argument("source", ResourceLocationArgument.id())
                                .then(Commands.argument("target", ResourceLocationArgument.id())
                                        .executes(MKWorkspaceCommands::swapBlockAtNearestWorkspace))));
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

    private static int openWorkspaceAtPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (!player.isCreative()) {
            player.sendSystemMessage(Component.literal("Only creative players can open workspace editors."));
            return Command.SINGLE_SUCCESS;
        }
        boolean opened = new MKStructureWorkspaceService().openWorkspaceScreenAtPlayer(player);
        if (!opened) {
            player.sendSystemMessage(Component.literal("No workspace found at your current position."));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int regenerateAtPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        MKStructureWorkspace nearest = getNearestWorkspace(player);
        if (nearest == null) {
            player.sendSystemMessage(Component.literal("No structure workspaces to regenerate."));
            return Command.SINGLE_SUCCESS;
        }
        boolean regenerated = new MKStructureWorkspaceService().generateWorkspace(player.serverLevel(), nearest.anchor()).isPresent();
        player.sendSystemMessage(Component.literal(regenerated ?
                "Regenerated workspace at " + nearest.anchor() :
                "Failed to regenerate workspace at " + nearest.anchor()));
        return Command.SINGLE_SUCCESS;
    }

    private static int listBackupsAtNearestWorkspace(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        MKStructureWorkspace nearest = getNearestWorkspace(player);
        if (nearest == null) {
            player.sendSystemMessage(Component.literal("No structure workspaces to inspect."));
            return Command.SINGLE_SUCCESS;
        }
        var backups = new MKWorkspaceBackupManifestDiscovery().discoverBackups(player.server, nearest);
        if (backups.isEmpty()) {
            player.sendSystemMessage(Component.literal("No backups found for " +
                    nearest.namespace() + ":" + nearest.structureName()));
            return Command.SINGLE_SUCCESS;
        }
        player.sendSystemMessage(Component.literal("Backups for " + nearest.namespace() + ":" +
                nearest.structureName() + ":"));
        for (int i = 0; i < Math.min(backups.size(), 10); i++) {
            MKWorkspaceBackupManifestDiscovery.BackupCandidate backup = backups.get(i);
            player.sendSystemMessage(Component.literal(String.format("[%d] %s pieces=%d schema=%d file=%s",
                    i + 1,
                    backup.operation(),
                    backup.pieceCount(),
                    backup.schemaVersion(),
                    backup.fileName())));
        }
        if (backups.size() > 10) {
            player.sendSystemMessage(Component.literal("Showing 10 of " + backups.size() + " backups."));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int restoreLatestBackupAtNearestWorkspace(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        return restoreBackupAtNearestWorkspace(context, null);
    }

    private static int restoreSelectedBackupAtNearestWorkspace(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        return restoreBackupAtNearestWorkspace(context, StringArgumentType.getString(context, "fileName"));
    }

    private static int restoreBackupAtNearestWorkspace(CommandContext<CommandSourceStack> context, String fileName)
            throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (!player.isCreative()) {
            player.sendSystemMessage(Component.literal("Only creative players can restore workspace backups."));
            return Command.SINGLE_SUCCESS;
        }
        MKStructureWorkspace nearest = getNearestWorkspace(player);
        if (nearest == null) {
            player.sendSystemMessage(Component.literal("No structure workspaces to restore."));
            return Command.SINGLE_SUCCESS;
        }
        try {
            MKWorkspaceBackupRestoreService restoreService = new MKWorkspaceBackupRestoreService();
            MKWorkspaceBackupRestoreService.RestoreResult result = fileName == null ?
                    restoreService.restoreLatest(player.serverLevel(), nearest) :
                    restoreService.restoreByFileName(player.serverLevel(), nearest, fileName);
            if (!result.validationErrors().isEmpty()) {
                player.sendSystemMessage(Component.literal("Backup restore failed validation: " +
                        String.join("; ", result.validationErrors())));
                result.beforeRestoreBackupPathOpt().ifPresent(path ->
                        player.sendSystemMessage(Component.literal("Before-restore backup manifest: " + path)));
                return Command.SINGLE_SUCCESS;
            }
            if (result.workspaceOpt().isEmpty()) {
                player.sendSystemMessage(Component.literal(fileName == null ?
                        "No restorable backups found for " + nearest.namespace() + ":" + nearest.structureName() :
                        "Backup file not found for " + nearest.namespace() + ":" + nearest.structureName() + ": " + fileName));
                return Command.SINGLE_SUCCESS;
            }
            player.sendSystemMessage(Component.literal("Restored live workspace metadata/layout from " +
                    result.selectedBackupPathOpt().map(path -> path.getFileName().toString()).orElse("backup")));
            result.beforeRestoreBackupPathOpt().ifPresent(path ->
                    player.sendSystemMessage(Component.literal("Before-restore backup manifest: " + path)));
            player.sendSystemMessage(Component.literal("World blocks were not rewritten; export later when the live workspace is correct."));
            return Command.SINGLE_SUCCESS;
        } catch (IOException e) {
            player.sendSystemMessage(Component.literal("Backup restore failed while writing backup manifest: " + e.getMessage()));
            return Command.SINGLE_SUCCESS;
        }
    }

    private static int setPreviewMarginAtNearestWorkspace(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (!player.isCreative()) {
            player.sendSystemMessage(Component.literal("Only creative players can relayout workspaces."));
            return Command.SINGLE_SUCCESS;
        }
        MKStructureWorkspace nearest = getNearestWorkspace(player);
        if (nearest == null) {
            player.sendSystemMessage(Component.literal("No structure workspaces to relayout."));
            return Command.SINGLE_SUCCESS;
        }
        int previewMargin = IntegerArgumentType.getInteger(context, "value");
        try {
            var resultOpt = new MKWorkspacePieceRelayoutService().relayoutPreviewMargin(
                    player.serverLevel(), nearest, previewMargin);
            if (resultOpt.isEmpty()) {
                player.sendSystemMessage(Component.literal("Preview margin relayout was not applicable."));
                return Command.SINGLE_SUCCESS;
            }
            MKWorkspacePieceRelayoutService.RelayoutResult result = resultOpt.get();
            player.sendSystemMessage(Component.literal("Relayouted " + result.movedPieceCount() +
                    " pieces for preview margin " + previewMargin + "."));
            player.sendSystemMessage(Component.literal("Moved " + result.movedBlockCount() +
                    " workspace positions. Backup manifest: " + result.backupPath()));
            return Command.SINGLE_SUCCESS;
        } catch (IOException e) {
            player.sendSystemMessage(Component.literal("Preview margin relayout failed while writing backup manifest: " +
                    e.getMessage()));
            return Command.SINGLE_SUCCESS;
        } catch (IllegalStateException e) {
            player.sendSystemMessage(Component.literal("Preview margin relayout failed: " + e.getMessage()));
            return Command.SINGLE_SUCCESS;
        }
    }

    private static int swapBlockAtNearestWorkspace(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (!player.isCreative()) {
            player.sendSystemMessage(Component.literal("Only creative players can mutate workspaces."));
            return Command.SINGLE_SUCCESS;
        }
        ResourceLocation source = ResourceLocationArgument.getId(context, "source");
        ResourceLocation target = ResourceLocationArgument.getId(context, "target");
        if (BuiltInRegistries.BLOCK.getOptional(source).isEmpty()) {
            player.sendSystemMessage(Component.literal("Unknown source block: " + source));
            return Command.SINGLE_SUCCESS;
        }
        if (BuiltInRegistries.BLOCK.getOptional(target).isEmpty()) {
            player.sendSystemMessage(Component.literal("Unknown target block: " + target));
            return Command.SINGLE_SUCCESS;
        }
        MKStructureWorkspace nearest = getNearestWorkspace(player);
        if (nearest == null) {
            player.sendSystemMessage(Component.literal("No structure workspaces to mutate."));
            return Command.SINGLE_SUCCESS;
        }
        try {
            MKStructureWorkspaceMutationService.WorkspaceBlockSwapResult result =
                    new MKStructureWorkspaceMutationService().swapBlocks(
                            player.serverLevel(), nearest, Map.of(source, target));
            player.sendSystemMessage(Component.literal("Swapped " + result.replacedCount() + " blocks across " +
                    result.pieceCount() + " pieces in " + nearest.namespace() + ":" + nearest.structureName()));
            player.sendSystemMessage(Component.literal("Backup manifest: " + result.backupPath()));
            return Command.SINGLE_SUCCESS;
        } catch (IOException e) {
            player.sendSystemMessage(Component.literal("Block swap failed while writing backup manifest: " + e.getMessage()));
            return Command.SINGLE_SUCCESS;
        }
    }

    private static MKStructureWorkspace getNearestWorkspace(ServerPlayer player) {
        BlockPos playerPos = player.blockPosition();
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(player.serverLevel());
        return data.getAllWorkspaces().stream()
                .min((left, right) -> Integer.compare(
                        left.anchor().distManhattan(playerPos),
                        right.anchor().distManhattan(playerPos)))
                .orElse(null);
    }
}
