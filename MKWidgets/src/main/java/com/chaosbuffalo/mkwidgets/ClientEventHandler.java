package com.chaosbuffalo.mkwidgets;

import com.chaosbuffalo.mkwidgets.client.gui.example.TestScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;


@EventBusSubscriber(modid = MKWidgets.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientEventHandler {

    public static final KeyMapping openTestUi = new KeyMapping("key.mkwidgets.test.desc",
            InputConstants.KEY_APOSTROPHE,
            "key.mkwidgets.category");

    @SubscribeEvent
    public static void onEvent(InputEvent.Key event) {
        if (openTestUi.consumeClick()) {
            Minecraft.getInstance().setScreen(new TestScreen(
                    Component.literal("MK Widgets Test")));
        }
    }

    @EventBusSubscriber(modid = MKWidgets.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModEvents {
        @SubscribeEvent
        public static void registerKeyBinding(RegisterKeyMappingsEvent event) {
            event.register(openTestUi);
        }
    }
}
