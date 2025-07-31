package com.chaosbuffalo.mknpc.npc;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.network.packets.NpcDefinitionClientUpdatePacket;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = MKNpc.MODID)
public class NpcDefinitionManager {
    public static final Map<ResourceLocation, NpcDefinitionClient> CLIENT_DEFINITIONS = new HashMap<>();

    public static void resolveDefinitions(MinecraftServer server) {
        Registry<NpcDefinition> registry = server.registryAccess().registryOrThrow(NpcRegistries.NPC_DEFINITIONS);
        registry.stream().forEach(def -> {
            boolean resolved = def.resolveParents(registry);
            if (!resolved) {
                MKNpc.LOGGER.error("Failed to resolve parents for {}",
                        def.getDefinitionName());
            }
        });
        registry.stream().forEach(def -> {
            def.resolveEntityType();
        });
    }

    @SubscribeEvent
    public static void onDataPackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) {
            resolveDefinitions(event.getPlayerList().getServer());
        }
        NpcDefinitionClientUpdatePacket updatePacket = new NpcDefinitionClientUpdatePacket(event.getPlayerList().getServer().registryAccess().registryOrThrow(NpcRegistries.NPC_DEFINITIONS)
                .stream().map(NpcDefinitionClient::new).collect(Collectors.toList()));

        ServerPlayer player = event.getPlayer();
        MKNpc.LOGGER.debug("Sending Npc Client Definitions to {}: {} npcs", player != null ? player : "all", CLIENT_DEFINITIONS.size());

        if (player != null) {
            PacketDistributor.sendToPlayer(player, updatePacket);
        } else {
            PacketDistributor.sendToAllPlayers(updatePacket);
        }
    }
}
