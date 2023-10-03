package com.chaosbuffalo.mkfaction.event;

import com.chaosbuffalo.mkcore.utils.CapabilityUtils;
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
    public static void attachEntityCapability(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity living) {
            if (living instanceof Player player) {
                var provider = CapabilityUtils.provider(FactionCapabilities.PLAYER_FACTION_CAPABILITY,
                        PlayerFactionHandler::new,
                        player);

                event.addCapability(FactionCapabilities.PLAYER_FACTION_CAP_ID, provider);
            } else {
                var provider = CapabilityUtils.provider(FactionCapabilities.MOB_FACTION_CAPABILITY,
                        MobFactionHandler::new,
                        living);

                event.addCapability(FactionCapabilities.MOB_FACTION_CAP_ID, provider);
            }
        }
    }
}
