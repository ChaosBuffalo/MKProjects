package com.chaosbuffalo.mkfaction.event;

import com.chaosbuffalo.mkfaction.MKFactionMod;
import com.chaosbuffalo.mkfaction.capabilities.FactionCapabilities;
import com.chaosbuffalo.mkfaction.capabilities.MobFactionHandler;
import com.chaosbuffalo.mkfaction.capabilities.PlayerFactionHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = MKFactionMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityHandler {

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void attachEntityCapability(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof Player) {
            e.addCapability(FactionCapabilities.PLAYER_FACTION_CAP_ID, new PlayerFactionHandler.Provider((Player) e.getObject()));
        } else if (e.getObject() instanceof LivingEntity) {
            e.addCapability(FactionCapabilities.MOB_FACTION_CAP_ID, new MobFactionHandler.Provider((LivingEntity) e.getObject()));
        }
    }
}
