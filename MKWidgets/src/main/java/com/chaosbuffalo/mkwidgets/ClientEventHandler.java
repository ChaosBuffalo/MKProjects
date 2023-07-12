package com.chaosbuffalo.mkwidgets;

import com.chaosbuffalo.mkwidgets.client.gui.example.TestScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKWidgets.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
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

    @Mod.EventBusSubscriber(modid = MKWidgets.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void registerKeyBinding(RegisterKeyMappingsEvent event) {
            event.register(openTestUi);
        }
    }
}
