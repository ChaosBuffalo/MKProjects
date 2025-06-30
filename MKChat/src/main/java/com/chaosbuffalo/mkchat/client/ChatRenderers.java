package com.chaosbuffalo.mkchat.client;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.init.ChatEntityTypes;
import net.minecraft.client.renderer.entity.PigRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = MKChat.MODID, value = Dist.CLIENT)
public class ChatRenderers {

    @SubscribeEvent
    public static void registerModels(EntityRenderersEvent.RegisterRenderers evt) {
        evt.registerEntityRenderer(ChatEntityTypes.TEST_CHAT.get(), PigRenderer::new);
    }
}
