package com.chaosbuffalo.mkchat.event;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.capabilities.*;
import com.chaosbuffalo.mkcore.utils.CapabilityUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = MKChat.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityHandler {

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void attachEntityCapability(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            var provider = CapabilityUtils.provider(ChatCapabilities.PLAYER_DIALOGUE_CAPABILITY,
                    PlayerDialogueHandler::new,
                    player);

            event.addCapability(ChatCapabilities.PLAYER_DIALOGUE_CAP_ID, provider);
        } else if (event.getObject() instanceof LivingEntity living) {
            var provider = CapabilityUtils.provider(ChatCapabilities.NPC_DIALOGUE_CAPABILITY,
                    NpcDialogueHandler::new,
                    living);

            event.addCapability(ChatCapabilities.NPC_DIALOGUE_CAP_ID, provider);
        }
    }
}
