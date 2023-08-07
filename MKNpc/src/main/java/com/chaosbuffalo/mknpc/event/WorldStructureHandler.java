package com.chaosbuffalo.mknpc.event;

import com.chaosbuffalo.mknpc.ContentDB;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.capabilities.WorldStructureManager;
import com.chaosbuffalo.mknpc.world.gen.IStructureStartMixin;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = MKNpc.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WorldStructureHandler {
    public static final Map<ResourceLocation, MKJigsawStructure> MK_STRUCTURE_INDEX = new HashMap<>();

    @SubscribeEvent
    public static void serverStarted(final ServerStartedEvent event) {
        WorldStructureHandler.cacheStructures(event.getServer());
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent ev) {
        if (ev.phase == TickEvent.Phase.END && ev.level instanceof ServerLevel serverLevel) {
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
                    over.setupStructureDataIfAbsent(start, ev.level);
                    activeStructures.visitStructure(IStructureStartMixin.getInstanceIdFromStart(start), player);
                }
            }
            if (ev.level.dimension() == Level.OVERWORLD) {
                over.update();
            }
        }
    }

    public static void cacheStructures(MinecraftServer server) {
        server.registryAccess().registry(Registries.STRUCTURE).ifPresent(registry -> {
            MK_STRUCTURE_INDEX.clear();
            registry.holders().filter(r -> r.get() instanceof MKJigsawStructure).forEach(r -> {
                MKNpc.LOGGER.info("Caching MK Structure {}", r.key().location());
                MK_STRUCTURE_INDEX.put(r.key().location(), (MKJigsawStructure) r.get());
            });
        });

    }
}
