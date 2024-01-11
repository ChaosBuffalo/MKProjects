package com.chaosbuffalo.mknpc.command;

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
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

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
                        .executes(MKStructureCommands::resetStructures));
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
                player.sendSystemMessage(Component.translatable("mknpc.command.in_struct",
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
                player.sendSystemMessage(Component.translatable("mknpc.command.reset_struct",
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
                        player.sendSystemMessage(Component.translatable(
                                "mknpc.command.pois_struct_no_poi",
                                structureId, startId));
                    } else {
                        player.sendSystemMessage(Component.translatable("mknpc.command.pois_for_struct",
                                structureId, startId));
                        pois.forEach((key, value) -> value.forEach(
                                poi -> player.sendSystemMessage(Component.translatable(
                                        "mknpc.command.pois_struct_desc",
                                        key, poi.getLocation().toString()))));
                    }
                } else {
                    player.sendSystemMessage(Component.translatable(
                            "mknpc.command.pois_struct_not_found",
                            structureId, startId));
                }
            });
        }

        return Command.SINGLE_SUCCESS;
    }
}
