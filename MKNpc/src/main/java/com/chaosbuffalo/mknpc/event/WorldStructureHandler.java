package com.chaosbuffalo.mknpc.event;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.capabilities.NpcCapabilities;
import com.chaosbuffalo.mknpc.capabilities.WorldStructureManager;
import com.chaosbuffalo.mknpc.world.gen.IStructureStartMixin;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawStructure;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid= MKNpc.MODID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class WorldStructureHandler {

    public static List<ConfiguredStructureFeature<?, MKJigsawStructure>> MK_STRUCTURE_CACHE;
    public static final Map<ResourceLocation, ConfiguredStructureFeature<?, MKJigsawStructure>> MK_STRUCTURE_INDEX = new HashMap<>();


    @SubscribeEvent
    public static void serverStarted(final ServerStartedEvent event) {
        WorldStructureHandler.cacheStructures(event.getServer());
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent ev) {
        if (ev.phase == TickEvent.Phase.END && ev.world instanceof ServerLevel) {
            ServerLevel sWorld = (ServerLevel) ev.world;
            Level overworld = sWorld.getServer().getLevel(Level.OVERWORLD);
            if (overworld == null){
                return;
            }
            Optional<IWorldNpcData> overOpt = overworld.getCapability(NpcCapabilities.WORLD_NPC_DATA_CAPABILITY).resolve();
            if (overOpt.isPresent()) {
                StructureFeatureManager manager = sWorld.structureFeatureManager();
                IWorldNpcData over = overOpt.get();
                WorldStructureManager activeStructures = over.getStructureManager();
                for (ServerPlayer player : sWorld.players()) {
                    List<StructureStart> starts = WorldStructureHandler.MK_STRUCTURE_CACHE.stream().map(
                            x -> manager.getStructureAt(player.blockPosition(), x))
                            .filter(x -> x != StructureStart.INVALID_START)
                            .collect(Collectors.toList());
                    for (StructureStart start : starts) {
                        over.setupStructureDataIfAbsent(start, ev.world);
                        activeStructures.visitStructure(IStructureStartMixin.getInstanceIdFromStart(start), player);
                    }
                }
                if (ev.world.dimension() == Level.OVERWORLD) {
                    over.update();
                }
            }
        }
    }

    public static void cacheStructures(MinecraftServer server) {
        server.registryAccess().registry(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE.key()).ifPresent(registry -> {
            MK_STRUCTURE_CACHE = registry.entrySet().stream()
                    .filter(x -> x.getValue().feature instanceof MKJigsawStructure)
                    .map(x -> (ConfiguredStructureFeature<?, MKJigsawStructure>) x.getValue())
                    .collect(Collectors.toList());
            MK_STRUCTURE_INDEX.clear();
            MK_STRUCTURE_CACHE.forEach(x -> {
                ResourceLocation featureName = ForgeRegistries.STRUCTURE_FEATURES.getKey(x.feature);
                MKNpc.LOGGER.info("Caching MK Structure {}", featureName);
                MK_STRUCTURE_INDEX.put(featureName, x);
            });
        });

    }
}
