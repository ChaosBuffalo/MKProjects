package com.chaosbuffalo.mkfaction.event;

import com.chaosbuffalo.mkfaction.MKFactionMod;
import com.chaosbuffalo.mkfaction.capabilities.FactionCapabilities;
import com.chaosbuffalo.mkfaction.faction.FactionDefaultManager;
import com.chaosbuffalo.mkfaction.network.MobFactionAssignmentPacket;
import com.chaosbuffalo.mkfaction.network.PacketHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = MKFactionMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MobHandlers {

    @SubscribeEvent
    public static void playerStartTracking(PlayerEvent.StartTracking event) {
        ServerPlayer serverPlayer = (ServerPlayer) event.getPlayer();
        event.getTarget().getCapability(FactionCapabilities.MOB_FACTION_CAPABILITY).ifPresent(mobFaction ->
                serverPlayer.connection.send(PacketHandler.getNetworkChannel()
                        .toVanillaPacket(new MobFactionAssignmentPacket(mobFaction), NetworkDirection.PLAY_TO_CLIENT)));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getWorld().isClientSide)
            return;

        if (event.getEntity() instanceof LivingEntity && !(event.getEntity() instanceof ServerPlayer)) {
            event.getEntity().getCapability(FactionCapabilities.MOB_FACTION_CAPABILITY).ifPresent(mobFaction -> {
                if (!mobFaction.hasFaction()) {
                    FactionDefaultManager.getDefaultFaction(event.getEntity()).ifPresent(mobFaction::setFactionName);
                }
            });
        }
    }
}
