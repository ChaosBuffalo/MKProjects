package com.chaosbuffalo.mkfaction.event;

import com.chaosbuffalo.mkfaction.MKFactionMod;
import com.chaosbuffalo.mkfaction.capabilities.IMobFaction;
import com.chaosbuffalo.mkfaction.faction.FactionDefaultManager;
import com.chaosbuffalo.mkfaction.network.packets.MobFactionAssignmentPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = MKFactionMod.MODID)
public class MobHandlers {

    @SubscribeEvent
    public static void playerStartTracking(PlayerEvent.StartTracking event) {
        ServerPlayer serverPlayer = (ServerPlayer) event.getEntity();
        IMobFaction.get(event.getTarget()).ifPresent(mobFaction ->
                PacketDistributor.sendToPlayer(serverPlayer, new MobFactionAssignmentPacket(mobFaction)));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide)
            return;

        if (event.getEntity() instanceof LivingEntity living && !(event.getEntity() instanceof Player)) {
            IMobFaction mobFaction = IMobFaction.getMobOrThrow(living);
            if (!mobFaction.hasFaction()) {
                FactionDefaultManager.getDefaultFaction(living).ifPresent(mobFaction::setFaction);
            }
        }
    }
}
