package com.chaosbuffalo.mknpc.command;

import com.chaosbuffalo.mkcore.utils.ChatUtils;
import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.capabilities.PointOfInterestEntry;
import com.chaosbuffalo.mknpc.content.ContentDB;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.world.gen.StructureStartExtension;
import com.chaosbuffalo.mknpc.world.gen.StructureUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.LevelResource;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class MKStructureCommands {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("mkstruct")
                .then(Commands.literal("list")
                        .executes(MKStructureCommands::listStructures))
                .then(Commands.literal("pois")
                        .executes(MKStructureCommands::listPoiForStruct))
                .then(Commands.literal("reset")
                        .executes(MKStructureCommands::resetStructures))
                .then(Commands.literal("dump")
                        .then(Commands.argument("template", ResourceLocationArgument.id())
                                .suggests((ctx, builder) ->
                                        SharedSuggestionProvider.suggest(listTemplateIds(ctx.getSource()), builder))
                                .executes(MKStructureCommands::dumpStructure)))
                .then(Commands.literal("import")
                        .then(Commands.argument("template", ResourceLocationArgument.id())
                                .suggests((ctx, builder) ->
                                        SharedSuggestionProvider.suggest(listSnbtFiles(ctx.getSource()), builder))
                                .executes(MKStructureCommands::importStructure)))
                .then(Commands.literal("tonbt")
                        .then(Commands.argument("template", ResourceLocationArgument.id())
                                .suggests((ctx, builder) ->
                                        SharedSuggestionProvider.suggest(listSnbtFiles(ctx.getSource()), builder))
                                .executes(MKStructureCommands::convertSnbtToNbt)));
    }

    private static Stream<String> listTemplateIds(CommandSourceStack source) {
        return source.getServer().getStructureManager().listTemplates()
                .map(ResourceLocation::toString)
                .sorted();
    }

    static int listStructures(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        MinecraftServer server = player.getServer();
        if (server == null) {
            return Command.SINGLE_SUCCESS;
        }

        List<StructureStart> starts = StructureUtils.getStructuresOverlaps(player);
        if (starts.isEmpty()) {
            player.sendSystemMessage(Component.translatable("mknpc.command.not_in_struct"));
        } else {
            Registry<Structure> registry = ctx.getSource().registryAccess().registryOrThrow(Registries.STRUCTURE);
            starts.forEach(start -> {
                ResourceLocation structureId = registry.getKey(start.getStructure());
                player.sendSystemMessage(ChatUtils.translatable("mknpc.command.in_struct",
                        structureId, StructureStartExtension.getInstanceId(start)));
            });
        }

        return Command.SINGLE_SUCCESS;
    }

    static int resetStructures(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        MinecraftServer server = player.getServer();
        if (server == null) {
            return Command.SINGLE_SUCCESS;
        }

        List<StructureStart> starts = StructureUtils.getStructuresOverlaps(player);
        if (starts.isEmpty()) {
            player.sendSystemMessage(Component.translatable("mknpc.command.not_in_struct"));
        } else {
            Registry<Structure> registry = ctx.getSource().registryAccess().registryOrThrow(Registries.STRUCTURE);
            starts.forEach(start -> {
                ResourceLocation structureId = registry.getKey(start.getStructure());
                UUID instanceId = StructureStartExtension.getInstanceId(start);

                ContentDB.getPrimaryData().getStructureData(instanceId).ifPresent(MKStructureEntry::reset);
                player.sendSystemMessage(ChatUtils.translatable("mknpc.command.reset_struct",
                        structureId, StructureStartExtension.getInstanceId(start)));
            });
        }
        return Command.SINGLE_SUCCESS;
    }

    static int listPoiForStruct(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        MinecraftServer server = player.getServer();
        if (server == null) {
            return Command.SINGLE_SUCCESS;
        }

        List<StructureStart> starts = StructureUtils.getStructuresOverlaps(player);
        if (starts.isEmpty()) {
            player.sendSystemMessage(Component.translatable("mknpc.command.not_in_struct"));
        } else {
            IWorldNpcData cap = ContentDB.getPrimaryData();
            Registry<Structure> registry = ctx.getSource().registryAccess().registryOrThrow(Registries.STRUCTURE);
            starts.forEach(start -> {
                UUID startId = StructureStartExtension.getInstanceId(start);
                Optional<MKStructureEntry> entry = cap.getStructureData(startId);
                ResourceLocation structureId = registry.getKey(start.getStructure());
                if (entry.isPresent()) {
                    Map<String, List<PointOfInterestEntry>> pois = entry.get().getPointsOfInterest();
                    if (pois.entrySet().stream().allMatch(m -> m.getValue().isEmpty())) {
                        player.sendSystemMessage(ChatUtils.translatable(
                                "mknpc.command.pois_struct_no_poi",
                                structureId, startId));
                    } else {
                        player.sendSystemMessage(ChatUtils.translatable("mknpc.command.pois_for_struct",
                                structureId, startId));
                        pois.forEach((key, value) -> value.forEach(
                                poi -> player.sendSystemMessage(Component.translatable(
                                        "mknpc.command.pois_struct_desc",
                                        key, poi.getLocation().toString()))));
                    }
                } else {
                    player.sendSystemMessage(ChatUtils.translatable(
                            "mknpc.command.pois_struct_not_found",
                            structureId, startId));
                }
            });
        }

        return Command.SINGLE_SUCCESS;
    }

    private static Stream<String> listSnbtFiles(CommandSourceStack source) {
        Path debugDir = source.getServer().getWorldPath(LevelResource.GENERATED_DIR)
                .resolve("debug_structures");
        if (!Files.isDirectory(debugDir)) {
            return Stream.empty();
        }
        try {
            return Files.walk(debugDir, 2)
                    .filter(p -> p.toString().endsWith(".snbt"))
                    .map(p -> {
                        Path rel = debugDir.relativize(p);
                        String namespace = rel.getName(0).toString();
                        String name = rel.getName(1).toString().replace(".snbt", "");
                        return namespace + ":" + name;
                    });
        } catch (IOException e) {
            return Stream.empty();
        }
    }

    static int importStructure(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        MinecraftServer server = player.getServer();
        if (server == null) {
            return Command.SINGLE_SUCCESS;
        }

        ResourceLocation templateId = ResourceLocationArgument.getId(ctx, "template");
        Path inFile = server.getWorldPath(LevelResource.GENERATED_DIR)
                .resolve("debug_structures")
                .resolve(templateId.getNamespace())
                .resolve(templateId.getPath().replace('/', '_') + ".snbt");

        if (!Files.exists(inFile)) {
            player.sendSystemMessage(Component.literal("SNBT file not found: " + inFile));
            return Command.SINGLE_SUCCESS;
        }

        try {
            String snbt = Files.readString(inFile);
            CompoundTag tag = NbtUtils.snbtToStructure(snbt);

            StructureTemplate template = new StructureTemplate();
            template.load(server.registryAccess().lookupOrThrow(Registries.BLOCK), tag);

            BlockPos pos = player.blockPosition();
            StructurePlaceSettings settings = new StructurePlaceSettings();
            template.placeInWorld(player.serverLevel(), pos, pos, settings,
                    player.serverLevel().getRandom(), Block.UPDATE_ALL);

            player.sendSystemMessage(Component.literal(
                    "Placed " + templateId + " at " + pos.toShortString()));
        } catch (IOException e) {
            player.sendSystemMessage(Component.literal("Failed to read SNBT: " + e.getMessage()));
        } catch (CommandSyntaxException e) {
            player.sendSystemMessage(Component.literal("Failed to parse SNBT: " + e.getMessage()));
        }

        return Command.SINGLE_SUCCESS;
    }

    static int convertSnbtToNbt(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        MinecraftServer server = player.getServer();
        if (server == null) {
            return Command.SINGLE_SUCCESS;
        }

        ResourceLocation templateId = ResourceLocationArgument.getId(ctx, "template");
        Path dir = server.getWorldPath(LevelResource.GENERATED_DIR)
                .resolve("debug_structures")
                .resolve(templateId.getNamespace());
        String baseName = templateId.getPath().replace('/', '_');
        Path snbtFile = dir.resolve(baseName + ".snbt");
        Path nbtFile  = dir.resolve(baseName + ".nbt");

        if (!Files.exists(snbtFile)) {
            player.sendSystemMessage(Component.literal("SNBT file not found: " + snbtFile));
            return Command.SINGLE_SUCCESS;
        }

        try {
            String snbt = Files.readString(snbtFile);
            CompoundTag tag = NbtUtils.snbtToStructure(snbt);
            NbtIo.writeCompressed(tag, nbtFile);
            player.sendSystemMessage(Component.literal("Converted " + templateId + " → " + nbtFile));
        } catch (IOException e) {
            player.sendSystemMessage(Component.literal("Failed to convert SNBT: " + e.getMessage()));
        } catch (CommandSyntaxException e) {
            player.sendSystemMessage(Component.literal("Failed to parse SNBT: " + e.getMessage()));
        }

        return Command.SINGLE_SUCCESS;
    }

    static int dumpStructure(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        MinecraftServer server = player.getServer();
        if (server == null) {
            return Command.SINGLE_SUCCESS;
        }

        ResourceLocation templateId = ResourceLocationArgument.getId(ctx, "template");
        Optional<StructureTemplate> templateOpt = server.getStructureManager().get(templateId);
        if (templateOpt.isEmpty()) {
            player.sendSystemMessage(Component.literal("Structure template not found: " + templateId));
            return Command.SINGLE_SUCCESS;
        }

        CompoundTag tag = templateOpt.get().save(new CompoundTag());
        String snbt = NbtUtils.structureToSnbt(tag);

        Path outDir = server.getWorldPath(LevelResource.GENERATED_DIR)
                .resolve("debug_structures")
                .resolve(templateId.getNamespace());
        Path outFile = outDir.resolve(templateId.getPath().replace('/', '_') + ".snbt");

        try {
            Files.createDirectories(outDir);
            try (BufferedWriter writer = Files.newBufferedWriter(outFile)) {
                writer.write(snbt);
            }
            player.sendSystemMessage(Component.literal("Dumped " + templateId + " to " + outFile));
        } catch (IOException e) {
            player.sendSystemMessage(Component.literal("Failed to write SNBT: " + e.getMessage()));
        }

        return Command.SINGLE_SUCCESS;
    }
}
