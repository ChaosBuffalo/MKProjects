package com.chaosbuffalo.mknpc.event;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.block_entities.MKSpawnerBlockEntity;
import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.capabilities.WorldStructureManager;
import com.chaosbuffalo.mknpc.content.ContentDB;
import com.chaosbuffalo.mknpc.world.gen.StructureStartExtension;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = MKNpc.MODID)
public class WorldStructureHandler {
    public static final Map<ResourceLocation, MKStructure> MK_STRUCTURE_INDEX = new HashMap<>();

    @SubscribeEvent
    public static void serverStarted(final ServerStartedEvent event) {
        WorldStructureHandler.cacheStructures(event.getServer());
    }

    @SubscribeEvent
    public static void onWorldTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            IWorldNpcData over = ContentDB.getPrimaryData();
            StructureManager levelStructures = serverLevel.structureManager();
            WorldStructureManager activeStructures = over.getStructureManager();
            for (ServerPlayer player : serverLevel.players()) {
                BlockPos playerPos = player.blockPosition();
                if (!levelStructures.hasAnyStructureAt(playerPos))
                    continue;

                List<StructureStart> starts = MK_STRUCTURE_INDEX.values().stream()
                        .map(x -> levelStructures.getStructureAt(playerPos, x))
                        .filter(StructureStart::isValid)
                        .toList();
                for (StructureStart start : starts) {
                    over.setupStructureDataIfAbsent(start, serverLevel);
                    activeStructures.visitStructure(StructureStartExtension.getInstanceId(start), player);
                }
            }
            if (serverLevel.dimension() == Level.OVERWORLD) {
                over.update();
            }
        }
    }

    public static void cacheStructures(MinecraftServer server) {
        server.registryAccess().registry(Registries.STRUCTURE).ifPresent(registry -> {
            MK_STRUCTURE_INDEX.clear();
            registry.holders().filter(r -> r.value() instanceof MKStructure).forEach(r -> {
                MKNpc.LOGGER.info("Caching MK Structure {}", r.key().location());
                MK_STRUCTURE_INDEX.put(r.key().location(), (MKStructure) r.value());
            });
        });
    }
}
