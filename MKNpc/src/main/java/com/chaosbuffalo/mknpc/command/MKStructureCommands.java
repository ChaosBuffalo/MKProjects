package com.chaosbuffalo.mknpc.command;

import com.chaosbuffalo.mknpc.ContentDB;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.capabilities.NpcCapabilities;
import com.chaosbuffalo.mknpc.capabilities.PointOfInterestEntry;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.world.gen.IStructureStartMixin;
import com.chaosbuffalo.mknpc.world.gen.StructureUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class MKStructureCommands {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("mkstruct")
                .then(Commands.literal("list").executes(MKStructureCommands::listStructures))
                .then(Commands.literal("pois").executes(MKStructureCommands::listPoiForStruct))
                .then(Commands.literal("reset").executes(MKStructureCommands::resetStructures));


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
            starts.forEach(start -> {
                ResourceLocation featureName = ctx.getSource().registryAccess().registryOrThrow(Registries.STRUCTURE).getKey(start.getStructure());
                player.sendSystemMessage(Component.translatable("mknpc.command.in_struct",
                        featureName, IStructureStartMixin.getInstanceIdFromStart(start)));
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
            starts.forEach(start -> {
                ResourceLocation featureName = ctx.getSource().registryAccess().registryOrThrow(Registries.STRUCTURE)
                        .getKey(start.getStructure());
                UUID instanceId = IStructureStartMixin.getInstanceIdFromStart(start);
                ContentDB.getPrimaryData().getStructureData(instanceId).ifPresent(MKStructureEntry::reset);
                player.sendSystemMessage(Component.translatable("mknpc.command.reset_struct",
                        featureName, instanceId));
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
            starts.forEach(start -> {
                UUID startId = IStructureStartMixin.getInstanceIdFromStart(start);
                Optional<MKStructureEntry> entry = ContentDB.getPrimaryData().getStructureData(startId);
                ResourceLocation featureName = ctx.getSource().registryAccess().registryOrThrow(Registries.STRUCTURE).getKey(start.getStructure());
                if (entry.isPresent()) {
                    Map<String, List<PointOfInterestEntry>> pois = entry.get().getPointsOfInterest();
                    if (pois.entrySet().stream().allMatch(m -> m.getValue().isEmpty())) {
                        player.sendSystemMessage(Component.translatable(
                                "mknpc.command.pois_struct_no_poi",
                                featureName, startId));
                    } else {
                        player.sendSystemMessage(Component.translatable("mknpc.command.pois_for_struct",
                                featureName, startId));
                        pois.forEach((key, value) -> value.forEach(
                                poi -> player.sendSystemMessage(Component.translatable(
                                        "mknpc.command.pois_struct_desc",
                                        key, poi.getLocation().toString()))));
                    }
                } else {
                    player.sendSystemMessage(Component.translatable(
                            "mknpc.command.pois_struct_not_found",
                            featureName, startId));
                }
            });
        }

        return Command.SINGLE_SUCCESS;
    }
}
