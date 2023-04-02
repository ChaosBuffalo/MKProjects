package com.chaosbuffalo.mknpc.command;

import com.chaosbuffalo.mknpc.capabilities.NpcCapabilities;
import com.chaosbuffalo.mknpc.capabilities.PointOfInterestEntry;
import com.chaosbuffalo.mknpc.world.gen.IStructureStartMixin;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.world.gen.StructureUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.Util;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.registries.ForgeRegistries;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class MKStructureCommands {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("mkstruct")
                .then(Commands.literal("list").executes(MKStructureCommands::listStructures))
                .then(Commands.literal("pois").executes(MKStructureCommands::listPoiForStruct));

    }

    static int listStructures(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        MinecraftServer server = player.getServer();
        if (server != null){

            Optional<List<StructureStart>> starts = StructureUtils.getStructuresOverlaps(player);
            if (starts.isPresent()) {
                List<StructureStart> s = starts.get();
                if (s.isEmpty()) {
                    player.sendMessage(new TranslatableComponent("mknpc.command.not_in_struct"), Util.NIL_UUID);
                } else {
                    s.forEach(start -> {
                        ResourceLocation featureName = ForgeRegistries.STRUCTURE_FEATURES.getKey(start.getFeature().feature);
                        player.sendMessage(new TranslatableComponent("mknpc.command.in_struct",
                                featureName, IStructureStartMixin.getInstanceIdFromStart(start)), Util.NIL_UUID);
                    });
                }

            } else {
                player.sendMessage(new TranslatableComponent("mknpc.command.not_in_struct"), Util.NIL_UUID);
            }





        }

        return Command.SINGLE_SUCCESS;
    }

    static int listPoiForStruct(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        MinecraftServer server = player.getServer();
        if (server != null){

            Optional<List<StructureStart>> starts = StructureUtils.getStructuresOverlaps(player);
            if (starts.isPresent()) {
                List<StructureStart> s = starts.get();
                if (s.isEmpty()) {
                    player.sendMessage(new TranslatableComponent("mknpc.command.not_in_struct"), Util.NIL_UUID);
                } else {
                    Level overworld = server.getLevel(Level.OVERWORLD);
                    if (overworld != null){
                        overworld.getCapability(NpcCapabilities.WORLD_NPC_DATA_CAPABILITY)
                                .ifPresent(cap ->  {
                                    s.forEach(start -> {
                                        UUID startId = IStructureStartMixin.getInstanceIdFromStart(start);
                                        MKStructureEntry entry = cap.getStructureData(startId);
                                        ResourceLocation featureName = ForgeRegistries.STRUCTURE_FEATURES.getKey(start.getFeature().feature);
                                        if (entry != null) {
                                            Map<String, List<PointOfInterestEntry>> pois = entry.getPointsOfInterest();
                                            if (pois.entrySet().stream().allMatch(m -> m.getValue().isEmpty())) {
                                                player.sendMessage(new TranslatableComponent(
                                                        "mknpc.command.pois_struct_no_poi",
                                                        featureName, startId), Util.NIL_UUID);
                                            } else {
                                                player.sendMessage(new TranslatableComponent("mknpc.command.pois_for_struct",
                                                        featureName, startId), Util.NIL_UUID);
                                                pois.forEach((key, value) -> value.forEach(
                                                        poi -> player.sendMessage(new TranslatableComponent(
                                                                "mknpc.command.pois_struct_desc",
                                                                key, poi.getLocation().toString()), Util.NIL_UUID)));
                                            }
                                        } else {
                                            player.sendMessage(new TranslatableComponent(
                                                    "mknpc.command.pois_struct_not_found",
                                                    featureName, startId), Util.NIL_UUID);
                                        }
                                    });
                                });
                    } else {
                        player.sendMessage(new TranslatableComponent("mknpc.command.cant_find_cap"), Util.NIL_UUID);
                    }

                }

            } else {
                player.sendMessage(new TranslatableComponent("mknpc.command.not_in_struct"), Util.NIL_UUID);
            }


        }

        return Command.SINGLE_SUCCESS;
    }

}
