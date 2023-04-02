package com.chaosbuffalo.mkchat.client;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.init.ChatEntityTypes;
import net.minecraft.client.renderer.entity.PigRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKChat.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChatRenderers {

    @SubscribeEvent
    public static void registerModels(EntityRenderersEvent.RegisterRenderers evt) {
        evt.registerEntityRenderer(ChatEntityTypes.TEST_CHAT.get(), PigRenderer::new);
    }
}
